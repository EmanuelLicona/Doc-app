package com.application.pm1_proyecto_final.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.activities.BaseActivity;
import com.application.pm1_proyecto_final.activities.ChaatActivity;
import com.application.pm1_proyecto_final.adapters.UsersChatAdapter;
import com.application.pm1_proyecto_final.api.UserApiMethods;
import com.application.pm1_proyecto_final.listeners.UserListener;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.providers.UsersProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentChat extends Fragment implements UserListener {

    UsersProvider usersProvider;
    TextView textUsersEmpty;
    ProgressBar progressBar;
    RecyclerView recyclerViewUser;
    EditText editTextSearchComp;
    private PreferencesManager preferencesManager;
    UsersChatAdapter usersChatAdapter;
    List<User> userList;

    public FragmentChat() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Chats");

        usersProvider = new UsersProvider();
        progressBar = view.findViewById(R.id.usersProgressBar);
        textUsersEmpty = view.findViewById(R.id.textUsersEmpty);
        recyclerViewUser = view.findViewById(R.id.usersRecyclerView);
        preferencesManager = new PreferencesManager(getContext());

        editTextSearchComp = (EditText) view.findViewById(R.id.editTextSearchComp);

        userList = new ArrayList<>();
        getUsers();



        editTextSearchComp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    usersChatAdapter.getFilter().filter(charSequence);
                }catch (Exception e){}
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }

    private void getUsers() {
        loading(true);
        RequestQueue queue = Volley.newRequestQueue(getContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, UserApiMethods.USERS_FRIENDS+preferencesManager.getString(Constants.KEY_USER_ID), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray arrayUsers = jsonObject.getJSONArray("data");

                    for(int i = 0; i < arrayUsers.length(); i++) {
                        JSONObject rowUser = arrayUsers.getJSONObject(i);
                        User user = new User();
                        user.setId(rowUser.getString("id"));
                        user.setIdFirebase(rowUser.getString("idFirebase"));
                        user.setImage(rowUser.getString("image"));
                        user.setName(rowUser.getString("name"));
                        user.setLastname(rowUser.getString("lastname"));
                        user.setEmail(rowUser.getString("email"));
                        user.setStatus(rowUser.getString("status"));
                        userList.add(user);
                    }

                    if (userList.size() > 0) {
                        loadConfigAdapter();
                    } else {
                        textUsersEmpty.setVisibility(View.VISIBLE);
                    }
                    loading(false);
                }
                catch (JSONException ex) {
                    loading(false);
                    ResourceUtil.showAlert("Advertencia", "Se produjo un error al obtener la informacion de los usuarios que tiene publicaciones.", getContext(), "error");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading(false);
                ResourceUtil.showAlert("Advertencia", "Se produjo un error al obtener la informacion de los usuarios que tiene publicaciones.", getContext(), "error");
            }
        });
        queue.add(stringRequest);
    }

    private void loading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void loadConfigAdapter() {
        usersChatAdapter = new UsersChatAdapter(userList, this);
        recyclerViewUser.setAdapter(usersChatAdapter);
        recyclerViewUser.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getContext(), ChaatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        usersProvider.updateAvailability(1, preferencesManager.getString(Constants.KEY_USER_ID), getContext());
    }

    @Override
    public void onPause() {
        super.onPause();
        usersProvider.updateAvailability(0, preferencesManager.getString(Constants.KEY_USER_ID), getContext());
    }
}