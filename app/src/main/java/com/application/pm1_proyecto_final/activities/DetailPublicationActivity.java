package com.application.pm1_proyecto_final.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.adapters.CommentAdapter;
import com.application.pm1_proyecto_final.api.UserApiMethods;
import com.application.pm1_proyecto_final.databinding.ActivityDetailPublicationBinding;
import com.application.pm1_proyecto_final.models.Comment;
import com.application.pm1_proyecto_final.models.Publication;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.providers.CommentsProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.RelativeTime;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DetailPublicationActivity extends AppCompatActivity {

    private ActivityDetailPublicationBinding binding;
    private Publication publicationReceiver;
    private CommentsProvider commentsProvider;
    private String mPublicationId;
    private PreferencesManager preferencesManager;
    private CommentAdapter commentAdapter;
    private List<User> userListApi;
    private AlertDialog.Builder pBuilderSelector;
    private CharSequence options[];
    private static final int REQUEST_PERMISSION_STORAGE = 300;
    private String pathUri = "", typeFile = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailPublicationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userListApi = new ArrayList<>();
        commentsProvider = new CommentsProvider();
        preferencesManager = new PreferencesManager(DetailPublicationActivity.this);
        pBuilderSelector = new AlertDialog.Builder(this);
        pBuilderSelector.setTitle("Seleccione una opción");
        options = new CharSequence[]{"Visualizar Archivo", "Descargar Archivo"};

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DetailPublicationActivity.this);
        binding.recyclerViewComments.setLayoutManager(linearLayoutManager);

        loadReceiverDetails();
        setListeners();
    }

    private void setListeners() {
        binding.btnBackDetailPublication.setOnClickListener(view -> { finish(); });
        binding.btnShowProfile.setOnClickListener(view -> {
            Intent intent = new Intent(DetailPublicationActivity.this, UserProfileActivity.class);
            intent.putExtra(Constants.KEY_USER_ID, publicationReceiver.getSenderId());
            startActivity(intent);
        });
        binding.btnNewComment.setOnClickListener(view -> {
            showDialogComment();
        });

        binding.imagePublicationDetail.setOnClickListener(view -> {
            pBuilderSelector.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0) {
                        viewFile(publicationReceiver);
                    } else if (i == 1) {
                        downloadFile(publicationReceiver);
                    }
                }
            });
            pBuilderSelector.show();
        });
    }

    private void showDialogComment() {
        AlertDialog.Builder alert = new AlertDialog.Builder(DetailPublicationActivity.this);
        alert.setTitle("COMENTARIO");
        alert.setMessage("Ingresa tu comentario");

        final EditText editText = new EditText(DetailPublicationActivity.this);
        editText.setHint("Texto");

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(36, 0, 36, 36);
        editText.setLayoutParams(params);
        RelativeLayout container = new RelativeLayout(DetailPublicationActivity.this);
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        container.setLayoutParams(relativeParams);
        container.addView(editText);
        alert.setView(container);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String value = editText.getText().toString();
                if (!value.isEmpty()) {
                    createComment(value);
                }
                else {
                    ResourceUtil.showAlert("Advertencia", "Debe ingresar el comentario",DetailPublicationActivity.this, "error");
                }
            }
        });

        alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        alert.show();
    }

    private void createComment(String value) {
        Comment comment = new Comment();
        comment.setComment(value);
        comment.setIdPost(mPublicationId);
        comment.setIdUser(preferencesManager.getString(Constants.KEY_USER_ID));
        comment.setTimestamp(new Date().getTime());

        commentsProvider.createComment(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(DetailPublicationActivity.this, "Comentario agregado", Toast.LENGTH_SHORT).show();
                } else  {
                    Toast.makeText(DetailPublicationActivity.this, "Se produjo un error al crear el comentario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadReceiverDetails(){
        publicationReceiver = (Publication) getIntent().getSerializableExtra("Publication");
        mPublicationId = getIntent().getStringExtra("publicationId");

        try{
            if (!publicationReceiver.getImage().isEmpty() && !publicationReceiver.getImage().equals("IMAGE")) {
                binding.circleImageProfile.setImageBitmap(ResourceUtil.decodeImage(publicationReceiver.getImage()));
            }
        }catch (Exception e){
            binding.circleImageProfile.setImageBitmap(null);
        }

        viewImageByTypeFile(publicationReceiver.getType().split("/"), publicationReceiver);

        binding.textViewUsername.setText(publicationReceiver.getNameUserPublication());
        binding.textViewTitle.setText(publicationReceiver.getTitle());
        binding.textViewDescription.setText(publicationReceiver.getDescription());
        String relativeTime = RelativeTime.getTimeAgo(publicationReceiver.getTimestamp(), this);
        binding.textViewRelativeTime.setText(relativeTime);
    }

    private void viewImageByTypeFile(String[] extensionFile, Publication publication) {
        if (publication.getType().equals("application/pdf")) {
            binding.imagePublicationDetail.setImageResource(R.drawable.pdf_publication);
        } else if(extensionFile[0].equals("image")) {
            Picasso.with(DetailPublicationActivity.this).load(publicationReceiver.getPath()).into(binding.imagePublicationDetail);
        } else if (extensionFile[0].equals("audio")) {
            binding.imagePublicationDetail.setImageResource(R.drawable.audio_publication);
        } else if(extensionFile[0].equals("video")) {
            binding.imagePublicationDetail.setImageResource(R.drawable.video_publication);
        } else if(extensionFile[1].equals("msword") || extensionFile[1].equals("vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            binding.imagePublicationDetail.setImageResource(R.drawable.word_image);
        } else if(extensionFile[1].equals("onenote")) {
            binding.imagePublicationDetail.setImageResource(R.drawable.onenote_image);
        } else if(extensionFile[1].equals("vnd.ms-powerpoint") || extensionFile[1].equals("vnd.openxmlformats-officedocument.presentationml.presentation")) {
            binding.imagePublicationDetail.setImageResource(R.drawable.power_point_image);
        } else if(extensionFile[1].equals("vnd.ms-excel") || extensionFile[1].equals("vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            binding.imagePublicationDetail.setImageResource(R.drawable.excel_image);
        } else if(extensionFile[1].equals("plain")) {
            binding.imagePublicationDetail.setImageResource(R.drawable.text_image);
        } else if(extensionFile[1].equals("javascript")) {
            binding.imagePublicationDetail.setImageResource(R.drawable.javascript_image);
        } else if(extensionFile[1].equals("json")) {
            binding.imagePublicationDetail.setImageResource(R.drawable.json_image);
        } else if(extensionFile[1].equals("x-java-source,java") || extensionFile[1].equals("java-vm")) {
            binding.imagePublicationDetail.setImageResource(R.drawable.java_image);
        } else if(extensionFile[1].equals("zip")) {
            binding.imagePublicationDetail.setImageResource(R.drawable.zip_image);
        } else if(extensionFile[1].equals("x-rar-compressed")) {
            binding.imagePublicationDetail.setImageResource(R.drawable.winrar_image);
        } else if(extensionFile[1].equals("html")) {
            binding.imagePublicationDetail.setImageResource(R.drawable.html_image);
        } else {
            binding.imagePublicationDetail.setImageResource(R.drawable.text_image);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        getAllUsers();
    }

    private void getAllUsers() {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, UserApiMethods.GET_USER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray arrayUsers = jsonObject.getJSONArray("data");

                    for(int i = 0; i < arrayUsers.length(); i++) {
                        JSONObject rowUser = arrayUsers.getJSONObject(i);
                        User user = new User();
                        user.setId(rowUser.getString("id"));
                        user.setImage(rowUser.getString("image"));
                        user.setName(rowUser.getString("name"));
                        user.setLastname(rowUser.getString("lastname"));
                        userListApi.add(user);
                    }
                    getAllComment();
                }
                catch (JSONException ex) {
                    ResourceUtil.showAlert("Advertencia", "Se produjo un error al obtener la informacion de los usuarios que tiene publicaciones.", DetailPublicationActivity.this, "error");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ResourceUtil.showAlert("Advertencia", "Se produjo un error al obtener la informacion de los usuarios que tiene publicaciones.", DetailPublicationActivity.this, "error");
            }
        });
        queue.add(stringRequest);
    }

    private void getAllComment() {
        Query query = commentsProvider.getAll(mPublicationId);
        FirestoreRecyclerOptions<Comment> options = new FirestoreRecyclerOptions.Builder<Comment>()
                .setQuery(query, Comment.class)
                .build();
        commentAdapter = new CommentAdapter(options, DetailPublicationActivity.this, (ArrayList<User>) userListApi);
        commentAdapter.notifyDataSetChanged();
        binding.recyclerViewComments.setAdapter(commentAdapter);
        commentAdapter.startListening();
    }

    private void viewFile(Publication publication) {
        try {
            String typeFile = publication.getType();
            String extensionFile = typeFile.split("/")[1];

            if (ResourceUtil.viewOrDownloadFile(extensionFile).equals("download")) {
                downloadFile(publication);
                return;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(publication.getPath()), typeFile);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.startActivity(intent.createChooser(intent, "Elija la aplicación para abrir el documento"));
        } catch (ActivityNotFoundException e) {
            ResourceUtil.showAlert("Advertencia", "Se produjo un error al visualizar el archivo.", this, "error");
        }
    }

    private void downloadFile(Publication publication) {
        pathUri = publication.getPath();
        typeFile = ResourceUtil.getTypeFile(publication.getType().split("/")[1]);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions(permissions, REQUEST_PERMISSION_STORAGE);
                } else {
                    startDownloadFile();
                }
            }
        } else {
            startDownloadFile();
        }

    }

    private void startDownloadFile() {
        if (!pathUri.isEmpty() && !typeFile.isEmpty()) {
            Toast.makeText(this, "Descargando el archivo", Toast.LENGTH_SHORT).show();
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pathUri));
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            request.setTitle("Descargar");
            request.setDescription("Descargando archivo....");
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "" + System.currentTimeMillis() + "." + typeFile);

            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            manager.enqueue(request);
        } else {
            ResourceUtil.showAlert("Advertencia", "Se produjo un error al descargar el archivo", this, "error");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_STORAGE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startDownloadFile();
        } else {
            Toast.makeText(this, "Permiso denegado para guardar el archivo.", Toast.LENGTH_SHORT).show();
        }
    }
}