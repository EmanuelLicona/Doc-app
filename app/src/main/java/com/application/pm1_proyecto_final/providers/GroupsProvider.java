package com.application.pm1_proyecto_final.providers;

import com.application.pm1_proyecto_final.models.Group;
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

    public GroupsProvider() {
        collection = FirebaseFirestore.getInstance().collection(NAME_COLLECTION);
    }

    public Task<DocumentSnapshot> getGroup(String id) {
        return collection.document(id).get();
    }

    public Task<DocumentReference> create(Group group) {

        Map<String, Object> map = new HashMap<>();
        map.put("title", group.getTitle());
        map.put("description", group.getDescription());
        map.put("user_create", group.getUser_create());
        map.put("image", group.getImage());



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
