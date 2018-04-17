package com.spreadwin.musicplayer1.Fragment;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spreadwin.musicplayer1.Adapter.MusicListAdapter;
import com.spreadwin.musicplayer1.Constant;
import com.spreadwin.musicplayer1.R;
import com.spreadwin.musicplayer1.event.LoopEvent;
import com.spreadwin.musicplayer1.model.Music;
import com.spreadwin.musicplayer1.service.BackService;
import com.spreadwin.musicplayer1.utils.MediaUtils;
import com.spreadwin.musicplayer1.utils.PinyinComparator;
import com.spreadwin.musicplayer1.utils.PinyinUtils;
import com.spreadwin.musicplayer1.utils.SortModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by LHD on 2018/4/13.
 */

public class ListFragment extends Fragment {
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView mRecyclerView;
    private TextView loopTextView;

    private List<Music> list = new ArrayList<Music>();
    private List<SortModel> SourceDateList;
    public  static  final  String TAG="看wo";
    private   static MusicListAdapter adapter;
    private  int CurrentState;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      return  inflater.inflate(R.layout.fragment_list,container,false) ;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView=view.findViewById(R.id.music_list);
        loopTextView=view.findViewById(R.id.loop_textView);
        getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(Environment.getExternalStorageDirectory().toString() + "/kwmusiccar/Song")));
        getActivity().sendBroadcast(new Intent(BackService.LOCAL_MUSIC_ACTION).putExtra("state", "update_musiclist"));
        EventBus.getDefault().register(this);
         initData();



        linearLayoutManager=new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
         mRecyclerView.setLayoutManager(linearLayoutManager);
         adapter=new MusicListAdapter(getContext(),SourceDateList);
         mRecyclerView.setAdapter(adapter);

        super.onViewCreated(view, savedInstanceState);
    }


    private  void  initData(){

        //获取歌曲列表 ，并根据歌曲拼音首字母将歌曲名字和歌手分别放入一个数组中；
        list = MediaUtils.getAudioList(getContext(), false);
        String[] NameList=new String[list.size()];
        String[] SingerList=new String[list.size()];
        for (int i=0 ;i<list.size();i++){
            NameList[i]=list.get(i).getTitle();
            SingerList[i]=list.get(i).getArtist();
        }
        SourceDateList=filledData(NameList,SingerList);
        //使用集合的sort方法对首字母进行排序。
        Collections.sort(SourceDateList,new PinyinComparator());
        if (MediaUtils.getAudioList(getActivity(), false).size() == 0) {
            Log.d(TAG, "prepareFinish " + "completion");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_TRADITIONAL);
            builder.setTitle("提示")
                    .setMessage("没有本地音乐").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).create().show();
        }

    }
     //用这个方法 获得一组包含了 名字和首字母大写的 Model
    private List<SortModel> filledData(String[] musicTitle ,String[] singer) {
        List<SortModel> mSortList = new ArrayList<>();

        for (int i = 0; i < musicTitle.length; i++) {
            SortModel sortModel = new SortModel();
            sortModel.setName(musicTitle[i]);
            sortModel.setSinger(singer[i]);
            //汉字转换成拼音
            String sortString = PinyinUtils.getPingYin(musicTitle[i]).substring(0, 1).toUpperCase();
            Log.d(TAG, "filledData: "+sortString);
            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                sortModel.setLetters(sortString.toUpperCase());
            } else {
                sortModel.setLetters("#");
            }
            mSortList.add(sortModel);
        }
        return mSortList;

    }

    @Override
    public void onDestroyView() {
       EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void  onEvent(LoopEvent event){
        CurrentState=event.getCurrentState();
        if (CurrentState== Constant.LOOP){
            loopTextView.setText(R.string.play_recycle_list);
        }else if (CurrentState==Constant.RANDOM){
            loopTextView.setText(R.string.play_random);
        }else {
            loopTextView.setText(R.string.play_recycle_Single);
        }


    }
    public static  void Playing(int position){
                adapter.isPlaying(position);
    }
}
