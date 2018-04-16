package com.spreadwin.musicplayer1.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spreadwin.musicplayer1.R;
import com.spreadwin.musicplayer1.event.MasterEvent;
import com.spreadwin.musicplayer1.utils.SortModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by LHD on 2018/4/13.
 */

public class MusicListAdapter extends  RecyclerView.Adapter<MusicListAdapter.ViewHolder>{
    private List<SortModel> mData;
    private Context mContext;
    private LayoutInflater mInflater;
    private boolean isPlay = false;
    private int playPosition = 9999;



    public  MusicListAdapter(Context context, List<SortModel> data){
        mData=data;
        mInflater=LayoutInflater.from(context);
        this.mContext=context;

    }


    public  static  class  ViewHolder extends RecyclerView.ViewHolder{
        TextView tag,MusicName,Singer;
        ImageView status;
        LinearLayout linearLayout;
        public ViewHolder(View itemView) {
            super(itemView);
        }


    }

    @Override
    public MusicListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.list_item,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        viewHolder.MusicName=view.findViewById(R.id.music_name);
        viewHolder.Singer=view.findViewById(R.id.singer);
        viewHolder.tag=view.findViewById(R.id.tag);
        viewHolder.status=view.findViewById(R.id.music_status);
        viewHolder.linearLayout=view.findViewById(R.id.ll_song_item);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder( final MusicListAdapter.ViewHolder holder, final int position) {
                  int section=getSectionForPosition(position);
                  if (position==getPositionForSection(section)){
                      //如果是第一次出现 就将上面的字母标识显现，并将字母改为当前的正确的字母
                          holder.tag.setVisibility(View.VISIBLE);
                          holder.tag.setText(mData.get(position).getLetters());

                  }else {
                          holder.tag.setVisibility(View.GONE);

                  }
                   holder.MusicName.setText(mData.get(position).getName());
                   holder.Singer.setText(mData.get(position).getSinger());

                   holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {
                           MasterEvent masterEvent = new MasterEvent();
                           masterEvent.setPosition(position);
                           EventBus.getDefault().post(masterEvent);
                       }
                   });
                   if (position==playPosition){
                       holder.status.setImageResource(R.drawable.ic_play1);
                   }


    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public int getSectionForPosition(int position) {
        return mData.get(position).getLetters().charAt(0);
    }
    public int getPositionForSection(int section) {
        for (int i = 0; i < getItemCount(); i++) {
            String sortStr = mData.get(i).getLetters();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }
    public void isPlaying(int position) {
        isPlay = true;
        playPosition = position;
        notifyDataSetChanged();
    }


}
