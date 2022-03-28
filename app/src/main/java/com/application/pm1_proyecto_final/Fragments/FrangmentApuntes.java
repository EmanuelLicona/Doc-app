package com.application.pm1_proyecto_final.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.activities.CreateGroupActivity;
import com.application.pm1_proyecto_final.activities.NotesActivity;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class FrangmentApuntes extends Fragment {

    FloatingActionButton buttonaddNote;
    PreferencesManager preferencesManager;
    RecyclerView recyclerView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view = inflater.inflate(R.layout.fragment_apuntes, container, false);

        preferencesManager = new PreferencesManager(getContext());

        buttonaddNote = (FloatingActionButton) view.findViewById(R.id.btnaddNote);

        recyclerView = (RecyclerView) view.findViewById(R.id.groupsRecyclerView);

        buttonaddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), NotesActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}