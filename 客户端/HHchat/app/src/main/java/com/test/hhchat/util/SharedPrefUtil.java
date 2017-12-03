package com.test.hhchat.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Administrator on 2017/11/22 0022.
 */

public class SharedPrefUtil {

    public static String getValue(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, "");
    }

    public static boolean putValue(Context context, String key, String value) {
        if (value == null) {
            value = "";
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        boolean result = editor.commit();
        return result;
    }
}
