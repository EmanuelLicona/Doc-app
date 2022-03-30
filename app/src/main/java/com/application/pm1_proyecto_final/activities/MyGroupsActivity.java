package com.application.pm1_proyecto_final.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.adapters.GroupAdapter;
import com.application.pm1_proyecto_final.api.GroupApiMethods;
import com.application.pm1_proyecto_final.listeners.Grouplistener;
import com.application.pm1_proyecto_final.models.Group;
import com.application.pm1_proyecto_final.providers.GroupsProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyGroupsActivity extends AppCompatActivity implements Grouplistener{

    PreferencesManager preferencesManager;

    AppCompatImageView btnBack;

    ProgressBar progressBar;

    RecyclerView recyclerView;

    GroupAdapter groupAdapter;

    EditText editTextSearch;

    TextView textViewMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups);

        init();
        getMyGroups();
        setListeners();
    }

    private void init(){
        preferencesManager = new PreferencesManager(getApplicationContext());


        btnBack = (AppCompatImageView) findViewById(R.id.btnMyGroupBack);
        progressBar = (ProgressBar) findViewById(R.id.groupsProgressBar);

        recyclerView = (RecyclerView) findViewById(R.id.myGroupsRecyclerView);

        editTextSearch = (EditText) findViewById(R.id.textSearchMyGroup);

        textViewMessage = (TextView) findViewById(R.id.textMessageGroups);
     }

    private void setListeners(){
        btnBack.setOnClickListener(v -> onBackPressed());

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
        loading(true);

        RequestQueue requestQueue = Volley.newRequestQueue(this);



        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                (GroupApiMethods.GET_GROUP_USER_CREATE+preferencesManager.getString(Constants.KEY_USER_ID)),
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


                                JSONArray array = response.getJSONObject("data").getJSONArray("groups_creates");

                                for (int i = 0; i < array.length(); i++) {
                                     jsonObject = new JSONObject(array.get(i).toString());


                                     groupTemp = new Group();
                                     groupTemp.setId(jsonObject.getString("id"));
                                     groupTemp.setTitle(jsonObject.getString("title"));
                                     groupTemp.setDescription(jsonObject.getString("description"));
                                     groupTemp.setImage(jsonObject.getString("image"));
                                     groupTemp.setStatus(jsonObject.getString("status"));
                                     groupTemp.setUser_create(jsonObject.getString("user_id_created"));

                                     groups.add(groupTemp);

                                }

                                loading(false);

                                if(groups.size() == 0){
                                    textViewMessage.setVisibility(View.VISIBLE);

                                }else{
                                    textViewMessage.setVisibility(View.GONE);
                                }

                                groupAdapter = new GroupAdapter(groups, MyGroupsActivity.this);
                                recyclerView.setAdapter(groupAdapter);

                            }else{
                                Toast.makeText(getApplicationContext(), "Error: "+response.getString("msg"), Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
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
    public void onClickGroup(Group group) {
        Intent intent = new Intent(getApplicationContext(), PublicationActivity.class);
        intent.putExtra(GroupsProvider.NAME_COLLECTION, group);
        startActivity(intent);
        finish();
    }
}