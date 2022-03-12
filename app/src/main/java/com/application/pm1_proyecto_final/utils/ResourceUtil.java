package com.application.pm1_proyecto_final.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ResourceUtil {

    public static final String EMAIL = "grupomovil7321@gmail.com";
    public static final String PASSWORD = "Movilgrupo7321";

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

}
