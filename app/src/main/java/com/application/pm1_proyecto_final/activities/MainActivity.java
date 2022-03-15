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

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.JavaMailAPI;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.google.android.material.textfield.TextInputEditText;


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

        if(preferencesManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
            finish();
        }

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
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();
        String response = validateFieldsLogin(email, password);

        if (!response.equals("OK")) {
            ResourceUtil.showAlert("Advertencia", response, this, "error");
            return;
        }

        pDialog.show();
//        mAuthProvider.login(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                pDialog.dismiss();
//                if(task.isSuccessful()) {
//
//                    preferencesManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
//                    preferencesManager.putString(Constants.KEY_USER_ID, task.getResult().getUser().getUid());
//                    preferencesManager.putString(UsersProvider.KEY_EMAIL, txtEmail.getText().toString());
//
//
//                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//
//                    finish();
//                } else {
//                    ResourceUtil.showAlert("Advertencia","El email y/o password ingresados son incorrectos.", MainActivity.this, "error");
//                }
//            }
//        });
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