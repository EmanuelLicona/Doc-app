package com.application.pm1_proyecto_final.providers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.application.pm1_proyecto_final.activities.BaseActivity;
import com.application.pm1_proyecto_final.api.UserApiMethods;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.utils.Constants;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.util.HashMap;

public class UsersProvider {
    private CollectionReference collection;

    public final static String NAME_COLLECTION = "Users";
    public final static String KEY_NAME = "name";
    public final static String KEY_EMAIL = "email";
    public final static String KEY_JSON = "groups";
    public final static String KEY_LASTNAME = "lastname";
    public final static String KEY_IMAGE = "image";

    public UsersProvider() {
        collection = FirebaseFirestore.getInstance().collection("Users");
    }

    public Task<DocumentSnapshot> getUser(String id) {
        return collection.document(id).get();
    }

    public Task<Void> create(User user) {
        return collection.document(user.getId()).set(user);
    }

    public void updateAvailability(int value, String idUser, Context context) {
        if (idUser != null) {
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            HashMap<String, Integer> params = new HashMap<>();
            params.put(Constants.KEY_AVAILABILITY, value);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, UserApiMethods.PUT_USER + idUser, new JSONObject(params), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("ERROR_AVAILABILITY", "Error al actualizar availability user.");
                }
            });
            requestQueue.add(jsonObjectRequest);
        }
    }
}
