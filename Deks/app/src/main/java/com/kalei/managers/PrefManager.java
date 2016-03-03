package com.kalei.managers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.kalei.models.Photo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PrefManager {

    private final static String FRONTDOOR_DOMAIN = "frontdoor_domain";

    private final static String SHARED_PREFERENCES_VERSION = "shared_preferences_version";

    private final static String FIRST_TIME_USER = "first_time_user";
    private final static String LAST_LOGGED_IN_AFFILIATE = "last_logged_in_affiliate";

    private final static String MYVIDEOS_FAVORITES = "myvideos_favorite";
    private final static String MYVIDEOS_WATCHLIST = "myvideos_watchlist";
    private final static String MYVIDEOS_CONTINUE_WATCHING = "myvideos_continue_watching";
    private final static String MYVIDEOS_WATCHED_VIDEOS = "myvideos_watched_videos";

    private final static String ADS_ENABLED = "ads_enabled";
    private final static String CRASHLYTICS_ENABLED = "crashlytics_enabled";
    private final static String STREAMING_OVER_CELLULAR_ENABLED = "streaming_over_cellular_enabled";
    private final static String STREAMING_OVER_CELLULAR_MESSAGE = "streaming_over_cellular_message";

    private final static String CHROMECAST_HAS_BEEN_SHOWCASED = "chromecast_has_been_showcased";
    private final static String CHROMECAST_RECEIVER_ID = "chromecast_receiver_id";
    private final static String SEND_WIFI_ONLY = "send_wifi_only";
    private final static String PHOTO_LIST = "photo_list";

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

    /**
     * Helper method to get a boolean value from preferences
     */
    private static boolean getBoolean(Context context, @NonNull String key, boolean defValue) {
        return getPreferences(context).getBoolean(key, defValue);
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

    public static void setSendWifiOnly(Context context, boolean isSendWifiOnly) {
        putBoolean(context, SEND_WIFI_ONLY, isSendWifiOnly);
    }

    public static boolean getSendWifiOnly(Context context) {
        return getBoolean(context, SEND_WIFI_ONLY, false);
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

    /**
     * Sets whether the user has been Showcased the Chromecast icon or not
     */
    public static void setChromecastHasBeenShowcased(Context context, boolean hasBeenShowcased) {
        putBoolean(context, CHROMECAST_HAS_BEEN_SHOWCASED, hasBeenShowcased);
    }

    /**
     * Determines if the user has been Showcased the Chromecast icon or not
     */
    public static boolean getChromecastHasBeenShowcased(Context context) {
        return getBoolean(context, CHROMECAST_HAS_BEEN_SHOWCASED, false);
    }

    /**
     * Sets whether this is a first time user or not
     */
    public static void setFirstTimeUser(Context context, boolean isFirstTime) {
        putBoolean(context, FIRST_TIME_USER, isFirstTime);
    }

    /**
     * Determines if this is a first time user or not
     */
    public static boolean isFirstTimeUser(Context context) {
        return getBoolean(context, FIRST_TIME_USER, true);
    }

    /**
     * Saves the Watchlist JSON to preferences
     */
    public static void setMyVideosWatchlist(Context context, String json) {
        putString(context, MYVIDEOS_WATCHLIST, json);
    }

    public static void setMyVideosFavorites(Context context, String json) {
        putString(context, MYVIDEOS_FAVORITES, json);
    }

    public static String getMyVideosFavorites(Context context) {
        return getString(context, MYVIDEOS_FAVORITES, null);
    }

    /**
     * Gets the Watchlist JSON from preferences
     */
    public static String getMyVideosWatchlist(Context context) {
        return getString(context, MYVIDEOS_WATCHLIST, null);
    }

    /**
     * Saves the Continue Watching JSON to preferences
     */
    public static void setMyVideosContinueWatching(Context context, String json) {
        putString(context, MYVIDEOS_CONTINUE_WATCHING, json);
    }

    /**
     * Gets the Continue Watching JSON from preferences
     */
    public static String getMyVideosContinueWatching(Context context) {
        return getString(context, MYVIDEOS_CONTINUE_WATCHING, null);
    }

    /**
     * Saves the Continue Watching JSON to preferences
     */
    public static void setMyVideosWatchedVideos(Context context, String json) {
        putString(context, MYVIDEOS_WATCHED_VIDEOS, json);
    }

    /**
     * Gets the Continue Watching JSON from preferences
     */
    public static String getMyVideosWatchedVideos(Context context) {
        return getString(context, MYVIDEOS_WATCHED_VIDEOS, null);
    }

    /**
     * Saves the last logged in Affiliate to preferences
     */
//    public static void setLastLoggedInAffiliate(Context context, Affiliate affiliate) {
//        Gson gson = new Gson();
//        String json = gson.toJson(affiliate, Affiliate.class);
//        putString(context, LAST_LOGGED_IN_AFFILIATE, json);
//    }
//
//    /**
//     * Gets the last logged in affiliate from preferences
//     */
//    public static Affiliate getLastLoggedInAffiliate(Context context) {
//        Gson gson = new Gson();
//        String json = getString(context, LAST_LOGGED_IN_AFFILIATE, null);
//        return gson.fromJson(json, Affiliate.class);
//    }

    /**
     * Sets whether Ads are enabled or not in prefernces
     */
    public static void setAdsEnabled(Context context, boolean isEnabled) {
        putBoolean(context, ADS_ENABLED, isEnabled);
    }

    /**
     * Gets whether Ads is enabled or not
     */
    public static boolean isAdsEnabled(Context context) {
        return getBoolean(context, ADS_ENABLED, true);
    }

    /**
     * Sets whether video streaming is available over cellular
     */
    public static void setStreamingOverCellularEnabled(Context context, boolean isEnabled) {
        putBoolean(context, STREAMING_OVER_CELLULAR_ENABLED, isEnabled);
    }

    /**
     * Gets whether video streaming over cellular is enabled or not
     */
    public static boolean isStreamingOverCellularEnabled(Context context) {
        return getBoolean(context, STREAMING_OVER_CELLULAR_ENABLED, true);
    }

    /**
     * Gets whether video streaming over cellular enabled message is shown or not
     */
    public static boolean isStreamingOverCellularEnabledMessageShown(Context context) {
        return getBoolean(context, STREAMING_OVER_CELLULAR_MESSAGE, false);
    }

    /**
     * Sets whether video streaming over cellular enabled message is shown or not
     */
    public static void setIsStreamingOverCellularEnabledMessageShown(Context context, boolean isShown) {
        putBoolean(context, STREAMING_OVER_CELLULAR_MESSAGE, isShown);
    }

    /**
     * Gets the current version of data stored in preferences
     */
//    public static String getSharedPreferencesVersion(Context context) {
//        return getString(context, SHARED_PREFERENCES_VERSION, BuildConfig.SHARED_PREFERENCES_VERSION);
//    }

    /**
     * Sets the current version of data stored in preferences
     */
    public static void setSharedPreferencesVersion(Context context, String value) {
        putString(context, SHARED_PREFERENCES_VERSION, value);
    }

    /**
     * Sets whether Crashlytics is enabled or not
     */
    public static void setCrashlyticsEnabled(Context context, boolean isEnabled) {
        putBoolean(context, CRASHLYTICS_ENABLED, isEnabled);
    }

    /**
     * Gets whether Crashlytics is enabled or not
     */
    public static boolean isCrashlyticsEnabled(Context context) {
        return getBoolean(context, CRASHLYTICS_ENABLED, true);
    }

    /**
     * Gets the Chromecast receiver id
     */
//    public static String getChromecastReceiverId(Context context) {
//        String value = getString(context, CHROMECAST_RECEIVER_ID, BuildConfig.CHROMECAST_RECEIVER_ID);
//
//        if (value == null || (value != null && TextUtils.isEmpty(value.trim()))) {
//            value = BuildConfig.CHROMECAST_RECEIVER_ID;
//        }
//
//        return value;
//    }

    /**
     * Sets the Chromecast receiver id
     */
    public static void setChromecastReceiverId(Context context, String value) {
        putString(context, CHROMECAST_RECEIVER_ID, value);
    }

    /**
     * Gets the front door domain
     */
//    public static String getFrontDoorDomain(Context context) {
//        String domain = getString(context, FRONTDOOR_DOMAIN, null);
//
//        if (domain == null || TextUtils.getTrimmedLength(domain) == 0) {
//            domain = BuildConfig.API_FRONT_DOOR_DOMAIN;
//        }
//
//        return domain;
//    }
//
//    /**
//     * Gets the front door url list (DEBUG only)
//     */
//    public static String[] getFrontDoorUrlList(Context context) {
//        return new String[]{
//                String.format("http://%s/%s", context.getString(R.string.api_domain_prod), BuildConfig.API_FRONT_DOOR_PATH),
//                String.format("http://%s/%s", context.getString(R.string.api_domain_staging), BuildConfig.API_FRONT_DOOR_PATH),
//                String.format("http://%s/%s", context.getString(R.string.api_domain_test), BuildConfig.API_FRONT_DOOR_PATH),
//                "Other" // NOTE: This must stay "Other"
//        };
//    }

    /**
     * Sets the front door url (DEBUG only)
     */
    public static void setFrontDoorDomain(Context context, String value) {
        putString(context, FRONTDOOR_DOMAIN, value);
    }
}