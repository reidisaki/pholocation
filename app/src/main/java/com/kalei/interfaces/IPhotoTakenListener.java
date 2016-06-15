package com.kalei.interfaces;

/**
 * Created by risaki on 3/11/16.
 */
public interface IPhotoTakenListener {
    void onPhotoConfirm();
    void onPhotoCancel();
    void onPhotoTaken(String scaledImagePath, String originalFilePath);
    void onPhotoProcessed(String scaledImagePath, String originalFilePath);
}
