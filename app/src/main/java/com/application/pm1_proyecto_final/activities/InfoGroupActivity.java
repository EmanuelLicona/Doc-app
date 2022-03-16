package com.application.pm1_proyecto_final.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.adapters.GroupAdapter;
import com.application.pm1_proyecto_final.adapters.UsersGroupAdapter;
import com.application.pm1_proyecto_final.api.GroupApiMethods;
import com.application.pm1_proyecto_final.api.UserApiMethods;
import com.application.pm1_proyecto_final.models.Group;
import com.application.pm1_proyecto_final.models.GroupUser;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.providers.GroupUserProvider;
import com.application.pm1_proyecto_final.providers.GroupsProvider;
import com.application.pm1_proyecto_final.providers.UsersProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class InfoGroupActivity extends AppCompatActivity {

    Group reseiverGroup;

    TextView textViewDescription, textViewTitle, textViewImage;

    AppCompatImageView imageViewBack;

    RoundedImageView roundedImageView;

    PreferencesManager preferencesManager;

    List<User> users;

    UsersGroupAdapter usersGroupAdapter;

    RecyclerView recyclerViewUsuarios;

    Button btnAddMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_group);


        init();
        setListeners();
        loadReceiverDetails();

        loadUsersGroups();
    }

    private void init(){
        reseiverGroup = null;

        preferencesManager = new PreferencesManager(getApplicationContext());

        textViewTitle = (TextView) findViewById(R.id.textTitleGroupInfo);
        textViewDescription = (TextView) findViewById(R.id.textViewDescInfo);
        textViewImage = (TextView) findViewById(R.id.textImageInfo);
        imageViewBack = (AppCompatImageView) findViewById(R.id.btnInfoGroupBack);

        roundedImageView = (RoundedImageView) findViewById(R.id.imageGroupInfo);

        recyclerViewUsuarios = (RecyclerView) findViewById(R.id.usersGroupRecyclerView);

        btnAddMember = (Button) findViewById(R.id.btnAddMember);
    }

    private void setListeners(){
        imageViewBack.setOnClickListener(v->onBackPressed());

        btnAddMember.setOnClickListener(v -> dialogAddMember());
    }

    private void loadReceiverDetails(){
        reseiverGroup = (Group) getIntent().getSerializableExtra(GroupsProvider.NAME_COLLECTION);

        textViewTitle.setText(reseiverGroup.getTitle());
        textViewDescription.setText(reseiverGroup.getDescription());

        if(!reseiverGroup.getImage().isEmpty()){

            roundedImageView.setImageBitmap(getGroupImage(reseiverGroup.getImage()));
            textViewImage.setText(null);
        }
    }

    //En este metodo solo deberian cargar los usuarios que estan aceptados
    private void loadUsersGroups(){
        ArrayList<User> arrayList = new ArrayList<>();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                (GroupApiMethods.GET_USERS_FOR_GROUP_ACTIVE+reseiverGroup.getId()),
                null,
                new com.android.volley.Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            JSONObject  jsonObject = null;

                            User usertemp = null;

                            List<User> arrayList = new ArrayList<>();


                            if(response.getString("res").equals("true")){
//                                t = response.getJSONObject("data").getString("name");


//                                JSONArray array = response.getJSONObject("data").getJSONArray("users");
                                JSONArray array = response.getJSONArray("data");

                                for (int i = 0; i < array.length(); i++) {
                                    jsonObject = new JSONObject(array.get(i).toString());


                                    usertemp = new User();
                                    usertemp.setId(jsonObject.getString("id"));
                                    usertemp.setName(jsonObject.getString("name"));
                                    usertemp.setLastname(jsonObject.getString("lastname"));
                                    usertemp.setNumberAccount(jsonObject.getString("numberAccount"));
//                                    usertemp.setPhone(jsonObject.getString("phone"));
                                    usertemp.setStatus(jsonObject.getString("status_user"));
                                    usertemp.setImage(jsonObject.getString("image"));
//                                    usertemp.setAddress(jsonObject.getString("address"));
//                                    usertemp.setBirthDate(jsonObject.getString("birthDate"));
//                                    usertemp.setCarrera(jsonObject.getString("carrera"));
                                    usertemp.setEmail(jsonObject.getString("email"));
//                                    usertemp.setPassword(jsonObject.getString("password"));



                                    arrayList.add(usertemp);

                                }


                                if(arrayList.size() > 0){
                                    users = arrayList;

                                    UsersGroupAdapter usersGroupAdapter = new UsersGroupAdapter(users, preferencesManager);

                                    recyclerViewUsuarios.setAdapter(usersGroupAdapter);
                                }else{
                                    Toast.makeText(getApplicationContext(), "Advertencia: No se encuentran datos", Toast.LENGTH_SHORT).show();
                                }

                            }else{
                                Toast.makeText(getApplicationContext(), "Error: "+response.getString("msg"), Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }


        );

        requestQueue.add(request);

    }


    private void dialogAddMember(){

        AlertDialog.Builder builder = new AlertDialog.Builder(InfoGroupActivity.this);

        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_add_member, null);

        builder.setView(view);

        AlertDialog dialog = builder.create();

        dialog.show();

        EditText text =(EditText) view.findViewById(R.id.textEmailDialog);

        Button btnAdd = (Button) view.findViewById(R.id.btnAddMemberDialog);

        btnAdd.setOnClickListener(v -> {
            if(text.getText().toString().isEmpty()){

                ResourceUtil.showAlert("Advertencia", "Por favor escribe un correo", InfoGroupActivity.this, "error");
            }else if(!Patterns.EMAIL_ADDRESS.matcher(text.getText().toString()).matches()){

                ResourceUtil.showAlert("Advertencia", "Por favor escribe un correo valido", InfoGroupActivity.this, "error");

            }else{
                searhEmail(text.getText().toString());

                dialog.dismiss();
            }
        });


    }

    private void searhEmail(String email) {

        if(email.equals(preferencesManager.getString(UsersProvider.KEY_EMAIL))){
            ResourceUtil.showAlert("Advertencia", "No se puede enviar la invitacion a usted mismo.", InfoGroupActivity.this, "error");
            return;
        }

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, UserApiMethods.EXIST_EMAIL + email, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String existEmail = response.getString("data");

                    if (existEmail.equals("[]")) {
                        ResourceUtil.showAlert("Advertencia", "El correo ingresado no pertenece a ningun usuario.", InfoGroupActivity.this, "error");
                    } else {

                        User usertemp = new User();

                        JSONArray array = response.getJSONArray("data");

                        JSONObject jsonObject = array.getJSONObject(0);


                        usertemp = new User();
                        usertemp.setId(jsonObject.getString("id"));
                        usertemp.setName(jsonObject.getString("name"));
                        usertemp.setLastname(jsonObject.getString("lastname"));
                        usertemp.setNumberAccount(jsonObject.getString("numberAccount"));
//                                    usertemp.setPhone(jsonObject.getString("phone"));
                        usertemp.setStatus(jsonObject.getString("status"));
                        usertemp.setImage(jsonObject.getString("image"));
//                                    usertemp.setAddress(jsonObject.getString("address"));
//                                    usertemp.setBirthDate(jsonObject.getString("birthDate"));
//                                    usertemp.setCarrera(jsonObject.getString("carrera"));
                        usertemp.setEmail(jsonObject.getString("email"));
//                                    usertemp.setPassword(jsonObject.getString("password"));


//                        Toast.makeText(InfoGroupActivity.this, usertemp.getId(), Toast.LENGTH_SHORT).show();
                        statusInvitation(usertemp);
                    }
                } catch (JSONException e) {
                    ResourceUtil.showAlert("Advertencia", "Se produjo un error al validar el email", InfoGroupActivity.this, "error");
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(InfoGroupActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(request);

    }

    private void statusInvitation(User user) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("group_id", reseiverGroup.getId());
//        params.put("status", "-6");


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, GroupApiMethods.POST_STATUS_USER_GROUP, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {


                try {
                    String resposeData = response.getString("data");



                    if(!resposeData.equals("[]")){

//                        UpdateInvitation();

                        JSONArray array = response.getJSONArray("data");
                        JSONObject jsonObject = array.getJSONObject(0);

                        if(jsonObject.getString("status").equals(GroupUser.STATUS_INVITED)){

                            ResourceUtil.showAlert("Mensaje", "El usuario ya a sido invitado", InfoGroupActivity.this, "info");
                        }else if(jsonObject.getString("status").equals(GroupUser.STATUS_ACCEPT)){

                            ResourceUtil.showAlert("Mensaje", "El usuario ya a sido agregado al grupo", InfoGroupActivity.this, "info");

                        }else{
                            updateInvitation(user.getId(), reseiverGroup.getId(), GroupUser.STATUS_INVITED);
                        }

                    }else {
                        sendInvitation(user.getId(), reseiverGroup.getId());
                    }

                } catch (JSONException e) {
                    ResourceUtil.showAlert("Advertencia", "Se produjo un error al validar el status", InfoGroupActivity.this, "error");
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                ResourceUtil.showAlert("Advertencia", "Se produjo un error al validar el status.",InfoGroupActivity.this, "error");
                error.printStackTrace();

            }
        });

        requestQueue.add(jsonObjectRequest);

    }

    private void sendInvitation(String idUser, String idGroup) {

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", idUser);
        params.put("group_id", idGroup);
        params.put("status", GroupUser.STATUS_INVITED);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, GroupApiMethods.POST_GROUP_USER, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {


                try {
                    String resposeData = response.getString("data");

                    if(!resposeData.equals("[]")){

                        ResourceUtil.showAlert("Mensaje", "Invitacion enviada correctamente", InfoGroupActivity.this, "success");

                    }else {
                        ResourceUtil.showAlert("Advertencia", "Se produjo un error al enviar la invitacion", InfoGroupActivity.this, "error");
                    }

                } catch (JSONException e) {
                    ResourceUtil.showAlert("Advertencia", "Se produjo un error al enviar la invitacion", InfoGroupActivity.this, "error");
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                ResourceUtil.showAlert("Advertencia", "Se produjo un error al registrar el grupo.",InfoGroupActivity.this, "error");
                error.printStackTrace();

            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    private void updateInvitation(String idUser, String idGroup, String status) {

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", idUser);
        params.put("group_id", idGroup);
        params.put("status", status);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, GroupApiMethods.POST_USER_GROUP_UPDATE, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {


                try {
                    String resposeData = response.getString("data");

                    if(!resposeData.equals("[]")){

                        ResourceUtil.showAlert("Mensaje", "Invitacion enviada correctamente", InfoGroupActivity.this, "success");

                    }else {
                        ResourceUtil.showAlert("Advertencia", "Se produjo un error al enviar la invitacion (U)", InfoGroupActivity.this, "error");
                    }

                } catch (JSONException e) {
                    ResourceUtil.showAlert("Advertencia", "Se produjo un error al enviar la invitacion (U)", InfoGroupActivity.this, "error");
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                ResourceUtil.showAlert("Advertencia", "Se produjo un error al registrar el grupo.",InfoGroupActivity.this, "error");
                error.printStackTrace();

            }
        });

        requestQueue.add(jsonObjectRequest);
    }


    private static Bitmap getGroupImage(String encodedImage){

        byte[] bytes = android.util.Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }



}