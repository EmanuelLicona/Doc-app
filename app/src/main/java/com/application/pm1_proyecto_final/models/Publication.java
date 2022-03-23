package com.application.pm1_proyecto_final.models;

import java.util.Date;

public class Publication {

    public static final String STATUS_SENT = "1";
    public static final String STATUS_DELETE = "2";

    private String idFirebase, senderId, groupId, title, description, imageProfileUser, path, type, datatime, status, position;
    private Date dateObject;

    public Publication() {
    }

    public Publication(String idFirebase, String senderId, String groupId, String title, String description, String imageProfileUser, String path, String type, String datatime, String status, String position, Date dateObject) {
        this.idFirebase = idFirebase;
        this.senderId = senderId;
        this.groupId = groupId;
        this.title = title;
        this.description = description;
        this.imageProfileUser = imageProfileUser;
        this.path = path;
        this.type = type;
        this.datatime = datatime;
        this.status = status;
        this.position = position;
        this.dateObject = dateObject;
    }

    public String getIdFirebase() {
        return idFirebase;
    }

    public void setIdFirebase(String idFirebase) {
        this.idFirebase = idFirebase;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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

    public String getImageProfileUser() {
        return imageProfileUser;
    }

    public void setImageProfileUser(String imageProfileUser) {
        this.imageProfileUser = imageProfileUser;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDatatime() {
        return datatime;
    }

    public void setDatatime(String datatime) {
        this.datatime = datatime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Date getDateObject() {
        return dateObject;
    }

    public void setDateObject(Date dateObject) {
        this.dateObject = dateObject;
    }
}
