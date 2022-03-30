package com.application.pm1_proyecto_final.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.listeners.Chatlistener;
import com.application.pm1_proyecto_final.models.Publication;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.providers.PublicationProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.RelativeTime;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class PublicationAdapter extends FirestoreRecyclerAdapter<Publication, PublicationAdapter.ViewHolder> {
    Context context;
    List<User> userList;
    PublicationProvider publicationProvider;
    PreferencesManager preferencesManager;
    private Chatlistener chatListener;

    public PublicationAdapter(@NonNull FirestoreRecyclerOptions<Publication> options, Context context, ArrayList<User> userArrayList, Chatlistener chatListener) {
        super(options);
        this.context = context;
        this.userList = userArrayList;
        publicationProvider = new PublicationProvider();
        preferencesManager = new PreferencesManager(context);
        this.chatListener = chatListener;
    }

    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull Publication publication) {
        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String publicationId = document.getId();

        holder.txtTitleViewPublication.setText(publication.getTitle());
        holder.txtDescriptionPost.setText(publication.getDescription());
        String relativeTime = RelativeTime.getTimeAgo(publication.getTimestamp(), context);
        holder.textDateTimeSend.setText(relativeTime);
        String[] infoUser = getInfoUser(publication.getSenderId());

        if (infoUser.length != 0) {
            holder.imageProfile.setImageBitmap(ResourceUtil.decodeImage(infoUser[0]));
            holder.txtNameUserPost.setText(infoUser[1]);
        }
        if (publication.getSenderId().equals(preferencesManager.getString(Constants.KEY_USER_ID))) {
            holder.txtMyPublication.setText(" - Mi Publicación");
        }

        String[] extensionFile = publication.getType().split("/");
        viewImageByTypeFile(extensionFile, holder, publication);

        holder.viewHolder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (publication.getSenderId().equals(preferencesManager.getString(Constants.KEY_USER_ID))) {
                    showConfirmDelete(publicationId);
                }
                return false;
            }
        });

        holder.imageViewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatListener.onClickFile(publication);
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_publication, parent, false);
        return new ViewHolder(view);
    }

    private void viewImageByTypeFile(String[] extensionFile, ViewHolder holder, Publication publication) {

        if (publication.getType().equals("application/pdf")) {
            holder.imageViewPost.setImageResource(R.drawable.pdf_publication);
        } else if(extensionFile[0].equals("image")) {
            Picasso.with(context).load(publication.getPath()).into(holder.imageViewPost);
        } else if (extensionFile[0].equals("audio")) {
            holder.imageViewPost.setImageResource(R.drawable.audio_publication);
        } else if(extensionFile[0].equals("video")) {
            holder.imageViewPost.setImageResource(R.drawable.video_publication);
        } else if(extensionFile[1].equals("msword") || extensionFile[1].equals("vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            holder.imageViewPost.setImageResource(R.drawable.word_image);
        } else if(extensionFile[1].equals("onenote")) {
            holder.imageViewPost.setImageResource(R.drawable.onenote_image);
        } else if(extensionFile[1].equals("vnd.ms-powerpoint") || extensionFile[1].equals("vnd.openxmlformats-officedocument.presentationml.presentation")) {
            holder.imageViewPost.setImageResource(R.drawable.power_point_image);
        } else if(extensionFile[1].equals("vnd.ms-excel") || extensionFile[1].equals("vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            holder.imageViewPost.setImageResource(R.drawable.excel_image);
        } else if(extensionFile[1].equals("plain")) {
            holder.imageViewPost.setImageResource(R.drawable.text_image);
        } else if(extensionFile[1].equals("javascript")) {
            holder.imageViewPost.setImageResource(R.drawable.javascript_image);
        } else if(extensionFile[1].equals("json")) {
            holder.imageViewPost.setImageResource(R.drawable.json_image);
        } else if(extensionFile[1].equals("x-java-source,java") || extensionFile[1].equals("java-vm")) {
            holder.imageViewPost.setImageResource(R.drawable.java_image);
        } else if(extensionFile[1].equals("zip")) {
            holder.imageViewPost.setImageResource(R.drawable.zip_image);
        } else if(extensionFile[1].equals("x-rar-compressed")) {
            holder.imageViewPost.setImageResource(R.drawable.winrar_image);
        } else if(extensionFile[1].equals("html")) {
            holder.imageViewPost.setImageResource(R.drawable.html_image);
        } else {
            holder.imageViewPost.setImageResource(R.drawable.text_image);
        }

    }

    private String[] getInfoUser(String idUser) {
        String[] arrayUser = new String[2];

        for (User item : userList) {

            if (item.getId().equals(idUser)) {
                arrayUser[0] = item.getImage();
                arrayUser[1] = item.getName() + " " + item.getLastname();
                break;
            }

        }
        return arrayUser;
    }

    private void showConfirmDelete(final String publicationId) {
        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Eliminar publicación")
                .setMessage("¿Está seguro de eliminar la publicación?")
                .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deletePublication(publicationId);
                    }
                })
                .setNegativeButton("NO", null)
                .show();
    }

    private void deletePublication(String postId) {
        publicationProvider.delete(postId).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    ResourceUtil.showAlert("Confirmación", "La publicación se elimino correctamente.",context, "success");
                }
                else {
                    ResourceUtil.showAlert("Confirmación", "No se pudo eliminar la publicación.",context, "error");
                }
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView imageProfile;
        ImageView imageViewPost;
        TextView txtNameUserPost;
        TextView txtMyPublication;
        TextView txtTitleViewPublication;
        TextView txtDescriptionPost;
        TextView textDateTimeSend;
        View viewHolder;

        public ViewHolder(View view) {
            super(view);
            viewHolder = view;

            imageProfile = view.findViewById(R.id.imageProfileSend);
            imageViewPost = view.findViewById(R.id.imageViewPost);
            txtNameUserPost = view.findViewById(R.id.txtNameUserPost);
            txtMyPublication = view.findViewById(R.id.txtMyPublication);
            txtTitleViewPublication = view.findViewById(R.id.txtTitleViewPublication);
            txtDescriptionPost = view.findViewById(R.id.txtDescriptionPost);
            textDateTimeSend = view.findViewById(R.id.textDateTimeSend);

        }
    }
}
