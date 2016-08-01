package com.kalei.receivers;

import com.kalei.utils.PhotoLocationUtils;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by risaki on 3/2/16.
 */
public class WifiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i("pl", "onReceive reset countesr to 0");
        Log.i("pl", "Resetting");
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE") || intent.getAction().equals(PhotoLocationUtils.NOTIFICATION_RETRY_ACTION)) {
                PhotoLocationUtils.processEmailPicture(context, intent);
            }
            if (intent.getAction().equals(PhotoLocationUtils.NOTIFICATION_DELETED_ACTION)) {
                PhotoLocationUtils.mFailedSends = 0;
                PhotoLocationUtils.mSuccessfulSends = 0;
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
            }
        }
    }
}