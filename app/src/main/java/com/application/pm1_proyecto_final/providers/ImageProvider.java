package com.application.pm1_proyecto_final.providers;

import android.content.Context;

import com.application.pm1_proyecto_final.utils.CompressorBitmapImage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageProvider {
    StorageReference mStorage;

    public ImageProvider() {
        mStorage = FirebaseStorage.getInstance().getReference();
    }

    public UploadTask save(Context context, File file) {
        byte[] imageByte = CompressorBitmapImage.getImage(context, file.getPath(), 500,500);
        Date now = new Date();
        String fileName = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH).format(now);
        StorageReference storage = mStorage = FirebaseStorage.getInstance().getReference().child(fileName+ ".jpg");
        mStorage = storage; //Sirve para luego obtener la URL de la img y guardarla en la BD
        UploadTask task = storage.putBytes(imageByte);
        return task;
    }

    public StorageReference getStorage() {
        return mStorage;
    }
}
