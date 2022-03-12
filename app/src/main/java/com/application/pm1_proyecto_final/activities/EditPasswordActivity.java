package com.application.pm1_proyecto_final.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.providers.AuthProvider;
import com.application.pm1_proyecto_final.providers.UsersProvider;
import com.application.pm1_proyecto_final.utils.JavaMailAPI;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

public class EditPasswordActivity extends AppCompatActivity {

    ImageView btnSendCode;
    Button btnVerifyCode;
    String codeGenerated;
    UsersProvider usersProvider;
    AuthProvider authProvider;
    EditText txtCode1, txtCode2, txtCode3, txtCode4, txtCode5, txtCode6;

    String nameUser = "", email = "", password = "", code1 = "", code2 = "", code3 = "", code4 = "", code5 = "", code6 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);

        btnSendCode = (ImageView) findViewById(R.id.btnSendCode);
        btnVerifyCode = (Button) findViewById(R.id.btnCodeVerify);
        usersProvider = new UsersProvider();
        authProvider = new AuthProvider();

        txtCode1 = (EditText) findViewById(R.id.txtCode1);
        txtCode2 = (EditText) findViewById(R.id.txtCode2);
        txtCode3 = (EditText) findViewById(R.id.txtCode3);
        txtCode4 = (EditText) findViewById(R.id.txtCode4);
        txtCode5 = (EditText) findViewById(R.id.txtCode5);
        txtCode6 = (EditText) findViewById(R.id.txtCode6);

        getUser();

        btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail(email, nameUser);
            }
        });

        btnVerifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyCode();
            }
        });
    }

    private void verifyCode() {
        code1 = txtCode1.getText().toString();
        code2 = txtCode2.getText().toString();
        code3 = txtCode3.getText().toString();
        code4 = txtCode4.getText().toString();
        code5 = txtCode5.getText().toString();
        code6 = txtCode6.getText().toString();

        if (!code1.isEmpty() && !code2.isEmpty() && !code3.isEmpty() && !code4.isEmpty() && !code5.isEmpty() && !code6.isEmpty()) {
            String code = code1+""+code2+""+code3+""+code4+""+code5+""+code6;

            if (code.equals(codeGenerated)) {
                Toast.makeText(this, "ACTUALIZAR PASSWORD EN LA BD", Toast.LENGTH_SHORT).show();
            } else {
                ResourceUtil.showAlert("Advertencia", "El código de verificación no es correcto.", this, "error");
            }

        } else {
            ResourceUtil.showAlert("Advertencia", "Llene todos los campos.", this, "error");
        }

    }

    private void sendEmail(String email, String nameUser) {
        codeGenerated = ResourceUtil.createCodeRandom(6);
        password = ResourceUtil.createCodeRandom(9);
        String message = "Cambio de contraseña solicitado. \n" +
                "Código Verificación: "+codeGenerated +"\n" +
                "Nuevo Password: "+password;
        String subject = nameUser + " Te saluda DOC-APP";

        JavaMailAPI javaMailAPI = new JavaMailAPI(this, email, subject, message);
        javaMailAPI.execute();
    }

    private void getUser() {
        usersProvider.getUser(authProvider.getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {

                    if (documentSnapshot.contains("name") && documentSnapshot.contains("lastname")) {
                        nameUser = documentSnapshot.getString("name") + " "+documentSnapshot.getString("lastname");
                    }
                    if (documentSnapshot.contains("email")) {
                        email = documentSnapshot.getString("email");
                    }

                }

            }
        });
    }
}