package com.application.pm1_proyecto_final.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.activities.NotesActivity;
import com.application.pm1_proyecto_final.adapters.NoteAdapter;
import com.application.pm1_proyecto_final.api.NoteApiMethods;
import com.application.pm1_proyecto_final.listeners.Notelistener;
import com.application.pm1_proyecto_final.models.Note;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class FrangmentApuntes extends Fragment implements View.OnClickListener, Notelistener {

    FloatingActionButton buttonaddNote;
    PreferencesManager preferencesManager;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    NoteAdapter noteAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view = inflater.inflate(R.layout.fragment_apuntes, container, false);

        preferencesManager = new PreferencesManager(getContext());

        buttonaddNote = (FloatingActionButton) view.findViewById(R.id.btnaddNote);

        recyclerView = (RecyclerView) view.findViewById(R.id.myNotesRecyclerView);

        //progressBar = (ProgressBar) view.findViewById(R.id.myNotesProgressBar);

        buttonaddNote.setOnClickListener(this);
        getMyNotes();
        return view;
    }

    private void getMyNotes(){
        //loading(true);

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                (NoteApiMethods.GET_NOTE_USER_CREATE+preferencesManager.getString(Constants.KEY_USER_ID)),
                null,
                new com.android.volley.Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {


                        try {

                            JSONObject  jsonObject = null;

                            Note noteTemp = null;

                            List<Note> notes = new ArrayList<>();


                            if(response.getString("res").equals("true")){
//                                t = response.getJSONObject("data").getString("name");


                                JSONArray array = response.getJSONObject("data").getJSONArray("notas_creates");
                                for (int i = 0; i < array.length(); i++) {
                                    jsonObject = new JSONObject(array.get(i).toString());


                                    noteTemp = new Note();
                                    noteTemp.setId(jsonObject.getString("id"));
                                    noteTemp.setTitle(jsonObject.getString("title"));
                                    noteTemp.setDescription(jsonObject.getString("content"));
                                    noteTemp.setStatus(jsonObject.getString("status"));
                                    noteTemp.setUser_create(jsonObject.getString("user_id"));

                                    notes.add(noteTemp);

                                }

                                //loading(false);

                                if(notes.size() > 0){

                                    noteAdapter = new NoteAdapter(notes, FrangmentApuntes.this);
                                    recyclerView.setAdapter(noteAdapter);

                                }else{
                                    Toast.makeText(getContext(), "Advertencia: No se encuentran datos", Toast.LENGTH_SHORT).show();
                                }

                            }else{
                                Toast.makeText(getContext(), "Error: "+response.getString("msg"), Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            Toast.makeText(getContext(), "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }


        );

        requestQueue.add(request);

    }

    private void loading(boolean isLoading) {

        if(isLoading){
            progressBar.setVisibility(View.VISIBLE);
        }else{
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnaddNote:
                Intent intent = new Intent(getContext(), NotesActivity.class);
                startActivity(intent);
                break;
        }

    }

    public void onResume() {
        super.onResume();

        getMyNotes();
    }

    @Override
    public void onClickNote(Note note) {

    }
}