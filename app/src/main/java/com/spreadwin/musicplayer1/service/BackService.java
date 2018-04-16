package com.spreadwin.musicplayer1.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.spreadwin.musicplayer1.App;
import com.spreadwin.musicplayer1.Constant;
import com.spreadwin.musicplayer1.model.Music;
import com.spreadwin.musicplayer1.utils.MediaUtils;
import com.spreadwin.musicplayer1.utils.PreferencesUtils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Lemuel
 * @email ming.li@spreadwin.com
 */
public class BackService extends Service {
    public int currentPosition = 0;
    public int state = Constant.LOOP;
    public MediaPlayer sPlayer = App.getInstance();
    private MyBinder myBinder = new MyBinder();
    public ArrayList<Music> mMusicList;
    public static final String LOCAL_MUSIC_ACTION = "android.intent.action.SPREADWIN.LOCALMUSIC";
    public static final String LOCAL_MUSIC_TO_CARD_ACTION = "android.intent.action.SPREADWIN.TOCARDMUSIC";
    public static final String LOCAL_MUSIC_TO_FRAGMENT_ACTION = "android.intent.action.SPREADWIN.TOCARDMUSIC";
    public static final String ACTION_ACC_OFF = "ACTION_ACC_OFF";
    private CardToServiceReceiver mReceiver;
    private AccReceiver mAccReceiver;
    private AudioManager mAudioManager;
    private android.media.AudioManager.OnAudioFocusChangeListener afChangeListener;
    private int mStartMode = 0;  //startMode:1  onStartComm// and
    private int mAudioFocus = 0;
    public boolean isPrepareMusic = false;

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyBinder extends Binder {
        public BackService getService() {
            return BackService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMusicList = MediaUtils.getAudioList(getApplicationContext(),false);
        //注册广播

        mReceiver = new CardToServiceReceiver(); //注册发送给卡片的广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LOCAL_MUSIC_ACTION);
        registerReceiver(mReceiver, intentFilter);

        mAccReceiver = new AccReceiver(); //注册发送给卡片的广播
        IntentFilter intentFilterAcc = new IntentFilter();
        intentFilterAcc.addAction(ACTION_ACC_OFF);
        registerReceiver(mAccReceiver, intentFilterAcc);




//        if (mMusicList.size()>0) {
//            prepareMusic(currentPosition);
//
//        }
        sPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mMusicList.size() == 0) {
                    return;
                }
                prepareUpcomingMusic();
                continuePlay();
                Music music = mMusicList.get(currentPosition);
                sendBroadcast(new Intent(BackService.LOCAL_MUSIC_TO_CARD_ACTION).putExtra("title", music.getTitle())
                        .putExtra("songID", music.getId()).putExtra("albumId",music.getAlbumId()).putExtra("state", "play"));
                sendBroadcast(new Intent(LOCAL_MUSIC_TO_FRAGMENT_ACTION).putExtra("cmd", "next").putExtra("position", currentPosition));
            }
        });
        sPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d("MediaPlayer", "error");
                return true;
            }
        });
        afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int focusChange) {

                Log.d("AudioManager",":"+focusChange);
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                        sPlayer.setVolume(0.2f,0.2f);


                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {


                        if (!sPlayer.isPlaying())
                            sPlayer.start();
                        sendBroadcast(new Intent(LOCAL_MUSIC_TO_CARD_ACTION).putExtra("state", "play"));
                        sendBroadcast(new Intent(LOCAL_MUSIC_TO_FRAGMENT_ACTION).putExtra("cmd", "play"));

                       sPlayer.setVolume(1.0f,1.0f);


                    // Resume playback
                }
                else if (focusChange == AudioManager.AUDIOFOCUS_LOSS)
                {
                    if(sPlayer.isPlaying())
                        sPlayer.pause();
                    mAudioManager.abandonAudioFocus(afChangeListener);
                    sendBroadcast(new Intent(LOCAL_MUSIC_TO_CARD_ACTION).putExtra("state", "pause"));
                    sendBroadcast(new Intent(LOCAL_MUSIC_TO_FRAGMENT_ACTION).putExtra("cmd", "pause"));
                    mAudioFocus = 0;
                }
                else  if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
                {
                    if(sPlayer.isPlaying())
                        sPlayer.pause();
                    sendBroadcast(new Intent(LOCAL_MUSIC_TO_CARD_ACTION).putExtra("state", "pause"));
                    sendBroadcast(new Intent(LOCAL_MUSIC_TO_FRAGMENT_ACTION).putExtra("cmd", "pause"));
                    mAudioFocus = 0;
                }


            }
        };
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
     //   mAudioFocus = mAudioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC,
      //          AudioManager.AUDIOFOCUS_GAIN);


        currentPosition  = PreferencesUtils.getInt(getApplicationContext(),"currentPosition",0);
        if (currentPosition>= mMusicList.size())
        {
            currentPosition = 0;
        }
        Log.d("BackService", "onCreate");
      //  Exception e = new Exception("this is a log");
      //  e.printStackTrace();
    }


    //保证服务存在
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mMusicList.size()==0) {
            stopSelf();
        }

        if (mMusicList.size()>0) {
            Music music = mMusicList.get(currentPosition);
            sendBroadcast(new Intent(BackService.LOCAL_MUSIC_TO_CARD_ACTION).putExtra("title", music.getTitle())
                    .putExtra("songID", music.getId()).putExtra("albumId", music.getAlbumId()));

        }
//        Notification noti = new Notification.Builder(this)
//                .setContentTitle("music")
//                .setContentText("music")
//                .setSmallIcon(R.drawable.ic_left_cd_logo)
//                .build();
//        startForeground(12346, noti);
        Log.d("BackService", "startForeground");
        Log.d("BackService", "onStartCommand"+startId+flags);
    //    Exception e = new Exception("this is a log");
     //   e.printStackTrace();
        mStartMode = 1;

        return START_STICKY;
    }

    public void updateMusicList()
    {

        mMusicList = MediaUtils.getAudioList(getApplicationContext(),true);
        if(mMusicList.size() == 0)
        {
            return;
        }
        if (currentPosition>=mMusicList.size())
        {
            currentPosition = 0;
            prepareMusic(currentPosition);
            Music music = mMusicList.get(currentPosition);
            sendBroadcast(new Intent(BackService.LOCAL_MUSIC_TO_CARD_ACTION).putExtra("title", music.getTitle())
                    .putExtra("songID", music.getId()).putExtra("albumId", music.getAlbumId()).putExtra("state", "play"));
        }
    }


    /**
     * 准备音乐播放
     * @param position
     */

    public void prepareMusic(int position){
       // Exception e1 = new Exception("this is a log");
       // e1.printStackTrace();
        PreferencesUtils.putInt(getApplicationContext(), "currentPosition", position);
        if (mMusicList!=null && mMusicList.size()!=0){
            currentPosition = position;
            try {
                sPlayer.pause();
                sPlayer.reset();
                sPlayer.setDataSource(mMusicList.get(currentPosition).getPath());
                sPlayer.prepare();
                isPrepareMusic = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(BackService.this, "获取歌曲失败！", Toast.LENGTH_SHORT).show();
        }
    }

    public void prepareLastMusic(){
        switch (state) {
            case Constant.LOOP:
            case Constant.SINGLE_LOOP:
                if (currentPosition > 0) {
                    currentPosition--;
                } else {
                    currentPosition = mMusicList.size() - 1;
                }
                break;
            case Constant.RANDOM:
                currentPosition = (int) ((Math.random() * mMusicList.size() * 10) / 10);
                break;
        }

        prepareMusic(currentPosition);
    }

    public void prepareNextMusic(){
        switch (state) {
            case Constant.SINGLE_LOOP:
            case Constant.LOOP:
                if (currentPosition < mMusicList.size() - 1) {
                    currentPosition++;
                } else {
                    currentPosition = 0;
                }
                break;
            case Constant.RANDOM:
                currentPosition = (int) ((Math.random() * mMusicList.size() * 10) / 10);
                break;
        }
        prepareMusic(currentPosition);
    }
    public void prepareUpcomingMusic(){
        switch (state) {
            case Constant.SINGLE_LOOP:
                break;
            case Constant.LOOP:
                if (currentPosition < mMusicList.size() - 1) {
                    currentPosition++;
                } else {
                    currentPosition = 0;
                }
                break;
            case Constant.RANDOM:
                currentPosition = (int) ((Math.random() * mMusicList.size() * 10) / 10);
                break;
        }
        prepareMusic(currentPosition);
    }


    /**
     * 开始音乐播放
     */
    public boolean continuePlay(){
        if(mAudioFocus == 0)
        {
            mAudioFocus = mAudioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            if(mAudioFocus == 0)
            {
                return false;
            }
        }

        if (!sPlayer.isPlaying())
            sPlayer.start();
        return true;
    }

    /**
     * 暂停音乐播放
     */
    public void pausePlay(){
        if(sPlayer.isPlaying())
            sPlayer.pause();
        mAudioManager.abandonAudioFocus(afChangeListener);
        mAudioFocus = 0;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getPosition() {
        return currentPosition;
    }

    public  class CardToServiceReceiver extends BroadcastReceiver {
        //接收外部的广播消息，然后分发给每个Fragment 做界面更新
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mMusicList.size()==0) {
                return;
            }

            if (intent.getAction().equals(LOCAL_MUSIC_ACTION)){
                if (intent.getStringExtra("state")!=null){
                    if(intent.getStringExtra("state").equals("play_last")) {

                        prepareLastMusic();
                        Music music = mMusicList.get(currentPosition);
                        sendBroadcast(new Intent(BackService.LOCAL_MUSIC_TO_CARD_ACTION).putExtra("title", music.getTitle())
                                .putExtra("songID", music.getId()).putExtra("albumId",music.getAlbumId()).putExtra("state", "play"));
                        sendBroadcast(new Intent(LOCAL_MUSIC_TO_FRAGMENT_ACTION).putExtra("cmd", "last").putExtra("position", currentPosition));
                        continuePlay();
                        Log.d("BackService", LOCAL_MUSIC_ACTION + "play_last");
                    }else if(intent.getStringExtra("state").equals("play_next")){
                        if (state==Constant.SINGLE_LOOP){
                            state=Constant.LOOP;
                            prepareNextMusic();
                            state=Constant.SINGLE_LOOP;
                        }else {
                            prepareNextMusic();
                        }
                        Music m =  mMusicList.get(currentPosition);
                        sendBroadcast(new Intent(BackService.LOCAL_MUSIC_TO_CARD_ACTION).putExtra("title", m.getTitle())
                                .putExtra("songID", m.getId()).putExtra("albumId",m.getAlbumId()).putExtra("state", "play"));
                        sendBroadcast(new Intent(LOCAL_MUSIC_TO_FRAGMENT_ACTION).putExtra("cmd", "next").putExtra("position", currentPosition));
                        //  sendBroadcast(new Intent(LOCAL_MUSIC_ACTION).putExtra("details_fragment","next").putExtra("position", currentPosition));
                        continuePlay();
                        Log.d("BackService", LOCAL_MUSIC_ACTION + "play_next");
                    }
                    else if(intent.getStringExtra("state").equals("play_pause")){
                        pausePlay();
                        sendBroadcast(new Intent(LOCAL_MUSIC_TO_CARD_ACTION).putExtra("state", "pause"));
                        sendBroadcast(new Intent(LOCAL_MUSIC_TO_FRAGMENT_ACTION).putExtra("cmd", "pause"));
                        // sendBroadcast(new Intent(LOCAL_MUSIC_ACTION).putExtra("details_fragment","pause"));
                        Log.d("BackService", LOCAL_MUSIC_ACTION + "play_pause");
                    } else if(intent.getStringExtra("state").equals("play_continue")){
                        if (!isPrepareMusic)
                        {
                            prepareMusic(currentPosition);
                        }

                        continuePlay();
                        sendBroadcast(new Intent(LOCAL_MUSIC_TO_CARD_ACTION).putExtra("state", "play"));
                        sendBroadcast(new Intent(LOCAL_MUSIC_TO_FRAGMENT_ACTION).putExtra("cmd", "play"));
                        //   sendBroadcast(new Intent(LOCAL_MUSIC_ACTION).putExtra("details_fragment","play"));
                        Log.d("BackService", LOCAL_MUSIC_ACTION + "play_continue");
                    } else if(intent.getStringExtra("state").equals("update_musiclist")){
                        updateMusicList();
                    }
                }
            }
        }
    }
    public  class AccReceiver extends BroadcastReceiver {
        //接收外部的广播消息，然后分发给每个Fragment 做界面更新
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_ACC_OFF)) {
                Log.d("BackService","ACTION_ACC_OFF");
                pausePlay();
                sendBroadcast(new Intent(LOCAL_MUSIC_TO_CARD_ACTION).putExtra("state", "pause"));
                sendBroadcast(new Intent(LOCAL_MUSIC_TO_FRAGMENT_ACTION).putExtra("cmd", "pause"));
            }

        }
    }

    @Override
    public void onDestroy() {
        Log.d("BackServicme","onDestroy:"+mMusicList.size()+mStartMode);
        if (mMusicList.size()!=0)
        {

            Intent mIntent = new Intent();
            mIntent.setAction("android.intent.action.SPREADWIN.LOCALMUSIC.STARTSERVICE");//你定义的service的action
            mIntent.setPackage(getPackageName());//这里你需要设置你应用的包名
            startService(mIntent);
            Log.d("BackService", "startService");
        }


        stopForeground(true);
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unregisterReceiver(mAccReceiver);
        mAudioManager.abandonAudioFocus(afChangeListener);

    }
}

