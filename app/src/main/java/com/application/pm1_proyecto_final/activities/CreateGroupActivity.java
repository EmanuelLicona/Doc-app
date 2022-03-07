package com.application.pm1_proyecto_final.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.application.pm1_proyecto_final.R;
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
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CreateGroupActivity extends AppCompatActivity {

    AppCompatImageView imageViewBack;
    TextView txtAddImage;
    TextInputEditText txtTitle, txtDescription;
    Button btnSaveGroup;
    String encodedImage;
    RoundedImageView roundedImageView;

    boolean returnStatus;

    private PreferencesManager preferencesManager;

    GroupsProvider groupsProvider;

    SweetAlertDialog pDialog;


    User reseiverUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        init();
        setListeners();

        loadReceiverDetails();

    }


    private void init(){

        preferencesManager = new PreferencesManager(getApplicationContext());

        groupsProvider = new GroupsProvider();

        encodedImage = "";

        reseiverUser = null;

        pDialog = ResourceUtil.showAlertLoading(CreateGroupActivity.this);

        imageViewBack = (AppCompatImageView) findViewById(R.id.btnCreateGroupBack);
        txtAddImage = (TextView) findViewById(R.id.textAddImageRegister);
        txtTitle = (TextInputEditText) findViewById(R.id.txtTitleGroupRegister);
        txtDescription = (TextInputEditText) findViewById(R.id.txtDescriptionGroupRegister);
        roundedImageView = (RoundedImageView) findViewById(R.id.imageGroupRegister);
        btnSaveGroup = (Button) findViewById(R.id.btnSaveGroupRegister);

        txtDescription.setText(preferencesManager.getString(Constants.KEY_USER_ID));
    }

    private void setListeners(){
        imageViewBack.setOnClickListener(v -> onBackPressed());

        txtAddImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);


            pickImage.launch(intent);
        });

        btnSaveGroup.setOnClickListener(v -> {
            if(isValidGroup()){

               saveGroup();

            }
        });
    }


    private void saveGroup(){

        pDialog.show();

        Group group = new Group();

        User usertemp = new User();

        usertemp.setId(reseiverUser.getId());
        usertemp.setName(reseiverUser.getName());
        usertemp.setLastname(reseiverUser.getLastname());
        usertemp.setImage(reseiverUser.getImage());
        usertemp.setEmail(reseiverUser.getEmail());



        group.setTitle(txtTitle.getText().toString());
        group.setDescription(txtDescription.getText().toString());
        group.setUser_create(preferencesManager.getString(Constants.KEY_USER_ID));
        group.setImage(encodedImage);
        group.setStatus(Group.STATUS_ACTIVE);


        Gson gson = new Gson();
        ArrayList<User> users = new ArrayList<>();
        users.add(usertemp);



        group.setJson_users(gson.toJson(users));

        groupsProvider.create(group).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {

                pDialog.dismiss();
                if (task.isSuccessful()) {

                    saveInvitationAdminGroup(task);

                    convertListGroupsFromJson(group);

                finish();

                } else {
                    ResourceUtil.showAlert("Advertencia", "El usuario no se pudo registrar.", CreateGroupActivity.this, "error");

                }


            }


        });
    }



    private boolean saveInvitationAdminGroup(Task<DocumentReference> task) {
        GroupUser groupUser = new GroupUser();

        returnStatus = false;

        groupUser.setIdGroup(task.getResult().getId());
        groupUser.setIdUser(preferencesManager.getString(Constants.KEY_USER_ID));
        groupUser.setStatus(GroupUser.STATUS_ACCEPT);
        groupUser.setDate(new Date());

        GroupUserProvider groupUserProvider = new GroupUserProvider();

        groupUserProvider.create(groupUser).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if(task.isSuccessful())
                    returnStatus = true;
            }
        });

        return returnStatus;

    }

    private void saveUsergroup(String json){

        FirebaseFirestore database = FirebaseFirestore.getInstance();

        DocumentReference documentReference =
                database.collection(UsersProvider.NAME_COLLECTION).document(preferencesManager.getString(Constants.KEY_USER_ID));

        documentReference.update(UsersProvider.KEY_JSON, json);
    }

    private void convertListGroupsFromJson(Group group){
        Gson gson = new Gson();

        String json = "";


        if(reseiverUser.getJson_groups() != null){
            ArrayList<Group>  groupsTem = gson.fromJson(reseiverUser.getJson_groups(), new TypeToken<ArrayList<Group>>(){}.getType());
            groupsTem.add(group);


           json = gson.toJson(groupsTem);
        }else{
            ArrayList<Group>  groupsTem = new ArrayList<>();
            groupsTem.add(group);


            json = gson.toJson(groupsTem);
        }

        saveUsergroup(json);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK){

                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();

                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            roundedImageView.setImageBitmap(bitmap);

                            txtAddImage.setVisibility(View.INVISIBLE);

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
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(bytes, Base64.DEFAULT);

    }

    private boolean isValidGroup(){

       if (txtTitle.getText().toString().trim().isEmpty()){
            ResourceUtil.showAlert("Advertencia", "Por favor escriba un titulo", CreateGroupActivity.this, "error");
            return false;
        }else if (txtDescription.getText().toString().trim().isEmpty()){
           ResourceUtil.showAlert("Advertencia", "Por favor escriba una descripcion", CreateGroupActivity.this, "error");
            return false;
        }else{
            return true;
        }

    }

    private void loadReceiverDetails(){
        reseiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);


//        Toast.makeText(this, reseiverUser.getJson_groups(), Toast.LENGTH_SHORT).show();
    }

}