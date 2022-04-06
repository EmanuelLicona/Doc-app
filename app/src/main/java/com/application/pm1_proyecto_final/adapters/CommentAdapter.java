package com.application.pm1_proyecto_final.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.models.Comment;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.providers.CommentsProvider;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.application.pm1_proyecto_final.utils.ResourceUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class CommentAdapter extends FirestoreRecyclerAdapter<Comment, CommentAdapter.ViewHolder> {
    Context context;
    List<User> userList;
    PreferencesManager preferencesManager;
    private CommentsProvider commentsProvider;

    public CommentAdapter(@NonNull FirestoreRecyclerOptions<Comment> options, Context context, ArrayList<User> userArrayList) {
        super(options);
        this.context = context;
        this.userList = userArrayList;
        preferencesManager = new PreferencesManager(context);
        this.commentsProvider = new CommentsProvider();
    }

    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull Comment comment) {
        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String commentId = document.getId();

        holder.txtComment.setText(comment.getComment());
        String[] infoUser = getInfoUser(comment.getIdUser());
        holder.txtUsername.setText(infoUser[1]);

        if (!infoUser[0].isEmpty() && !infoUser[0].equals("IMAGE")) {
            holder.imageProfile.setImageBitmap(ResourceUtil.decodeImage(infoUser[0]));
        }

        holder.viewHolder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (comment.getIdUser().equals(preferencesManager.getString(Constants.KEY_USER_ID))) {
                    showConfirmDelete(commentId);
                }
                return false;
            }
        });

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_comment, parent, false);
        return new ViewHolder(view);
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

    private void showConfirmDelete(final String commentId) {
        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Eliminar comentario")
                .setMessage("¿Está seguro de eliminar el comentario?")
                .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteComment(commentId);
                    }
                })
                .setNegativeButton("NO", null)
                .show();
    }

    private void deleteComment(String idComment) {
        commentsProvider.delete(idComment).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Comentario eliminado", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(context, "Error al eliminar el comentario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imageProfile;
        TextView txtComment;
        TextView txtUsername;
        View viewHolder;

        public ViewHolder(View view) {
            super(view);
            viewHolder = view;

            imageProfile = view.findViewById(R.id.circleImageComment);
            txtComment = view.findViewById(R.id.textViewComment);
            txtUsername = view.findViewById(R.id.textViewUsername);

        }
    }
}
