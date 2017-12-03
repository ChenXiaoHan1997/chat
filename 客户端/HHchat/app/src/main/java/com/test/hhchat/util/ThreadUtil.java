package com.test.hhchat.util;

import android.os.Handler;

/**
 * Created by Administrator on 2017/11/22 0022.
 */

public class ThreadUtil {

    public static void runInSubThread(Runnable r) {
        new Thread(r).start();
    }

    private static Handler handler = new Handler();

    public static void runInUiThread(Runnable r) {
        handler.post(r);
    }
}
