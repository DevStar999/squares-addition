package com.nerdcoredevelopment.squaresaddition;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.games.PlayGamesSdk;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PlayGamesSdk.initialize(this);
    }
}
