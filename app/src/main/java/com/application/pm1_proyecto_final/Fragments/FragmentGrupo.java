package com.application.pm1_proyecto_final.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.activities.ChatActivity;
import com.application.pm1_proyecto_final.activities.CreateGroupActivity;
import com.application.pm1_proyecto_final.activities.MyGroupsActivity;
import com.application.pm1_proyecto_final.adapters.GroupAdapter;
import com.application.pm1_proyecto_final.api.GroupApiMethods;
import com.application.pm1_proyecto_final.listeners.Grouplistener;
import com.application.pm1_proyecto_final.models.Group;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.providers.GroupsProvider;
import com.application.pm1_proyecto_final.providers.UsersProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class FragmentGrupo extends Fragment implements Grouplistener {

    FloatingActionButton fboton;

    PreferencesManager preferencesManager;

    RecyclerView recyclerView;

    GroupAdapter groupAdapter;

    EditText editTextSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_grupo, container, false);

//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        init(view);

        setListeners();

        getMyGroups();

        return view;
    }


    private void init(View view){

        preferencesManager = new PreferencesManager(getContext());

        fboton = (FloatingActionButton) view.findViewById(R.id.btnaddGrupo);

        recyclerView = (RecyclerView) view.findViewById(R.id.groupsRecyclerView);

        editTextSearch = (EditText) view.findViewById(R.id.textSearchGroup);
    }

    private void setListeners(){
        fboton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), CreateGroupActivity.class);
                startActivity(intent);
            }
        });

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
//        loading(true);

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());



        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                (GroupApiMethods.GET_GROUPS_FOR_USER_ACTIVE+preferencesManager.getString(Constants.KEY_USER_ID)),
                null,
                new com.android.volley.Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {


                        try {

                            JSONObject  jsonObject = null;

                            Group groupTemp = null;

                            List<Group> groups = new ArrayList<>();


                            if(response.getString("res").equals("true")){
//                                t = response.getJSONObject("data").getString("name");


                                JSONArray array = response.getJSONArray("data");

                                for (int i = 0; i < array.length(); i++) {
                                    jsonObject = new JSONObject(array.get(i).toString());


                                    groupTemp = new Group();
                                    groupTemp.setId(jsonObject.getString("id"));
                                    groupTemp.setTitle(jsonObject.getString("title"));
                                    groupTemp.setDescription(jsonObject.getString("description"));
                                    groupTemp.setImage(jsonObject.getString("image"));
                                    groupTemp.setStatus(jsonObject.getString("group_status"));
                                    groupTemp.setUser_create(jsonObject.getString("user_id_created"));

                                    groups.add(groupTemp);

                                }

//                                loading(false);

                                if(groups.size() > 0){

                                    groupAdapter = new GroupAdapter(groups, FragmentGrupo.this);
                                    recyclerView.setAdapter(groupAdapter);
//                            recyclerView.setVisibility(View.VISIBLE);

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

    @Override
    public void onResume() {
        super.onResume();

        getMyGroups();
    }

    @Override
    public void onClickGroup(Group group) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(GroupsProvider.NAME_COLLECTION, group);
        startActivity(intent);
    }
}