package com.example.jameedean.loginapps.model;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by root on 28/10/2017.
 */
@IgnoreExtraProperties
public class NoteModel {

    private String title, agency;
    private String description;
    private String imageUrl;
    private long createdAt;

    public NoteModel() {}

    public NoteModel(String title, String agency, String description, String imageUrl, long createdAt) {
        this.title = title;
        this.agency = agency;
        this.description = description;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {return imageUrl;}

    public void setImageUrl(String imageUrl) {this.imageUrl = imageUrl;}

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

}
