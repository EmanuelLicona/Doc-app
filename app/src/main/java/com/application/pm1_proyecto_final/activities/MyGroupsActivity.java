package com.application.pm1_proyecto_final.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.adapters.GroupAdapter;
import com.application.pm1_proyecto_final.listeners.Grouplistener;
import com.application.pm1_proyecto_final.models.Group;
import com.application.pm1_proyecto_final.providers.GroupsProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyGroupsActivity extends AppCompatActivity implements Grouplistener{

    PreferencesManager preferencesManager;

    AppCompatImageView btnBack;

    ProgressBar progressBar;

    RecyclerView recyclerView;

    GroupAdapter groupAdapter;

    EditText editTextSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups);


        init();
        getMyGroups();
        setListeners();
    }

    private void init(){
        preferencesManager = new PreferencesManager(getApplicationContext());


        btnBack = (AppCompatImageView) findViewById(R.id.btnMyGroupBack);
        progressBar = (ProgressBar) findViewById(R.id.myGroupsProgressBar);

        recyclerView = (RecyclerView) findViewById(R.id.myGroupsRecyclerView);

        editTextSearch = (EditText) findViewById(R.id.textSearchMyGroup);
     }

    private void setListeners(){
        btnBack.setOnClickListener(v -> onBackPressed());

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                groupAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void getMyGroups(){
        loading(true);

        FirebaseFirestore database = FirebaseFirestore.getInstance();


        database.collection(GroupsProvider.NAME_COLLECTION)
                .whereEqualTo(GroupsProvider.KEY_USER_CREATE, preferencesManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful() && task.getResult()!=null){

                        List<Group> groups = new ArrayList<>();
//                        Group grouptemp = null;
                        for(QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){

                            Group grouptemp = new Group();

                            grouptemp.setId(queryDocumentSnapshot.getId());
                            grouptemp.setTitle(queryDocumentSnapshot.getString(GroupsProvider.KEY_TITLE));
                            grouptemp.setDescription(queryDocumentSnapshot.getString(GroupsProvider.KEY_DESCRIPTION));
                            grouptemp.setImage(queryDocumentSnapshot.getString(GroupsProvider.KEY_IMAGE));
                            grouptemp.setUser_create(queryDocumentSnapshot.getString(GroupsProvider.KEY_USER_CREATE));

                            groups.add(grouptemp);
                        }

                        loading(false);

                        if(groups.size() > 0){

                            groupAdapter = new GroupAdapter(groups, this);
                           recyclerView.setAdapter(groupAdapter);
//                            recyclerView.setVisibility(View.VISIBLE);

                        }else{
                            Toast.makeText(getApplicationContext(), "Advertencia: No se encuentran datos", Toast.LENGTH_SHORT).show();
                        }

                    }else{
                        loading(false);
                        Toast.makeText(getApplicationContext(), "Error: No se pudieron obtener los datos", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(error ->{
                        Toast.makeText(getApplicationContext(), "Error: "+error.toString(), Toast.LENGTH_SHORT).show();
                    });

    }

    private void loading(boolean isLoading) {

        if(isLoading){
            progressBar.setVisibility(View.VISIBLE);
        }else{
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClickGroup(Group group) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(GroupsProvider.NAME_COLLECTION, group);
        startActivity(intent);
        finish();
    }
}