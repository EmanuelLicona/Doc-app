package com.application.pm1_proyecto_final.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.application.pm1_proyecto_final.databinding.ItemContainerUserBinding;
import com.application.pm1_proyecto_final.listeners.UserListener;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.utils.ResourceUtil;

import java.util.List;

public class UsersChatAdapter extends  RecyclerView.Adapter<UsersChatAdapter.UserChatViewHolder> {

    private final List<User> users;
    private final UserListener userListener;

    public UsersChatAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserChatViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserChatViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserChatViewHolder extends RecyclerView.ViewHolder {

        ItemContainerUserBinding binding;

        UserChatViewHolder(ItemContainerUserBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        void setUserData(User user) {
            String nameUser = user.getName()+" "+user.getLastname();
            binding.textNameUserChat.setText(nameUser);
            binding.textEmailChat.setText(user.getEmail());
            binding.imageProfileItem.setImageBitmap(ResourceUtil.decodeImage(user.getImage()));
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }

    }


}
