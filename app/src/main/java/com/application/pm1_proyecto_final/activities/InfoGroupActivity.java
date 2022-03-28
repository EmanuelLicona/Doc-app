package com.application.pm1_proyecto_final.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class InfoGroupActivity extends AppCompatActivity {

    Group reseiverGroup;

    TextView textViewDescription, textViewTitle, textViewImage;

    AppCompatImageView imageViewBack, imageViewEdit;

    RoundedImageView roundedImageView;

    PreferencesManager preferencesManager;

    List<User> users;

    RecyclerView recyclerViewUsuarios;

    Button btnAddMember, btnDeleteGroup, btnLeaveGroup;

    String encodedImage = "";

    SweetAlertDialog pDialog;

    RoundedImageView roundedImageViewDialog;
    Button btnSaveGroupDialog;
    TextView textImageDialog;
    TextInputEditText textInputEditTextTitle, textInputEditTextDescription;

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

        pDialog = ResourceUtil.showAlertLoading(InfoGroupActivity.this);

        textViewTitle = (TextView) findViewById(R.id.textTitleGroupInfo);
        textViewDescription = (TextView) findViewById(R.id.textViewDescInfo);
        textViewImage = (TextView) findViewById(R.id.textImageInfo);
        imageViewBack = (AppCompatImageView) findViewById(R.id.btnInfoGroupBack);
        imageViewEdit = (AppCompatImageView) findViewById(R.id.imageEditGroup);

        roundedImageView = (RoundedImageView) findViewById(R.id.imageGroupInfo);

        recyclerViewUsuarios = (RecyclerView) findViewById(R.id.usersGroupRecyclerView);

        btnAddMember = (Button) findViewById(R.id.btnAddMember);
        btnDeleteGroup = (Button) findViewById(R.id.btnCerrarGrupo);
        btnLeaveGroup = (Button) findViewById(R.id.btnSalirGrupo);
    }

    private void setListeners(){
        imageViewBack.setOnClickListener(v->onBackPressed());

        btnAddMember.setOnClickListener(v -> dialogAddMember());

        imageViewEdit.setOnClickListener(v -> dialogEditGroup());

        btnLeaveGroup.setOnClickListener(v -> {

            showAlertMessageLeave("Mensaje", "¿ Desea salir del grupo ?", InfoGroupActivity.this);

        });
    }

    private void loadReceiverDetails(){
        reseiverGroup = (Group) getIntent().getSerializableExtra(GroupsProvider.NAME_COLLECTION);

        textViewTitle.setText(reseiverGroup.getTitle());
        textViewDescription.setText(reseiverGroup.getDescription());

        if(!reseiverGroup.getImage().isEmpty()){

            roundedImageView.setImageBitmap(getGroupImage(reseiverGroup.getImage()));
            textViewImage.setText(null);
        }



        if(!preferencesManager.getString(Constants.KEY_USER_ID).equals(reseiverGroup.getUser_create())){

            btnAddMember.setVisibility(View.GONE);
            btnDeleteGroup.setVisibility(View.GONE);
            imageViewEdit.setVisibility(View.GONE);

        }else{
            btnLeaveGroup.setVisibility(View.GONE);
        }
    }

    //En este metodo solo deberian cargar los usuarios que estan aceptados
    private void loadUsersGroups(){
        ArrayList<User> arrayList = new ArrayList<>();

        RequestQueue requestQueue = Volley.newRequestQueue(InfoGroupActivity.this);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
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

                                    UsersGroupAdapter usersGroupAdapter = new UsersGroupAdapter(users, reseiverGroup, preferencesManager);

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

    private void dialogEditGroup(){

        AlertDialog.Builder builder = new AlertDialog.Builder(InfoGroupActivity.this);

        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_edit_group, null);

        builder.setView(view);

        AlertDialog dialog = builder.create();

        dialog.show();

            roundedImageViewDialog = (RoundedImageView) view.findViewById(R.id.imageGroupEdit);
            btnSaveGroupDialog = (Button) view.findViewById(R.id.btnSaveGroupEdit);

            textImageDialog =(TextView) view.findViewById(R.id.textAddImageEdit);

            textInputEditTextTitle = (TextInputEditText) view.findViewById(R.id.txtTitleGroupEdit);
            textInputEditTextDescription = (TextInputEditText) view.findViewById(R.id.txtDescriptionGroupEdit);


            textInputEditTextTitle.setText(reseiverGroup.getTitle());
            textInputEditTextDescription.setText(reseiverGroup.getDescription());
            roundedImageViewDialog.setImageBitmap(getGroupImage(reseiverGroup.getImage()));



            roundedImageViewDialog.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);


                pickImageDialog.launch(intent);
            });

            btnSaveGroupDialog.setOnClickListener(v -> {
                if(isValidGroup()){

                    saveGroup(dialog);

                }
            });




    }

    private boolean isValidGroup(){

        if (textInputEditTextTitle.getText().toString().trim().isEmpty()){
            ResourceUtil.showAlert("Advertencia", "Por favor escriba un titulo", InfoGroupActivity.this, "error");
            return false;
        }else if (textInputEditTextDescription.getText().toString().trim().isEmpty()){
            ResourceUtil.showAlert("Advertencia", "Por favor escriba una descripcion", InfoGroupActivity.this, "error");
            return false;
        }else{
            return true;
        }

    }

    private void saveGroup(AlertDialog dialog){

        pDialog.show();

        Group group = new Group();

        encodedImage = (encodedImage.isEmpty())?reseiverGroup.getImage():encodedImage;

        group.setTitle(textInputEditTextTitle.getText().toString());
        group.setDescription(textInputEditTextDescription.getText().toString());
        group.setUser_create(reseiverGroup.getUser_create());
        group.setImage(encodedImage);
        group.setStatus(Group.STATUS_ACTIVE);

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        HashMap<String, String> params = new HashMap<>();
        params.put("id", reseiverGroup.getId());
        params.put("idFirebase", ResourceUtil.createCodeRandom(6));
        params.put("title", group.getTitle());
        params.put("description", group.getDescription());
        params.put("image", group.getImage());
        params.put("status", group.getStatus());
        params.put("user_id_created", group.getUser_create());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, GroupApiMethods.POST_GROUP+reseiverGroup.getId(), new JSONObject(params),
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                pDialog.dismiss();

                try {
                    String resposeData = response.getString("data");


                    if(!resposeData.equals("[]")){

                        dialog.dismiss();

                        textViewTitle.setText(group.getTitle());
                        textViewDescription.setText(group.getDescription());

                        if(!group.getImage().isEmpty()){

                            roundedImageView.setImageBitmap(getGroupImage(group.getImage()));
//                            textViewImage.setText(null);
                        }


                    }else {
                        ResourceUtil.showAlert("Advertencia", "Se produjo un error al editar el grupo.",InfoGroupActivity.this, "error");
                    }

                } catch (JSONException e) {
                    ResourceUtil.showAlert("Advertencia", "Se produjo un error al editar el grupo",InfoGroupActivity.this, "error");
                }


//                Toast.makeText(CreateGroupActivity.this, response.toString(), Toast.LENGTH_SHORT).show();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                ResourceUtil.showAlert("Advertencia", "Se produjo un error al registrar el grupo.",InfoGroupActivity.this, "error");
                Log.d("ERROR_USER", "Error Register: "+error.getMessage());

//                Toast.makeText(CreateGroupActivity.this, "Error: " +error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);


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

    private void leaveGroup(String idUser, String idGroup, String status) {

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

                        ResourceUtil.showAlert("Mensaje", "Has salido el grupo", InfoGroupActivity.this, "success");

                        FirebaseMessaging.getInstance().unsubscribeFromTopic(reseiverGroup.getId());
//                        finish();

                    }else {
                        ResourceUtil.showAlert("Advertencia", "Se produjo un error ", InfoGroupActivity.this, "error");
                    }

                } catch (JSONException e) {
                    ResourceUtil.showAlert("Advertencia", "Se produjo un error", InfoGroupActivity.this, "error");
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                ResourceUtil.showAlert("Advertencia", "Se produjo un error .",InfoGroupActivity.this, "error");
                error.printStackTrace();

            }
        });

        requestQueue.add(jsonObjectRequest);
    }


    private static Bitmap getGroupImage(String encodedImage){

        byte[] bytes = android.util.Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private final ActivityResultLauncher<Intent> pickImageDialog = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK){

                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();

                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            roundedImageViewDialog.setImageBitmap(bitmap);

//                            textImageDialog.setVisibility(View.INVISIBLE);

                            encodedImage = encodeImage(bitmap);
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private String encodeImage(Bitmap bitmap){

        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();

        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(bytes, Base64.DEFAULT);

    }


    public void showAlertMessageLeave(String title, String response, Context context) {

        new SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(title)
                .setContentText(response)
                .setConfirmText("NO")
                .setCancelButton("SI",v->{
                    leaveGroup(preferencesManager.getString(Constants.KEY_USER_ID), reseiverGroup.getId(), GroupUser.STATUS_LEFT);
                    v.dismiss();
                })
                .show();
    }


}