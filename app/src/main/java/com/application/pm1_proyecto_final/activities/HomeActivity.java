package com.application.pm1_proyecto_final.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.application.pm1_proyecto_final.Fragments.FragmentGrupo;
import com.application.pm1_proyecto_final.Fragments.FragmentMain;
import com.application.pm1_proyecto_final.Fragments.FragmentPerfil;
import com.application.pm1_proyecto_final.Fragments.FrangmentApuntes;
import com.application.pm1_proyecto_final.Fragments.FrangmentInfo;
import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.providers.UsersProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;

    BottomNavigationView bottomNavigation;
    MenuItem menuI;


    User userLog;

    private PreferencesManager preferencesManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        preferencesManager = new PreferencesManager(getApplicationContext());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);


        seleccionado(0);
        openFragment(new FragmentMain());

//        getUserLog();




    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {//llamar al menu del toolbar
        getMenuInflater().inflate(R.menu.menu_toolbar,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void seleccionado(int n){
        menuI = navigationView.getMenu().getItem(n);
        navigationItemSelectedListener.onNavigationItemSelected(menuI);
        menuI.setChecked(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.item_buscar){
            Toast.makeText(getApplicationContext(),"BUSCANDO", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    if(item.getItemId() == R.id.botton_home){
                        openFragment(new FragmentMain());
                    }else if(item.getItemId() == R.id.botton_grupos){
                        openFragment(new FragmentGrupo());
                    }
                    return true;
                }
            };

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);

        if(item.getItemId() == R.id.item_inicio){
            openFragment(new FragmentMain());
        }else if(item.getItemId() == R.id.item_perfil){
            openFragment(new FragmentPerfil());

        }else if(item.getItemId() == R.id.item_solicitud_grupo) {

            openMyInvitations();
        }else if(item.getItemId() == R.id.item_mis_grupos){
            openMyGroups();
        }else if(item.getItemId() == R.id.item_informacion){
            openFragment(new FrangmentInfo());
        } else if(item.getItemId() == R.id.item_cerrar_sesion){

            preferencesManager.clear();

            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            finish();
        }
        return false;
    }

    private void openMyGroups() {

        Intent intent = new Intent(getApplicationContext(), MyGroupsActivity.class);
        startActivity(intent);
    }

    private void openMyInvitations(){
        Intent intent = new Intent(getApplicationContext(), InvitationActivity.class);
        startActivity(intent);
    }


    private void getUserLog(){

        FirebaseFirestore database = FirebaseFirestore.getInstance();

        database.collection(UsersProvider.NAME_COLLECTION)
                .whereEqualTo(UsersProvider.KEY_EMAIL, preferencesManager.getString(UsersProvider.KEY_EMAIL))
                .get()
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        userLog = new User();

                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);

                        userLog.setId(documentSnapshot.getId());

                        userLog.setEmail(documentSnapshot.getString(UsersProvider.KEY_EMAIL));
                        userLog.setName(documentSnapshot.getString(UsersProvider.KEY_NAME));
                        userLog.setLastname(documentSnapshot.getString(UsersProvider.KEY_LASTNAME));
                        userLog.setImage(documentSnapshot.getString(UsersProvider.KEY_IMAGE));

                        userLog.setJson_groups(documentSnapshot.getString(UsersProvider.KEY_JSON));


                    }


                }).addOnFailureListener(error -> {
            Toast.makeText(getApplicationContext(), "NO se pudo recuperar el usuario", Toast.LENGTH_SHORT).show();
        });
    }
}