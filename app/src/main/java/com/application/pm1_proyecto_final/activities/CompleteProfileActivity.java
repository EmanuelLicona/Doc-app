package com.application.pm1_proyecto_final.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.Spinner;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.providers.AuthProvider;
import com.application.pm1_proyecto_final.providers.ImageProvider;
import com.application.pm1_proyecto_final.providers.UsersProvider;
import com.application.pm1_proyecto_final.utils.FileUtil;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CompleteProfileActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    TextInputEditText txtName, txtLastname, txtNumberAccount, txtPhone, txtCourse, txtBirthDate, txtAddress;
    Spinner spinnerListCourses;
    ImageView btnShowDialogDate, addImgPhotoUser;
    File imageFile;
    Button btnRegister;

    ImageProvider imageProvider;
    UsersProvider usersProvider;
    AuthProvider authProvider;
    SweetAlertDialog pDialog;
    AlertDialog.Builder pBuilderSelector;

    CharSequence options[];
    private final int GALLERY_REQUEST_CODE = 100;
    private final int PHOTO_REQUEST_CODE = 200;
    String birthDate = "", name = "", lastname = "", numberAccount = "", phone = "", address = "", course = "";

    // Variables tomar foto
    String mAbsolutePhotoPath;
    String mPhotoPath;
    File mPhotoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        imageProvider = new ImageProvider();
        usersProvider = new UsersProvider();
        authProvider = new AuthProvider();
        pDialog = ResourceUtil.showAlertLoading(CompleteProfileActivity.this);
        pBuilderSelector = new AlertDialog.Builder(this);
        pBuilderSelector.setTitle("Seleccione una opción");
        options = new CharSequence[]{"Imagen de galería", "Tomar foto"};

        spinnerListCourses = (Spinner) findViewById(R.id.spinnerListCourses);
        btnShowDialogDate = (ImageView) findViewById(R.id.btnShowDialogDate);
        addImgPhotoUser = (ImageView) findViewById(R.id.addImgPhotoUser);

        txtName = (TextInputEditText) findViewById(R.id.txtName);
        txtLastname = (TextInputEditText) findViewById(R.id.txtLastname);
        txtNumberAccount = (TextInputEditText) findViewById(R.id.txtNumberCount);
        txtPhone = (TextInputEditText) findViewById(R.id.txtPhone);
        txtAddress = (TextInputEditText) findViewById(R.id.txtAddress);
        txtBirthDate = (TextInputEditText) findViewById(R.id.txtBirthDate);

        btnRegister = (Button) findViewById(R.id.btnRegister);

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
                clickRegisterUser();
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
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createPhotoFile();
            } catch (Exception e) {
                ResourceUtil.showAlert("Advertencia", "Se produjo un error con el archivo de imagen.", this, "error");
                Log.d("ERROR_PHOTOIMAGE", "takePhoto()::RegisterActivity "+e.getMessage());
            }

            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(this, "com.application.pm1_proyecto_final.activities.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, PHOTO_REQUEST_CODE);
            }

        }
    }

    private File createPhotoFile() throws IOException {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Date now = new Date();
        String fileName = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH).format(now);
        File photoFile = File.createTempFile(fileName +"_PHOTO", ".jpg", storageDir);
        mPhotoPath = "file:" + photoFile.getAbsolutePath();
        mAbsolutePhotoPath = photoFile.getAbsolutePath();

        return photoFile;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void clickRegisterUser() {
        String response = validateFields();
        if (!response.equals("OK")) {
            ResourceUtil.showAlert("Advertencia", response, CompleteProfileActivity.this, "error");
        } else {
            usersProvider.getUserByField(numberAccount, "numberAccount").addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> taskValidateAccount) {
                    if (taskValidateAccount.isSuccessful()) {
                        if (taskValidateAccount.getResult().size() >= 1) {
                            ResourceUtil.showAlert("Advertencia", "El numero de cuenta UTH ingresado pertenece a otro estudiante", CompleteProfileActivity.this, "error");
                        }else {
                            if (imageFile == null) { //Tomo la foto
                                update(mPhotoFile);
                            } else if (mPhotoPath == null) { // De galeria
                                update(imageFile);
                            }

                        }
                    } else {
                        Log.d("ERROR_VERIFY_ACCOUNT", "Error getting documents by numberAccount: ", taskValidateAccount.getException());
                    }
                }
            });
        }

    }

    private void update(File imgFile) {
        pDialog.show();

        User user = new User();
        user.setId(authProvider.getUid());
        user.setName(name);
        user.setLastname(lastname);
        user.setCarrera(course);
        user.setPhone(phone);
        user.setNumberAccount(numberAccount);
        user.setAddress(address);
        user.setStatus("ACTIVO");
        user.setBirthDate(birthDate);

        if ( imgFile == null ) {
            user.setImage("");

            usersProvider.update(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> taskUser) {
                    pDialog.dismiss();
                    if (taskUser.isSuccessful()) {
                        Intent intent = new Intent(CompleteProfileActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        ResourceUtil.showAlert("Advertencia", "El usuario no se pudo registrar.", CompleteProfileActivity.this, "error");
                    }
                }

            });
        } else {
            imageProvider.save(CompleteProfileActivity.this, imgFile).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> taskImage) {
                    if(taskImage.isSuccessful()) {
                        imageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String urlImage = uri.toString();
                                user.setImage(urlImage);

                                //GUARDANDO EN BD
                                usersProvider.update(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> taskUser) {
                                        pDialog.dismiss();
                                        if (taskUser.isSuccessful()) {
                                            Intent intent = new Intent(CompleteProfileActivity.this, HomeActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }else {
                                            ResourceUtil.showAlert("Advertencia", "No se pudo completar la informacion del estudiante.", CompleteProfileActivity.this, "error");
                                        }
                                    }
                                });
                            }
                        });
                    } else {
                        pDialog.dismiss();
                        ResourceUtil.showAlert("Advertencia", "No se pudo completar la informacion del estudiante, error al insertar la imagen.", CompleteProfileActivity.this, "error");
                    }
                }
            });
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                mPhotoFile = null;
                imageFile = FileUtil.from(this, data.getData());
                addImgPhotoUser.setImageBitmap(BitmapFactory.decodeFile(imageFile.getAbsolutePath())); // Imprimiendo IMG
            } catch (Exception e) {
                Log.d("IMG_ERROR", "Se produjo un error: "+e.getMessage());
                ResourceUtil.showAlert("Advertencia", "Se produjo un error al cargar la imagen: "+e.getMessage(), CompleteProfileActivity.this, "error");
            }
        }

        // SELECCIONANDO TOMAR FOTO
        if (requestCode == PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            imageFile = null;
            mPhotoFile = new File(mAbsolutePhotoPath);
            Picasso.with(CompleteProfileActivity.this).load(mPhotoPath).into(addImgPhotoUser);
        }
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int m, int dayOfMonth) {
        int month = m + 1;
        String mo = "";
        if (month >= 1 && month <= 9) {
            mo = "0"+month;
        } else {
            mo = String.valueOf(month);
        }

        String date = dayOfMonth + "/" + mo + "/" +year;
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