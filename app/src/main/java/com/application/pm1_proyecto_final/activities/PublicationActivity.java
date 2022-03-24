package com.application.pm1_proyecto_final.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
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
    TextView textViewTitle, txtExistPublications;
    PreferencesManager preferencesManager;
    ProgressBar progressBar;
    List<Publication> publications;
    List<User> userListApi;
    PublicationAdapter publicationAdapter;
    FirebaseFirestore database;
    FloatingActionButton btnNewFile;
    RecyclerView chatRecyclerView;
    String pathUri = "", typeFile = "";

    AlertDialog.Builder pBuilderSelector;
    CharSequence options[];
    private static final int REQUEST_PERMISSION_STORAGE = 300;


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
        txtExistPublications = (TextView) findViewById(R.id.existPublications);
        btnNewFile = (FloatingActionButton) findViewById(R.id.btnNewFile);

        pBuilderSelector = new AlertDialog.Builder(this);
        pBuilderSelector.setTitle("Seleccione una opción");
        options = new CharSequence[]{"Visualizar Archivo", "Descargar Archivo"};

        publications = new ArrayList<>();
        userListApi = new ArrayList<>();
        loading(true);
        getAllUsers();

        publicationAdapter = new PublicationAdapter(publications, preferencesManager.getString(Constants.KEY_USER_ID),
                this,PublicationActivity.this);
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

    private void existPublications() {
        if (publications.isEmpty()) {
            txtExistPublications.setVisibility(View.VISIBLE);
        } else {
            txtExistPublications.setVisibility(View.GONE);
        }
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
        HashMap<String, Object> params = new HashMap<>();
        params.put(Constants.KEY_STATUS_MESSAGE, Publication.STATUS_DELETE);
//        positionGeneral = position;

        database.collection(Constants.KEY_COLLECTION_CHAT).document(publication.getIdFirebase()).update(params)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        userListApi.remove(position);
                        publicationAdapter.notifyItemRemoved(position);
                        ResourceUtil.showAlert("Confirmación", "Publicación Eliminado Correctamente.", PublicationActivity.this, "success");
                    }else {
                        ResourceUtil.showAlert("Advertencia", "Se produjo un error al eliminar la publicación.", PublicationActivity.this, "error");
                    }
                })
                .addOnFailureListener(e -> {
                    ResourceUtil.showAlert("Advertencia", "Error al eliminar la publicación.", PublicationActivity.this, "error");
//                    positionGeneral=-1;
                });
    }

    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_STATUS_MESSAGE, Publication.STATUS_SENT)
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

            for (DocumentChange documentChange : value.getDocumentChanges()) {
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
        existPublications();
    };


    @Override
    public void onClickChat(Publication publication, int position) {

        if(publication.getStatus().equals(Publication.STATUS_DELETE)){
            ResourceUtil.showAlert("Advertencia", "Esta publicación ya ha sido eliminada.", PublicationActivity.this, "error");
            return;
        }

        if(publication.getSenderId().equals(preferencesManager.getString(Constants.KEY_USER_ID))){
            showAlertMessage("Confirmación", "¿Desea eliminar esta publicación?", PublicationActivity.this, publication, position);
        }
    }

    @Override
    public void onClickFile(Publication publication, int position) {
        pBuilderSelector.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    viewFile(publication);
                } else if(i == 1) {
                    downloadFile(publication);
                }
            }
        });
        pBuilderSelector.show();
    }

    private void viewFile(Publication publication) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(publication.getPath()), publication.getType());
        startActivity(intent);
    }

    private void downloadFile(Publication publication) {
        pathUri = publication.getPath();
        typeFile = publication.getType().split("/")[1];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions(permissions, REQUEST_PERMISSION_STORAGE);
                } else {
                    startDownloadFile();
                }
            }
        } else {
            startDownloadFile();
        }

    }

    private void startDownloadFile() {
        if (!pathUri.isEmpty() && !typeFile.isEmpty()) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pathUri));
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            request.setTitle("Descargar");
            request.setDescription("Descargando archivo....");
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, ""+ System.currentTimeMillis()+"."+typeFile);

            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            manager.enqueue(request);
        } else {
            ResourceUtil.showAlert("Advertencia", "Se produjo un error al descargar el archivo", this, "error");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_STORAGE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startDownloadFile();
        } else {
            Toast.makeText(this, "Permiso denegado para guardar el archivo.", Toast.LENGTH_SHORT).show();
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