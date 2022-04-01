package com.application.pm1_proyecto_final.activities;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CompleteProfileActivity extends AppCompatActivity {

    SweetAlertDialog pDialog;
    String birthDate = "", name = "", lastname = "", numberAccount = "", phone = "", address = "", course = "", email = "", password = "", codeGenerated = "", nameUser = "", image = "";
    Button btnCodeVerify;
    EditText txtEmail, txtCode1, txtCode2, txtCode3, txtCode4, txtCode5, txtCode6;
    TextView txtChangeEmail, btnSendCode;
    boolean submittedCode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        btnCodeVerify = (Button) findViewById(R.id.btnCodeVerify);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtCode1 = (EditText) findViewById(R.id.txtCode1Complete);
        txtCode2 = (EditText) findViewById(R.id.txtCode2Complete);
        txtCode3 = (EditText) findViewById(R.id.txtCode3Complete);
        txtCode4 = (EditText) findViewById(R.id.txtCode4Complete);
        txtCode5 = (EditText) findViewById(R.id.txtCode5Complete);
        txtCode6 = (EditText) findViewById(R.id.txtCode6Complete);

        txtChangeEmail = (TextView) findViewById(R.id.txtChangeEmail);
        btnSendCode = (TextView) findViewById(R.id.btnSendCodeComplete);

        pDialog = ResourceUtil.showAlertLoading(CompleteProfileActivity.this);

        Bundle bundleData = getIntent().getExtras();
        String[] data = bundleData.getStringArray("DATA_USER");
        loadDataUser(data);

        btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(CompleteProfileActivity.this);
                builder1.setMessage("¿Enviar el código de verificación nuevamente?");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Si",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                sendEmail(email, nameUser);
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

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });

        txtChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(CompleteProfileActivity.this);
                builder1.setMessage("¿Quiere cambiar el correo electrónico?");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Si",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                submittedCode = true;
                                txtEmail.setEnabled(true);
                                btnCodeVerify.setText("Enviar Código");
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


        btnCodeVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (submittedCode) {
                    sendCodeAgain();
                }else {
                    verifyCodeGenerated();
                }

            }
        });
    }

    private void verifyCodeGenerated() {
        String codeVerify = "";
        String code1 = txtCode1.getText().toString();
        String code2 = txtCode2.getText().toString();
        String code3 = txtCode3.getText().toString();
        String code4 = txtCode4.getText().toString();
        String code5 = txtCode5.getText().toString();
        String code6 = txtCode6.getText().toString();

        if (code1.isEmpty() || code2.isEmpty() || code3.isEmpty() || code4.isEmpty() || code5.isEmpty() || code6.isEmpty()) {
            ResourceUtil.showAlert("Advertencia", "Debes ingresar todos los digitos del código de verificación.", this, "error");
            return;
        }

        codeVerify = code1.concat(code2).concat(code3).concat(code4).concat(code5).concat(code6);
        if (codeVerify.equals(codeGenerated)) {
            registerUser();
        } else {
            ResourceUtil.showAlert("Advertencia", "El código de verificación no es correcto.", this, "error");
        }
    }

    private void registerUser() {
        pDialog.show();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        HashMap<String, String> params = new HashMap<>();
        params.put("idFirebase", ResourceUtil.createCodeRandom(6));
        params.put("name", name);
        params.put("lastname", lastname);
        params.put("numberAccount", numberAccount);
        params.put("phone", phone);
        params.put("status", "ACTIVO");
        params.put("address", address);
        params.put("birthDate", birthDate);
        params.put("carrera", course);
        params.put("image", image);
        params.put("imageCover", "IMAGE");
        params.put("email", email);
        params.put("password", password);
        params.put("availability", "0");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, UserApiMethods.POST_USER, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                pDialog.dismiss();
                Intent intent = new Intent(CompleteProfileActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                ResourceUtil.showAlert("Advertencia", "Se produjo un error al registrar el usuario.",CompleteProfileActivity.this, "error");
                Log.d("ERROR_USER", "Error Register: "+error.getMessage());
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private void sendCodeAgain() {
        email = txtEmail.getText().toString().trim();

        if (email.isEmpty()) {
            ResourceUtil.showAlert("Advertencia", "Debe ingresar el correo electrónico", this, "error");
            return;
        }

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, UserApiMethods.EXIST_EMAIL + email, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String existEmail = response.getString("data");

                    if (!existEmail.equals("[]")) {
                        ResourceUtil.showAlert("Advertencia", "El correo electrónico ingresado ya pertenece a otro usuario.", CompleteProfileActivity.this, "error");
                    } else {
                        sendEmail(email, nameUser);
                        btnCodeVerify.setText("Verificar");
                        txtEmail.setEnabled(false);
                        submittedCode = false;
                    }

                } catch (JSONException e) {
                    ResourceUtil.showAlert("Advertencia", "Se produjo un error al validar el email", CompleteProfileActivity.this, "error");
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CompleteProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(request);


    }

    private void loadDataUser(String[] data) {
        this.name = data[0];
        this.lastname = data[1];
        this.numberAccount = data[2];
        this.phone = data[3];
        this.email = data[4];
        this.password = data[5];
        this.address = data[6];
        this.course = data[7];
        this.birthDate = data[8];
        this.codeGenerated = data[9];
        this.image = data[10];
        this.nameUser = name + " "+ lastname;

        txtEmail.setText(email);
        txtEmail.setEnabled(false);
    }

    private void sendEmail(String email, String nameUser) {
        codeGenerated = "";
        codeGenerated = ResourceUtil.createCodeRandom(6);
        String message = "Te damos la bienvenida a DOC-APP. Para garantizar la seguridad de tu cuenta, verifica tu dirección de correo electrónico. \n" + "Código Verificación: "+codeGenerated;
        String subject = nameUser + " Bienvenido a DOC-APP";

        JavaMailAPI javaMailAPI = new JavaMailAPI(this, email, subject, message);
        javaMailAPI.execute();
    }
}