package com.test.hhchat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.test.hhchat.Constants;
import com.test.hhchat.util.ThreadUtil;

/**
 * Created by Administrator on 2017/11/22 0022.
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(Constants.TIME_SPLASH);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    ThreadUtil.runInUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                        }
                    });
                }
            }
        });
    }
}
