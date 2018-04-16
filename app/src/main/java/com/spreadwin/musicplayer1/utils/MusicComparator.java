package com.spreadwin.musicplayer1.utils;

import com.spreadwin.musicplayer1.model.Music;

import java.util.Comparator;

/**
 * Created by LHD on 2018/4/16.
 */

public class MusicComparator implements Comparator<Music> {
    @Override
    public int compare(Music M1, Music M2) {

        if (M1.getLetters().equals("@")
                || M2.getLetters().equals("#")) {
            return -1;
        } else if (M1.getLetters().equals("#")
                || M2.getLetters().equals("@")) {
            return 1;
        } else {
            return M1.getLetters().compareTo(M2.getLetters());
        }
    }
}
