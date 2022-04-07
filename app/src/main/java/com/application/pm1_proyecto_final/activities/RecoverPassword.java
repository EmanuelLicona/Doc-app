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

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecoverPassword extends AppCompatActivity {

    CircleImageView btnBackEditPassB;
    Button btnSendNewPassword;
    TextView btnSendAgainPassword, textInformation;

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
        btnSendAgainPassword = (TextView) findViewById(R.id.btnSendCodeRecoverPassword);
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

        btnSendAgainPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(RecoverPassword.this);
                builder1.setMessage("¿Generar la contraseña nuevamente?");
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

                AlertDialog alert = builder1.create();
                alert.show();
            }
        });

    }

    private void clickGetNewPassword() {

        if (!visibilityFields) {
            user.setEmail(txtEmail.getText().toString().trim());

            if (user.getEmail().isEmpty()) {
                ResourceUtil.showAlert("Advertencia", "Debes ingresar el correo electrónico.", RecoverPassword.this, "error");
                return;
            }

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
            String passwordTempUser = txtPasswordTemp.getText().toString();
            String newPasswordUser = txtNewPassword.getText().toString();
            String passwordConfirmUser = txtPasswordConfirmRecover.getText().toString();
            String response = validateInputsPassword(passwordTempUser,newPasswordUser, passwordConfirmUser);

            if (response.equals("OK")) {
                updatePassword();
            } else {
                ResourceUtil.showAlert("Advertencia", response, RecoverPassword.this, "error");
            }

        }

    }

    private void updatePassword() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        HashMap<String, String> params = new HashMap<>();
//        params.put("idFirebase", user.getIdFirebase());
        params.put("name", user.getName());
        params.put("lastname", user.getLastname());
        params.put("numberAccount", user.getNumberAccount());
        params.put("phone", user.getPhone());
        params.put("status", user.getStatus());
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
                setVisibilityFields("hide");
                txtEmail.setText("");
                passwordTemp = "";
                visibilityFields = false;
                ResourceUtil.showAlert("Confirmación", "Contraseña actualizada.",RecoverPassword.this, "success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ResourceUtil.showAlert("Advertencia", "Se produjo un error al actualizar el password del usuario.",RecoverPassword.this, "error");
                Log.d("ERROR_USER", "Error Update: "+error.getMessage());
            }
        });
        requestQueue.add(jsonObjectRequest);
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

    private void setVisibilityFields(String action) {
        if (action.equalsIgnoreCase("view")) {
            textInformation.setText("La contraseña enviada es temporal, por su seguridad ingrese su nueva contraseña y confirmela.");
            btnSendNewPassword.setText("SALVAR");
            containerPasswordTemp.setVisibility(View.VISIBLE);
            containerNewPassword.setVisibility(View.VISIBLE);
            containerPasswordConfirm.setVisibility(View.VISIBLE);
            txtEmail.setVisibility(View.GONE);
            btnSendAgainPassword.setVisibility(View.VISIBLE);
        } else {
            textInformation.setText("Le enviaremos su nueva contraseña al correo electrónico proporcionado.");
            btnSendNewPassword.setText("OBTENER NUEVA CONTRASEÑA");
            containerPasswordTemp.setVisibility(View.GONE);
            containerNewPassword.setVisibility(View.GONE);
            containerPasswordConfirm.setVisibility(View.GONE);
            txtEmail.setVisibility(View.VISIBLE);
            btnSendAgainPassword.setVisibility(View.GONE);
        }
        txtNewPassword.setText("");
        txtPasswordTemp.setText("");
        txtPasswordConfirmRecover.setText("");
    }

    private void sendEmail(String email, String nameUser) {
        passwordTemp = ResourceUtil.createCodeRandom(8);
        String message = "Cambio de contraseña solicitado. \n" +
                "Nueva Contraseña: "+passwordTemp;
        String subject = nameUser + " Te saluda DOC-APP";

        JavaMailAPI javaMailAPI = new JavaMailAPI(this, email, subject, message);
        javaMailAPI.execute();
    }

    private String validateInputsPassword(String passwordTempUser, String newPassword, String passwordConfirm) {
        String response = "OK";

        if (passwordTempUser.isEmpty()) {
            response = "Debe ingresar la contraseña enviada.";
        } else if(newPassword.isEmpty()) {
            response = "Debe ingresar la nueva contraseña.";
        } else if(passwordConfirm.isEmpty()) {
            response = "Debe ingresar la contraseña de confirmación.";
        } else if(!passwordTempUser.equals(passwordTemp)) {
            response = "La contraseña enviada ingresada no es correcta.";
        }else if(!newPassword.equals(passwordConfirm)) {
            response = "La nueva contraseña no coincide con la contraseña de confirmación.";
        }

        user.setPassword(newPassword);
        return response;
    }

}