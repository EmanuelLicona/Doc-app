package com.application.pm1_proyecto_final.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.listeners.Grouplistener;
import com.application.pm1_proyecto_final.models.Group;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private  List<Group> listGroup;
    private  Grouplistener grouplistener;

    private  List<Group> filterlist;
    private CustomFilter filter;

    public GroupAdapter(List<Group> listGroup, Grouplistener grouplistener) {
        this.listGroup = listGroup;

        this.grouplistener = grouplistener;

        this.filterlist = listGroup;
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
        View view;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageProfileGroup);

            title = itemView.findViewById(R.id.textTitleGroup);
            description = itemView.findViewById(R.id.textDescriptionGroup);

            card = itemView.findViewById(R.id.cardGroup);

            view = itemView;

        }

        void setData(Group group){

            imageView.setImageBitmap(getGroupImage(group.getImage()));

            title.setText(group.getTitle());

            description.setText(group.getDescription());

            view.setOnClickListener(v -> grouplistener.onClickGroup(group));
        }


    }

    private static Bitmap getGroupImage(String encodedImage){

        byte[] bytes = android.util.Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


    //Filter
    /**********************************************************************************/

    class CustomFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {

            FilterResults filterResults = new FilterResults();

            if(charSequence != null && charSequence.length()>0){

                charSequence = charSequence.toString().toUpperCase();

                ArrayList<Group> filters = new ArrayList<Group>();

                for(int i = 0;i < filterlist.size(); i++){

                    if(filterlist.get(i).getTitle().toUpperCase().contains(charSequence)){

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

            listGroup = (ArrayList<Group>) filterResults.values;
            notifyDataSetChanged();
        }
    }

    public Filter getFilter(){

        if(filter == null){
            filter = new CustomFilter();
        }

        return filter;
    }

    public ArrayList<Group> getFilterlist(){
        return (ArrayList<Group>) filterlist;
    }
}
