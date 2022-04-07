package com.application.pm1_proyecto_final.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    ImageView btnShowDialogDate, imageViewCover;
    Spinner spinnerListCourses;
    TextInputEditText txtBirthDate, txtName, txtLastname, txtNumberAccount, txtPhone, txtAddress;
    EditText txtNumberAccountCurrent;
    String birthDate = "", name = "", lastname = "", numberAccount = "", numberAccountCurrent = "", phone = "",  address = "", course = "", mImageProfile = "", mImageCover = "", email = "", password = "";

    AlertDialog.Builder pBuilderSelector;
    SweetAlertDialog pDialog;
    Button btnEditProfile;
    CircleImageView btnBack, circleImageViewProfile;

    CharSequence options[];
    private final int GALLERY_REQUEST_CODE_PROFILE = 100;
    private final int GALLERY_REQUEST_CODE_COVER = 150;
    private final int PHOTO_REQUEST_CODE_PROFILE = 200;
    private final int PHOTO_REQUEST_CODE_COVER = 250;
    String currentPhotoPath;

    ArrayAdapter<CharSequence> adapter;
    PreferencesManager preferencesManager;

    Bitmap bitmapImageProfile, bitmapImageCover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        imageViewCover = (ImageView) findViewById(R.id.btnImageCoverProfile);
        circleImageViewProfile = (CircleImageView) findViewById(R.id.btnCircleImageProfile);

        spinnerListCourses = (Spinner) findViewById(R.id.spinnerListCoursesProfile);
        btnShowDialogDate = (ImageView) findViewById(R.id.btnShowDialogDateProfile);
        btnBack = (CircleImageView) findViewById(R.id.btnBack);
        btnEditProfile = (Button) findViewById(R.id.btnEditProfile);

        txtName = (TextInputEditText) findViewById(R.id.txtNameProfile);
        txtLastname = (TextInputEditText) findViewById(R.id.txtLastnameProfile);
        txtNumberAccount = (TextInputEditText) findViewById(R.id.txtNumberCountProfile);
        txtNumberAccountCurrent = (EditText) findViewById(R.id.txtNumberAccountCurrent);
        txtPhone = (TextInputEditText) findViewById(R.id.textPhoneProfile);
        txtAddress = (TextInputEditText) findViewById(R.id.txtAddressProfile);
        txtBirthDate = (TextInputEditText) findViewById(R.id.txtBirthDateProfile);
        preferencesManager = new PreferencesManager(EditProfileActivity.this);

        pDialog = ResourceUtil.showAlertLoading(this);
        pBuilderSelector = new AlertDialog.Builder(this);
        pBuilderSelector.setTitle("Seleccione una opción");
        options = new CharSequence[]{"Imagen de galería", "Tomar foto"};

        loadConfiguration();

        btnShowDialogDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    showDatePickerDialog();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        circleImageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectOptionImage(1);
            }
        });

        imageViewCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectOptionImage(2);
            }
        });

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                clickEditProfile();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void clickEditProfile() {
        String response = validateFields();
        if (response.equals("OK")) {
            pDialog.show();
            prepareUpdate();
        }else {
            ResourceUtil.showAlert("Advertencia", response, this, "error");
        }
    }

    private void prepareUpdate() {
        if (bitmapImageProfile != null) {
            mImageProfile = ResourceUtil.getImageBase64(bitmapImageProfile);
        }
        if (bitmapImageCover != null) {
            mImageCover = ResourceUtil.getImageBase64(bitmapImageCover);
        }

        User user = new User();
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
        user.setId(preferencesManager.getString(Constants.KEY_USER_ID));
        user.setEmail(email);
        user.setPassword(password);
        updateInfoUser(user);

    }

    private void updateInfoUser(User user) {
        if (pDialog.isShowing()) {
            pDialog.show();
        }

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        HashMap<String, String> params = new HashMap<>();
//        params.put("idFirebase", ResourceUtil.createCodeRandom(6));
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
                if (bitmapImageProfile != null) {
                    preferencesManager.putString(Constants.KEY_IMAGE_USER, user.getImage());
                }
                preferencesManager.putString(Constants.KEY_NAME_USER, user.getName() +" "+user.getLastname());
                pDialog.dismiss();
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                ResourceUtil.showAlert("Advertencia", "Se produjo un error al actualizar el usuario.",EditProfileActivity.this, "error");
                Log.d("ERROR_USER", "Error Update: "+error.getMessage());
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private void selectOptionImage(int numberImage) {
        pBuilderSelector.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) { // Imagen Galeria
                    if (numberImage == 1) {
                        openGallery(GALLERY_REQUEST_CODE_PROFILE);
                    }else if(numberImage == 2) {
                        openGallery(GALLERY_REQUEST_CODE_COVER);
                    }
                } else { // Tomar Foto
                    if (numberImage == 1) {
                        takePhoto(PHOTO_REQUEST_CODE_PROFILE);
                    }else if(numberImage == 2) {
                        takePhoto(PHOTO_REQUEST_CODE_COVER);
                    }
                }
            }
        });
        pBuilderSelector.show();
    }

    private void takePhoto(int requestCode) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, requestCode);
        } else {
            dispatchTakePictureIntent(requestCode);
        }
    }

    private void openGallery(int requestCode) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
        } else {
            selectImage(requestCode);
        }
    }

    private void selectImage(int requestCode) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //PERMISOS DE GALERIA
        if (requestCode == GALLERY_REQUEST_CODE_PROFILE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImage(GALLERY_REQUEST_CODE_PROFILE);
        }
        if (requestCode == GALLERY_REQUEST_CODE_COVER && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImage(GALLERY_REQUEST_CODE_COVER);
        }

        //PERMISOS DE CAMARA
        if(requestCode == PHOTO_REQUEST_CODE_PROFILE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent(PHOTO_REQUEST_CODE_PROFILE);
        }
        if(requestCode == PHOTO_REQUEST_CODE_COVER && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent(PHOTO_REQUEST_CODE_COVER);
        }
    }

    private void dispatchTakePictureIntent(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.toString();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                try {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.application.pm1_proyecto_final.activities.fileprovider",
                            photoFile);

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, requestCode);
                } catch (Exception ex) {
                    Log.i("ERROR", "dispatchTakePictureIntent():: "+ex.toString());
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()); //timeStamp = marca de tiempo
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //createTempFile = crear archivo temporal
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri uri;
        // SELECCION IMAGEN DE LA GALERIA
        if (requestCode == GALLERY_REQUEST_CODE_PROFILE && resultCode == RESULT_OK && data != null) {
            uri = data.getData();
            try {
                circleImageViewProfile.setImageURI(uri);
                bitmapImageProfile = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            } catch (Exception e) {
                Log.d("IMGERROR", "Se produjo un error: "+e.getMessage());
                Toast.makeText(this, "Se produjo un error: "+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == GALLERY_REQUEST_CODE_COVER && resultCode == RESULT_OK && data != null) {
            uri = data.getData();
            try {
                imageViewCover.setImageURI(uri);
                bitmapImageCover = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            } catch (Exception e) {
                Log.d("IMG_ERROR", "Se produjo un error: "+e.getMessage());
                Toast.makeText(this, "Se produjo un error "+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        // SELECCION TOMAR FOTOGRAFIA
        if (requestCode == PHOTO_REQUEST_CODE_PROFILE && resultCode == RESULT_OK) {
            bitmapImageProfile = BitmapFactory.decodeFile(currentPhotoPath);
            circleImageViewProfile.setImageBitmap(bitmapImageProfile);
        }
        if (requestCode == PHOTO_REQUEST_CODE_COVER && resultCode == RESULT_OK) {
            bitmapImageCover = BitmapFactory.decodeFile(currentPhotoPath);
            imageViewCover.setImageBitmap(bitmapImageCover);
        }
    }

    private void showDatePickerDialog() throws ParseException {
        String date = txtBirthDate.getText().toString();
        Calendar c = Calendar.getInstance();

        if (!date.isEmpty() && date != null) {
            Date dateUserSelected = new SimpleDateFormat("dd/MM/yyyy").parse(date);
            c.setTime(dateUserSelected);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int m, int dayOfMonth) {
        int month = m + 1;
        String mo = "";
        String day = "";

        if (month >= 1 && month <= 9) {
            mo = "0"+month;
        } else {
            mo = String.valueOf(month);
        }

        if (dayOfMonth >= 1 && dayOfMonth <= 9) {
            day = "0"+dayOfMonth;
        } else {
            day = String.valueOf(dayOfMonth);
        }

        String date = day + "/" + mo + "/" +year;
        txtBirthDate.setText(date);
    }

    private void loadConfiguration() {
        txtBirthDate.setEnabled(false);
        adapter = ArrayAdapter.createFromResource(this, R.array.list_courses , android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        spinnerListCourses.setAdapter(adapter);
        getUser();
    }

    private void getUser() {
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
                    email = response.getJSONObject("data").getString("email");
                    password = response.getJSONObject("data").getString("password");

                    numberAccountCurrent = numberAccount;

                    if (!name.isEmpty()) {
                        txtName.setText(name);
                    }
                    if (!lastname.isEmpty()) {
                        txtLastname.setText(lastname);
                    }
                    if (!phone.isEmpty()) {
                        txtPhone.setText(phone);
                    }
                    if (!address.isEmpty()) {
                        txtAddress.setText(address);
                    }
                    if (!numberAccount.isEmpty()) {
                        txtNumberAccount.setText(numberAccount);
                        txtNumberAccountCurrent.setText(numberAccountCurrent);
                    }
                    if (!course.isEmpty()) {
                        spinnerListCourses.setSelection(adapter.getPosition(course));
                    }
                    if (!birthDate.isEmpty()) {
                        txtBirthDate.setText(birthDate);
                    }
                    if (!mImageProfile.isEmpty() && !mImageProfile.equals("IMAGE")) {
                        Bitmap bitmap = ResourceUtil.decodeImage(mImageProfile);
                        circleImageViewProfile.setImageBitmap(bitmap);
                    }
                    if (!mImageCover.isEmpty() && !mImageCover.equals("IMAGE")) {
                        Bitmap bitmap = ResourceUtil.decodeImage(mImageCover);
                        imageViewCover.setImageBitmap(bitmap);
                    }


                } catch (JSONException e) {
                    ResourceUtil.showAlert("Advertencia", "Se produjo un error al cargar el usuario.", EditProfileActivity.this, "error");
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(EditProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(request);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String validateFields() {
        String response = "";

        name = txtName.getText().toString();
        lastname = txtLastname.getText().toString();
        numberAccount = txtNumberAccount.getText().toString();
        numberAccountCurrent = txtNumberAccountCurrent.getText().toString();
        phone = txtPhone.getText().toString();
        address = txtAddress.getText().toString();
        birthDate = txtBirthDate.getText().toString();
        course = spinnerListCourses.getSelectedItem().toString();

        if (name.isEmpty()) {
            response  = "Debes ingresar tu nombre, es obligatorio.";
        } else if(lastname.isEmpty()) {
            response  = "Debes ingresar tu apellido, es obligatorio.";
        } else if(numberAccount.isEmpty()) {
            response  = "Debes ingresar tu numero de cuenta UTH, es obligatorio.";
        } else if(phone.isEmpty()) {
            response  = "Debes ingresar tu numero de telefono, es obligatorio.";
        }  else if(course.equals("--Seleccionar--")) {
            response  = "Debes seleccionar la carrera que está cursando, es obligatorio.";
        } else if (birthDate.isEmpty()) {
            response  = "Debes ingresar tu fecha de nacimiento, es obligatorio.";
        } else if (!ResourceUtil.validateDateBirth(birthDate)) {
            response  = "Debes ser mayor de 15 años para poder registrarte.";
        } else {
            response = "OK";
        }

        return response;
    }
}