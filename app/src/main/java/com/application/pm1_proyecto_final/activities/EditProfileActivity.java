package com.application.pm1_proyecto_final.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.application.pm1_proyecto_final.Fragments.FragmentPerfil;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    ImageView btnShowDialogDate, imageViewCover;
    Spinner spinnerListCourses;
    TextInputEditText txtBirthDate, txtName, txtLastname, txtNumberAccount, txtPhone, txtAddress;
    EditText txtNumberAccountCurrent;
    String birthDate = "", name = "", lastname = "", numberAccount = "", numberAccountCurrent = "", phone = "",  address = "", course = "", mImageProfile = "", mImageCover = "";

    AlertDialog.Builder pBuilderSelector;
    SweetAlertDialog pDialog;
    Button btnEditProfile;
    CircleImageView btnBack, circleImageViewProfile;

    CharSequence options[];
    private final int GALLERY_REQUEST_CODE_PROFILE = 100;
    private final int GALLERY_REQUEST_CODE_COVER = 150;
    private final int PHOTO_REQUEST_CODE_PROFILE = 200;
    private final int PHOTO_REQUEST_CODE_COVER = 250;

    // FOTO 1
    String mAbsolutePhotoPath;
    String mPhotoPath;
    File mPhotoFile;

    // FOTO 2
    String mAbsolutePhotoPath2;
    String mPhotoPath2;
    File mPhotoFile2;

    File mImageFile;
    File mImageFile2;

    ImageProvider imageProvider;
    UsersProvider usersProvider;
    AuthProvider authProvider;
    ArrayAdapter<CharSequence> adapter;

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

        pDialog = ResourceUtil.showAlertLoading(this);
        pBuilderSelector = new AlertDialog.Builder(this);
        pBuilderSelector.setTitle("Seleccione una opción");
        options = new CharSequence[]{"Imagen de galería", "Tomar foto"};

        imageProvider = new ImageProvider();
        usersProvider = new UsersProvider();
        authProvider = new AuthProvider();

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

            if (!numberAccountCurrent.equals(numberAccount)) {
                usersProvider.getUserByField(numberAccount, "numberAccount").addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> taskValidateAccount) {
                        if (taskValidateAccount.isSuccessful()) {

                            if (taskValidateAccount.getResult().size() >= 1) {
                                ResourceUtil.showAlert("Advertencia", "El numero de cuenta UTH ingresado pertenece a otro estudiante", EditProfileActivity.this, "error");
                            }else {
                                prepareUpdate();
                            }

                        } else {
                            Log.d("ERROR_VERIFY_ACCOUNT", "Error getting documents by numberAccount: ", taskValidateAccount.getException());
                        }
                    }
                });
            } else {
                prepareUpdate();
            }

        }else {
            ResourceUtil.showAlert("Advertencia", response, this, "error");
        }
    }

    private void prepareUpdate() {
        if (mImageFile != null && mImageFile2 != null) { // AMBAS IMAGENES DE GALERIA
            saveImageCoverAndProfile(mImageFile, mImageFile2);

        } else if (mPhotoFile != null && mPhotoFile2 != null) { // AMBAS IMAGENES TOMADAS CON LA CAMARA
            saveImageCoverAndProfile(mPhotoFile, mPhotoFile2);

        } else if (mImageFile != null && mPhotoFile2 != null) { // UNA DE GALERIA Y OTRA DE LA CAMARA
            saveImageCoverAndProfile(mImageFile, mPhotoFile2);

        } else if (mPhotoFile != null && mImageFile2 != null) { // UNA TOMADA Y OTRA DE LA GALERIA
            saveImageCoverAndProfile(mPhotoFile, mImageFile2);

        } else if(mPhotoFile != null ) {
            saveImage(mPhotoFile, true); // mPhotoFile = IMG PROFILE

        } else if(mPhotoFile2 != null ) { // mPhotoFile2 = IMG COVER
            saveImage(mPhotoFile2, false);

        } else if(mImageFile != null ) { // mImageFile = IMG PROFILE
            saveImage(mImageFile, true);

        } else if(mImageFile2 != null ) { // mImageFile = IMG PROFILE
            saveImage(mImageFile2, false); // mImageFile2 = IMG COVER

        } else {
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
            user.setId(authProvider.getUid());

            updateInfoUser(user);
        }
    }

    private void updateInfoUser(User user) {
        if (pDialog.isShowing()) {
            pDialog.show();
        }

        usersProvider.update(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pDialog.dismiss();
                if (task.isSuccessful()) {
                    finish();
//                    ResourceUtil.showAlert("Confirmación", "Usuario actualizado correctamente.",EditProfileActivity.this, "success");
                }else {
                    ResourceUtil.showAlert("Confirmación", "Error al actualizar el usuario.",EditProfileActivity.this, "error");
                }
            }
        });

    }

    private void saveImage(File image, boolean isProfileImage) {
        pDialog.show();
        imageProvider.save(EditProfileActivity.this, image).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    imageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String url = uri.toString();
                            User user = new User();
                            user.setName(name);
                            user.setLastname(lastname);
                            user.setPhone(phone);
                            user.setNumberAccount(numberAccount);
                            user.setAddress(address);
                            user.setCarrera(course);
                            user.setBirthDate(birthDate);
                            user.setId(authProvider.getUid());

                            if (isProfileImage) {
                                user.setImage(url);
                                user.setImageCover(mImageCover);
                            } else {
                                user.setImageCover(url);
                                user.setImage(mImageProfile);
                            }

                            updateInfoUser(user);
                        }
                    });
                } else {
                    pDialog.dismiss();
                    Toast.makeText(EditProfileActivity.this, "La imagen no se pudo almacenar.", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void saveImageCoverAndProfile(File imageFile1, File imageFile2) {
        pDialog.show();
        imageProvider.save(EditProfileActivity.this, imageFile1).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    imageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String urlProfile = uri.toString(); // Obteniendo la URL de la imagen guardada

                            imageProvider.save(EditProfileActivity.this, imageFile2).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> taskImage2) {
                                    if (taskImage2.isSuccessful()) {
                                        imageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri2) {
                                                String urlCover = uri2.toString();
                                                User user = new User();
                                                user.setName(name);
                                                user.setLastname(lastname);
                                                user.setPhone(phone);
                                                user.setNumberAccount(numberAccount);
                                                user.setAddress(address);
                                                user.setCarrera(course);
                                                user.setBirthDate(birthDate);
                                                user.setId(authProvider.getUid());
                                                user.setImageCover(urlCover);
                                                user.setImage(urlProfile);

                                                updateInfoUser(user);
                                            }
                                        });
                                    } else {
                                        Toast.makeText(EditProfileActivity.this, "La imagen de fondo no se pudo almacenar.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                    });
                } else {
                    pDialog.dismiss();
                    Toast.makeText(EditProfileActivity.this, "La imagen de perfil no se pudo almacenar.", Toast.LENGTH_LONG).show();
                }

            }
        });
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
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) { // Si selecciono algo
            File photoFile = null;
            try {
                photoFile = createPhotoFile(requestCode);
            }catch (Exception e) {
                Log.d("ERROR_PHOTO_FILE", "takePhoto():: Error: "+e.getMessage());
            }

            if (photoFile != null) {
                Uri photoUrl = FileProvider.getUriForFile(EditProfileActivity.this, "com.application.pm1_proyecto_final.activities.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUrl);
                startActivityForResult(takePictureIntent, requestCode);
            }

        }
    }

    private void openGallery(int requestCode) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, requestCode);
    }

    private File createPhotoFile(int requestCode) throws IOException {
        String fileName = "IMG_USER_"+System.currentTimeMillis();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File photoFile = File.createTempFile(
                fileName,
                ".jpg",
                storageDir
        );

        if (requestCode == PHOTO_REQUEST_CODE_PROFILE) {
            mPhotoPath = "file:"+photoFile.getAbsolutePath();
            mAbsolutePhotoPath = photoFile.getAbsolutePath();
        } else if(requestCode == PHOTO_REQUEST_CODE_COVER) {
            mPhotoPath2 = "file:"+photoFile.getAbsolutePath();
            mAbsolutePhotoPath2 = photoFile.getAbsolutePath();
        }

        return photoFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // SELECCION IMAGEN DE LA GALERIA
        if (requestCode == GALLERY_REQUEST_CODE_PROFILE && resultCode == RESULT_OK) {
            try {
                mPhotoFile = null;
                mImageFile = FileUtil.from(this, data.getData());
                circleImageViewProfile.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));

            } catch (Exception e) {
                Log.d("IMGERROR", "Se produjo un error: "+e.getMessage());
                Toast.makeText(this, "Se produjo un error: "+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == GALLERY_REQUEST_CODE_COVER && resultCode == RESULT_OK) {
            try {
                mPhotoFile2 = null;
                mImageFile2 = FileUtil.from(this, data.getData());
                imageViewCover.setImageBitmap(BitmapFactory.decodeFile(mImageFile2.getAbsolutePath()));

            } catch (Exception e) {
                Log.d("IMG_ERROR", "Se produjo un error: "+e.getMessage());
                Toast.makeText(this, "Se produjo un error "+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        // SELECCION TOMAR FOTOGRAFIA
        if (requestCode == PHOTO_REQUEST_CODE_PROFILE && resultCode == RESULT_OK) {
            mImageFile = null;
            mPhotoFile = new File(mAbsolutePhotoPath);
            Picasso.with(EditProfileActivity.this).load(mPhotoPath).into(circleImageViewProfile);
        }
        if (requestCode == PHOTO_REQUEST_CODE_COVER && resultCode == RESULT_OK) {
            mImageFile2 = null;
            mPhotoFile2 = new File(mAbsolutePhotoPath2);
            Picasso.with(EditProfileActivity.this).load(mPhotoPath2).into(imageViewCover);
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
        usersProvider.getUser(authProvider.getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {

                    if (documentSnapshot.contains("name")) {
                        name = documentSnapshot.getString("name");
                        txtName.setText(name);
                    }
                    if (documentSnapshot.contains("lastname")) {
                        lastname = documentSnapshot.getString("lastname");
                        txtLastname.setText(lastname);
                    }
                    if (documentSnapshot.contains("phone")) {
                        phone = documentSnapshot.getString("phone");
                        txtPhone.setText(phone);
                    }
                    if (documentSnapshot.contains("address")) {
                        address = documentSnapshot.getString("address");
                        txtAddress.setText(address);
                    }
                    if (documentSnapshot.contains("numberAccount")) {
                        numberAccount = documentSnapshot.getString("numberAccount");
                        txtNumberAccount.setText(numberAccount);
                        txtNumberAccountCurrent.setText(numberAccount);
                    }
                    if (documentSnapshot.contains("carrera")) {
                        String carrera = documentSnapshot.getString("carrera");
                        spinnerListCourses.setSelection(adapter.getPosition(carrera));
                    }
                    if (documentSnapshot.contains("birthDate")) {
                        birthDate = documentSnapshot.getString("birthDate");
                        txtBirthDate.setText(birthDate);
                    }
                    if (documentSnapshot.contains("image")){
                        mImageProfile = documentSnapshot.getString("image");
                        if (mImageProfile != null && !mImageProfile.isEmpty()) {
                            Picasso.with(EditProfileActivity.this).load(mImageProfile).into(circleImageViewProfile);
                        }
                    }
                    if (documentSnapshot.contains("image_cover")){
                        mImageCover = documentSnapshot.getString("image_cover");
                        if (mImageCover != null && !mImageCover.isEmpty()) {
                            Picasso.with(EditProfileActivity.this).load(mImageCover).into(imageViewCover);
                        }
                    }
                }

            }
        });
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