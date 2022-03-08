package com.application.pm1_proyecto_final.models;

import java.io.Serializable;
import java.util.Date;

public class GroupUser implements Serializable {

    public final static String STATUS_INVITED = "0"; // Estatus cuando el usuario a sido invitado
    public final static String STATUS_ACCEPT = "1"; // Estatus si el usuario acepto la invitacion
    public final static String STATUS_NO_ACCEPT = "2"; // Estatus si el usuario no acepto la invitacion
    public final static String STATUS_LEFT = "3"; // Estatus si el usuario se salio del grupo

    private String id;
    private String idUser;
    private String idGroup;
    private String nameGroup;
    private String status; // Este campo alacenara los status del usuario en el grupo
    private Date date; // Este campo almacenara la fecha de de los status

    public GroupUser(String idUser, String idGroup, String status, Date date) {
        this.id = "";
        this.idUser = idUser;
        this.idGroup = idGroup;
        this.status = status;
        this.date = date;
    }

    public GroupUser() {
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

    public String getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(String idGroup) {
        this.idGroup = idGroup;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getNameGroup() {
        return nameGroup;
    }

    public void setNameGroup(String nameGroup) {
        this.nameGroup = nameGroup;
    }
}
