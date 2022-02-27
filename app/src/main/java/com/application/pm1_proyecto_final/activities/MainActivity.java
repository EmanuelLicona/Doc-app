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

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.providers.AuthProvider;
import com.application.pm1_proyecto_final.providers.UsersProvider;
import com.application.pm1_proyecto_final.utils.JavaMailAPI;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.DocumentSnapshot;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    TextView txtViewRegister;
    TextInputEditText txtEmail, txtPassword;
    Button btnLogin;
    SignInButton btnLoginGoogle;
    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;
    SweetAlertDialog pDialog;

    private GoogleSignInClient mGoogleSignInClient;
    private final int REQUEST_CODE_GOOGLE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pDialog = ResourceUtil.showAlertLoading(MainActivity.this);

        txtViewRegister = (TextView) findViewById(R.id.textViewRegister);
        txtEmail = findViewById(R.id.txtEmailMain);
        txtPassword = findViewById(R.id.txtPasswordMain);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLoginGoogle = findViewById(R.id.btnLoginGoogle);

        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.cliente_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        txtViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        btnLoginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInGoogle();
            }
        });

    }

    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == REQUEST_CODE_GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("ERROR", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        pDialog.show();
        mAuthProvider.googleLogin(acct)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String id = mAuthProvider.getUid();
                            checkUserExist(id);
                        } else {
                            pDialog.dismiss();
                            // If sign in fails, display a message to the user.
                            Log.w("ERROR", "signInWithCredential:failure", task.getException());
                            ResourceUtil.showAlert("Advertencia","No se pudo iniciar sesion con google.", MainActivity.this, "error");
                        }
                    }
                });
    }

    private void checkUserExist(final String id) {
        mUsersProvider.getUser(id).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists() && documentSnapshot.get("numberAccount") != null) {
                    pDialog.dismiss();
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                } else {
                    User user = new User();
                    String email = mAuthProvider.getEmail();
                    user.setEmail(email);
                    user.setId(id);

                    mUsersProvider.create(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            pDialog.dismiss();
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(MainActivity.this, CompleteProfileActivity.class);
                                startActivity(intent);
                            } else {
                                ResourceUtil.showAlert("Advertencia","No se pudo almacenar la informacion del estudiante.", MainActivity.this, "error");
                            }
                        }
                    });
                }
            }
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
        mAuthProvider.login(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                pDialog.dismiss();
                if(task.isSuccessful()) {
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    ResourceUtil.showAlert("Advertencia","El email y/o password ingresados son incorrectos.", MainActivity.this, "error");
                }
            }
        });
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