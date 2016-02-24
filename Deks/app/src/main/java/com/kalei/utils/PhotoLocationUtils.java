package com.kalei.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import com.android.ex.chips.recipientchip.DrawableRecipientChip;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by risaki on 2/20/16.
 */
public class PhotoLocationUtils {

    public static String EMAIL_KEY = "email_key";
    public static String MY_PREFS_NAME = "photolocation";

    public static void saveData(DrawableRecipientChip[] chips, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        Gson gson = new GsonBuilder().create();
        Log.i("Reid", "reid list must be humongous");
        List<String> stringList = new ArrayList<>();
        for (int i = 0; i < chips.length; i++) {
            stringList.add(chips[i].getEntry().getDestination());
        }
        editor.putString(EMAIL_KEY, gson.toJson(stringList).toString());
        editor.commit();
    }

    public static List<String> getData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        Type listType = new TypeToken<ArrayList<String>>() {
        }.getType();
        List<String> list = new Gson().fromJson(prefs.getString(EMAIL_KEY, null), listType);
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    public static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static String getEmailStringList(Context context) {
        StringBuilder sb = new StringBuilder();
        for (String r : getData(context)) {
            sb.append(r + ",");
        }
        String s = sb.toString();

        return s.length() > 0 ? s.substring(0, s.length() - 1) : s;
    }
}
