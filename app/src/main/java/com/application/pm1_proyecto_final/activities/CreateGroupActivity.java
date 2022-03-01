package com.application.pm1_proyecto_final.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.google.android.material.textfield.TextInputEditText;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class CreateGroupActivity extends AppCompatActivity {

    AppCompatImageView imageViewBack;
    TextView txtAddImage;
    TextInputEditText txtTitle, txtDescription;
    Button btnSaveGroup;
    String encodedImage;
    RoundedImageView roundedImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);


        init();
        setListeners();

    }


    private void init(){
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

            }
        });
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK){

                    System.out.println(result.getResultCode() + "");

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
}