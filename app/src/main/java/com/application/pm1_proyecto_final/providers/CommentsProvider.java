package com.application.pm1_proyecto_final.providers;

import com.application.pm1_proyecto_final.models.Comment;
import com.application.pm1_proyecto_final.utils.Constants;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class CommentsProvider {

    CollectionReference collectionReference;

    public CommentsProvider() {
        this.collectionReference = FirebaseFirestore.getInstance().collection("Comments");
    }

    public Task<Void> createComment(Comment comment) {
        return collectionReference.document().set(comment);
    }

    public Query getAll(String idPublication) {
        return collectionReference.whereEqualTo("idPost", idPublication).orderBy("timestamp", Query.Direction.DESCENDING);
    }
}
