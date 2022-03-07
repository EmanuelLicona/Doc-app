package com.application.pm1_proyecto_final.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.adapters.GroupAdapter;
import com.application.pm1_proyecto_final.adapters.UsersGroupAdapter;
import com.application.pm1_proyecto_final.models.Group;
import com.application.pm1_proyecto_final.models.GroupUser;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.providers.GroupUserProvider;
import com.application.pm1_proyecto_final.providers.GroupsProvider;
import com.application.pm1_proyecto_final.providers.UsersProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

public class InfoGroupActivity extends AppCompatActivity {

    Group reseiverGroup;

    TextView textViewDescription, textViewTitle, textViewImage;

    AppCompatImageView imageViewBack;

    RoundedImageView roundedImageView;

    PreferencesManager preferencesManager;

    List<User> users;

    UsersGroupAdapter usersGroupAdapter;

    RecyclerView recyclerViewUsuarios;

    Button btnAddMember;

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

        textViewTitle = (TextView) findViewById(R.id.textTitleGroupInfo);
        textViewDescription = (TextView) findViewById(R.id.textViewDescInfo);
        textViewImage = (TextView) findViewById(R.id.textImageInfo);
        imageViewBack = (AppCompatImageView) findViewById(R.id.btnInfoGroupBack);

        roundedImageView = (RoundedImageView) findViewById(R.id.imageGroupInfo);

        recyclerViewUsuarios = (RecyclerView) findViewById(R.id.usersGroupRecyclerView);

        btnAddMember = (Button) findViewById(R.id.btnAddMember);
    }

    private void setListeners(){
        imageViewBack.setOnClickListener(v->onBackPressed());

        btnAddMember.setOnClickListener(v -> dialogAddMember());
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

    private void loadUsersGroups(){

        ArrayList<User> arrayList = Group.converJsonToArrayListUsers(reseiverGroup.getJson_users());

        users = arrayList;

        UsersGroupAdapter usersGroupAdapter = new UsersGroupAdapter(users, preferencesManager);

        recyclerViewUsuarios.setAdapter(usersGroupAdapter);

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

        btnAdd.setOnClickListener(v -> addMemberForGroup(text.getText().toString()));

        if(!text.getText().toString().isEmpty()){

        }



    }

    private void addMemberForGroup(String email){

        Toast.makeText(this, reseiverGroup.getJson_users(), Toast.LENGTH_SHORT).show();
    }


    private static Bitmap getGroupImage(String encodedImage){

        byte[] bytes = android.util.Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }



}