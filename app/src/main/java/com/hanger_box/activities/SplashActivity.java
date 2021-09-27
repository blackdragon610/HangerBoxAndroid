package com.hanger_box.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.FirebaseApp;
import com.hanger_box.R;
import com.hanger_box.common.Common;
import com.hanger_box.common.LocalStorageManager;
import com.hanger_box.models.UserModel;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String userInfo = LocalStorageManager.getObjectFromLocal("account");
                if (userInfo == null) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }else {
                    Common.me = new UserModel(Common.cm.convertToHashMapFromString(userInfo));
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        }, 2000);
    }
}