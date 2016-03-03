package com.kalei.models;

/**
 * Created by risaki on 3/2/16.
 */
public class Photo extends Media {
    private String scaledImage;

    public String getOriginalImage() {
        return originalImage;
    }

    public void setOriginalImage(final String originalImage) {
        this.originalImage = originalImage;
    }

    public String getScaledImage() {
        return scaledImage;
    }

    public void setScaledImage(final String scaledImage) {
        this.scaledImage = scaledImage;
    }

    private String originalImage;
}
