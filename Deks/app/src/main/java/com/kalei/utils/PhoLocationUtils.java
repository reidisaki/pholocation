package com.kalei.utils;

import com.kalei.pholocation.MainActivity;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by risaki on 2/20/16.
 */
public class PhoLocationUtils {

    public static void saveData(Map<String, String> map, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(MainActivity.MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        for (String i : map.keySet()) {
            editor.putString(i, map.get(i));
        }
        editor.commit();
    }

    public static Map<String, String> getData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(MainActivity.MY_PREFS_NAME, Context.MODE_PRIVATE);
        Map<String, String> map = new HashMap<String, String>();
        map.put(MainActivity.EMAIL_KEY, prefs.getString(MainActivity.EMAIL_KEY, "pchung528+catchall@gmail.com"));//"No name defined" is the default value.
        return map;
    }
}
