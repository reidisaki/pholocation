package com.kalei.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by risaki on 2/20/16.
 */
public class PhotoLocationUtils {

    public static String EMAIL_KEY = "email_key";
    public static String MY_PREFS_NAME = "photolocation";

    public static void saveData(Map<String, String> map, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        for (String i : map.keySet()) {
            editor.putString(i, map.get(i));
        }
        editor.commit();
    }

    public static Map<String, String> getData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        Map<String, String> map = new HashMap<String, String>();
        map.put(EMAIL_KEY, prefs.getString(EMAIL_KEY, "pchung528+catchall@gmail.com"));//"No name defined" is the default value.
        return map;
    }

    public static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
