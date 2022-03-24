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
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.application.pm1_proyecto_final.Fragments.FragmentGrupo;
import com.application.pm1_proyecto_final.Fragments.FragmentMain;
import com.application.pm1_proyecto_final.Fragments.FragmentPerfil;
import com.application.pm1_proyecto_final.Fragments.FrangmentApuntes;
import com.application.pm1_proyecto_final.Fragments.FrangmentInfo;
import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.api.UserApiMethods;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.providers.UsersProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;
    TextView txtNameUserMenu, txtEmailUserMenu;
    ImageView imgViewProfile;

    BottomNavigationView bottomNavigation;
    MenuItem menuI;

    User userLog;

    FragmentGrupo fragmentGrupo;
    FragmentMain fragmentMain;
    FrangmentInfo frangmentInfo;
    View view;

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
        getInfoUserLogged();

        fragmentGrupo = new FragmentGrupo();
        fragmentMain = new FragmentMain();
        frangmentInfo = new FrangmentInfo();

        openFragment(fragmentMain);
    }

    public void setDataToNavigationDrawer() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
        txtNameUserMenu = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txtNameUserMenu);
        txtEmailUserMenu = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txtEmailMenu);
        imgViewProfile = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.imgViewProfileMenu);
    }

    private void getInfoUserLogged() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, UserApiMethods.GET_USER_ID + preferencesManager.getString(Constants.KEY_USER_ID),
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    setDataToNavigationDrawer();
                    String name = response.getJSONObject("data").getString("name");
                    String lastname = response.getJSONObject("data").getString("lastname");
                    String email = response.getJSONObject("data").getString("email");
                    String imageProfile = response.getJSONObject("data").getString("image");

                    if (!name.isEmpty() && !lastname.isEmpty()) {
                        String nameUser = name +" "+ lastname;
                        txtNameUserMenu.setText(nameUser);
                    }
                    if (!imageProfile.isEmpty() && !imageProfile.equals("IMAGE")) {
                        Bitmap bitmap = ResourceUtil.decodeImage(imageProfile);
                        imgViewProfile.setImageBitmap(bitmap);
                    }
                    if (!email.isEmpty()) {
                        txtEmailUserMenu.setText(email);
                    }

                } catch (Exception e) {
                    ResourceUtil.showAlert("Advertencia", "Error: "+e.getMessage(), HomeActivity.this, "error");
//                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(HomeActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(request);
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
                        openFragment(fragmentMain);
                    }else if(item.getItemId() == R.id.botton_grupos){
                        openFragment(fragmentGrupo);
                    }
                    return true;
                }
            };

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);

        if(item.getItemId() == R.id.item_inicio){
            openFragment(fragmentMain);
        }else if(item.getItemId() == R.id.item_perfil){
            openFragment(new FragmentPerfil());

        }else if(item.getItemId() == R.id.item_solicitud_grupo) {

            openMyInvitations();
        }else if(item.getItemId() == R.id.item_mis_grupos){
            openMyGroups();
        }else if(item.getItemId() == R.id.item_informacion){
            openFragment(frangmentInfo);
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


}