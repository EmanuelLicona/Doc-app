package com.application.pm1_proyecto_final.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.ResourceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private TextView txtUsername, txtPhone, txtEmail, txtCarrera;
    private ImageView imageViewCover;
    private CircleImageView circleImageProfile;
    private String receiverIdUser = "";
    private CircleImageView btnBackShowProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        imageViewCover = (ImageView) findViewById(R.id.imageViewCover);
        circleImageProfile = (CircleImageView) findViewById(R.id.circleImageProfile);
        txtEmail = (TextView) findViewById(R.id.textViewEmail);
        txtUsername = (TextView) findViewById(R.id.textViewUsername);
        txtPhone = (TextView) findViewById(R.id.textViewPhone);
        txtCarrera = (TextView) findViewById(R.id.textViewCarrera);
        btnBackShowProfile = (CircleImageView) findViewById(R.id.btnBackShowProfile);

        receiverIdUser = getIntent().getStringExtra(Constants.KEY_USER_ID);
        getInfoUser();

        btnBackShowProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void getInfoUser() {
        RequestQueue requestQueue = Volley.newRequestQueue(UserProfileActivity.this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, UserApiMethods.GET_USER_ID + receiverIdUser,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String nameUser = response.getJSONObject("data").getString("name") + " "+response.getJSONObject("data").getString("lastname");
                    txtUsername.setText(nameUser);
                    txtCarrera.setText(response.getJSONObject("data").getString("carrera"));
                    txtEmail.setText(response.getJSONObject("data").getString("email"));
                    txtPhone.setText(response.getJSONObject("data").getString("phone"));
                    String image = response.getJSONObject("data").getString("image");
                    String imageCover = response.getJSONObject("data").getString("imageCover");

                    if (!image.isEmpty() && !image.equals("IMAGE")) {
                        Bitmap bitmap = ResourceUtil.decodeImage(image);
                        circleImageProfile.setImageBitmap(bitmap);
                    }
                    if (!imageCover.isEmpty() && !imageCover.equals("IMAGE")) {
                        Bitmap bitmap = ResourceUtil.decodeImage(imageCover);
                        imageViewCover.setImageBitmap(bitmap);
                    }

                } catch (JSONException e) {
                    ResourceUtil.showAlert("Advertencia", "Se produjo un error al cargar el usuario.", UserProfileActivity.this, "error");
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(UserProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(request);
    }
}