package com.application.pm1_proyecto_final.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.application.pm1_proyecto_final.providers.UsersProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;

public class BaseActivity extends AppCompatActivity {

    private PreferencesManager preferencesManager;
    private UsersProvider usersProvider;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferencesManager = new PreferencesManager(getApplicationContext());
        usersProvider = new UsersProvider();
    }

    @Override
    protected void onPause() {
        super.onPause();
        usersProvider.updateAvailability(0, preferencesManager.getString(Constants.KEY_USER_ID), getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        usersProvider.updateAvailability(1, preferencesManager.getString(Constants.KEY_USER_ID), getApplicationContext());
    }


}
