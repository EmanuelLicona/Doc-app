package com.application.pm1_proyecto_final.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.adapters.GroupAdapter;
import com.application.pm1_proyecto_final.adapters.InvitationAdapter;
import com.application.pm1_proyecto_final.adapters.UsersGroupAdapter;
import com.application.pm1_proyecto_final.api.GroupApiMethods;
import com.application.pm1_proyecto_final.listeners.Invitationlistener;
import com.application.pm1_proyecto_final.models.Group;
import com.application.pm1_proyecto_final.models.GroupUser;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.providers.GroupUserProvider;
import com.application.pm1_proyecto_final.providers.GroupsProvider;
import com.application.pm1_proyecto_final.providers.UsersProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class InvitationActivity extends AppCompatActivity implements Invitationlistener {

    ListView listView;

    PreferencesManager preferencesManager;

    AppCompatImageView btnBack;

    ArrayList<GroupUser> listUsersGroupsTemp;

    ProgressBar progressBar;

    TextView textViewMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);

        init();
        getInvitationsUser();


        setListeners();
    }


    private void init(){

        preferencesManager = new PreferencesManager(getApplicationContext());

        listView = (ListView) findViewById(R.id.listViewInvitation);

        btnBack = (AppCompatImageView) findViewById(R.id.btnInvitationBack);


        progressBar = (ProgressBar) findViewById(R.id.invitationProgressBar);

        textViewMessage = (TextView) findViewById(R.id.textMessageInvitation);



    }

    private void setListeners(){
        btnBack.setOnClickListener(v -> onBackPressed());
    }


    private void getInvitationsUser(){

        loading(true);

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                (GroupApiMethods.GET_USER_INVITATION+preferencesManager.getString(Constants.KEY_USER_ID)),
                null,
                new com.android.volley.Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {


                        try {

                            JSONObject  jsonObject = null;

                            GroupUser groupTemp = null;

                           listUsersGroupsTemp = new ArrayList<>();


                            if(response.getString("res").equals("true")){
//                                t = response.getJSONObject("data").getString("name");


                                JSONArray array = response.getJSONArray("data");

                                for (int i = 0; i < array.length(); i++) {
                                    jsonObject = new JSONObject(array.get(i).toString());


                                    groupTemp = new GroupUser();

//                                    groupTemp.setId(jsonObject.getString("id"));
                                    groupTemp.setNameGroup(jsonObject.getString("title"));
                                    groupTemp.setIdGroup(jsonObject.getString("id"));
                                    groupTemp.setIdUser(preferencesManager.getString(Constants.KEY_USER_ID));
                                    groupTemp.setStatus(jsonObject.getString("status_user_group"));
                                    groupTemp.setDescriptionGroup(jsonObject.getString("description"));
                                    groupTemp.setImage(jsonObject.getString("image"));


                                    listUsersGroupsTemp.add(groupTemp);

                                }

                                if(listUsersGroupsTemp.size() == 0){
                                    textViewMessage.setVisibility(View.VISIBLE);
                                }else{
                                    textViewMessage.setVisibility(View.GONE);
                                }

                                    InvitationAdapter adapterInvitation = new InvitationAdapter(getApplicationContext(), listUsersGroupsTemp, InvitationActivity.this);

                                    listView.setAdapter(adapterInvitation);

                                    loading(false);

//                                }else{
//                                    Toast.makeText(getApplicationContext(), "Advertencia: No se encuentran datos", Toast.LENGTH_SHORT).show();
//                                }

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

    private void dialogInvitation(GroupUser groupUser){

        AlertDialog.Builder builder = new AlertDialog.Builder(InvitationActivity.this);

        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_invitation_group, null);

        builder.setView(view);

        AlertDialog dialog = builder.create();

        dialog.show();

        TextView text =(TextView) view.findViewById(R.id.txtTitleGroupDialogInvitation);

        text.setText(groupUser.getNameGroup());

        Button btnAdd = (Button) view.findViewById(R.id.btnInvitationAgree);
        Button btnNoAdd = (Button) view.findViewById(R.id.btnInvitationNoAgree);


        btnAdd.setOnClickListener(v -> {
            AgreeInvitation(groupUser, dialog);
        });


        btnNoAdd.setOnClickListener(v -> {
            NoAgreeInvitation(groupUser, dialog);
        });



    }

    private void NoAgreeInvitation(GroupUser groupUser, AlertDialog dialog) {

        updateInvitation(groupUser.getIdUser(), groupUser.getIdGroup(), GroupUser.STATUS_NO_ACCEPT);

        dialog.dismiss();

        getInvitationsUser();
    }

    private void AgreeInvitation(GroupUser groupUser, AlertDialog dialog) {

        updateInvitation(groupUser.getIdUser(), groupUser.getIdGroup(), GroupUser.STATUS_ACCEPT);

        FirebaseMessaging.getInstance().unsubscribeFromTopic(groupUser.getIdGroup());

        dialog.dismiss();

        getInvitationsUser();
    }


    private void updateInvitation(String idUser, String idGroup, String status) {

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", idUser);
        params.put("group_id", idGroup);
        params.put("status", status);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, GroupApiMethods.POST_USER_GROUP_UPDATE, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {


                try {
                    String resposeData = response.getString("data");

                    if(!resposeData.equals("[]")){

                        ResourceUtil.showAlert("Mensaje", "Invitacion contestada correctamente", InvitationActivity.this, "success");

                    }else {
                        ResourceUtil.showAlert("Advertencia", "Se produjo un error al contestar la invitacion", InvitationActivity.this, "error");
                    }

                } catch (JSONException e) {
                    ResourceUtil.showAlert("Advertencia", "Se produjo un error al contestar la invitacion", InvitationActivity.this, "error");
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                ResourceUtil.showAlert("Advertencia", "Se produjo un error al registrar el grupo.",InvitationActivity.this, "error");
                error.printStackTrace();

            }
        });

        requestQueue.add(jsonObjectRequest);
    }


    private void loading(boolean isLoading) {

        if(isLoading){
            progressBar.setVisibility(View.VISIBLE);
        }else{
            progressBar.setVisibility(View.GONE);
        }
    }


    @Override
    public void OnClickInvitation(GroupUser groupUser) {
        dialogInvitation(groupUser);
    }
}