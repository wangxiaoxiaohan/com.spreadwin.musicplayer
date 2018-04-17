package com.spreadwin.musicplayer1;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.spreadwin.musicplayer1.Fragment.ListFragment;
import com.spreadwin.musicplayer1.event.LoopEvent;
import com.spreadwin.musicplayer1.event.MasterEvent;
import com.spreadwin.musicplayer1.model.Music;
import com.spreadwin.musicplayer1.service.BackService;
import com.spreadwin.musicplayer1.utils.DateUtils;
import com.spreadwin.musicplayer1.utils.MediaUtils;
import com.spreadwin.musicplayer1.utils.MusicComparator;
import com.spreadwin.musicplayer1.utils.PinyinUtils;
import com.spreadwin.musicplayer1.utils.SortModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends FragmentActivity implements View.OnClickListener {
  private LinearLayout ll_list;
  private LinearLayout ll_play;
  private ImageView image_change;
  private  ImageView image_recycle_model;
  private CircleImageView cd_cover;
  private  Boolean isListVisibility=false;
  private ListFragment listFragment;
  private int currentPosition = 0;
  private ArrayList<Music> mList;
  private ServiceConnection connection;
  private BackService mBackService;
    private MediaPlayer player;
    private int currentState = Constant.LOOP;
    private SeekBar mSeekBar;
    private ImageView playorpause,next,previous;

    private ObjectAnimator RotateAnim;
    private Handler mSeekBarSyncHandler;
    private Runnable mSeekBarSyncThread;
    private TextView mCurrentTime;
    private TextView mTotalTime;
    private TextView songName;
    public  static  final String TAG="看这里";
    private DetailsReceiver mReceiver=null;

    private static final int STATE_PLAY = 1;
    private static final int STATE_PAUSE = 2;
    private  ArrayList<Music> mOldMusicList;


    public void prepareView() {
        mBackService.updateMusicList();
        mOldMusicList=mBackService.mMusicList;
        mList=filledData(mOldMusicList);
        Collections.sort(mList,new MusicComparator());
        if (mList.size() == 0)
            return;

        //获取当前信息
        currentPosition = mBackService.getPosition();
        if (!player.isPlaying()) {
            mBackService.prepareMusic(currentPosition);
        }
        currentState = mBackService.getState();
        //获取状态信息
        switch (currentState) {
            case Constant.LOOP:
                image_recycle_model.setImageResource(R.drawable.ic_list_cycle);
                break;
            case Constant.RANDOM:
               image_recycle_model.setImageResource(R.drawable.ic_random_play);
                break;
            case Constant.SINGLE_LOOP:
                image_recycle_model.setImageResource(R.drawable.ic_single_cycle);
        }
        //获取歌曲信息
        final Music music = mList.get(currentPosition);
        updateMusicInfo(music);

        //获取播放信息
        if (player.isPlaying()) {
            setPlayOrPauseState(STATE_PLAY);
        } else {
            setPlayOrPauseState(STATE_PAUSE);
        }

    }
    private ArrayList<Music> filledData(ArrayList<Music> musicList) {
       ArrayList<Music> mSortMusicList = new ArrayList<>();

        for (int i = 0; i < musicList.size(); i++) {
            Music music =musicList.get(i);

            //汉字转换成拼音
            String sortString = PinyinUtils.getPingYin(music.getTitle()).substring(0, 1).toUpperCase();
            Log.d(TAG, "filledData: "+sortString);
            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
               music.setLetters(sortString.toUpperCase());
            } else {
                music.setLetters("#");
            }
            mSortMusicList.add(music);
        }
        return mSortMusicList;

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
        EventBus.getDefault().register(this);
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(BackService.LOCAL_MUSIC_TO_FRAGMENT_ACTION);
        mReceiver=new DetailsReceiver();
        registerReceiver(mReceiver,intentFilter);
        Intent intent=new Intent(this,BackService.class);
        bindService(intent,connection,BIND_AUTO_CREATE);

    }

    private void initView() {


        ll_list=findViewById(R.id.ll_list);
        image_change=findViewById(R.id.image_change);
        ll_play=findViewById(R.id.ll_play);
        image_recycle_model=findViewById(R.id.recycle_model);
        cd_cover=findViewById(R.id.cd_cover);
        mSeekBar=findViewById(R.id.seekBar);
        mCurrentTime=findViewById(R.id.current_time);
        mTotalTime=findViewById(R.id.total_time);
        playorpause=findViewById(R.id.bt_playorpause);
        previous=findViewById(R.id.bt_previous);
        next=findViewById(R.id.bt_next);
        songName=findViewById(R.id.songname);
        image_change.setOnClickListener(this);
        listFragment=new ListFragment();

        image_recycle_model.setImageResource(R.drawable.ic_list_cycle);
        RotateAnim = ObjectAnimator.ofFloat(cd_cover, "rotation", 0, 360);
        RotateAnim.setDuration(10000);
        RotateAnim.setInterpolator(null);
        RotateAnim.setRepeatCount(-1);
        playorpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBackService == null) {
                    return;
                }
                if (mBackService.mMusicList.size() == 0) {
                    return;
                }
                if (player.isPlaying()) {
                    mBackService.pausePlay();
                    setPlayOrPauseState(STATE_PAUSE);

                    sendBroadcast(new Intent(BackService.LOCAL_MUSIC_TO_CARD_ACTION).putExtra("state", "pause"));
                } else {
                    mBackService.continuePlay();
                    setPlayOrPauseState(STATE_PLAY);

                   sendBroadcast(new Intent(BackService.LOCAL_MUSIC_TO_CARD_ACTION).putExtra("state", "play"));
                }
            }
        });

        connection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mBackService = ((BackService.MyBinder) iBinder).getService();
                player = mBackService.sPlayer;
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                registerSeekBarListener();
                registerListener();
                prepareView();
                Log.d(TAG, "onServiceConnected: 绑定成功");

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName)  {


                mBackService=null;

            }

          
        };

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction mTransaction = fragmentManager.beginTransaction();
        mTransaction.replace(R.id.ll_list,listFragment).commit();



    }
    private void setPlayOrPauseState(int state) {
        if (state == STATE_PLAY) {
            if (!RotateAnim.isStarted()) {
               RotateAnim.start();
            } else {
                RotateAnim.resume();
            }
            mSeekBarSyncHandler.removeCallbacks(mSeekBarSyncThread);
            mSeekBarSyncHandler.post(mSeekBarSyncThread);
            playorpause.setImageResource(R.drawable.bt_stop);
        } else if (state == STATE_PAUSE) {
            RotateAnim.pause();
            try {
                if (mSeekBarSyncHandler != null && mSeekBarSyncThread != null) {
                    mSeekBarSyncHandler.removeCallbacks(mSeekBarSyncThread);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            playorpause.setImageResource(R.drawable.bt_play);
        }
    }
    private void registerSeekBarListener() {

        if (mSeekBar != null) {
            mSeekBarSyncHandler = new Handler();
            mSeekBarSyncThread = new Runnable() {
                public void run() {
                    //获得歌曲现在播放位置并设置成播放进度条的值
                    int playerPos = player.getCurrentPosition();
                    mSeekBar.setProgress(playerPos);
                    mCurrentTime.setText(DateUtils.getTime(playerPos));
                    //每次延迟100毫秒再启动线程
                    mSeekBarSyncHandler.postDelayed(mSeekBarSyncThread, 500);

                }
            };
            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, final int progress,
                                              boolean fromUser) {

                    if (mBackService.mMusicList.size() == 0) {
                        return;
                    }
                    // fromUser判断是用户改变的滑块的值
                    if (fromUser == true) {
                        mCurrentTime.setText(DateUtils.getTime(progress));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                    if (mBackService.mMusicList.size() == 0) {
                        return;
                    }
                    player.seekTo(seekBar.getProgress());
                }
            });

        }
    }
    private void updateMusicInfo(Music music) {
        songName.setText(music.getTitle());
        setMvpMusic(music);
        mSeekBar.setMax(player.getDuration());
        mSeekBar.setProgress(player.getCurrentPosition());
        mCurrentTime.setText(DateUtils.getTime(player.getCurrentPosition()));
        mTotalTime.setText(DateUtils.getTime(player.getDuration()));

    }

    private void setMvpMusic(Music music) {
        Log.d("music.getPath()", "" + music.getPath());
        Bitmap b = MediaUtils.getArtwork(this, music, true);
        if (b != null) {
            cd_cover.setImageBitmap(b);
        } else {
            cd_cover.setImageResource(R.drawable.normal_pic_1);
        }
    }
     private void registerListener(){

         previous.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (mBackService.mMusicList.size() == 0) {
                     return;
                 }
                 previousClick();
                 Music music = mList.get(currentPosition);
                 sendBroadcast(new Intent(BackService.LOCAL_MUSIC_TO_CARD_ACTION).putExtra("title", music.getTitle())
                         .putExtra("songID", music.getId()).putExtra("albumId", music.getAlbumId()).putExtra("state", "play"));
             }
         });
         next.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (mBackService.mMusicList.size() == 0) {
                     return;
                 }
                 nextClick();
                 Music music = mList.get(currentPosition);
                sendBroadcast(new Intent(BackService.LOCAL_MUSIC_TO_CARD_ACTION).putExtra("title", music.getTitle())
                         .putExtra("songID", music.getId()).putExtra("albumId", music.getAlbumId()).putExtra("state", "play"));
             }
         });
        image_recycle_model.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBackService.mMusicList.size() == 0) {
                    return;
                }
                switch (currentState) {
                    case Constant.LOOP:
                        image_recycle_model.setImageResource(R.drawable.ic_random_play);
                        currentState = Constant.RANDOM;
                        mBackService.setState(currentState);
                        LoopEvent loopEvent=new LoopEvent();
                        loopEvent.setCurrentState(Constant.RANDOM);
                        EventBus.getDefault().post(loopEvent);
                        break;
                    case Constant.RANDOM:
                        image_recycle_model.setImageResource(R.drawable.ic_single_cycle);
                        currentState = Constant.SINGLE_LOOP;
                        mBackService.setState(currentState);
                        LoopEvent loopEvent1=new LoopEvent();
                        loopEvent1.setCurrentState(Constant.SINGLE_LOOP);
                        EventBus.getDefault().post(loopEvent1);
                        break;
                    default:
                        image_recycle_model.setImageResource(R.drawable.ic_list_cycle);
                        currentState = Constant.LOOP;
                        mBackService.setState(currentState);
                        LoopEvent loopEvent2=new LoopEvent();
                        loopEvent2.setCurrentState(Constant.LOOP);
                        EventBus.getDefault().post(loopEvent2);
                        break;
                }
            }
        });
    }
    private void nextClick() {
        switch (currentState) {
            case Constant.LOOP:
            case Constant.SINGLE_LOOP:
                if (currentPosition < mList.size() - 1) {
                    currentPosition++;
                } else {
                    currentPosition = 0;
                }
                break;
            case Constant.RANDOM:
                currentPosition = (int) ((Math.random() * mList.size() * 10) / 10);
                break;
        }
        stuffCurrentMusic();
    }

    private void previousClick() {
        switch (currentState) {
            case Constant.LOOP:
            case Constant.SINGLE_LOOP:
                if (currentPosition > 0) {
                    currentPosition--;
                } else {
                    currentPosition = mList.size() - 1;
                }
                break;
            case Constant.RANDOM:
                currentPosition = (int) ((Math.random() * mList.size() * 10) / 10);
                break;
        }
        stuffCurrentMusic();
    }
    private void stuffCurrentMusic() {
        stuffMusic();
        setPlayOrPauseState(STATE_PLAY);

        mBackService.continuePlay();


    }
    private void stuffMusic() {
        mBackService.prepareMusic(currentPosition);
        Music music = mList.get(currentPosition);
        updateMusicInfo(music);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MasterEvent event) {
        currentPosition = event.getPosition();
        mBackService.prepareMusic(currentPosition);
        Music music = mList.get(currentPosition);
        updateMusicInfo(music);
        setPlayOrPauseState(STATE_PLAY);
        listFragment.Playing(currentPosition);
        mBackService.continuePlay();
        sendBroadcast(new Intent(BackService.LOCAL_MUSIC_TO_CARD_ACTION).putExtra("title", music.getTitle())
                .putExtra("songID", music.getId()).putExtra("albumId", music.getAlbumId()).putExtra("state", "play"));
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.image_change:


                if (isListVisibility){
                    ll_list.setVisibility(View.GONE);

                    image_change.setImageDrawable(getResources().getDrawable(R.drawable.bt_extension_normal));
                    isListVisibility=false;
                    image_recycle_model.setVisibility(View.VISIBLE);


                }else {
                    ll_list.setVisibility(View.VISIBLE);


                    image_change.setImageDrawable(getResources().getDrawable(R.drawable.bt_extension_down));
                    isListVisibility=true;
                    image_recycle_model.setVisibility(View.INVISIBLE);

                }
             break;
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        unregisterReceiver(mReceiver);
        unbindService(connection);
        super.onDestroy();
    }
       public class DetailsReceiver extends BroadcastReceiver {

        public DetailsReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BackService.LOCAL_MUSIC_TO_FRAGMENT_ACTION)) {
                if (intent.getStringExtra("cmd") != null) {
                    //        Log.d("MasterFragment", "onReceive3:"+intent.getStringExtra("cmd"));
                    if (intent.getStringExtra("cmd").equals("last")) {
                        currentPosition = intent.getIntExtra("position", 0);
                        Music music = mList.get(currentPosition);
                        updateMusicInfo(music);
//                        BaseActivity.playing(currentPosition);
//                        //            Log.d("MasterFragment", currentPosition + "");
                        setPlayOrPauseState(STATE_PLAY);
                    } else if (intent.getStringExtra("cmd").equals("next")) {
                        currentPosition = intent.getIntExtra("position", 0);
                        Music music2 = mList.get(currentPosition);
                        updateMusicInfo(music2);
//                        BaseActivity.playing(currentPosition);
//                        //            Log.d("MasterFragment", currentPosition + "");
                        setPlayOrPauseState(STATE_PLAY);

                    } else if (intent.getStringExtra("cmd").equals("pause")) {
                        setPlayOrPauseState(STATE_PAUSE);

                    } else if (intent.getStringExtra("cmd").equals("play")) {
                        setPlayOrPauseState(STATE_PLAY);

                    }
                }
            }
        }

    }


}
