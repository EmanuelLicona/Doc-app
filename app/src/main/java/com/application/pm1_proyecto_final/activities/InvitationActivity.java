package com.application.pm1_proyecto_final.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.adapters.GroupAdapter;
import com.application.pm1_proyecto_final.adapters.InvitationAdapter;
import com.application.pm1_proyecto_final.models.Group;
import com.application.pm1_proyecto_final.models.GroupUser;
import com.application.pm1_proyecto_final.providers.GroupUserProvider;
import com.application.pm1_proyecto_final.providers.GroupsProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class InvitationActivity extends AppCompatActivity {

    ListView listView;



    PreferencesManager preferencesManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);

        init();


        getInvitationsUser();
    }


    private void init(){

        preferencesManager = new PreferencesManager(getApplicationContext());

        listView = (ListView) findViewById(R.id.listViewInvitation);

    }

    private void setListeners(){

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

                           InvitationAdapter adapter = new InvitationAdapter(getApplicationContext(), groups);
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

}