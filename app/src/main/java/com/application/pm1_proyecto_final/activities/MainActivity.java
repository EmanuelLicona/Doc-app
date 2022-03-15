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
import com.application.pm1_proyecto_final.api.UserApiMethods;
import com.application.pm1_proyecto_final.providers.UsersProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.JavaMailAPI;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.google.android.material.textfield.TextInputEditText;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    TextView txtViewRegister;
    TextInputEditText txtEmail, txtPassword;
    Button btnLogin;
    SweetAlertDialog pDialog;

    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferencesManager = new PreferencesManager(getApplicationContext());
//
//        User user = new User();
//
//        //Usuario temporal emulando un inicion de sesion
//        user.setId(2+"");
//        user.setName("Abdiel");
//        user.setLastname("Licona");
//        user.setEmail("alicoescobar@gmail.com");


        if(preferencesManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
            finish();

        }
        
//        else{
//            preferencesManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
//            preferencesManager.putString(Constants.KEY_USER_ID, user.getId()+"");
//            preferencesManager.putString(UsersProvider.KEY_EMAIL, user.getEmail());
//        }

        pDialog = ResourceUtil.showAlertLoading(MainActivity.this);

        txtViewRegister = (TextView) findViewById(R.id.textViewRegister);
        txtEmail = findViewById(R.id.txtEmailMain);
        txtPassword = findViewById(R.id.txtPasswordMain);

        btnLogin = (Button) findViewById(R.id.btnLogin);

        txtViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { login(); }
        });
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
                    String emailUser = dataUser.getString("email");
                    String passwordUser = dataUser.getString("password");

                    pDialog.dismiss();
                    if (email.equals(emailUser) && password.equals(passwordUser)) {
                        preferencesManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferencesManager.putString(Constants.KEY_USER_ID, dataUser.getString("id"));
                        preferencesManager.putString(UsersProvider.KEY_EMAIL, emailUser);

                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        ResourceUtil.showAlert("Advertencia", "Correo electrónico y/o password incorrectos", MainActivity.this, "error");
                    }

                } catch (JSONException e) {
                    pDialog.dismiss();
                    ResourceUtil.showAlert("Advertencia", "Correo electrónico y/o password incorrectos", MainActivity.this, "error");
                    e.printStackTrace();
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