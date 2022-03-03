package com.application.pm1_proyecto_final.models;

import java.io.Serializable;

public class Group implements Serializable {

    public final static String STATUS_ACTIVE = "1";
    public final static String STATUS_INACTIVE = "0";

    private String id;
    private String title;
    private String description;
    private String user_create;
    private String image;
    private String status;


    public Group(String id, String title, String description, String user_create, String image, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.user_create = user_create;
        this.image = image;
        this.status = status;
    }

    public Group() {}


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUser_create() {
        return user_create;
    }

    public void setUser_create(String user_create) {
        this.user_create = user_create;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
