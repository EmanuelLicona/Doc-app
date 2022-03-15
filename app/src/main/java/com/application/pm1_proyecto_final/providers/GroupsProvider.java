package com.application.pm1_proyecto_final.providers;

import com.application.pm1_proyecto_final.models.Group;
import com.application.pm1_proyecto_final.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class GroupsProvider {

    private CollectionReference collection;

    public final static String NAME_COLLECTION = "Groups";
    public final static String  KEY_USER_CREATE= "user_create";
    public final static String  KEY_TITLE= "title";
    public final static String  KEY_DESCRIPTION= "description";
    public final static String  KEY_IMAGE= "image";
    public final static String  KEY_STATUS= "status";
    public final static String  KEY_USERS= "users";

    public GroupsProvider() {
        collection = FirebaseFirestore.getInstance().collection(NAME_COLLECTION);
    }

    public Task<DocumentSnapshot> getGroup(String id) {
        return collection.document(id).get();
    }

    public Task<DocumentReference> create(Group group) {

//        Map<String, Object> groups = new HashMap<>();
//
//        Group group1 = new Group();
//        group1.setId("1");
//        group1.setTitle("Title1");
//        group1.setDescription("Desq");
//
//        Group group2 = new Group();
//        group2.setId("2");
//        group2.setTitle("Title2");
//        group2.setDescription("Des2");
//
//        Group group3 = new Group();
//        group3.setId("3");
//        group3.setTitle("Title3");
//        group3.setDescription("Desq3");
//
//        groups.put("Group1", group1);
//        groups.put("Group2", group2);
//        groups.put("Group3", group3);


        Map<String, Object> map = new HashMap<>();
        map.put(KEY_TITLE, group.getTitle());
        map.put(KEY_DESCRIPTION, group.getDescription());
        map.put(KEY_USER_CREATE, group.getUser_create());
        map.put(KEY_IMAGE, group.getImage());
        map.put(KEY_STATUS, group.getStatus());
//        map.put(KEY_USERS, group.getJson_users());


//        map.put("Groups", groups);



        return collection.add(map);
    }

    public Task<Void> update(Group group) {
        Map<String, Object> map = new HashMap<>();
        map.put("title", group.getTitle());
        map.put("description", group.getDescription());
        map.put("user_create", group.getUser_create());
        map.put("image", group.getImage());


        return collection.document(group.getId()).update(map);
    };
}
