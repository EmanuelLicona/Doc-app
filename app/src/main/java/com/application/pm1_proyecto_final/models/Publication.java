package com.application.pm1_proyecto_final.models;

import java.io.Serializable;
import java.util.Date;

public class Publication implements Serializable {

    private String description;
    private String groupId;
    private String path;
    private String senderId;
    private String status;
    private Long timestamp;
    private String title;
    private String type;
    private String image;
    private String nameUserPublication;

    public Publication() {
    }

    public Publication(String description, String groupId, String path, String senderId, String status, Long timestamp, String title, String type) {
        this.description = description;
        this.groupId = groupId;
        this.path = path;
        this.senderId = senderId;
        this.status = status;
        this.timestamp = timestamp;
        this.title = title;
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNameUserPublication() {
        return nameUserPublication;
    }

    public void setNameUserPublication(String nameUserPublication) {
        this.nameUserPublication = nameUserPublication;
    }
}
