package com.application.pm1_proyecto_final.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.models.Group;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private final List<Group> listGroup;

    public GroupAdapter(List<Group> listGroup) {
        this.listGroup = listGroup;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_container_group, parent, false);

        return new GroupViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        holder.setData(listGroup.get(position));
    }

    @Override
    public int getItemCount() {
        return listGroup.size();
    }

    class GroupViewHolder extends RecyclerView.ViewHolder{

        RoundedImageView imageView;
        TextView title, description;
        ConstraintLayout card;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageProfileGroup);

            title = itemView.findViewById(R.id.textTitleGroup);
            description = itemView.findViewById(R.id.textDescriptionGroup);

            card = itemView.findViewById(R.id.cardGroup);

        }

        void setData(Group group){

            imageView.setImageBitmap(getGroupImage(group.getImage()));

            title.setText(group.getTitle());

            description.setText(group.getDescription());

        }


    }

    private static Bitmap getGroupImage(String encodedImage){

        byte[] bytes = android.util.Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
