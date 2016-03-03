package com.kalei.models;

import android.location.Location;

import java.util.Date;

/**
 * Created by risaki on 3/2/16.
 */
public abstract class Media {
    String id;
    String filePath;

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
