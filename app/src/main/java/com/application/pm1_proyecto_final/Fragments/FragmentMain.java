package com.application.pm1_proyecto_final.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.activities.CreateGroupActivity;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FragmentMain extends Fragment {

    TextView textView;


    FloatingActionButton fboton;

    PreferencesManager preferencesManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container,false);

        init(view);
//        setListeners();
        return view;
    }



    private void init(View view){

        preferencesManager = new PreferencesManager(getContext());

        textView = view.findViewById(R.id.textMain);



        String textMain = "Hola " + preferencesManager.getString(Constants.KEY_NAME_USER) +
                ", en esta plataforma usted podra realizar tareas exclusivas para nuestros estudiantes" +
                " con el fin de facilitar la interaccion entre los mismos, desarrollando un sentimiento de " +
                " pertenencia y compañerismo."
                ;


        textView.setText(textMain);

    }

    private void setListeners(){
        fboton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getContext(), CreateGroupActivity.class);
                startActivity(intent);
            }
        });
    }
}