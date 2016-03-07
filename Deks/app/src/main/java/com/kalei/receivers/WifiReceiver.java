package com.kalei.receivers;

import com.kalei.utils.PhotoLocationUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by risaki on 3/2/16.
 */
public class WifiReceiver extends BroadcastReceiver {
    public static final String NOTIFICATION_DELETED_ACTION = "NOTIFICATION_DELETED";

    @Override
    public void onReceive(final Context context, Intent intent) {
        PhotoLocationUtils.processEmailPicture(context, intent);
    }
}