package com.kalei.managers;

import com.google.api.client.util.DateTime;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.kalei.models.Photo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PrefManager {
    private final static String SAVE_ORIGINAL = "save_original";
    private final static String SEND_WIFI_ONLY = "send_wifi_only";
    private final static String PHOTO_LIST = "photo_list";
    private final static String FLASH_OPTION = "flash_option";
    private final static String COMMENT_REQUIRED = "comment_required";
    private final static String PICTURES_SENT = "pictures_sent";
    private final static String DATE_LAST_CHECKED = "date_last_checked";
    private final static String CHECKED_TODAY = "checked_today";

    //appboy keys
    private final static String APPBOY_VIDEOS_VIEWED = "app_boy_video_viewed";

    private static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getPreferences(context).edit();
    }

    /**
     * Helper method to save a boolean to preferences
     */
    private static void putBoolean(Context context, @NonNull String key, boolean value) {
        getEditor(context).putBoolean(key, value).commit();
    }

    private static void putInt(Context context, @NonNull String key, int value) {
        getEditor(context).putInt(key, value).commit();
    }

    /**
     * Helper method to get a boolean value from preferences
     */
    private static boolean getBoolean(Context context, @NonNull String key, boolean defValue) {
        return getPreferences(context).getBoolean(key, defValue);
    }

    private static int getInt(Context context, @NonNull String key, int defValue) {
        return getPreferences(context).getInt(key, defValue);
    }

    /**
     * Helper method to save a boolean to preferences
     */
    private static void putString(Context context, @NonNull String key, String value) {
        getEditor(context).putString(key, value).commit();
    }

    /**
     * Helper method to get a boolean value from preferences
     */
    private static String getString(Context context, @NonNull String key, String defValue) {
        return getPreferences(context).getString(key, defValue);
    }

    /**
     * Initializes shared preferences
     */
    public static void initialize(Context context) {
        // If the version stored on disk is different than what's in build.gradle then the version has changed
        // in which case force the shared preferences data to clear.  The user will lose saved data but
        // at least the application won't crash as a result of JSON parsing errors
//        if (!getSharedPreferencesVersion(context).equals(BuildConfig.SHARED_PREFERENCES_VERSION)) {
//            clear(context);
//        }
    }

    public static void setPhoto(Context context, Photo p) {
        List<Photo> photoList = getPhotoList(context);
        photoList.add(p);
        savePhotoList(context, photoList);
    }

    public static List<Photo> getPhotoList(Context context) {
        String photoListString = getString(context, PHOTO_LIST, null);
        List<Photo> photoList = new ArrayList<>();
        if (photoListString != null) {
            Gson g = new Gson();
            Type type = new TypeToken<List<Photo>>() {
            }.getType();
            photoList = g.fromJson(photoListString, type);
        }
        return photoList;
    }

    public static void setFlashOption(Context context, boolean isFlashOn) {
        putBoolean(context, FLASH_OPTION, isFlashOn);
    }

    public static boolean getFlashOption(Context context) {
        return getBoolean(context, FLASH_OPTION, false);
    }

    public static void savePhotoList(Context context, List<Photo> photoList) {
        Gson g = new Gson();
        putString(context, PHOTO_LIST, g.toJson(photoList));
    }

    /**
     * Clears all data from preferences
     */
    public static void clear(Context context) {
        getEditor(context).clear().commit();
    }

    public static void saveOriginalPhoto(Context context, final boolean isChecked) {
        putBoolean(context, SAVE_ORIGINAL, isChecked);
    }

    public static boolean getOriginalOnly(Context context) {
        return getBoolean(context, SAVE_ORIGINAL, false);
    }

    public static void setSendWifiOnly(Context context, boolean isSendWifiOnly) {
        putBoolean(context, SEND_WIFI_ONLY, isSendWifiOnly);
    }

    public static boolean getSendWifiOnly(Context context) {
        return getBoolean(context, SEND_WIFI_ONLY, false);
    }

    public static void setCommentRequired(Context context, boolean requireComment) {
        putBoolean(context, COMMENT_REQUIRED, requireComment);
    }

    public static boolean getCommentRequired(Context context) {
        return getBoolean(context, COMMENT_REQUIRED, false);
    }

    public static void setPicturesSent(Context context, int picturesSent) {
        putInt(context, PICTURES_SENT, picturesSent);
    }

    public static int getPicturesSent(Context context) {
        return getInt(context, PICTURES_SENT, 0);
    }

    public static void setDateChecked(Context context, String dateSet) {
        putString(context, DATE_LAST_CHECKED, dateSet);
    }

    public static String getDateChecked(Context context) {
        return getString(context, DATE_LAST_CHECKED, String.valueOf(new Date().getTime()));
    }

    public static void setCheckedToday(Context context, boolean checkedToday) {
        putBoolean(context, CHECKED_TODAY, checkedToday);
    }

    public static boolean getCheckedToday(Context context) {
        return getBoolean(context, CHECKED_TODAY, false);
    }
}