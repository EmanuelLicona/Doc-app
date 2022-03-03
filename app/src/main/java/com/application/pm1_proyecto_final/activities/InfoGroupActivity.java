package com.application.pm1_proyecto_final.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.TextView;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.models.Group;
import com.application.pm1_proyecto_final.providers.GroupsProvider;
import com.makeramen.roundedimageview.RoundedImageView;

public class InfoGroupActivity extends AppCompatActivity {

    Group reseiverGroup;

    TextView textViewDescription, textViewTitle, textViewImage;

    AppCompatImageView imageViewBack;

    RoundedImageView roundedImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_group);


        init();
        setListeners();
        loadReceiverDetails();
    }

    private void init(){
        reseiverGroup = null;

        textViewTitle = (TextView) findViewById(R.id.textTitleGroupInfo);
        textViewDescription = (TextView) findViewById(R.id.textViewDescInfo);
        textViewImage = (TextView) findViewById(R.id.textImageInfo);
        imageViewBack = (AppCompatImageView) findViewById(R.id.btnInfoGroupBack);

        roundedImageView = (RoundedImageView) findViewById(R.id.imageGroupInfo);
    }

    private void setListeners(){
        imageViewBack.setOnClickListener(v->onBackPressed());
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

    private static Bitmap getGroupImage(String encodedImage){

        byte[] bytes = android.util.Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}