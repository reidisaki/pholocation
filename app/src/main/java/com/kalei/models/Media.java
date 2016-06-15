package com.kalei.models;

import android.location.Location;

import java.util.Date;
import java.util.List;

/**
 * Created by risaki on 3/2/16.
 */
public abstract class Media {
    String id;
    String filePath;

    public boolean isDidSend() {
        return didSend;
    }

    public void setDidSend(final boolean didSend) {
        this.didSend = didSend;
    }

    boolean didSend;

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(final List<String> emails) {
        this.emails = emails;
    }

    List<String> emails;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(final double longitude) {
        this.longitude = longitude;
    }

    public double getLattitude() {
        return lattitude;
    }

    public void setLattitude(final double lattitude) {
        this.lattitude = lattitude;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(final String caption) {
        this.caption = caption;
    }

    double longitude;
    double lattitude;
    String caption;

    public String getMapLink() {
        return mapLink;
    }

    public void setMapLink(final String mapLink) {
        this.mapLink = mapLink;
    }

    String mapLink;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFileName(final String filePath) {
        this.filePath = filePath;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(final Location location) {
        this.location = location;
    }

    public Date getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(final Date dateTaken) {
        this.dateTaken = dateTaken;
    }

    Location location;
    Date dateTaken;
}
