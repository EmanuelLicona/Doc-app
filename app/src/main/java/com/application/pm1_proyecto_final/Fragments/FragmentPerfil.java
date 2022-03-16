package com.application.pm1_proyecto_final.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.activities.EditPasswordActivity;
import com.application.pm1_proyecto_final.activities.EditProfileActivity;
import com.application.pm1_proyecto_final.api.UserApiMethods;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.ResourceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;


public class FragmentPerfil extends Fragment {

    LinearLayout linearLayoutEditProfile, linearLayoutEditPassword;
    View view;
    TextView txtUsername, txtPhone, txtEmail, txtPostNumber, txtCarrera;
    ImageView imageViewCover;
    CircleImageView circleImageProfile;
    String email = "", nameUser = "";
    PreferencesManager preferencesManager;


    public FragmentPerfil() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil,container,false);

        linearLayoutEditProfile = view.findViewById(R.id.linearLayaoutEditProfile);
        linearLayoutEditPassword = view.findViewById(R.id.linearLayaoutEditPassword);
        imageViewCover = (ImageView) view.findViewById(R.id.imageViewCover);
        circleImageProfile = (CircleImageView) view.findViewById(R.id.circleImageProfile);

        txtEmail = view.findViewById(R.id.textViewEmail);
        txtUsername = view.findViewById(R.id.textViewUsername);
        txtPhone = view.findViewById(R.id.textViewPhone);
        txtPostNumber = view.findViewById(R.id.textViewPostNumber);
        txtCarrera = view.findViewById(R.id.textViewCarrera);

        preferencesManager = new PreferencesManager(getContext());

        linearLayoutEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToEditProfile();
            }
        });

        linearLayoutEditPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToEditPassword();
            }
        });

        return view;
    }

    private void getInfoUserLogged() {
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, UserApiMethods.GET_USER_ID + preferencesManager.getString(Constants.KEY_USER_ID),
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    String name = response.getJSONObject("data").getString("name");
                    String lastname = response.getJSONObject("data").getString("lastname");
                    String phone = response.getJSONObject("data").getString("phone");
                    email = response.getJSONObject("data").getString("email");
                    String course = response.getJSONObject("data").getString("carrera");
                    String image = response.getJSONObject("data").getString("image");
                    String imageCover = response.getJSONObject("data").getString("imageCover");


                    if (!name.isEmpty() && !lastname.isEmpty()) {
                        nameUser = name +" "+ lastname;
                        txtUsername.setText(nameUser);
                    }
                    if (!phone.isEmpty()) {
                        txtPhone.setText(phone);
                    }
                    if (!email.isEmpty()) {
                        txtEmail.setText(email);
                    }
                    if (!course.isEmpty()) {
                        txtCarrera.setText(course);
                    }
                    if (!image.isEmpty() && !image.equals("IMAGE")) {
                        Bitmap bitmap = ResourceUtil.decodeImage(image);
                        circleImageProfile.setImageBitmap(bitmap);
                    }
                    if (!imageCover.isEmpty() && !imageCover.equals("IMAGE")) {
                        Bitmap bitmap = ResourceUtil.decodeImage(imageCover);
                        imageViewCover.setImageBitmap(bitmap);
                    }


                } catch (JSONException e) {
                    ResourceUtil.showAlert("Advertencia", "Se produjo un error al cargar el usuario.", getContext(), "error");
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(request);
    }

    private void goToEditPassword() {
        Intent intent = new Intent(getContext(), EditPasswordActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("nameUser", nameUser);
        startActivity(intent);
    }

    private void goToEditProfile() {
        Intent intent = new Intent(getContext(), EditProfileActivity.class);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        getInfoUserLogged();
    }
}