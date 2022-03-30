package com.application.pm1_proyecto_final.models;

import java.io.Serializable;

public class Note implements Serializable {
    private String id;
    private String title;
    private String description;
    private String status;
    private String user_create;

    public Note() {
    }

    public Note(String id, String title, String description,String user_create, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.user_create = user_create;
        this.status = status;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
