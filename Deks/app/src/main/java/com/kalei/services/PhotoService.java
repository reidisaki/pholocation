package com.kalei.services;

import com.kalei.utils.PhotoLocationUtils;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by risaki on 3/2/16.
 */
public class PhotoService extends IntentService {
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
        PhotoLocationUtils.processEmailPicture(this, intent);
    }
}
