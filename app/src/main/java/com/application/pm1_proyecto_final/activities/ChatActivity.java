package com.application.pm1_proyecto_final.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.models.Group;
import com.application.pm1_proyecto_final.providers.GroupsProvider;

public class ChatActivity extends AppCompatActivity {

    Group reseiverGroup;

    AppCompatImageView imageViewInfo, imageViewBack;

    TextView textViewTitle;

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

        imageViewInfo = (AppCompatImageView) findViewById(R.id.imageInfoChat);
        imageViewBack = (AppCompatImageView) findViewById(R.id.btnChatBack);
        textViewTitle = (TextView) findViewById(R.id.groupTitleChat);
    }

    private void setListeners(){
        imageViewBack.setOnClickListener(v -> onBackPressed());
        imageViewInfo.setOnClickListener(view -> moveToInfo());
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
}