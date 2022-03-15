package com.application.pm1_proyecto_final.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.activities.CreateGroupActivity;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.providers.UsersProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class FragmentGrupo extends Fragment {

    FloatingActionButton fboton;

    PreferencesManager preferencesManager;

    User userLog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_grupo, container, false);

//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        init(view);

        setListeners();



        return view;
    }


    private void init(View view){

        preferencesManager = new PreferencesManager(getContext());

        fboton = (FloatingActionButton) view.findViewById(R.id.btnaddGrupo);
    }

    private void setListeners(){
        fboton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                getUserLog();


            }
        });
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

                        Intent intent = new Intent(getContext(), CreateGroupActivity.class);
                        intent.putExtra(Constants.KEY_USER, userLog);
                        startActivity(intent);
                    }


                }).addOnFailureListener(error -> {
            Toast.makeText(getContext(), "NO se pudo recuperar el usuario", Toast.LENGTH_SHORT).show();
        });
    }
}