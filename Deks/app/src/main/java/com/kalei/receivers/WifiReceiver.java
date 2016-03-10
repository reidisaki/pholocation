package com.kalei.receivers;

import com.kalei.utils.PhotoLocationUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by risaki on 3/2/16.
 */
public class WifiReceiver extends BroadcastReceiver {
    public static final String NOTIFICATION_DELETED_ACTION = "NOTIFICATION_DELETED";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i("Reid", "onReceive reset countesr to 0");
        Log.i("Reid", "Resetting");
        if (intent.getAction().equals(PhotoLocationUtils.NOTIFICATION_DELETED_ACTION)) {
            PhotoLocationUtils.mFailedSends = 0;
            PhotoLocationUtils.mSuccessfulSends = 0;
        } else {
            PhotoLocationUtils.processEmailPicture(context, intent);
        }
    }
}