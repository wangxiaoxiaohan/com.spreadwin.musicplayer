package com.spreadwin.musicplayer1.utils;

/**
 * Created by LHD on 2018/4/12.
 */

public class SortModel {

   private String name; //歌曲名字
    private String letters;//歌曲拼音的首字母
    private  String singer;//歌手名字

  public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLetters() {
        return letters;
    }

    public void setLetters(String letters) {
        this.letters = letters;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }
    public String getSinger() {
        return singer;
    }
}
