package com.application.pm1_proyecto_final.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.providers.AuthProvider;
import com.application.pm1_proyecto_final.providers.UsersProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.JavaMailAPI;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditPasswordActivity extends AppCompatActivity {

    ImageView btnSendCode;
    CircleImageView btnBackEditPass;
    Button btnVerifyCode;
    String codeGenerated;
    EditText txtCode1, txtCode2, txtCode3, txtCode4, txtCode5, txtCode6;
    TextView txtEmailSend, btnSendCodeEditPassword;
    PreferencesManager preferencesManager;

    String nameUser = "", email = "", password = "", code1 = "", code2 = "", code3 = "", code4 = "", code5 = "", code6 = "";
    String birthDate = "", name = "", lastname = "", numberAccount = "", phone = "",  address = "", course = "", mImageProfile = "", mImageCover = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);

        btnSendCode = (ImageView) findViewById(R.id.btnSendCode);
        btnVerifyCode = (Button) findViewById(R.id.btnCodeVerify);
        preferencesManager = new PreferencesManager(this);

        txtCode1 = (EditText) findViewById(R.id.txtCode1);
        txtCode2 = (EditText) findViewById(R.id.txtCode2);
        txtCode3 = (EditText) findViewById(R.id.txtCode3);
        txtCode4 = (EditText) findViewById(R.id.txtCode4);
        txtCode5 = (EditText) findViewById(R.id.txtCode5);
        txtCode6 = (EditText) findViewById(R.id.txtCode6);
        txtEmailSend = (TextView) findViewById(R.id.txtEmailSend);
        btnSendCodeEditPassword = (TextView) findViewById(R.id.btnSendCodeEditPassword);
        btnBackEditPass = (CircleImageView) findViewById(R.id.btnBackEditPass);

        getInfoUser();

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

        btnBackEditPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnSendCodeEditPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(EditPasswordActivity.this);
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
    }

    private void getInfoUser() {
        email = getIntent().getStringExtra("email");
        nameUser = getIntent().getStringExtra("nameUser");

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, UserApiMethods.GET_USER_ID + preferencesManager.getString(Constants.KEY_USER_ID),
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    name = response.getJSONObject("data").getString("name");
                    lastname = response.getJSONObject("data").getString("lastname");
                    phone = response.getJSONObject("data").getString("phone");
                    course = response.getJSONObject("data").getString("carrera");
                    birthDate = response.getJSONObject("data").getString("birthDate");
                    address = response.getJSONObject("data").getString("address");
                    numberAccount = response.getJSONObject("data").getString("numberAccount");
                    mImageProfile = response.getJSONObject("data").getString("image");
                    mImageCover = response.getJSONObject("data").getString("imageCover");

                } catch (JSONException e) {
                    ResourceUtil.showAlert("Advertencia", "Se produjo un error al cargar el usuario.", EditPasswordActivity.this, "error");
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(EditPasswordActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(request);

        txtEmailSend.setText(email);
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

                User user = new User();
                user.setPassword(password);
                user.setId(preferencesManager.getString(Constants.KEY_USER_ID));
                user.setName(name);
                user.setLastname(lastname);
                user.setPhone(phone);
                user.setNumberAccount(numberAccount);
                user.setAddress(address);
                user.setCarrera(course);
                user.setBirthDate(birthDate);
                user.setStatus("ACTIVO");
                user.setImage(mImageProfile);
                user.setImageCover(mImageCover);
                user.setEmail(email);
                updatePasswordUser(user);

            } else {
                ResourceUtil.showAlert("Advertencia", "El código de verificación no es correcto.", this, "error");
            }

        } else {
            ResourceUtil.showAlert("Advertencia", "Llene todos los campos.", this, "error");
        }

    }

    private void updatePasswordUser(User user) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        HashMap<String, String> params = new HashMap<>();
        params.put("idFirebase", ResourceUtil.createCodeRandom(6));
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

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, UserApiMethods.PUT_USER+user.getId(), new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                codeGenerated = null;
                password = "";
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ResourceUtil.showAlert("Advertencia", "Se produjo un error al actualizar el password del usuario.",EditPasswordActivity.this, "error");
                Log.d("ERROR_USER", "Error Update: "+error.getMessage());
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private void sendEmail(String email, String nameUser) {
        codeGenerated = ResourceUtil.createCodeRandom(6);
        password = ResourceUtil.createCodeRandom(9);
        String message = "Cambio de contraseña solicitado. \n" +
                "Código Verificación: "+codeGenerated +"\n" +
                "Nueva Contraseña: "+password;
        String subject = nameUser + " Te saluda DOC-APP";

        JavaMailAPI javaMailAPI = new JavaMailAPI(this, email, subject, message);
        javaMailAPI.execute();
    }
}