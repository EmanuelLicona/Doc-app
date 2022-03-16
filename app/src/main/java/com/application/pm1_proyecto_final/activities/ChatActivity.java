package com.application.pm1_proyecto_final.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

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
import com.application.pm1_proyecto_final.models.ChatMessage;
import com.application.pm1_proyecto_final.models.Group;
import com.application.pm1_proyecto_final.providers.GroupsProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
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

public class ChatActivity extends AppCompatActivity {

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
                preferencesManager.getString(Constants.KEY_USER_ID)
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

        mensaje.put(Constants.KEY_SENDER_ID, preferencesManager.getString(Constants.KEY_USER_ID));
        mensaje.put(Constants.KEY_GROUP_ID, reseiverGroup.getId());
        mensaje.put(Constants.KEY_MESSAGE, "Mensage: " + value);
        mensaje.put(Constants.KEY_TIMESTAMP, new Date());

        database.collection(Constants.KEY_COLLECTION_CHAT).add(mensaje)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(this, "Si", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(this, "No", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
//                .whereEqualTo(Constants.KEY_SENDER_ID, preferencesManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_GROUP_ID, reseiverGroup.getId())
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {

        if(error!=null){
            return ;
        }

        if(value != null){
            int count = chatMessages.size();

            for (DocumentChange documentChange: value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();

                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.groupId = documentChange.getDocument().getString(Constants.KEY_GROUP_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.datatime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);

                    chatMessages.add(chatMessage);
                }

            }

            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));

            if(count == 0){
                chatAdapter.notifyDataSetChanged();
            }else{
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());

                chatRecyclerView.smoothScrollToPosition(chatMessages.size()-1);
            }

            chatRecyclerView.setVisibility(View.VISIBLE);

        }

//        binding.progressBar.setVisibility(View.GONE);

//        if(conversationId == null){
//            checkForConversion();
//        }
    };


}