package com.application.pm1_proyecto_final.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.listeners.Chatlistener;
import com.application.pm1_proyecto_final.models.Publication;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PublicationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final List<Publication> publications;
    private final String senderId;

    private Chatlistener chatlistener;
    Context context;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public PublicationAdapter(List<Publication> publications, String senderId, Chatlistener chatlistener, Context context) {
        this.publications = publications;
        this.senderId = senderId;
        this.chatlistener = chatlistener;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == VIEW_TYPE_SENT){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_send_message, parent, false);
            return new PublicationAdapter.SendMessageViewHolder(view);

        }else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_reseive_message, parent, false);
            return new PublicationAdapter.ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT){
            ((SendMessageViewHolder) holder).setData(publications.get(position));
        }else {
            ((ReceivedMessageViewHolder) holder).setData(publications.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return publications.size();
    }

    @Override
    public int getItemViewType(int position) {

        if(publications.get(position).getSenderId().equals(senderId)){
            return VIEW_TYPE_SENT;
        }else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    class SendMessageViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;
        TextView txtMyPublication;
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
            txtMyPublication = itemView.findViewById(R.id.txtMyPublication);
            txtDateTime = itemView.findViewById(R.id.textDateTimeSend);
            txtDescription = itemView.findViewById(R.id.txtDescriptionPost);
            imageViewPost = itemView.findViewById(R.id.imageViewPost);
            imageProfileSend = itemView.findViewById(R.id.imageProfileSend);

            view = itemView;
        }

        void setData(Publication publication){
            PreferencesManager preferencesManager = new PreferencesManager(context);
            txtTitle.setText(publication.getTitle());
            txtMyPublication.setText(" - Mi Publicación");
            txtDescription.setText(publication.getDescription());
            txtNameUser.setText(preferencesManager.getString(Constants.KEY_NAME_USER));
            imageProfileSend.setImageBitmap(ResourceUtil.decodeImage(preferencesManager.getString(Constants.KEY_IMAGE_USER)));
            txtDateTime.setText(publication.getDatatime());
            String[] extensionFile = publication.getType().split("/");

            if (publication.getType().equals("application/pdf")) {
                imageViewPost.setImageResource(R.drawable.pdf_publication);
            } else if(extensionFile[0].equals("image")) {
                Picasso.with(context).load(publication.getPath()).into(imageViewPost);
            } else if (extensionFile[0].equals("audio")) {
                imageViewPost.setImageResource(R.drawable.audio_publication);
            } else if(extensionFile[0].equals("video")) {
                imageViewPost.setImageResource(R.drawable.video_publication);
            }

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    chatlistener.onClickChat(publication, getLayoutPosition());

                    return false;
                }
            });
        }

    }

    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;
        TextView txtNameUser;
        TextView txtDateTime;
        TextView txtDescription;
        ImageView imageViewPost;
        RoundedImageView imageProfileReceive;
        View view;


        ReceivedMessageViewHolder(@NonNull View itemView){
            super(itemView);

            txtTitle = itemView.findViewById(R.id.txtTitleViewPublicationReceive);
            txtNameUser = itemView.findViewById(R.id.txtNameUserPostReceive);
            txtDateTime = itemView.findViewById(R.id.textDateTimeReceive);
            txtDescription = itemView.findViewById(R.id.txtDescriptionPostReceive);
            imageViewPost = itemView.findViewById(R.id.imageViewPostReceive);
            imageProfileReceive = itemView.findViewById(R.id.imageProfileReceive);


            view = itemView;
        }

        void setData(Publication publication) {
            txtTitle.setText(publication.getTitle());
            txtDateTime.setText(publication.getDatatime());
            txtDescription.setText(publication.getDescription());
            txtNameUser.setText(publication.getNameUser());
            imageProfileReceive.setImageBitmap(ResourceUtil.decodeImage(publication.getImageProfileUser()));

            String[] extensionFile = publication.getType().split("/");

            if (publication.getType().equals("application/pdf")) {
                imageViewPost.setImageResource(R.drawable.pdf_publication);
            } else if(extensionFile[0].equals("image")) {
                Picasso.with(context).load(publication.getPath()).into(imageViewPost);
            } else if (extensionFile[0].equals("audio")) {
                imageViewPost.setImageResource(R.drawable.audio_publication);
            } else if(extensionFile[0].equals("video")) {
                imageViewPost.setImageResource(R.drawable.video_publication);
            }

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    chatlistener.onClickChat(publication, getLayoutPosition());
                    return false;
                }
            });
        }
    }

}
