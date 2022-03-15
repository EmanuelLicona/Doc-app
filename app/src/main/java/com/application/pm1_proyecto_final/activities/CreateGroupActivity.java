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
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.application.pm1_proyecto_final.R;
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
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        init();
        setListeners();

    }


    private void init(){

        preferencesManager = new PreferencesManager(getApplicationContext());

        groupsProvider = new GroupsProvider();

        encodedImage = "";

        pDialog = ResourceUtil.showAlertLoading(CreateGroupActivity.this);

        imageViewBack = (AppCompatImageView) findViewById(R.id.btnCreateGroupBack);
        txtAddImage = (TextView) findViewById(R.id.textAddImageRegister);
        txtTitle = (TextInputEditText) findViewById(R.id.txtTitleGroupRegister);
        txtDescription = (TextInputEditText) findViewById(R.id.txtDescriptionGroupRegister);
        roundedImageView = (RoundedImageView) findViewById(R.id.imageGroupRegister);
        btnSaveGroup = (Button) findViewById(R.id.btnSaveGroupRegister);

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

        encodedImage = (encodedImage.isEmpty())?"imagen":encodedImage;

        group.setTitle(txtTitle.getText().toString());
        group.setDescription(txtDescription.getText().toString());
        group.setUser_create(preferencesManager.getString(Constants.KEY_USER_ID));
        group.setImage(encodedImage);
        group.setStatus(Group.STATUS_ACTIVE);

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        HashMap<String, String> params = new HashMap<>();
        params.put("idFirebase", ResourceUtil.createCodeRandom(6));
        params.put("title", group.getTitle());
        params.put("description", group.getDescription());
        params.put("image", group.getImage());
        params.put("status", group.getStatus());
        params.put("user_id_created", group.getUser_create());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, GroupApiMethods.POST_GROUP, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                pDialog.dismiss();

                try {
                    String resposeData = response.getString("data");


                    if(!resposeData.equals("[]")){

                        JSONObject jsonObject = response.getJSONObject("data");

                        saveGroupUserAdmin(jsonObject.getString("id"));

                    }else {
                        ResourceUtil.showAlert("Advertencia", "Se produjo un error al registrar el grupo.",CreateGroupActivity.this, "error");
                    }

                } catch (JSONException e) {
//                    ResourceUtil.showAlert("Advertencia", "Se produjo un error al registrar el grupo.",CreateGroupActivity.this, "error");
                }


//                Toast.makeText(CreateGroupActivity.this, response.toString(), Toast.LENGTH_SHORT).show();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                ResourceUtil.showAlert("Advertencia", "Se produjo un error al registrar el grupo.",CreateGroupActivity.this, "error");
                Log.d("ERROR_USER", "Error Register: "+error.getMessage());

//                Toast.makeText(CreateGroupActivity.this, "Error: " +error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);


    }

    private void saveGroupUserAdmin(String idGroup) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", preferencesManager.getString(Constants.KEY_USER_ID));
        params.put("group_id", idGroup);
        params.put("status", GroupUser.STATUS_ACCEPT);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, GroupApiMethods.POST_GROUP_USER, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                pDialog.dismiss();

                try {
                    String resposeData = response.getString("data");


                    if(!resposeData.equals("[]")){


                        ResourceUtil.showAlert("Mensaje", "Grupo guardado correctamente.",CreateGroupActivity.this, "success");

                        cleanComponets();

                    }else {
                        ResourceUtil.showAlert("Advertencia", "Se produjo un error al registrar el grupo.",CreateGroupActivity.this, "error");
                    }

                } catch (JSONException e) {
//                    ResourceUtil.showAlert("Advertencia", "Se produjo un error al registrar el grupo.",CreateGroupActivity.this, "error");
                }


//                Toast.makeText(CreateGroupActivity.this, response.toString(), Toast.LENGTH_SHORT).show();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                ResourceUtil.showAlert("Advertencia", "Se produjo un error al registrar el grupo.",CreateGroupActivity.this, "error");
                Log.d("ERROR_USER", "Error Register: "+error.getMessage());

//                Toast.makeText(CreateGroupActivity.this, "Error: " +error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);

    }

    private void cleanComponets() {

        txtTitle.setText(null);
        txtDescription.setText(null);
        txtAddImage.setVisibility(View.VISIBLE);

        roundedImageView.setImageBitmap(null);

//        roundedImageView.setBackgroundResource(R.drawable.background_image);

        encodedImage = "";

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
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);
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

}