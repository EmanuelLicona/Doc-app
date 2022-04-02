package com.application.pm1_proyecto_final.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.application.pm1_proyecto_final.databinding.ItemContainerUserBinding;
import com.application.pm1_proyecto_final.listeners.UserListener;
import com.application.pm1_proyecto_final.models.Group;
import com.application.pm1_proyecto_final.models.User;
import com.application.pm1_proyecto_final.utils.ResourceUtil;

import java.util.ArrayList;
import java.util.List;

public class UsersChatAdapter extends  RecyclerView.Adapter<UsersChatAdapter.UserChatViewHolder> {

    private  List<User> users;
    private  UserListener userListener;

    private  List<User> filterlist;
    private  CustomFilter filter;

    public UsersChatAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;

        this.filterlist = users;

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
            binding.imageProfileItem.setText(ResourceUtil.letterIcon(user.getName(), user.getLastname()));
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }

    }


    //Filter
    /**********************************************************************************/

    class CustomFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {

            FilterResults filterResults = new FilterResults();

            if(charSequence != null && charSequence.length()>0){

                charSequence = charSequence.toString().toUpperCase();

                ArrayList<User> filters = new ArrayList<>();

                for(int i = 0;i < filterlist.size(); i++){

                    if((filterlist.get(i).getName() + filterlist.get(i).getLastname()).toUpperCase().contains(charSequence)){

                        filters.add(filterlist.get(i));
                    }
                }

                filterResults.count = filters.size();
                filterResults.values = filters;

            }else {

                filterResults.count = filterlist.size();
                filterResults.values = filterlist;
            }

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

            users = (ArrayList<User>) filterResults.values;
            notifyDataSetChanged();
        }
    }

    public Filter getFilter(){

        if(filter == null){
            filter = new CustomFilter();
        }

        return filter;
    }



}
