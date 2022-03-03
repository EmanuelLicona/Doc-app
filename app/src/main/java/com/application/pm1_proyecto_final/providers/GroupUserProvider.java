package com.application.pm1_proyecto_final.providers;

import com.application.pm1_proyecto_final.models.Group;
import com.application.pm1_proyecto_final.models.GroupUser;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GroupUserProvider {

    private CollectionReference collection;

    public final static String NAME_COLLECTION = "GroupUser";
    public final static String  KEY_ID_USER = "idUser";
    public final static String  KEY_ID_GROUP = "idGroup";
    public final static String  KEY_STATUS= "status";
    public final static String  KEY_DATE= "date";

    public GroupUserProvider() {
        collection = FirebaseFirestore.getInstance().collection(NAME_COLLECTION);
    }

    public Task<DocumentSnapshot> getGroup(String id) {
        return collection.document(id).get();
    }

    public Task<DocumentReference> create(GroupUser groupUser) {

        Map<String, Object> map = new HashMap<>();

        map.put(KEY_ID_USER, groupUser.getIdUser());
        map.put(KEY_ID_GROUP, groupUser.getIdGroup());
        map.put(KEY_STATUS, groupUser.getStatus());
        map.put(KEY_DATE, groupUser.getDate());



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
