package com.kalei.interfaces;

/**
 * Created by risaki on 2/22/16.
 */
public interface IMailListener {
    void onMailFailed(Exception e, String imageName);
    void onMailSucceeded(String imageName);
}
