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
import android.util.Patterns;
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
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

        btnAdd.setOnClickListener(v -> {
            if(text.getText().toString().isEmpty()){

                ResourceUtil.showAlert("Advertencia", "Por favor escribe un correo", InfoGroupActivity.this, "error");
            }else if(!Patterns.EMAIL_ADDRESS.matcher(text.getText().toString()).matches()){

                ResourceUtil.showAlert("Advertencia", "Por favor escribe un correo valido", InfoGroupActivity.this, "error");

            }else{
                searhEmail(text.getText().toString());

                dialog.dismiss();
            }
        });


    }

    private void searhEmail(String email) {

        FirebaseFirestore database=FirebaseFirestore.getInstance();

        database.collection(UsersProvider.NAME_COLLECTION)
                .whereEqualTo(UsersProvider.KEY_EMAIL, email)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null){
                       if( task.getResult().getDocuments().size() > 0){

                           DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);

                           User user = new User();

                           user.setEmail(documentSnapshot.getString(UsersProvider.KEY_EMAIL));
                           user.setId(documentSnapshot.getId());

                           sendInvitation(user);
                       }else{
                           ResourceUtil.showAlert("Advertencia", "El correo escrito no existe", InfoGroupActivity.this, "error");
                       }
                    }


                }).addOnFailureListener(error -> {
                    Toast.makeText(getApplicationContext(), "Error al enviar invitacion", Toast.LENGTH_SHORT).show();
                });


    }

    private void sendInvitation(User user) {



        FirebaseFirestore database=FirebaseFirestore.getInstance();

        database.collection(GroupUserProvider.NAME_COLLECTION)
                .whereEqualTo(GroupUserProvider.KEY_ID_GROUP, reseiverGroup.getId())
                .whereEqualTo(GroupUserProvider.KEY_ID_USER, user.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null){



                        GroupUser groupUser = new GroupUser();

                        groupUser.setIdGroup(reseiverGroup.getId());
                        groupUser.setNameGroup(reseiverGroup.getTitle());
                        groupUser.setIdUser(user.getId());
                        groupUser.setStatus(GroupUser.STATUS_INVITED);
                        groupUser.setNameGroup(reseiverGroup.getTitle());
                        groupUser.setDate(new Date());



                        if( task.getResult().getDocuments().size() > 0){

                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);

                            HashMap<String, Object> map = new HashMap<>();

                            map.put(GroupUserProvider.KEY_STATUS, GroupUser.STATUS_INVITED);
                            map.put(GroupUserProvider.KEY_TITLE, groupUser.getNameGroup());
                            map.put(GroupUserProvider.KEY_DATE, new Date());

                            database.collection(GroupUserProvider.NAME_COLLECTION).
                                    document(documentSnapshot.getId())
                                    .update(map)
                                    .addOnCompleteListener(task1 -> {

                                        if(task1.isSuccessful()){
                                            ResourceUtil.showAlert("Mensaje", "Invitacion Enviada", InfoGroupActivity.this, "success");
                                        }else{
                                            ResourceUtil.showAlert("Oops", "No se pudo enviar la invitacion", InfoGroupActivity.this, "error");
                                        }

                                    });

                        }else{
                            GroupUserProvider groupUserProvider = new GroupUserProvider();
                            groupUserProvider.create(groupUser).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if(task.isSuccessful()){
                                        ResourceUtil.showAlert("Mensaje", "Invitacion Enviada", InfoGroupActivity.this, "success");
                                    }else{
                                        ResourceUtil.showAlert("Oops", "No se pudo enviar la invitacion", InfoGroupActivity.this, "error");
                                    }
                                }
                            });
                        }
                    }


                }).addOnFailureListener(error -> {
                         Toast.makeText(getApplicationContext(), "Error al enviar invitacion", Toast.LENGTH_SHORT).show();
                  });






    }


    private static Bitmap getGroupImage(String encodedImage){

        byte[] bytes = android.util.Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }



}