package com.application.pm1_proyecto_final.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.utils.Constants;
import com.application.pm1_proyecto_final.utils.PreferencesManager;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class UsersGroupAdapter extends RecyclerView.Adapter<UsersGroupAdapter.UsersViewHolder> {

    private List<User> users;

    PreferencesManager preferencesManager;

    public UsersGroupAdapter(List<User> users, PreferencesManager preferencesManager) {
        this.users = users;
        this.preferencesManager = preferencesManager;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_container_user_group, parent, false);

        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        holder.setData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UsersViewHolder extends RecyclerView.ViewHolder {

        RoundedImageView imageView;
        TextView name, description;
        View view;


        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageProfileUserGroup);

            name = itemView.findViewById(R.id.textNameUserGroup);
            description = itemView.findViewById(R.id.textDescriptionUser);

            view = itemView;
        }

        void setData(User user){
            //Falta ingresar la imagen
            name.setText(user.getName() +" "+ user.getLastname());

            if(user.getId().equals(preferencesManager.getString(Constants.KEY_USER_ID))){
                description.setText("Administrador");
            }else {
                description.setText("Miembro");
            }

            imageView.setImageBitmap(decodeImage(user.getImage()));

        }

        private Bitmap decodeImage(String encodedImage){

            byte[] bytes = android.util.Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }
}
