package com.application.pm1_proyecto_final.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.application.pm1_proyecto_final.databinding.ItemContainerRecentConversionBinding;
import com.application.pm1_proyecto_final.listeners.ConversationListener;
import com.application.pm1_proyecto_final.models.Chat;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.utils.ResourceUtil;

import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder> {

    private final List<Chat> chatMessages;
    private final ConversationListener conversationListener;

    public RecentConversationsAdapter(List<Chat> chatMessages, ConversationListener conversationListener) {
        this.chatMessages = chatMessages;
        this.conversationListener = conversationListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRecentConversionBinding binding;

        ConversionViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding){
            super(itemContainerRecentConversionBinding.getRoot());
            binding = itemContainerRecentConversionBinding;
        }

        void setData(Chat chat) {
            binding.textNameUserChat.setText(chat.getConversionName());
            binding.txtRecentMessage.setText(chat.getMessage());
            String[] nameUser = chat.getConversionName().split(" ");
            binding.imageProfileItem.setText(ResourceUtil.letterIcon(nameUser[0], nameUser[1]));

            binding.getRoot().setOnClickListener(view -> {
                User user = new User();
                user.setId(chat.getConversionId());
                user.setName(chat.getConversionName());
                conversationListener.onConversationClicked(user);
            });
        }

    }

}
