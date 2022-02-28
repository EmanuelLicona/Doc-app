package com.application.pm1_proyecto_final.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.application.pm1_proyecto_final.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FragmentMain extends Fragment {



    FloatingActionButton fboton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main,container,false);
        fboton = view.findViewById(R.id.btnadd);
        fboton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),"Crear nueva publicacion", Toast.LENGTH_LONG).show();
            }
        });
        return view;
    }
}