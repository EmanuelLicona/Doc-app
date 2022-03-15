package com.application.pm1_proyecto_final.models;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    private String id;
    private String name;
    private String lastname;
    private String email;
    private String carrera;
    private String phone;
    private String numberAccount;
    private String address;
    private String status;
    private String birthDate;
    private String image;
    private String password;
    private String json_groups;
    private String imageCover;

    public User() {
    }

    public User(String id, String name, String lastname, String email, String carrera, String phone, String numberAccount, String address, String status, String birthDate, String image, String password) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.carrera = carrera;
        this.phone = phone;
        this.numberAccount = numberAccount;
        this.address = address;
        this.status = status;
        this.birthDate = birthDate;
        this.image = image;
        this.password = password;
        this.json_groups = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCarrera() {
        return carrera;
    }

    public void setCarrera(String carrera) {
        this.carrera = carrera;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNumberAccount() {
        return numberAccount;
    }

    public void setNumberAccount(String numberAccount) {
        this.numberAccount = numberAccount;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getJson_groups() {
        return json_groups;
    }

    public void setJson_groups(String json_groups) {
        this.json_groups = json_groups;
    }

    public static ArrayList<Group> converJsonToArrayListGroups(String json) {

        Gson gson = new Gson();

        if (json != null) {
            ArrayList<Group> groups = gson.fromJson(json, new TypeToken<ArrayList<Group>>() {
            }.getType());
            return groups;
        } else {

            return new ArrayList<Group>();
        }
    }
    public String getImageCover() {
        return imageCover;
    }

    public void setImageCover(String imageCover) {
        this.imageCover = imageCover;
    }
}
