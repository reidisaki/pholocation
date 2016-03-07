package com.kalei.services;

import com.kalei.utils.PhotoLocationUtils;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by risaki on 3/2/16.
 */
public class PhotoService extends IntentService {
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;
    public static int mSuccessfulSends = 0;
    public static int mFailedSends = 0;
    public List<String> imageFileNames;
    public List<String> imageFailedFileNames;
    public static final String NOTIFICATION_DELETED_ACTION = "NOTIFICATION_DELETED";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public PhotoService(final String name) {
        super(name);
    }

    public PhotoService() {
        super("PhotoService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        Log.i("Reid", "got intent here");
        PhotoLocationUtils.processEmailPicture(this, intent);
    }
}
