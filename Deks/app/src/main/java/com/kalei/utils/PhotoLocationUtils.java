package com.kalei.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import com.kalei.models.Recipient;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by risaki on 2/20/16.
 */
public class PhotoLocationUtils {

    public static String EMAIL_KEY = "email_key";
    public static String MY_PREFS_NAME = "photolocation";

    public static void saveData(List<Recipient> list, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        Gson gson = new GsonBuilder().create();
        editor.putString(EMAIL_KEY, gson.toJson(list).toString());
        editor.commit();
    }

    public static List<Recipient> getData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        Type listType = new TypeToken<ArrayList<Recipient>>() {
        }.getType();
        List<Recipient> list = new Gson().fromJson(prefs.getString(EMAIL_KEY, null), listType);
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
        for (Recipient r : getData(context)) {
            sb.append(r.getEmail() + ",");
        }
        return sb.toString();
    }
}
