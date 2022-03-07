package com.application.pm1_proyecto_final.models;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;

public class Group implements Serializable {

    public final static String STATUS_ACTIVE = "1";
    public final static String STATUS_INACTIVE = "0";

    private String id;
    private String title;
    private String description;
    private String user_create;
    private String image;
    private String status;
    private String json_users;


    public Group(String id, String title, String description, String user_create, String image, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.user_create = user_create;
        this.image = image;
        this.status = status;
        this.json_users = "";
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

    public String getJson_users() {
        return json_users;
    }

    public void setJson_users(String json_users) {
        this.json_users = json_users;
    }


    public static ArrayList<User> converJsonToArrayListUsers(String json){

        Gson gson = new Gson();

        if(json != null){
              ArrayList<User>  users = gson.fromJson(json, new TypeToken<ArrayList<User>>(){}.getType());
            return  users;
        }else{

            return new ArrayList<User>();
        }

    }
}
