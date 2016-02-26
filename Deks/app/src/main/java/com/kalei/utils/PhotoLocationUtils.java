package com.kalei.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import com.android.ex.chips.RecipientEntry;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by risaki on 2/20/16.
 */
public class PhotoLocationUtils {

    public static String EMAIL_KEY = "email_key";
    public static String MY_PREFS_NAME = "photolocation";

    public static void saveDataObjects(List<RecipientEntry> chips, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriSerializer())
                .create();

        editor.putString(EMAIL_KEY, gson.toJson(chips).toString());
        editor.commit();
    }

    public static List<RecipientEntry> getDataObjects(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        Type listType = new TypeToken<ArrayList<RecipientEntry>>() {
        }.getType();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriDeserializer())
                .create();
        List<RecipientEntry> list = gson.fromJson(prefs.getString(EMAIL_KEY, null), listType);
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
        for (RecipientEntry r : getDataObjects(context)) {
            sb.append(r.getDestination() + ",");
        }
        String s = sb.toString();

        return s.length() > 0 ? s.substring(0, s.length() - 1) : s;
    }

    public static class UriSerializer implements JsonSerializer<Uri> {
        public JsonElement serialize(Uri src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

    public static class UriDeserializer implements JsonDeserializer<Uri> {
        @Override
        public Uri deserialize(final JsonElement src, final Type srcType,
                final JsonDeserializationContext context) throws JsonParseException {
            return Uri.parse(src.getAsString());
        }
    }
}
