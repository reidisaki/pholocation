package com.kalei;

/**
 * Created by risaki on 2/22/16.
 */
public interface IMailListener {
    void onMailFailed(Exception e);
    void onMailSucceeded();
}
