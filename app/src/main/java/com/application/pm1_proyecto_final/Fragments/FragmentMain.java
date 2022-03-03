package com.application.pm1_proyecto_final.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.activities.CreateGroupActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FragmentMain extends Fragment {



    FloatingActionButton fboton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main,container,false);




        init(view);

//        setListeners();

        return view;
    }



    private void init(View view){
//        fboton = (FloatingActionButton) view.findViewById(R.id.btnadd);
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