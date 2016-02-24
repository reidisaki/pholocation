package com.kalei.models;

/**
 * Created by risaki on 2/22/16.
 */
public class Recipient {
    public Recipient() {
    }

    public Recipient(final String email) {
        this.email = email;
    }

    public Recipient(final String lastName, final String id, final String firstName, final String email) {
        this.lastName = lastName;
        this.id = id;
        this.firstName = firstName;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String id;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String firstName;

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String lastName;

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String email;
}
