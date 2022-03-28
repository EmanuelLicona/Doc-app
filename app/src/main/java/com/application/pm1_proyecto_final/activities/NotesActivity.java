package com.application.pm1_proyecto_final.activities;

import static java.security.AccessController.getContext;

import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.adapters.GroupAdapter;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NotesActivity extends AppCompatActivity {
    AppCompatImageView imageViewBack;
    EditText txtTitle,txtDescription;
    Button btnSaveNote;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        imageViewBack = (AppCompatImageView) findViewById(R.id.btnNoteBack);
        txtTitle = (EditText) findViewById(R.id.txtTitleNote);
        txtDescription = (EditText) findViewById(R.id.txtDescriptionNote);

        btnSaveNote = (Button) findViewById(R.id.btnSaveNote);

        setListener();

    }

    private void setListener(){
        imageViewBack.setOnClickListener(v -> onBackPressed());

        btnSaveNote.setOnClickListener(v -> {
            if(validNote()){

                saveNote();

            }
        });
    }


    private void saveNote(){

    }


    private boolean validNote(){

        if (txtTitle.getText().toString().trim().isEmpty()){
            ResourceUtil.showAlert("Advertencia", "Por favor escriba un titulo", NotesActivity.this, "error");
            return false;
        }else if (txtDescription.getText().toString().trim().isEmpty()){
            ResourceUtil.showAlert("Advertencia", "Por favor escriba una descripcion", NotesActivity.this, "error");
            return false;
        }else{
            return true;
        }

    }
}