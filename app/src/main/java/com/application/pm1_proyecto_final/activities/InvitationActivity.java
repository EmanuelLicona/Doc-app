package com.application.pm1_proyecto_final.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.adapters.GroupAdapter;
import com.application.pm1_proyecto_final.adapters.InvitationAdapter;
import com.application.pm1_proyecto_final.adapters.UsersGroupAdapter;
import com.application.pm1_proyecto_final.listeners.Invitationlistener;
import com.application.pm1_proyecto_final.models.Group;
import com.application.pm1_proyecto_final.models.GroupUser;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.providers.GroupUserProvider;
import com.application.pm1_proyecto_final.providers.GroupsProvider;
import com.application.pm1_proyecto_final.providers.UsersProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class InvitationActivity extends AppCompatActivity implements Invitationlistener {

    ListView listView;

    PreferencesManager preferencesManager;

    AppCompatImageView btnBack;

    User userLog;

    ArrayList<Group> listGroups;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);

        init();
        getInvitationsUser();


        setListeners();
    }


    private void init(){

        preferencesManager = new PreferencesManager(getApplicationContext());

        listView = (ListView) findViewById(R.id.listViewInvitation);

        btnBack = (AppCompatImageView) findViewById(R.id.btnInvitationBack);

    }

    private void setListeners(){
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void getInvitationsUser(){

        FirebaseFirestore database = FirebaseFirestore.getInstance();


        database.collection(GroupUserProvider.NAME_COLLECTION)
                .whereEqualTo(GroupUserProvider.KEY_ID_USER, preferencesManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(GroupsProvider.KEY_STATUS, GroupUser.STATUS_INVITED)
                .get()
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful() && task.getResult()!=null){

                        ArrayList<GroupUser> groups = new ArrayList<>();

//                        Group grouptemp = null;
                        for(QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){

                            GroupUser grouptemp = new GroupUser();

                            grouptemp.setId(queryDocumentSnapshot.getId());
                            grouptemp.setNameGroup(queryDocumentSnapshot.getString(GroupUserProvider.KEY_TITLE));
                            grouptemp.setDate(queryDocumentSnapshot.getDate(GroupUserProvider.KEY_DATE));


                            groups.add(grouptemp);
                        }



//                        loading(false);

                        if(groups.size() > 0){

                           InvitationAdapter adapter = new InvitationAdapter(getApplicationContext(), groups, this);
                            listView.setAdapter(adapter);

                        }else{
                            Toast.makeText(getApplicationContext(), "Advertencia: No se encuentran datos", Toast.LENGTH_SHORT).show();
                        }

                    }else{
//                        loading(false);
                        Toast.makeText(getApplicationContext(), "Error: No se pudieron obtener los datos", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(error ->{
            Toast.makeText(getApplicationContext(), "Error: "+error.toString(), Toast.LENGTH_SHORT).show();
        });

    }

    private void dialogInvitation(GroupUser groupUser){

        AlertDialog.Builder builder = new AlertDialog.Builder(InvitationActivity.this);

        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_invitation_group, null);

        builder.setView(view);

        AlertDialog dialog = builder.create();

        dialog.show();

        TextView text =(TextView) view.findViewById(R.id.txtTitleGroupDialogInvitation);

        text.setText(groupUser.getNameGroup());

        Button btnAdd = (Button) view.findViewById(R.id.btnInvitationAgree);
        Button btnNoAdd = (Button) view.findViewById(R.id.btnInvitationNoAgree);


        btnAdd.setOnClickListener(v -> {
            AgreeInvitation(groupUser, dialog);
        });


        btnNoAdd.setOnClickListener(v -> {
            NoAgreeInvitation(groupUser, dialog);
        });



    }

    private void NoAgreeInvitation(GroupUser groupUser, AlertDialog dialog) {

        FirebaseFirestore database = FirebaseFirestore.getInstance();

        HashMap<String, Object> map = new HashMap<>();

        map.put(GroupUserProvider.KEY_STATUS, GroupUser.STATUS_NO_ACCEPT);
        map.put(GroupUserProvider.KEY_TITLE, groupUser.getNameGroup());
        map.put(GroupUserProvider.KEY_DATE, new Date());

        database.collection(GroupUserProvider.NAME_COLLECTION).
                document(groupUser.getId())
                .update(map)
                .addOnCompleteListener(task1 -> {

                    if(task1.isSuccessful()){

                        updateUser(database);

//                        ResourceUtil.showAlert("Mensaje", "Invitacion rechazada", InvitationActivity.this, "success");
                    }else{
                        ResourceUtil.showAlert("Oops", "A ocurrido un error", InvitationActivity.this, "error");
                    }

                });




        dialog.dismiss();
    }

    private void updateUser(FirebaseFirestore database) {

//        HashMap<String, Object> map = new HashMap<>();
//
//        map.put(UsersProvider.KEY_JSON, GroupUser.STATUS_NO_ACCEPT);
//
//        database.collection(GroupUserProvider.NAME_COLLECTION).
//                document(groupUser.getId())
//                .update(map)
//                .addOnCompleteListener(task1 -> {
//
//                    if(task1.isSuccessful()){
//
//                        updateUser(database);
//
////                        ResourceUtil.showAlert("Mensaje", "Invitacion rechazada", InvitationActivity.this, "success");
//                    }else{
//                        ResourceUtil.showAlert("Oops", "A ocurrido un error", InvitationActivity.this, "error");
//                    }
//
//                });



    }

    private void AgreeInvitation(GroupUser groupUser, AlertDialog dialog) {


        FirebaseFirestore database = FirebaseFirestore.getInstance();

        HashMap<String, Object> map = new HashMap<>();

        map.put(GroupUserProvider.KEY_STATUS, GroupUser.STATUS_ACCEPT);
        map.put(GroupUserProvider.KEY_TITLE, groupUser.getNameGroup());
        map.put(GroupUserProvider.KEY_DATE, new Date());



        database.collection(GroupUserProvider.NAME_COLLECTION).
                document(groupUser.getId())
                .update(map)
                .addOnCompleteListener(task1 -> {

                    if(task1.isSuccessful()){
                        getInvitationsUser();
                        ResourceUtil.showAlert("Mensaje", "Invitacion rechazada", InvitationActivity.this, "success");
                    }else{
                        ResourceUtil.showAlert("Oops", "A ocurrido un error", InvitationActivity.this, "error");
                    }

                });




        dialog.dismiss();
    }

    private void getUserLog(){

        FirebaseFirestore database = FirebaseFirestore.getInstance();

        database.collection(UsersProvider.NAME_COLLECTION)
                .whereEqualTo(UsersProvider.KEY_EMAIL, preferencesManager.getString(UsersProvider.KEY_EMAIL))
                .get()
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        userLog = new User();

                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);

                        userLog.setId(documentSnapshot.getId());

                        userLog.setEmail(documentSnapshot.getString(UsersProvider.KEY_EMAIL));
                        userLog.setName(documentSnapshot.getString(UsersProvider.KEY_NAME));
                        userLog.setLastname(documentSnapshot.getString(UsersProvider.KEY_LASTNAME));
                        userLog.setImage(documentSnapshot.getString(UsersProvider.KEY_IMAGE));

                        userLog.setJson_groups(documentSnapshot.getString(UsersProvider.KEY_JSON));

                    }


                }).addOnFailureListener(error -> {
            Toast.makeText(this, "NO se pudo recuperar el usuario", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUsersGroups(){

        ArrayList<Group> arrayList = User.converJsonToArrayListGroups(userLog.getJson_groups());

        listGroups = arrayList;

    }

    @Override
    public void OnClickInvitation(GroupUser groupUser) {
        dialogInvitation(groupUser);
    }
}