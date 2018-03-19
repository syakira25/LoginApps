package com.example.jameedean.loginapps.model;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by JameeDean on 12/11/2017.
 */
@IgnoreExtraProperties
public class AgencyModel {

    private String name_agency;
    private String email_agency;
    private long createdAt;

    public AgencyModel() {}

    public AgencyModel(String name_agency, String email_agency, long createdAt) {
        this.name_agency = name_agency;
        this.email_agency = email_agency;
        this.createdAt = createdAt;
    }

    public String getName() {
        return name_agency;
    }

    public void setName(String name_agency) {
        this.name_agency = name_agency;
    }

    public String getEmail() {
        return email_agency;
    }

    public void setEmail(String email_agency) {
        this.email_agency = email_agency;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
