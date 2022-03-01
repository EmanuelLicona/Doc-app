package com.application.pm1_proyecto_final.models;

import java.io.Serializable;

public class Group implements Serializable {


    private String id;
    private String title;
    private String description;
    private String type;
    private String user_create;
    private String image;


    public Group(String id, String title, String description, String type, String user_create, String image) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.user_create = user_create;
        this.image = image;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
}
