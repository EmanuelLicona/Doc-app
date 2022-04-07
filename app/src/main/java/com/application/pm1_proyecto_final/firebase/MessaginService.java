package com.application.pm1_proyecto_final.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.activities.MainActivity;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.TokenPreference;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MessaginService extends FirebaseMessagingService {

    PreferencesManager preferencesManager;
    TokenPreference tokenPreference;


    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

       tokenPreference = new TokenPreference(getApplicationContext());
       tokenPreference.putString(Constants.KEY_FCM_TOKEN, token);

        Log.d("FCM", "onNewToken: "+token);

    }

//    Este metodo es cuando resivo notificacion cuando no estoy en segundo plano
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
//        Looper.prepare();
//
//        new Handler().post(() -> {
//
//            Toast.makeText(MessaginService.this, "onMessageReceived: "+ remoteMessage.getNotification().getBody().toString(), Toast.LENGTH_SHORT).show();
//        });
//
//        Looper.loop();

        preferencesManager = new PreferencesManager(getApplicationContext());


        if(!preferencesManager.getString(Constants.KEY_USER_ID).equals(remoteMessage.getData().get("senderId"))){
            String from = remoteMessage.getFrom();

            if(remoteMessage.getData().size()>0){

                String titulo = remoteMessage.getData().get("titulo");
                String detalle = remoteMessage.getData().get("detalle");

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    mostrarNotificacion(titulo, detalle);
                }

                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
                    mostrarNotificacionMenorOreo(titulo, detalle);
                }

            }
        }

    }

    private void mostrarNotificacionMenorOreo(String titulo, String detalle) {
        String id = "mensaje";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, id);

        builder.setSmallIcon(R.drawable.img_logo)
                .setContentTitle(titulo)
                .setContentText(detalle)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        detalle
                ))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(clickNoti())
                .setAutoCancel(true);

        Random random = new Random();
        int idNotify = random.nextInt(8000);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(idNotify, builder.build());

    }

    private void mostrarNotificacion(String titulo, String detalle) {

        String id = "mensaje";

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, id);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel nc = new NotificationChannel(id, "nuevo", NotificationManager.IMPORTANCE_HIGH);
            nc.setShowBadge(true);
            assert nm != null;
            nm.createNotificationChannel(nc);
        }

        builder.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(titulo)
                .setSmallIcon(R.drawable.img_logo)
                .setContentText(detalle)
                .setContentIntent(clickNoti())
                .setContentInfo("nuevo");

        Random random = new Random();
        int idNotify = random.nextInt(8000);

        assert nm != null;
        nm.notify(idNotify, builder.build());


    }

    private PendingIntent clickNoti() {
        Intent nf = new Intent(getApplicationContext(), MainActivity.class);
        nf.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        return PendingIntent.getActivity(this, 0, nf, PendingIntent.FLAG_CANCEL_CURRENT);
    }

}
