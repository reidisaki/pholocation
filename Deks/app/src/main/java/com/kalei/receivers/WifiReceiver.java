package com.kalei.receivers;

import com.kalei.utils.PhotoLocationUtils;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.util.List;

/**
 * Created by risaki on 3/2/16.
 */
public class WifiReceiver extends BroadcastReceiver {
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;
    public static int mSuccessfulSends = 0;
    public static int mFailedSends = 0;
    public List<String> imageFileNames;
    public List<String> imageFailedFileNames;
    public static final String NOTIFICATION_DELETED_ACTION = "NOTIFICATION_DELETED";

    @Override
    public void onReceive(final Context context, Intent intent) {
        PhotoLocationUtils.processEmailPicture(context, intent);
    }
}