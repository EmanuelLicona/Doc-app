package com.application.pm1_proyecto_final.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.application.pm1_proyecto_final.Fragments.FrangmentApuntes;
import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.api.NoteApiMethods;
import com.application.pm1_proyecto_final.api.UserApiMethods;
import com.application.pm1_proyecto_final.models.Note;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.google.android.gms.actions.ItemListIntents;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class EditActivityNote extends AppCompatActivity {
    AppCompatImageView imageViewBack;
    EditText txtTitle,txtDescription;
    Button btnEditNote,btnDeleteNote;
    String title="",description="";
    private PreferencesManager preferencesManager;
    SweetAlertDialog pDialog;
    Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        imageViewBack = (AppCompatImageView) findViewById(R.id.btnNoteBackEdit);
        txtTitle = (EditText) findViewById(R.id.txtTitleNoteEdit);
        txtDescription = (EditText) findViewById(R.id.txtDescriptionNoteEdit);

        btnEditNote = (Button) findViewById(R.id.btnEditNote);
        btnDeleteNote = (Button) findViewById(R.id.btnDeleteNote);

        preferencesManager = new PreferencesManager(getApplicationContext());




        pDialog = ResourceUtil.showAlertLoading(this);
        setData();
        setListener();
    }
    private void setListener(){
        imageViewBack.setOnClickListener(v -> onBackPressed());

        btnEditNote.setOnClickListener(v -> {
            String response = validNote();
            if (response.equals("OK")) {
                pDialog.show();
                prepareUpdateNote();
            }else {
                ResourceUtil.showAlert("Advertencia", response, this, "error");
            }
        });

        btnDeleteNote.setOnClickListener( view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(EditActivityNote.this);

            builder.setMessage("¿Seguro que desea eliminar la nota?").setTitle("Alerta");

            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    deleteNote();
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {}
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });


    }

    private void prepareUpdateNote(){

        note.setTitle(title);
        note.setDescription(description);
        //note.setId(preferencesManager.getString(Constants.KEY_USER_ID));
        updateNote(note);
    }

    private void updateNote(Note note) {
        if (pDialog.isShowing()) {
            pDialog.show();
        }
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        HashMap<String, String> params = new HashMap<>();
        params.put("id", note.getId());
        params.put("title", note.getTitle());
        params.put("content", note.getDescription());
        params.put("status", note.getStatus());
        params.put("user_id", note.getUser_create());
        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(Request.Method.PUT, NoteApiMethods.PUT_NOTE+note.getId(), new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                ResourceUtil.showAlert("Advertencia", "Se produjo un error al actualizar la nota.",EditActivityNote.this, "error");
                //Log.d("ERROR_NOTE", "Error Update: "+error.getMessage());
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private void deleteNote() {
        if (pDialog.isShowing()) {
            pDialog.show();
        }
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(Request.Method.DELETE, NoteApiMethods.DELETE_NOTE+note.getId(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                ResourceUtil.showAlert("Advertencia", "Se produjo un error al eliminar la nota.",EditActivityNote.this, "error");
                //Log.d("ERROR_NOTE", "Error Delete: "+error.getMessage());
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private void setData(){
        Bundle objeto = getIntent().getExtras();
        note =new Note();
        note = (Note) objeto.getSerializable("noteEdit");
        txtTitle.setText(note.getTitle());
        txtDescription.setText(note.getDescription());

        Toast.makeText(this, "ES "+note.getId(), Toast.LENGTH_SHORT).show();
    }

    private String validNote(){
        String response = "";
        title = txtTitle.getText().toString();
        description = txtDescription.getText().toString();
        if (title.isEmpty()){
            response  = "Por favor, ingresa el titulo.";
        }else if (description.isEmpty()){
            response  = "Por favor, ingresa una descripción.";
        }else{
            response  = "OK";
        }
        return response;
    }
}