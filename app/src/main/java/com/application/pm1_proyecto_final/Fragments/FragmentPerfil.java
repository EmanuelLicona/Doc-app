package com.application.pm1_proyecto_final.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.application.pm1_proyecto_final.activities.CompleteProfileActivity;
import com.application.pm1_proyecto_final.activities.EditPasswordActivity;
import com.application.pm1_proyecto_final.activities.EditProfileActivity;
import com.application.pm1_proyecto_final.activities.MainActivity;
import com.application.pm1_proyecto_final.activities.RecoverPassword;
import com.application.pm1_proyecto_final.api.UserApiMethods;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.ResourceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;


public class FragmentPerfil extends Fragment {

    LinearLayout linearLayoutEditProfile, linearLayoutEditPassword;
    View view;
    TextView txtUsername, txtPhone, txtEmail, txtPostNumber, txtCarrera;
    ImageView imageViewCover;
    CircleImageView circleImageProfile;
    String nameUser = "";
    PreferencesManager preferencesManager;
    Button btnCancelAccount;
    User user;


    public FragmentPerfil() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil,container,false);

        linearLayoutEditProfile = view.findViewById(R.id.linearLayaoutEditProfile);
        linearLayoutEditPassword = view.findViewById(R.id.linearLayaoutEditPassword);
        imageViewCover = (ImageView) view.findViewById(R.id.imageViewCover);
        circleImageProfile = (CircleImageView) view.findViewById(R.id.circleImageProfile);
        btnCancelAccount = (Button) view.findViewById(R.id.btnCancelAccount);

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

        btnCancelAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                builder1.setMessage("¿Está seguro de cancelar su cuenta?");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Si",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                cancelAccount();
                                dialog.cancel();
                            }
                        });

                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder1.create();
                alert.show();
            }
        });

        return view;
    }

    private void cancelAccount() {
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        HashMap<String, String> params = new HashMap<>();
        params.put("idFirebase", user.getIdFirebase());
        params.put("id", user.getId());
        params.put("name", user.getName());
        params.put("lastname", user.getLastname());
        params.put("numberAccount", user.getNumberAccount());
        params.put("phone", user.getPhone());
        params.put("status", "INACTIVO");
        params.put("address", user.getAddress());
        params.put("birthDate", user.getBirthDate());
        params.put("carrera", user.getCarrera());
        params.put("image", user.getImage());
        params.put("imageCover", user.getImageCover());
        params.put("email", user.getEmail());
        params.put("password", user.getPassword());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, UserApiMethods.PUT_USER+user.getId(), new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ResourceUtil.showAlert("Advertencia", "Se produjo un error al cancelar su cuenta.",getContext(), "error");
                Log.d("ERROR_USER", "Error Update: "+error.getMessage());
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private void getInfoUserLogged() {
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, UserApiMethods.GET_USER_ID + preferencesManager.getString(Constants.KEY_USER_ID),
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    user = new User();
                    user.setId(preferencesManager.getString(Constants.KEY_USER_ID));
                    user.setName(response.getJSONObject("data").getString("name"));
                    user.setIdFirebase(response.getJSONObject("data").getString("idFirebase"));
                    user.setLastname(response.getJSONObject("data").getString("lastname"));
                    user.setPhone(response.getJSONObject("data").getString("phone"));
                    user.setEmail(response.getJSONObject("data").getString("email"));
                    user.setCarrera(response.getJSONObject("data").getString("carrera"));
                    user.setImage(response.getJSONObject("data").getString("image"));
                    user.setImageCover(response.getJSONObject("data").getString("imageCover"));
                    user.setPassword(response.getJSONObject("data").getString("password"));
                    user.setNumberAccount(response.getJSONObject("data").getString("numberAccount"));
                    user.setAddress(response.getJSONObject("data").getString("address"));
                    user.setBirthDate(response.getJSONObject("data").getString("birthDate"));

                    if (!user.getName().isEmpty() && !user.getLastname().isEmpty()) {
                        nameUser = user.getName() +" "+ user.getLastname();
                        txtUsername.setText(nameUser);
                    }
                    if (!user.getPhone().isEmpty()) {
                        txtPhone.setText(user.getPhone());
                    }
                    if (!user.getEmail().isEmpty()) {
                        txtEmail.setText(user.getEmail());
                    }
                    if (!user.getCarrera().isEmpty()) {
                        txtCarrera.setText(user.getCarrera());
                    }
                    if (!user.getImage().isEmpty() && !user.getImage().equals("IMAGE")) {
                        Bitmap bitmap = ResourceUtil.decodeImage(user.getImage());
                        circleImageProfile.setImageBitmap(bitmap);
                    }
                    if (!user.getImageCover().isEmpty() && !user.getImageCover().equals("IMAGE")) {
                        Bitmap bitmap = ResourceUtil.decodeImage(user.getImageCover());
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
        intent.putExtra("email", user.getEmail());
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