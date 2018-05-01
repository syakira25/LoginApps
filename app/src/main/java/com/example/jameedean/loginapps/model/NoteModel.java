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
    private String drawingUrl;
    private String timestamp;
    private String category;

    private long createdAt;

    public NoteModel() {}

    public NoteModel(String title, String agency, String description, String category, long createdAt) {
        this.title = title;
        this.agency = agency;
        this.description = description;
        this.category = category;
        this.createdAt = createdAt;
    }

    public NoteModel(String title, String agency, String description, String imageUrl, String drawUrl, String timestamp,String category,long createdAt) {
        this.title = title;
        this.agency = agency;
        this.description = description;
        this.imageUrl = imageUrl;
        this.drawingUrl = drawUrl;
        this.timestamp = timestamp;
        this.category = category;
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

    public String getDrawingUrl() {
        return drawingUrl;
    }

    public void setDrawingUrl(String drawingUrl) {
        this.drawingUrl = drawingUrl;
    }

    public String getTimestamp() {return timestamp;}

    public void setTimestamp(){this.timestamp = timestamp;}

    public String getCategory() {return category;}

    public void setCategory(String category){this.category=category;}

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

}
