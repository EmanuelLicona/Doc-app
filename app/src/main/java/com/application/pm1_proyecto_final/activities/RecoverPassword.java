package com.application.pm1_proyecto_final.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.application.pm1_proyecto_final.api.UserApiMethods;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.providers.UsersProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.JavaMailAPI;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecoverPassword extends AppCompatActivity {

    CircleImageView btnBackEditPassB;
    Button btnSendNewPassword;
    TextView btnSendPassword, textInformation;

    EditText txtEmail;
    TextInputEditText txtNewPassword, txtPasswordTemp, txtPasswordConfirmRecover;
    LinearLayout containerPasswordTemp, containerNewPassword, containerPasswordConfirm;

    boolean visibilityFields = false;
    String passwordTemp = "", nameUser = "";
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);

        btnSendNewPassword = (Button) findViewById(R.id.btnSendNewPassword);
        btnBackEditPassB = (CircleImageView) findViewById(R.id.btnBackEditPassB);
        btnSendPassword = (TextView) findViewById(R.id.btnSendCodeRecoverPassword);
        textInformation = (TextView) findViewById(R.id.textInformationRecover);

        txtEmail = (EditText) findViewById(R.id.txtEmailRecoverPassword);
        txtNewPassword = (TextInputEditText) findViewById(R.id.txtNewPassword);
        txtPasswordTemp = (TextInputEditText) findViewById(R.id.txtPasswordTemp);
        txtPasswordConfirmRecover = (TextInputEditText) findViewById(R.id.txtPasswordConfirmRecover);

        containerPasswordTemp = (LinearLayout) findViewById(R.id.containerPasswordTemp);
        containerNewPassword = (LinearLayout) findViewById(R.id.containerNewPassword);
        containerPasswordConfirm = (LinearLayout) findViewById(R.id.containerPasswordConfirm);
        user = new User();

        btnBackEditPassB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnSendNewPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickGetNewPassword();
            }
        });

        btnSendPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(RecoverPassword.this);
                builder1.setMessage("¿Enviar la contraseña nuevamente?");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Si",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                visibilityFields = false;
                                setVisibilityFields("hide");

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

    private void clickGetNewPassword() {

        if (!visibilityFields) {
            user.setEmail(txtEmail.getText().toString().trim());
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, UserApiMethods.EXIST_EMAIL + user.getEmail(), null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray userJson = response.getJSONArray("data");
                        JSONObject dataUser = userJson.getJSONObject(0);
                        String emailUser = dataUser.getString("email");
                        String status = dataUser.getString("status");

                        if (user.getEmail().equals(emailUser) && status.equals("ACTIVO")) {
                            loadDataUser(dataUser);
                        } else {
                            ResourceUtil.showAlert("Advertencia", "Se produjo un error al cambiar su contraseña.", RecoverPassword.this, "error");
                        }

                    } catch (JSONException e) {
                        ResourceUtil.showAlert("Advertencia", "El correo eletrónico no tiene una cuenta creada en Doc-App.", RecoverPassword.this, "error");
                        e.printStackTrace();
                    }
                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(RecoverPassword.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            requestQueue.add(request);
        } else {
            Toast.makeText(this, "Verificando y Guardando", Toast.LENGTH_SHORT).show();
        }


    }

    private void loadDataUser(JSONObject dataUser) {
        try {
            user.setName(dataUser.getString("name"));
            user.setLastname(dataUser.getString("lastname"));
            user.setId(dataUser.getString("id"));
            user.setIdFirebase(dataUser.getString("idFirebase"));
            user.setNumberAccount(dataUser.getString("numberAccount"));
            user.setStatus(dataUser.getString("status"));
            user.setImage(dataUser.getString("image"));
            user.setImageCover(dataUser.getString("imageCover"));
            user.setAddress(dataUser.getString("address"));
            user.setBirthDate(dataUser.getString("birthDate"));
            user.setCarrera(dataUser.getString("carrera"));
            user.setPassword(dataUser.getString("password"));
            user.setPhone(dataUser.getString("phone"));
            nameUser = user.getName() +" "+ user.getLastname();

            sendEmail(user.getEmail(), nameUser);
            setVisibilityFields("view");
            visibilityFields = true;
        } catch (JSONException e) {
            Log.d("ERRROR", "Se produjo un error al cargar la informacion del usuario "+e.getMessage());
        }
    }

    private void prepareUpdatePassword() {

        if (!visibilityFields) {
            // ENVIAR NUEVO PASSWORD


        } else {

        }

    }

    private void setVisibilityFields(String action) {
        if (action.equalsIgnoreCase("view")) {
            textInformation.setText("Le enviaremos su nueva contraseña al correo electrónico proporcionado.");
            btnSendNewPassword.setText("SALVAR");
            containerPasswordTemp.setVisibility(View.VISIBLE);
            containerNewPassword.setVisibility(View.VISIBLE);
            containerPasswordConfirm.setVisibility(View.VISIBLE);
            txtEmail.setVisibility(View.GONE);
        } else {
            textInformation.setText("La contraseña enviada es temporal, por su seguridad ingrese su nueva contraseña y confirmela.");
            btnSendNewPassword.setText("OBTENER NUEVA CONTRASEÑA");
            containerPasswordTemp.setVisibility(View.GONE);
            containerNewPassword.setVisibility(View.GONE);
            containerPasswordConfirm.setVisibility(View.GONE);
            txtEmail.setVisibility(View.VISIBLE);
        }
    }

    private void sendEmail(String email, String nameUser) {
        passwordTemp = ResourceUtil.createCodeRandom(8);
        String message = "Cambio de contraseña solicitado. \n" +
                "Nueva Contraseña: "+passwordTemp;
        String subject = nameUser + " Te saluda DOC-APP";

        JavaMailAPI javaMailAPI = new JavaMailAPI(this, email, subject, message);
        javaMailAPI.execute();
    }

}