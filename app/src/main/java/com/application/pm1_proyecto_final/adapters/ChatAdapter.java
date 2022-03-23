package com.application.pm1_proyecto_final.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.listeners.Chatlistener;
import com.application.pm1_proyecto_final.models.ChatMessage;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final List<ChatMessage> chatMessages;
    private final Bitmap reseiverProfileImage;
    private final String senderId;

    private Chatlistener chatlistener;
    Context context;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;


    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap reseiverProfileImage, String senderId, Chatlistener chatlistener, Context context) {
        this.chatMessages = chatMessages;
        this.reseiverProfileImage = reseiverProfileImage;
        this.senderId = senderId;
        this.chatlistener = chatlistener;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == VIEW_TYPE_SENT){

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_send_message, parent, false);
            return new ChatAdapter.SendMessageViewHolder(view);

        }else {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_reseive_message, parent, false);
            return new ChatAdapter.ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT){
            ((SendMessageViewHolder) holder).setData(chatMessages.get(position));
        }else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), reseiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {

        if(chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    class SendMessageViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitle;
        TextView txtNameUser;
        TextView txtDateTime;
        TextView txtDescription;
        ImageView imageViewPost;
        RoundedImageView imageProfileSend;

        View view;

        SendMessageViewHolder(@NonNull View itemView){
            super(itemView);

            txtTitle = itemView.findViewById(R.id.txtTitleViewPublication);
            txtNameUser = itemView.findViewById(R.id.txtNameUserPost);
            txtDateTime = itemView.findViewById(R.id.textDateTimeSend);
            txtDescription = itemView.findViewById(R.id.txtDescriptionPost);
            imageViewPost = itemView.findViewById(R.id.imageViewPost);
            imageProfileSend = itemView.findViewById(R.id.imageProfileSend);

            view = itemView;
        }

        void setData(ChatMessage chatMessage){

            txtTitle.setText(chatMessage.title);
            txtDescription.setText(chatMessage.description);
            imageProfileSend.setImageBitmap(ResourceUtil.decodeImage(chatMessage.imageProfileUser));
            txtDateTime.setText(chatMessage.datatime);
            String[] extensionFile = chatMessage.type.split("/");

            if (chatMessage.type.equals("application/pdf")) {
                imageViewPost.setImageResource(R.drawable.pdf_publication);
            } else if(extensionFile[0].equals("image")) {
                Picasso.with(context).load(chatMessage.path).into(imageViewPost);
            } else if (extensionFile[0].equals("audio")) {
                imageViewPost.setImageResource(R.drawable.audio_publication);
            } else if(extensionFile[0].equals("video")) {
                imageViewPost.setImageResource(R.drawable.video_publication);
            }

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    chatlistener.onClickChat(chatMessage, getLayoutPosition());

                    return false;
                }
            });
        }

    }

    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{

        TextView textMessage;
        TextView textDateTime;
        View view;
        RoundedImageView imageProfile;

        ReceivedMessageViewHolder(@NonNull View itemView){
            super(itemView);

            textMessage = itemView.findViewById(R.id.textMessageReseiver);
            textDateTime = itemView.findViewById(R.id.textDateTimeReseiver);
            imageProfile = itemView.findViewById(R.id.imageProfileReseiver);

            view = itemView;


        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage){
            textMessage.setText(chatMessage.title);
            textDateTime.setText(chatMessage.datatime);
            imageProfile.setImageBitmap(receiverProfileImage);

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    chatlistener.onClickChat(chatMessage, getLayoutPosition());
                    return false;
                }
            });
        }
    }

}
