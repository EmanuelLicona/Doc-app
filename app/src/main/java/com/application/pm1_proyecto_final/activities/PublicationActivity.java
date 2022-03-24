package com.application.pm1_proyecto_final.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.adapters.PublicationAdapter;
import com.application.pm1_proyecto_final.api.UserApiMethods;
import com.application.pm1_proyecto_final.listeners.Chatlistener;
import com.application.pm1_proyecto_final.models.Publication;
import com.application.pm1_proyecto_final.models.Group;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.providers.GroupsProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class PublicationActivity extends AppCompatActivity implements Chatlistener {

    Group reseiverGroup;

    AppCompatImageView imageViewInfo, imageViewBack;

    TextView textViewTitle;

    PreferencesManager preferencesManager;
    ProgressBar progressBar;

    List<Publication> publications;
    List<User> userListApi;

    PublicationAdapter publicationAdapter;

    FirebaseFirestore database;

    FloatingActionButton btnNewFile;

    RecyclerView chatRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        init();
        setListeners();
        loadReceiverDetails();
    }

    private void init(){
        reseiverGroup = null;

//        positionGeneral = -1;

        preferencesManager = new PreferencesManager(getApplicationContext());

        database = FirebaseFirestore.getInstance();

        chatRecyclerView = (RecyclerView) findViewById(R.id.chatRecyclerView);
        progressBar = (ProgressBar) findViewById(R.id.publicationsProgressBar);

        imageViewInfo = (AppCompatImageView) findViewById(R.id.imageInfoChat);
        imageViewBack = (AppCompatImageView) findViewById(R.id.btnChatBack);
        textViewTitle = (TextView) findViewById(R.id.groupTitleChat);
        btnNewFile = (FloatingActionButton) findViewById(R.id.btnNewFile);

        loading(true);
        publications = new ArrayList<>();
        userListApi = new ArrayList<>();
        getAllUsers();

        publicationAdapter = new PublicationAdapter(
                publications,
                preferencesManager.getString(Constants.KEY_USER_ID),
                this,
                PublicationActivity.this
        );

        chatRecyclerView.setAdapter(publicationAdapter);
    }

    private void getAllUsers() {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, UserApiMethods.GET_USER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray arrayUsers = jsonObject.getJSONArray("data");

                    for(int i = 0; i < arrayUsers.length(); i++) {
                        JSONObject rowUser = arrayUsers.getJSONObject(i);
                        User user = new User();
                        user.setId(rowUser.getString("id"));
                        user.setImage(rowUser.getString("image"));
                        user.setName(rowUser.getString("name"));
                        user.setLastname(rowUser.getString("lastname"));
                        userListApi.add(user);
                    }
                    loading(false);
                    if (!userListApi.isEmpty()) {
                        listenMessages();
                    } else {
                        Toast.makeText(PublicationActivity.this, "No se encontraron publicaciones.", Toast.LENGTH_SHORT).show();
                    }

                }
                catch (JSONException ex) {
                    ResourceUtil.showAlert("Advertencia", "Se produjo un error al obtener la informacion de los usuarios que tiene publicaciones.", PublicationActivity.this, "error");
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ResourceUtil.showAlert("Advertencia", "Se produjo un error al obtener la informacion de los usuarios que tiene publicaciones.", PublicationActivity.this, "error");
            }
        });
        queue.add(stringRequest);
    }

    private void setListeners(){
        imageViewBack.setOnClickListener(v -> onBackPressed());
        imageViewInfo.setOnClickListener(view -> moveToInfo());

        btnNewFile.setOnClickListener(v -> sendMessage());
    }

    private void moveToInfo() {
        Intent intent = new Intent(getApplicationContext(), InfoGroupActivity.class);
        intent.putExtra(GroupsProvider.NAME_COLLECTION, reseiverGroup);
        startActivity(intent);
    }

    private void loadReceiverDetails(){
        reseiverGroup = (Group) getIntent().getSerializableExtra(GroupsProvider.NAME_COLLECTION);
        textViewTitle.setText(reseiverGroup.getTitle());
    }

    /*
    *
    * */

    // PARA ENVIAR LA PUBLICACION
    private void sendMessage(){
        int position = (publications.size()==0) ? 0 : publications.size();
        String idGroup = reseiverGroup.getId();

        Intent intent = new Intent(PublicationActivity.this, CreatePublicationActivity.class);
        intent.putExtra("POSITION", position+"");
        intent.putExtra("ID_GROUP", idGroup);
        intent.putExtra(GroupsProvider.NAME_COLLECTION, reseiverGroup);
        startActivity(intent);
    }

    private void deleteMessage(Publication publication, int position){

        HashMap<String, Object> mensaje = new HashMap<>();


        mensaje.put(Constants.KEY_MESSAGE, "Mensage eliminado");
        mensaje.put(Constants.KEY_STATUS_MESSAGE, Publication.STATUS_DELETE);

//        positionGeneral = position;

        database.collection(Constants.KEY_COLLECTION_CHAT).document(publication.getIdFirebase())
                .update(mensaje)
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful()){
                        ResourceUtil.showAlert("Mensaje", "Publicacion eliminado correctamente", PublicationActivity.this, "success");
                    }else{
                        ResourceUtil.showAlert("Error", "La publicacion no se pudo eliminar", PublicationActivity.this, "error");
                    }


                })
                .addOnFailureListener(e -> {
                    ResourceUtil.showAlert("Error", "El mensaje no se pudo eliminar", PublicationActivity.this, "error");
//                    positionGeneral=-1;
                });
    }

    private Bitmap getBitmapFromEndodedString(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }


    private void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
//                .whereEqualTo(Constants.KEY_STATUS_MESSAGE, ChatMessage.STATUS_SENT)
                .whereEqualTo(Constants.KEY_GROUP_ID, reseiverGroup.getId())
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error!=null){
            return ;
        }

        if(value != null) {
            int count = publications.size();
            int position = -1;

            for (DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED) {
                    Publication publication = new Publication();
                    publication.setIdFirebase(documentChange.getDocument().getId());
                    publication.setSenderId(documentChange.getDocument().getString(Constants.KEY_SENDER_ID));

                    for (int i = 0; i < userListApi.size(); i++) {
                        if (userListApi.get(i).getId().equals(publication.getSenderId())) {
                            String nameUser = userListApi.get(i).getName()+" "+userListApi.get(i).getLastname();
                            publication.setImageProfileUser(userListApi.get(i).getImage());
                            publication.setNameUser(nameUser);
                        }
                    }

                    publication.setGroupId(documentChange.getDocument().getString(Constants.KEY_GROUP_ID));
                    publication.setDatatime(getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP)));
                    publication.setStatus(documentChange.getDocument().getString(Constants.KEY_STATUS_MESSAGE));
                    publication.setPosition(documentChange.getDocument().getString(Constants.KEY_POSITION_MESSAGE));
                    publication.setDateObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    publication.setTitle(documentChange.getDocument().getString("title"));
                    publication.setPath(documentChange.getDocument().getString("path"));
                    publication.setDescription(documentChange.getDocument().getString("description"));
                    publication.setType(documentChange.getDocument().getString("type"));

                    publications.add(publication);
                }

                if(documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    Toast.makeText(this, "ENTRANDO 2", Toast.LENGTH_SHORT).show();
                    Publication publication = new Publication();
                    publication.setIdFirebase(documentChange.getDocument().getId());
                    publication.setSenderId(documentChange.getDocument().getString(Constants.KEY_SENDER_ID));
                    publication.setGroupId(documentChange.getDocument().getString(Constants.KEY_GROUP_ID));
                    publication.setDatatime(getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP)));
                    publication.setStatus(documentChange.getDocument().getString(Constants.KEY_STATUS_MESSAGE));
                    publication.setPosition(documentChange.getDocument().getString(Constants.KEY_POSITION_MESSAGE));
                    publication.setDateObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    publication.setTitle(documentChange.getDocument().getString("title"));
                    publication.setImageProfileUser(documentChange.getDocument().getString("imageProfileUser"));
                    publication.setPath(documentChange.getDocument().getString("path"));
                    publication.setDescription(documentChange.getDocument().getString("description"));
                    publication.setType(documentChange.getDocument().getString("type"));

                    position = Integer.parseInt(publication.getPosition());
                    publications.set(position, publication);
                    count = -1;
                }
            }

            Collections.sort(publications, (obj1, obj2) -> obj1.getDateObject().compareTo(obj2.getDateObject()));

            if(count == 0){
                publicationAdapter.notifyDataSetChanged();

            }else if(count == -1){
                publicationAdapter.notifyItemRangeChanged(position, publications.size());
            }else{
                publicationAdapter.notifyItemRangeInserted(publications.size(), publications.size());
                chatRecyclerView.smoothScrollToPosition(publications.size()-1);
            }

            chatRecyclerView.setVisibility(View.VISIBLE);

        }
    };


    @Override
    public void onClickChat(Publication publication, int position) {

        if(publication.getStatus().equals(Publication.STATUS_DELETE)){
            ResourceUtil.showAlert("Error", "Esta publicacion ya ha sido eliminada", PublicationActivity.this, "error");
            return;
        }

        if(publication.getSenderId().equals(preferencesManager.getString(Constants.KEY_USER_ID))){
            showAlertMessage("Mensaje", "¿Desea eliminar esta publicacion?", PublicationActivity.this, publication, position);
        }else{
            ResourceUtil.showAlert("Error", "Solo puede eliminar publicaciones propias", PublicationActivity.this, "error");
        }


    }

    public void showAlertMessage(String title, String response, Context context, Publication publication, int position) {
        new SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
            .setTitleText(title)
            .setContentText(response)
            .setConfirmText("NO")
            .setCancelButton("SI",v->{
                deleteMessage(publication, position);
                v.dismiss();
            })
            .show();
    }

    private void loading(boolean isLoading) {
        if(isLoading){
            progressBar.setVisibility(View.VISIBLE);
        }else{
            progressBar.setVisibility(View.GONE);
        }
    }

}