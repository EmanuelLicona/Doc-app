package com.application.pm1_proyecto_final.providers;

import com.application.pm1_proyecto_final.utils.Constants;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class PublicationProvider {

    CollectionReference mCollection;

    public PublicationProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Publication");
    }

    public Query getAll(String idGroup) {
        return mCollection.whereEqualTo(Constants.KEY_GROUP_ID, idGroup).orderBy("timestamp", Query.Direction.DESCENDING);
    }

    public Task<Void> delete(String id) {
        return mCollection.document(id).delete();
    }

}
