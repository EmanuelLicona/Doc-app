package com.application.pm1_proyecto_final.activities;

import static java.security.AccessController.getContext;

import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

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
import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.adapters.GroupAdapter;
import com.application.pm1_proyecto_final.api.GroupApiMethods;
import com.application.pm1_proyecto_final.api.NoteApiMethods;
import com.application.pm1_proyecto_final.models.Group;
import com.application.pm1_proyecto_final.models.Note;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class NotesActivity extends AppCompatActivity {
    AppCompatImageView imageViewBack;
    EditText txtTitle,txtDescription;
    Button btnSaveNote;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        imageViewBack = (AppCompatImageView) findViewById(R.id.btnNoteBack);
        txtTitle = (EditText) findViewById(R.id.txtTitleNote);
        txtDescription = (EditText) findViewById(R.id.txtDescriptionNote);

        btnSaveNote = (Button) findViewById(R.id.btnSaveNote);

        preferencesManager = new PreferencesManager(getApplicationContext());


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
            //pDialog.show();

            Note note = new Note();

            note.setTitle(txtTitle.getText().toString());
            note.setDescription(txtDescription.getText().toString());
            note.setUser_create(preferencesManager.getString(Constants.KEY_USER_ID));
            note.setStatus(note.STATUS_ACTIVE);

            RequestQueue requestQueue = Volley.newRequestQueue(this);

            HashMap<String, String> params = new HashMap<>();
            params.put("title", note.getTitle());
            params.put("content", note.getDescription());
            params.put("status", note.getStatus());
            params.put("user_id", note.getUser_create());

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, NoteApiMethods.POST_NOTE, new JSONObject(params), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    //pDialog.dismiss();

                    try {
                        String resposeData = response.getString("data");


                        if(!resposeData.equals("[]")){

                            ResourceUtil.showAlert("Mensaje", "Nota registrada.",NotesActivity.this, "success");
                            cleanInputs();
                        }else {
                            ResourceUtil.showAlert("Advertencia", "Se produjo un error al registrar la nota.",NotesActivity.this, "error");
                        }

                    } catch (JSONException e) {
                          ResourceUtil.showAlert("Advertencia", "Se produjo un error al registrar la nota.",NotesActivity.this, "error");
                    }
                       //Toast.makeText(NotesActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //pDialog.dismiss();
                    ResourceUtil.showAlert("Advertencia", "Se produjo un error al registrar la nota.",NotesActivity.this, "error");
                    Log.d("ERROR_USER", "Error Register: "+error.getMessage());

//                Toast.makeText(CreateGroupActivity.this, "Error: " +error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            requestQueue.add(jsonObjectRequest);

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

    private void cleanInputs(){
        txtTitle.setText("");
        txtDescription.setText("");
    }
}