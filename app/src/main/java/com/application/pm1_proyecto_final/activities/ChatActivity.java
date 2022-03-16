package com.application.pm1_proyecto_final.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.adapters.ChatAdapter;
import com.application.pm1_proyecto_final.listeners.Chatlistener;
import com.application.pm1_proyecto_final.models.ChatMessage;
import com.application.pm1_proyecto_final.models.Group;
import com.application.pm1_proyecto_final.providers.GroupsProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ChatActivity extends AppCompatActivity implements Chatlistener {

    Group reseiverGroup;

    AppCompatImageView imageViewInfo, imageViewBack;

    TextView textViewTitle;

    PreferencesManager preferencesManager;

    List<ChatMessage> chatMessages;

    ChatAdapter chatAdapter;

    FirebaseFirestore database;

    FloatingActionButton btnNewFile;

    RecyclerView chatRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        init();
        setListeners();

        loadReceiverDetails();

        listenMessages();
    }

    private void init(){
        reseiverGroup = null;

//        positionGeneral = -1;

        preferencesManager = new PreferencesManager(getApplicationContext());

        database = FirebaseFirestore.getInstance();

        chatRecyclerView = (RecyclerView) findViewById(R.id.chatRecyclerView);

        imageViewInfo = (AppCompatImageView) findViewById(R.id.imageInfoChat);
        imageViewBack = (AppCompatImageView) findViewById(R.id.btnChatBack);
        textViewTitle = (TextView) findViewById(R.id.groupTitleChat);

        btnNewFile = (FloatingActionButton) findViewById(R.id.btnNewFile);




        chatMessages = new ArrayList<>();

        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEndodedString(""),
                preferencesManager.getString(Constants.KEY_USER_ID),
                this
        );

        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void setListeners(){
        imageViewBack.setOnClickListener(v -> onBackPressed());
        imageViewInfo.setOnClickListener(view -> moveToInfo());

        btnNewFile.setOnClickListener(v -> sendMessage());
    }

    private void moveToInfo() {
        Intent intent = new Intent(getApplicationContext(), InfoGroupActivity.class);
        intent.putExtra(GroupsProvider.NAME_COLLECTION, reseiverGroup);
        startActivity(intent);
    }

    private void loadReceiverDetails(){
        reseiverGroup = (Group) getIntent().getSerializableExtra(GroupsProvider.NAME_COLLECTION);

        textViewTitle.setText(reseiverGroup.getTitle());
    }

    /*
    *
    * */

    private void sendMessage(){

        int min = 1;
        int max = 10;

        Random random = new Random();

        int value = random.nextInt(max + min) + min;

        HashMap<String, Object> mensaje = new HashMap<>();

        int position = (chatMessages.size()==0)?0:chatMessages.size();

        mensaje.put(Constants.KEY_SENDER_ID, preferencesManager.getString(Constants.KEY_USER_ID));
        mensaje.put(Constants.KEY_GROUP_ID, reseiverGroup.getId());
        mensaje.put(Constants.KEY_MESSAGE, "Mensage: " + value);
        mensaje.put(Constants.KEY_STATUS_MESSAGE, ChatMessage.STATUS_SENT);
        mensaje.put(Constants.KEY_POSITION_MESSAGE, position+"");
        mensaje.put(Constants.KEY_TIMESTAMP, new Date());

        database.collection(Constants.KEY_COLLECTION_CHAT).add(mensaje)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
//                        Toast.makeText(this, "Si", Toast.LENGTH_SHORT).show();
                    }else{
//                        Toast.makeText(this, "No", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });


    }

    private void deleteMessage(ChatMessage chatMessage, int position){

        HashMap<String, Object> mensaje = new HashMap<>();


        mensaje.put(Constants.KEY_MESSAGE, "Mensage eliminado");
        mensaje.put(Constants.KEY_STATUS_MESSAGE, ChatMessage.STATUS_DELETE);

//        positionGeneral = position;

        database.collection(Constants.KEY_COLLECTION_CHAT).document(chatMessage.idFirebase)
                .update(mensaje)
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful()){
                        ResourceUtil.showAlert("Mensaje", "Publicacion eliminado correctamente", ChatActivity.this, "success");
                    }else{
                        ResourceUtil.showAlert("Error", "La publicacion no se pudo eliminar", ChatActivity.this, "error");
                    }


                })
                .addOnFailureListener(e -> {
                    ResourceUtil.showAlert("Error", "El mensaje no se pudo eliminar", ChatActivity.this, "error");
//                    positionGeneral=-1;
                });
    }



    private Bitmap getBitmapFromEndodedString(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


    private String getReadableDateTime(Date date){

        return new SimpleDateFormat("MM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }


    private void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
//                .whereEqualTo(Constants.KEY_STATUS_MESSAGE, ChatMessage.STATUS_SENT)
                .whereEqualTo(Constants.KEY_GROUP_ID, reseiverGroup.getId())
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {

        if(error!=null){
            return ;
        }

        if(value != null){
            int count = chatMessages.size();
            int position = -1;

            for (DocumentChange documentChange: value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();

                    chatMessage.idFirebase = documentChange.getDocument().getId();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.groupId = documentChange.getDocument().getString(Constants.KEY_GROUP_ID);
                    chatMessage.datatime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.status = documentChange.getDocument().getString(Constants.KEY_STATUS_MESSAGE);
                    chatMessage.position = documentChange.getDocument().getString(Constants.KEY_POSITION_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);

                    chatMessages.add(chatMessage);
                }

                if(documentChange.getType() == DocumentChange.Type.MODIFIED){

//                    Toast.makeText(this, documentChange.getDocument().getString(""), Toast.LENGTH_SHORT).show();
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.idFirebase = documentChange.getDocument().getId();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.groupId = documentChange.getDocument().getString(Constants.KEY_GROUP_ID);
                    chatMessage.datatime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.status = documentChange.getDocument().getString(Constants.KEY_STATUS_MESSAGE);
                    chatMessage.position = documentChange.getDocument().getString(Constants.KEY_POSITION_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);

                    position = Integer.parseInt(chatMessage.position);

                    chatMessages.set(position, chatMessage);

                    count = -1;
                }

            }

            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));

            if(count == 0){
                chatAdapter.notifyDataSetChanged();

            }else if(count == -1){

                chatAdapter.notifyItemRangeChanged(position, chatMessages.size());



            }else{
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());

                chatRecyclerView.smoothScrollToPosition(chatMessages.size()-1);
            }


            chatRecyclerView.setVisibility(View.VISIBLE);

        }
    };


    @Override
    public void onClickChat(ChatMessage chatMessage, int position) {

        if(chatMessage.status.equals(ChatMessage.STATUS_DELETE)){
            ResourceUtil.showAlert("Error", "Esta publicacion ya ha sido eliminada", ChatActivity.this, "error");
            return;
        }

        if(chatMessage.senderId.equals(preferencesManager.getString(Constants.KEY_USER_ID))){
            showAlertMessage("Mensaje", "¿Desea eliminar esta publicacion?", ChatActivity.this, chatMessage, position);
        }else{
            ResourceUtil.showAlert("Error", "Solo puede eliminar publicaciones propias", ChatActivity.this, "error");
        }


    }

    public void showAlertMessage(String title, String response, Context context, ChatMessage chatMessage, int position) {

            new SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText(title)
                    .setContentText(response)
                    .setConfirmText("NO")
                    .setCancelButton("SI",v->{
                        deleteMessage(chatMessage, position);
                        v.dismiss();
                    })
                    .show();
    }

}