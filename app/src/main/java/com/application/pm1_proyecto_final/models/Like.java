package com.application.pm1_proyecto_final.models;

public class Like {
    private String id;
    private String idUser;
    private String idPublication;
    private long timestamp;

    public Like(String id, String idUser, String idPublication, long timestamp) {
        this.id = id;
        this.idUser = idUser;
        this.idPublication = idPublication;
        this.timestamp = timestamp;
    }

    public Like() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getIdPublication() {
        return idPublication;
    }

    public void setIdPublication(String idPublication) {
        this.idPublication = idPublication;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
