package com.aiceking.chinamapview.application;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

public class MyApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
//        LeakCanary.install(this);
    }
}
