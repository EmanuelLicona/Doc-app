package com.application.pm1_proyecto_final.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.activities.EditPasswordActivity;
import com.application.pm1_proyecto_final.activities.EditProfileActivity;
import com.application.pm1_proyecto_final.providers.AuthProvider;
import com.application.pm1_proyecto_final.providers.UsersProvider;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FragmentPerfil extends Fragment {

    LinearLayout linearLayoutEditProfile, linearLayoutEditPassword;
    View view;
    TextView txtUsername, txtPhone, txtEmail, txtPostNumber, txtCarrera;
    ImageView imageViewCover;
    CircleImageView circleImageProfile;


    UsersProvider usersProvider;
    AuthProvider authProvider;

    public FragmentPerfil() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil,container,false);

        linearLayoutEditProfile = view.findViewById(R.id.linearLayaoutEditProfile);
        linearLayoutEditPassword = view.findViewById(R.id.linearLayaoutEditPassword);
        imageViewCover = (ImageView) view.findViewById(R.id.imageViewCover);
        circleImageProfile = (CircleImageView) view.findViewById(R.id.circleImageProfile);

        txtEmail = view.findViewById(R.id.textViewEmail);
        txtUsername = view.findViewById(R.id.textViewUsername);
        txtPhone = view.findViewById(R.id.textViewPhone);
        txtPostNumber = view.findViewById(R.id.textViewPostNumber);
        txtCarrera = view.findViewById(R.id.textViewCarrera);

        usersProvider = new UsersProvider();
        authProvider = new AuthProvider();

        linearLayoutEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToEditProfile();
            }
        });

        linearLayoutEditPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToEditPassword();
            }
        });

        return view;
    }

    private void getInfoUserLogged() {
        usersProvider.getUser(authProvider.getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("name") && documentSnapshot.contains("lastname")){
                        String nameUser = documentSnapshot.getString("name") +" "+documentSnapshot.getString("lastname");
                        txtUsername.setText(nameUser);
                    }
                    if (documentSnapshot.contains("phone")){
                        String phone = documentSnapshot.getString("phone");
                        txtPhone.setText(phone);
                    }
                    if (documentSnapshot.contains("email")){
                        String email = documentSnapshot.getString("email");
                        txtEmail.setText(email);
                    }
                    if (documentSnapshot.contains("carrera")){
                        String carrera = documentSnapshot.getString("carrera");
                        txtCarrera.setText(carrera);
                    }
                    if (documentSnapshot.contains("image")){
                        String imageProfile = documentSnapshot.getString("image");
                        if (imageProfile != null && !imageProfile.isEmpty()) {
                            Picasso.with(getContext()).load(imageProfile).into(circleImageProfile);
                        }
                    }
                    if (documentSnapshot.contains("image_cover")){
                        String imageCover = documentSnapshot.getString("image_cover");
                        if (imageCover != null && !imageCover.isEmpty()) {
                            Picasso.with(getContext()).load(imageCover).into(imageViewCover);
                        }
                    }
                }

            }
        });
    }

    private void goToEditPassword() {
        Intent intent = new Intent(getContext(), EditPasswordActivity.class);
        startActivity(intent);
    }

    private void goToEditProfile() {
        Intent intent = new Intent(getContext(), EditProfileActivity.class);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        getInfoUserLogged();
    }
}