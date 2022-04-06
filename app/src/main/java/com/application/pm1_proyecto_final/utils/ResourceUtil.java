package com.application.pm1_proyecto_final.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import android.os.Environment;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ResourceUtil {

    public static final String EMAIL = "grupomovil7321@gmail.com";
    public static final String PASSWORD = "grupo_movil2022_2";

    public static void showAlert(String title, String response, Context context, String action) {
        if (action.equals("success")) {
            new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(title)
                    .setContentText(response)
                    .show();
        } else {
            new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(title)
                    .setContentText(response)
                    .show();
        }
    }

    public static SweetAlertDialog showAlertLoading(Context context) {
        SweetAlertDialog pDialog = pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Loading ...");
            pDialog.setCancelable(true);

        return pDialog;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static boolean validateDateBirth(String dateValue) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate dateBirth = LocalDate.parse(dateValue, fmt);
        LocalDate now = LocalDate.now();
        Period result = Period.between(dateBirth, now);

        if ( result.getYears() < 15 ) {
            return false;
        }

        return true;
    }

    public static String createCodeRandom(int i) {
        String theAlphaNumericS;
        StringBuilder builder;

        theAlphaNumericS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        builder = new StringBuilder(i);

        for (int m = 0; m < i; m++) {
            // generate numeric
            int myindex = (int)(theAlphaNumericS.length() * Math.random());

            // add the characters
            builder.append(theAlphaNumericS.charAt(myindex));
        }

        return builder.toString();
    }

    public static String getImageBase64(Bitmap bitmap) {
        int previewWidth = 180;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();

        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bytes = stream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static Bitmap decodeImage(String encodedImage){
        byte[] bytes = android.util.Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static String getTypeFile(String type) {
        String typeFile = "";

        if (type.equals("msword")) {
            typeFile = "doc";
        } else if (type.equals("vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            typeFile = "docx";
        } else if (type.equals("vnd.ms-powerpoint")) {
            typeFile = "ppt";
        } else if (type.equals("vnd.openxmlformats-officedocument.presentationml.presentation")) {
            typeFile = "pptx";
        } else if (type.equals("vnd.ms-excel")) {
            typeFile = "xls";
        } else if (type.equals("vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            typeFile = "xlsx";
        } else if (type.equals("javascript")) {
            typeFile = "js";
        } else if (type.equals("java-vm")) {
            typeFile = "class";
        } else if (type.equals("x-rar-compressed")) {
            typeFile = "rar";
        } else {
            typeFile = type;
        }

        return typeFile;
    }

    public static String viewOrDownloadFile(String typeFile) {
        String response = "view";
        if (typeFile.equals("msword")) {
            response = "download";
        } else if (typeFile.equals("vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            response = "download";
        } else if (typeFile.equals("vnd.ms-powerpoint")) {
            response = "download";
        } else if (typeFile.equals("vnd.openxmlformats-officedocument.presentationml.presentation")) {
            response = "download";
        } else if (typeFile.equals("vnd.ms-excel")) {
            response = "download";
        } else if (typeFile.equals("vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            response = "download";
        } else if (typeFile.equals("javascript")) {
            response = "download";
        } else if (typeFile.equals("java-vm")) {
            response = "download";
        } else if (typeFile.equals("x-rar-compressed")) {
            response = "download";
        }
        return response;
    }

    public static String letterIcon(String name, String lastname) {
        String firstLetter =  (name.charAt(0)+"").toUpperCase();
        String secondLetter =  (lastname.charAt(0)+"").toUpperCase();
        String info = firstLetter+secondLetter;
        return info;
    }
}
