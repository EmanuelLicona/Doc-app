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
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.application.pm1_proyecto_final.providers.AuthProvider;
import com.application.pm1_proyecto_final.providers.ImageProvider;
import com.application.pm1_proyecto_final.providers.UsersProvider;
import com.application.pm1_proyecto_final.utils.FileUtil;
import com.application.pm1_proyecto_final.utils.JavaMailAPI;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;


public class RegisterActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    Spinner spinnerListCourses;
    TextInputEditText txtBirthDate, txtName, txtLastname, txtNumberAccount, txtPhone, txtEmail, txtPassword, txtConfirmPassword, txtAddress, txtCodeGeneratedConfirm;
    String birthDate = "", name = "", lastname = "", numberAccount = "", phone = "", email = "", password = "", confirmPassword = "", address = "", course = "", image = "";
    ImageView btnShowDialogDate, addImgPhotoUser;
    File imageFile;
    Button btnRegister;
    CircleImageView circleImageViewBack;
    LinearLayout containerGeneratedCode;

    ImageProvider imageProvider;
    UsersProvider usersProvider;
    AuthProvider authProvider;
    SweetAlertDialog pDialog;
    AlertDialog.Builder pBuilderSelector;

    CharSequence options[];
    private final int GALLERY_REQUEST_CODE = 100;
    private final int PHOTO_REQUEST_CODE = 200;
    static final int REQUEST_ACCESS_CAM = 201;
    private String codeGenerated;

    // Variables tomar foto
    String mAbsolutePhotoPath;
    String mPhotoPath;
    File mPhotoFile;

    Bitmap bitmap;
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        imageProvider = new ImageProvider();
        usersProvider = new UsersProvider();
        authProvider = new AuthProvider();
        pDialog = ResourceUtil.showAlertLoading(this);
        pBuilderSelector = new AlertDialog.Builder(this);
        pBuilderSelector.setTitle("Seleccione una opción");
        options = new CharSequence[]{"Imagen de galería", "Tomar foto"};

        spinnerListCourses = (Spinner) findViewById(R.id.spinnerListCoursesRegister);
        btnShowDialogDate = (ImageView) findViewById(R.id.btnShowDialogDateRegister);
        addImgPhotoUser = (ImageView) findViewById(R.id.addImgPhotoUserRegister);
        circleImageViewBack = findViewById(R.id.circleImageBack);
        containerGeneratedCode = (LinearLayout)  findViewById(R.id.containerGeneratedCode);

        txtName = (TextInputEditText) findViewById(R.id.txtNameRegister);
        txtLastname = (TextInputEditText) findViewById(R.id.txtLastnameRegister);
        txtNumberAccount = (TextInputEditText) findViewById(R.id.txtNumberCountRegister);
        txtPhone = (TextInputEditText) findViewById(R.id.txtPhoneRegister);
        txtEmail = (TextInputEditText) findViewById(R.id.txtEmailRegister);
        txtPassword = (TextInputEditText) findViewById(R.id.txtPasswordRegister);
        txtConfirmPassword = (TextInputEditText) findViewById(R.id.txtConfirmPasswordRegister);
        txtAddress = (TextInputEditText) findViewById(R.id.txtAddressRegister);
        txtBirthDate = (TextInputEditText) findViewById(R.id.txtBirthDateRegister);
        txtCodeGeneratedConfirm = (TextInputEditText) findViewById(R.id.txtCodeGeneratedConfirm);

        btnRegister = (Button) findViewById(R.id.btnRegisterRegister);

        loadConfiguration();

        btnShowDialogDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        addImgPhotoUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectOptionImage();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                clickSaveUser();
            }

        });

        circleImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void selectOptionImage() {

        pBuilderSelector.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) { // Imagen Galeria
                    openGallery();
                } else if(i == 1) { // Tomar fotografia
                    takePhoto();
                }
            }
        });

        pBuilderSelector.show();
    }

    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_ACCESS_CAM);
        } else {
            dispatchTakePictureIntent();
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

    private void dispatchTakePictureIntent() {
        image = "";
//        addImgPhotoUser.setImageBitmap(null);
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
                    startActivityForResult(takePictureIntent, PHOTO_REQUEST_CODE);
                } catch (Exception ex) {
                    Log.i("ERROR", "dispatchTakePictureIntent():: "+ex.toString());
                }
            }
        }
    }

    private void openGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_REQUEST_CODE);
        } else {
            selectImage();
        }
    }

    private void selectImage() {
        // SOLO ACEPTA IMAGENES JPEG
        image = "";
//        addImgPhotoUser.setImageBitmap(null);
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/jpeg");
        startActivityForResult(Intent.createChooser(galleryIntent, "Seleccione la imagen"), GALLERY_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == GALLERY_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImage();
        }
        if(requestCode == REQUEST_ACCESS_CAM && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void clickSaveUser() {
        String response = validateFields();
        if (!response.equals("OK")) {
            ResourceUtil.showAlert("Advertencia", response, RegisterActivity.this, "error");
        } else {
            String nameUser = name+" "+lastname;
            if (codeGenerated == null) {
                sendEmail(email, nameUser);
                containerGeneratedCode.setVisibility(View.VISIBLE);
                txtCodeGeneratedConfirm.requestFocus();
            } else {
                String codeConfirmUser = txtCodeGeneratedConfirm.getText().toString().trim();
                if (codeConfirmUser.isEmpty()) {
                    ResourceUtil.showAlert("Advertencia", "Debes ingresar el codigo de verificacion",RegisterActivity.this, "error");
                } else {
                    if (codeConfirmUser.equals(codeGenerated)) {
                        registerUser();
                    } else {
                        ResourceUtil.showAlert("Advertencia", "El codigo de verificacion no es correcto.",RegisterActivity.this, "error");
                    }
                }
            }
        }
    }

    private void registerUser() {
        pDialog.show();
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        HashMap<String, String> params = new HashMap<>();
        params.put("idFirebase", ResourceUtil.createCodeRandom(6));
        params.put("name", name);
        params.put("lastname", lastname);
        params.put("numberAccount", numberAccount);
        params.put("phone", phone);
        params.put("status", "ACTIVO");
        params.put("address", address);
        params.put("birthDate", birthDate);
        params.put("carrera", course);
        params.put("email", email);
        params.put("password", password);

        if (image.isEmpty() || image.equals("")) {
            params.put("image", "IMAGEN");
        } else {
            params.put("image", image);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, UserApiMethods.POST_USER, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                pDialog.dismiss();
                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                ResourceUtil.showAlert("Advertencia", "Se produjo un error al registrar el usuario.",RegisterActivity.this, "error");
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                addImgPhotoUser.setImageURI(uri);
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                image = ResourceUtil.getImageBase64(bitmap);
                Log.d("GALERIA", "Entrando galeria ");
            } catch (IOException e) {
                Log.d("ERROR_PERMISOS", "onActivityResult():: "+e.getMessage());
            }
        }

        if (requestCode == PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            addImgPhotoUser.setImageBitmap(bitmap);
            image = ResourceUtil.getImageBase64(bitmap);
        }
    }

    private void showDatePickerDialog() {
        String date = txtBirthDate.getText().toString();
        Calendar c = Calendar.getInstance();
        Date dateUserSelected = null;

        if (!date.isEmpty() && date != null) {
            try {
                dateUserSelected = new SimpleDateFormat("dd/MM/yyyy").parse(date);
            } catch (ParseException e) {
                Log.d("ERR_PARSE_DATE", "Error: "+e.getMessage());
            }
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
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.list_courses , android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        spinnerListCourses.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String validateFields() {
        String response = "";

        name = txtName.getText().toString();
        lastname = txtLastname.getText().toString();
        numberAccount = txtNumberAccount.getText().toString();
        phone = txtPhone.getText().toString();
        email = txtEmail.getText().toString();
        password = txtPassword.getText().toString();
        confirmPassword = txtConfirmPassword.getText().toString();
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
        } else if(email.isEmpty()) {
            response  = "Debes ingresar tu correo electronico, es obligatorio.";
        } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            response  = "El correo electronico ingresado no es valido.";
        } else if (password.isEmpty()) {
            response  = "Debes ingresar la contraseña, es obligatorio.";
        } else if(password.length() < 6) {
            response  = "La contraseña debe tener al menos 6 caracteres.";
        } else if(!password.equals(confirmPassword)) {
            response  = "Las contraseñas no coinciden.";
        } else if(course.equals("--Seleccionar--")) {
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

    private void sendEmail(String email, String nameUser) {
        codeGenerated = ResourceUtil.createCodeRandom(6);
        String message = "Te damos la bienvenida a DOC-APP. Para garantizar la seguridad de tu cuenta, verifica tu dirección de correo electrónico. \n" +
                "Código Verificación: "+codeGenerated;
        String subject = nameUser + " Bienvenido a DOC-APP";

        JavaMailAPI javaMailAPI = new JavaMailAPI(this, email, subject, message);
        javaMailAPI.execute();
    }

}