package com.application.pm1_proyecto_final.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.models.Publication;
import com.application.pm1_proyecto_final.models.Group;
import com.application.pm1_proyecto_final.providers.GroupsProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class CreatePublicationActivity extends AppCompatActivity {

    String position = "", idGroup = "", title = "", description = "", type = "", extension = "";
    ImageView imageViewPublication;
    CircleImageView btnBack;
    TextInputEditText txtTitle, txtDescription;
    Button btnCreatePublication;
    Uri dataPublication;
    FirebaseFirestore database;
    PreferencesManager preferencesManager;
    StorageReference storageReference;
    SweetAlertDialog pDialog;
    Group receiverGroup;

    private static final int REQUEST_UPLOAD_FILE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_publication);

        database = FirebaseFirestore.getInstance();
        preferencesManager = new PreferencesManager(getApplicationContext());
        receiverGroup = null;

        loadReceivedData();

        imageViewPublication = (ImageView) findViewById(R.id.imageViewPublication);
        btnBack = (CircleImageView) findViewById(R.id.btnBackCreatePublication);
        btnCreatePublication = (Button) findViewById(R.id.btnCreatePublication);
        txtTitle = (TextInputEditText) findViewById(R.id.txtTitlePublication);
        txtDescription = (TextInputEditText) findViewById(R.id.txtDescriptionPublication);
        pDialog = ResourceUtil.showAlertLoading(CreatePublicationActivity.this);

        imageViewPublication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();
            }
        });

        btnCreatePublication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePublication();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void loadReceivedData() {
        position = getIntent().getStringExtra("POSITION");
        idGroup = getIntent().getStringExtra("ID_GROUP");
        receiverGroup = (Group) getIntent().getSerializableExtra(GroupsProvider.NAME_COLLECTION);
    }

    private void savePublication() {
        String response = validateFields();

        if (!response.equals("OK")) {
            ResourceUtil.showAlert("Advertencia", response,this,"error");
        } else {
            pDialog.show();
            storageReference = FirebaseStorage.getInstance().getReference("publications/");
            String fileName = extension.toUpperCase()+"_"+System.currentTimeMillis()+"."+extension;
            StorageReference reference = storageReference.child(fileName);

            reference.putFile(dataPublication).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isComplete());
                    Uri uri = uriTask.getResult();

                    HashMap<String, Object> params = new HashMap<>();
                    params.put(Constants.KEY_SENDER_ID, preferencesManager.getString(Constants.KEY_USER_ID));
                    params.put(Constants.KEY_GROUP_ID, idGroup);
                    params.put("title", title);
                    params.put("description", description);
                    params.put("path", uri.toString());
                    params.put("type", type);
                    params.put(Constants.KEY_STATUS_MESSAGE, Publication.STATUS_SENT);
                    params.put(Constants.KEY_POSITION_MESSAGE, position);
                    params.put(Constants.KEY_TIMESTAMP, new Date());

                    database.collection(Constants.KEY_COLLECTION_CHAT).add(params).addOnCompleteListener(task -> {
                        pDialog.dismiss();
                        if(task.isSuccessful()){
                            Intent intent = new Intent(CreatePublicationActivity.this, PublicationActivity.class);
                            intent.putExtra(GroupsProvider.NAME_COLLECTION, receiverGroup);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }else{
                            ResourceUtil.showAlert("Advertencia", "Se produjo un error al guardar la publicación.",CreatePublicationActivity.this,"error");
                        }
                    })
                    .addOnFailureListener(e -> {
                        pDialog.dismiss();
                        ResourceUtil.showAlert("Advertencia", "Error al guardar la publicación.",CreatePublicationActivity.this,"error");
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pDialog.dismiss();
                    ResourceUtil.showAlert("Advertencia", "Se produjo un error al guardar el archivo seleccionado. "+e.getMessage(),CreatePublicationActivity.this,"error");
                }
            });
        }
    }

    private String validateFields() {
        String response = "OK";
        title = txtTitle.getText().toString().trim();
        description = txtDescription.getText().toString().trim();

        if (title.isEmpty()) {
            response = "Debes ingresar el título de la publicación";
        } else if(dataPublication == null) {
            response = "Debes seleccionar el archivo que tendra la publicación";
        }

        return response;
    }

    private void uploadFile() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("*/*");
        String[] mimeTypes = {"application/pdf", "image/*", "video/*", "audio/*"};
        galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(galleryIntent, REQUEST_UPLOAD_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_UPLOAD_FILE) {
            dataPublication = data.getData();
            type = getContentResolver().getType(dataPublication);
            extension = type.split("/")[1];
            /*
                application/pdf
                image/jpeg, image/png
                audio/ogg, audio/mpeg
                video/mp4
            */
            setPreviewImageView();
        }
    }

    private void setPreviewImageView() {
        String[] extensionFile = type.split("/");
        if (type.equals("application/pdf")) {
            imageViewPublication.setImageResource(R.drawable.pdf);
        } else if(extensionFile[0].equals("image")) {
            imageViewPublication.setImageURI(dataPublication);
        } else if (extensionFile[0].equals("audio")) {
            imageViewPublication.setImageResource(R.drawable.audio);
        } else if(extensionFile[0].equals("video")) {
            imageViewPublication.setImageResource(R.drawable.video);
        }

    }


}