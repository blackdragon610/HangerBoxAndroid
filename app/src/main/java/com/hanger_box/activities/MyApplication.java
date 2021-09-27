package com.hanger_box.activities;

import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.hanger_box.common.Common;

public class MyApplication extends MultiDexApplication {
    private static final String TAG = "FirebaseService";

    public void onCreate(){
        super.onCreate();

        Log.d(TAG, "Myapplication!!!" + "");
        Common.myApp = this;
    }

}
