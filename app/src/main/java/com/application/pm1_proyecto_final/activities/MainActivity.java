package com.application.pm1_proyecto_final.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.api.GroupApiMethods;
import com.application.pm1_proyecto_final.api.UserApiMethods;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.providers.UsersProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.JavaMailAPI;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.application.pm1_proyecto_final.utils.TokenPreference;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    TextView txtViewRegister, txtChangePassword;
    TextInputEditText txtEmail, txtPassword;
    Button btnLogin;
    SweetAlertDialog pDialog;
    User user;

    private PreferencesManager preferencesManager;
    private TokenPreference tokenPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_PM1_PROYECTO_FINAL);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferencesManager = new PreferencesManager(getApplicationContext());


        pDialog = ResourceUtil.showAlertLoading(MainActivity.this);

        txtViewRegister = (TextView) findViewById(R.id.textViewRegister);
        txtChangePassword = (TextView) findViewById(R.id.txtChangePassword);
        txtEmail = (TextInputEditText) findViewById(R.id.txtEmailMain);
        txtPassword = (TextInputEditText) findViewById(R.id.txtPasswordMain);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        user = new User();

        txtViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        txtChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RecoverPassword.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { login(); }
        });


        if(preferencesManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            if(!getGroupForNotification(preferencesManager.getString(Constants.KEY_USER_ID))){

                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();

            }else{
                ResourceUtil.showAlert("Advertencia", "A ocurrido un error al asignar temas del usuario", MainActivity.this, "error");
            }

        }

    }

    private void login() {
        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString();
        String response = validateFieldsLogin(email, password);

        if (!response.equals("OK")) {
            ResourceUtil.showAlert("Advertencia", response, this, "error");
            return;
        }

        pDialog.show();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, UserApiMethods.EXIST_EMAIL + email, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray userJson = response.getJSONArray("data");

                    JSONObject dataUser = userJson.getJSONObject(0);

                    user.setId(dataUser.getString("id"));
                    user.setIdFirebase(dataUser.getString("idFirebase"));
                    user.setName(dataUser.getString("name"));
                    user.setLastname(dataUser.getString("lastname"));
                    user.setNumberAccount(dataUser.getString("numberAccount"));
                    user.setPhone(dataUser.getString("phone"));
                    user.setStatus(dataUser.getString("status"));
                    user.setImage(dataUser.getString("image"));
                    user.setImageCover(dataUser.getString("imageCover"));
                    user.setAddress(dataUser.getString("address"));
                    user.setBirthDate(dataUser.getString("birthDate"));
                    user.setCarrera(dataUser.getString("carrera"));
                    user.setEmail(dataUser.getString("email"));
                    user.setPassword(dataUser.getString("password"));
                    updateUserDisabled();


                    if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                        if(!getGroupForNotification(dataUser.getString("id"))) {

                            pDialog.dismiss();

                            preferencesManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                            preferencesManager.putString(Constants.KEY_USER_ID, user.getId());
                            preferencesManager.putString(Constants.KEY_IMAGE_USER, user.getImage());
                            preferencesManager.putString(Constants.KEY_NAME_USER, user.getName() + " " + user.getLastname());
                            preferencesManager.putString(UsersProvider.KEY_EMAIL, user.getEmail());

                            updateFcmTokenOnAPI(user);

                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else{
                            pDialog.dismiss();
                            ResourceUtil.showAlert("Advertencia", "A ocurrido un error al asignar temas del usuario", MainActivity.this, "error");
                        }
                    } else {
                        pDialog.dismiss();
                        ResourceUtil.showAlert("Advertencia", "Correo electrónico y/o password incorrectos", MainActivity.this, "error");
                    }

                } catch (JSONException e) {
                    pDialog.dismiss();
                    ResourceUtil.showAlert("Advertencia", "Correo electrónico y/o password incorrectos", MainActivity.this, "error");
                    Log.d("ERROR_VALIDATELOGIN", "Error: "+e.getMessage());
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(request);

    }

    private void updateFcmTokenOnAPI(User user) {

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        HashMap<String, String> params = new HashMap<>();

        tokenPreference = new TokenPreference(getApplicationContext());

        String token = tokenPreference.getString(Constants.KEY_FCM_TOKEN);

        token = token.trim();

        preferencesManager.putString(Constants.KEY_FCM_TOKEN, token);

//        Toast.makeText(this, "id: "+user.getId() + "\nToken: "+ token, Toast.LENGTH_SHORT).show();

        params.put("idFirebase", token);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PUT,
                UserApiMethods.PUT_USER+user.getId(),
                new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(MainActivity.this, "Error al actualizar fcmToken: "+ error.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("volleyError", "onErrorResponse: ", error);
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    boolean error = false;
    private boolean getGroupForNotification(String id) {



        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                (GroupApiMethods.GET_GROUPS_FOR_USER_ACTIVE+id),
                null,
                new com.android.volley.Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject  jsonObject = null;

                            if(response.getString("res").equals("true")){


                                JSONArray array = response.getJSONArray("data");

                                for (int i = 0; i < array.length(); i++) {
                                    jsonObject = new JSONObject(array.get(i).toString());
                                    FirebaseMessaging.getInstance().subscribeToTopic(jsonObject.getString("id"));
                                }
                                error = false;
                            }else{
                                Toast.makeText(getApplicationContext(), "Error: "+response.getString("msg"), Toast.LENGTH_SHORT).show();
                                error = true;
                            }

                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            error = true;
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

        return error;
    }

    private void updateUserDisabled() {
        if (user.getStatus().equals("INACTIVO")) {
            RequestQueue request = Volley.newRequestQueue(MainActivity.this);
            HashMap<String, String> params = new HashMap<>();
//            params.put("idFirebase", user.getIdFirebase());
            params.put("id", user.getId());
            params.put("name", user.getName());
            params.put("lastname", user.getLastname());
            params.put("numberAccount", user.getNumberAccount());
            params.put("phone", user.getPhone());
            params.put("status", "ACTIVO");
            params.put("address", user.getAddress());
            params.put("birthDate", user.getBirthDate());
            params.put("carrera", user.getCarrera());
            params.put("image", user.getImage());
            params.put("imageCover", user.getImageCover());
            params.put("email", user.getEmail());
            params.put("password", user.getPassword());

            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.PUT, UserApiMethods.PUT_USER+user.getId(), new JSONObject(params), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    ResourceUtil.showAlert("Advertencia", "Se produjo un error al actualizar el estado del usuario.",MainActivity.this, "error");
                    Log.d("ERROR_USER", "Error Update User Disabled: "+error.getMessage());
                }
            });
            request.add(jsonRequest);
        }
    }

    private String validateFieldsLogin(String email, String password) {
        String response = "OK";

        if (email.isEmpty()) {
            response = "Debe ingresar el correo electronico, es obligatorio";
        } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            response = "El correo electronico ingresado no es valido";
        } else if(password.isEmpty()) {
            response = "Debe ingresar el password, es obligatorio";
        }

        return response;
    }

}