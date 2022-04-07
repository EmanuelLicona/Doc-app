package com.application.pm1_proyecto_final.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.adapters.PublicationAdapter;
import com.application.pm1_proyecto_final.api.GroupApiMethods;
import com.application.pm1_proyecto_final.api.UserApiMethods;
import com.application.pm1_proyecto_final.listeners.Chatlistener;
import com.application.pm1_proyecto_final.models.GroupUser;
import com.application.pm1_proyecto_final.models.Publication;
import com.application.pm1_proyecto_final.models.Group;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.providers.GroupsProvider;
import com.application.pm1_proyecto_final.providers.PublicationProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PublicationActivity extends AppCompatActivity implements Chatlistener {

    Group reseiverGroup;
    AppCompatImageView imageViewInfo, imageViewBack;
    TextView textViewTitle, txtExistPublications;
    PreferencesManager preferencesManager;
    ProgressBar progressBar;
    List<User> userListApi;
    PublicationAdapter publicationAdapter;
    RecyclerView chatRecyclerView;
    FirebaseFirestore database;
    FloatingActionButton btnNewFile;
    String pathUri = "", typeFile = "";
    PublicationProvider mPublicationProvider;

    AlertDialog.Builder pBuilderSelector;
    CharSequence options[];
    private static final int REQUEST_PERMISSION_STORAGE = 300;

    String statusUserLog;
    String namePublication = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        init();
        setListeners();
        loadReceiverDetails();
    }

    private void init() {
        reseiverGroup = null;

        preferencesManager = new PreferencesManager(getApplicationContext());

        database = FirebaseFirestore.getInstance();

        chatRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewPublication);

        imageViewInfo = (AppCompatImageView) findViewById(R.id.imageInfoChat);
        imageViewBack = (AppCompatImageView) findViewById(R.id.btnChatBack);
        textViewTitle = (TextView) findViewById(R.id.groupTitleChat);
        txtExistPublications = (TextView) findViewById(R.id.existPublications);
        btnNewFile = (FloatingActionButton) findViewById(R.id.btnNewFile);

        pBuilderSelector = new AlertDialog.Builder(this);
        pBuilderSelector.setTitle("Seleccione una opción");
        options = new CharSequence[]{"Visualizar Archivo", "Descargar Archivo"};

        userListApi = new ArrayList<>();
        mPublicationProvider = new PublicationProvider();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PublicationActivity.this);
        chatRecyclerView.setLayoutManager(linearLayoutManager);
        statusUserLog = "";
    }

    private void getAllPublication() {
        Query query = mPublicationProvider.getAll(reseiverGroup.getId());
        FirestoreRecyclerOptions<Publication> options = new FirestoreRecyclerOptions.Builder<Publication>()
                .setQuery(query, Publication.class)
                .build();
        publicationAdapter = new PublicationAdapter(options, PublicationActivity.this, (ArrayList<User>) userListApi, this);
        publicationAdapter.notifyDataSetChanged();
        chatRecyclerView.setAdapter(publicationAdapter);
        publicationAdapter.startListening();

        loading(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getAllUsers();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        if (!notPublications) {
//            publicationAdapter.stopListening();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getGroupReturn();

        statusUserGroup(preferencesManager.getString(Constants.KEY_USER_ID));
    }

    private void getAllUsers() {
        loading(true);
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, UserApiMethods.GET_USER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray arrayUsers = jsonObject.getJSONArray("data");

                    for (int i = 0; i < arrayUsers.length(); i++) {
                        JSONObject rowUser = arrayUsers.getJSONObject(i);
                        User user = new User();
                        user.setId(rowUser.getString("id"));
                        user.setImage(rowUser.getString("image"));
                        user.setName(rowUser.getString("name"));
                        user.setLastname(rowUser.getString("lastname"));
                        userListApi.add(user);
                    }
                    getAllPublication();
                } catch (JSONException ex) {
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

    private void setListeners() {
        imageViewBack.setOnClickListener(v -> onBackPressed());
        imageViewInfo.setOnClickListener(view -> {

            if (reseiverGroup.getStatus().equals(Group.STATUS_INACTIVE)) {
                ResourceUtil.showAlert("Advertencia", "Este grupo a sido eliminado", this, "error");
                return;
            }

            if (statusUserLog.equals(GroupUser.STATUS_LEFT)) {
                ResourceUtil.showAlert("Advertencia", "Usted a salido del grupo", this, "error");
                return;
            }

            moveToInfo();

        });

        btnNewFile.setOnClickListener(v -> {
            if (reseiverGroup.getStatus().equals(Group.STATUS_INACTIVE)) {
                ResourceUtil.showAlert("Advertencia", "Este grupo a sido eliminado", this, "error");
                return;
            }


            if (statusUserLog.equals(GroupUser.STATUS_LEFT)) {
                ResourceUtil.showAlert("Advertencia", "Usted a salido del grupo", this, "error");
                return;
            }
            sendMessage();
        });
    }

    private void moveToInfo() {
        Intent intent = new Intent(getApplicationContext(), InfoGroupActivity.class);
        intent.putExtra(GroupsProvider.NAME_COLLECTION, reseiverGroup);
        startActivity(intent);
    }

    private void loadReceiverDetails() {
        reseiverGroup = (Group) getIntent().getSerializableExtra(GroupsProvider.NAME_COLLECTION);
        textViewTitle.setText(reseiverGroup.getTitle());
    }

    // PARA ENVIAR LA PUBLICACION
    private void sendMessage() {
        String idGroup = reseiverGroup.getId();

        Intent intent = new Intent(PublicationActivity.this, CreatePublicationActivity.class);
        intent.putExtra("ID_GROUP", idGroup);
        intent.putExtra(GroupsProvider.NAME_COLLECTION, reseiverGroup);
        startActivity(intent);
    }

    @Override
    public void onClickFile(Publication publication) {
        pBuilderSelector.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    viewFile(publication);
                } else if (i == 1) {
                    downloadFile(publication);
                }
            }
        });
        pBuilderSelector.show();
    }

    @Override
    public void onClickPublicationDetail(Publication publication, String publicationId) {
        Intent intent = new Intent(PublicationActivity.this, DetailPublicationActivity.class);
        intent.putExtra("Publication", publication);
        intent.putExtra("publicationId", publicationId);
        startActivity(intent);
    }

    private void viewFile(Publication publication) {
        try {
            String typeFile = publication.getType();
            String extensionFile = typeFile.split("/")[1];
            namePublication = publication.getTitle();

            if (ResourceUtil.viewOrDownloadFile(extensionFile).equals("download")) {
                downloadFile(publication);
                return;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(publication.getPath()), typeFile);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.startActivity(intent.createChooser(intent, "Elija la aplicación para abrir el documento"));
        } catch (ActivityNotFoundException e) {
            ResourceUtil.showAlert("Advertencia", "Se produjo un error al visualizar el archivo.", this, "error");
        }
    }

    private void downloadFile(Publication publication) {
        pathUri = publication.getPath();
        typeFile = ResourceUtil.getTypeFile(publication.getType().split("/")[1]);
        namePublication = publication.getTitle();

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
            Toast.makeText(this, "Descargando el archivo", Toast.LENGTH_SHORT).show();
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pathUri));
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            if (!namePublication.isEmpty()) {
                request.setTitle("Descargar "+namePublication);
            } else {
                request.setTitle("Descargar");
            }

            request.setDescription("Descargando archivo....");
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "" + System.currentTimeMillis() + "." + typeFile);

            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            manager.enqueue(request);
        } else {
            ResourceUtil.showAlert("Advertencia", "Se produjo un error al descargar el archivo", this, "error");
        }
    }

    //Metodo para recuperar el grupo al volver a la actividad
    private void getGroupReturn() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                (GroupApiMethods.POST_GROUP + reseiverGroup.getId()),
                null,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONObject jsonObject = null;
                            Group groupTemp = null;

                            if (!response.has("res")) {
                                jsonObject = response.getJSONObject("data");

                                groupTemp = new Group();
                                groupTemp.setId(jsonObject.getString("id"));
                                groupTemp.setTitle(jsonObject.getString("title"));
                                groupTemp.setDescription(jsonObject.getString("description"));
                                groupTemp.setImage(jsonObject.getString("image"));
                                groupTemp.setStatus(jsonObject.getString("status"));
                                groupTemp.setUser_create(jsonObject.getString("user_id_created"));

                                reseiverGroup = groupTemp;
                                textViewTitle.setText(reseiverGroup.getTitle());
                            } else {
                                Toast.makeText(getApplicationContext(), "Error: " + response.getString("msg"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requestQueue.add(request);
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

    private void loading(boolean isLoading) {
        progressBar = (ProgressBar) findViewById(R.id.publicationsProgressBar);
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void statusUserGroup(String user) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", user);
        params.put("group_id", reseiverGroup.getId());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, GroupApiMethods.POST_STATUS_USER_GROUP, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {


                try {
                    String resposeData = response.getString("data");



                    if(!resposeData.equals("[]")){

                        JSONArray array = response.getJSONArray("data");
                        JSONObject jsonObject = array.getJSONObject(0);

                        statusUserLog = jsonObject.getString("status");

                    }
                } catch (JSONException e) {
                    ResourceUtil.showAlert("Advertencia", "Se produjo un error al validar el status", PublicationActivity.this, "error");
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ResourceUtil.showAlert("Advertencia", "Se produjo un error al validar el status.",PublicationActivity.this, "error");
                error.printStackTrace();
            }
        });

        requestQueue.add(jsonObjectRequest);

    }

}