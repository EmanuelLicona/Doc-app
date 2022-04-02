package com.application.pm1_proyecto_final.providers;

import com.application.pm1_proyecto_final.models.Like;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class LikeProvider {
    CollectionReference collectionReference;

    public LikeProvider() {
        this.collectionReference = FirebaseFirestore.getInstance().collection("Likes");
    }

    public Task<Void> create(Like like) {
        DocumentReference document = collectionReference.document();
        String id = document.getId();
        like.setId(id);
        return collectionReference.document(id).set(like);
    }

    public Query getLikeByPostAndUser(String idPublication, String idUser) {
        return collectionReference.whereEqualTo("idPublication", idPublication).whereEqualTo("idUser", idUser);
    }

    public Task<Void> delete(String id) {
        return collectionReference.document(id).delete();
    }

    public Query getAllLikeByPublication(String idPublication) {
        return collectionReference.whereEqualTo("idPublication", idPublication);
    }
}
