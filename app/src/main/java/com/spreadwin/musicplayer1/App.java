package com.spreadwin.musicplayer1;

import android.app.Application;
import android.media.MediaPlayer;

/**
 * @author Lemuel
 * @email ming.li@spreadwin.com
 */
public class App extends Application {
    private static MediaPlayer ourInstance = new MediaPlayer();
    @Override
    public void onCreate() {
        super.onCreate();
    //    startService(new Intent(this, BackService.class));
    }

    public static MediaPlayer getInstance() {
        return ourInstance;
    }


}
