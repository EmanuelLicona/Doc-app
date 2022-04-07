package com.application.pm1_proyecto_final.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.listeners.Invitationlistener;
import com.application.pm1_proyecto_final.listeners.UserListener;
import com.application.pm1_proyecto_final.models.GroupUser;
import com.application.pm1_proyecto_final.models.User;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

public class UserAddGroupAdapter extends BaseAdapter{


        private Context context;
        private ArrayList<User> listItem;
        private UserListener userListener;

        public UserAddGroupAdapter(Context context, ArrayList<User> listItem, UserListener userListener) {
            this.context = context;
            this.listItem = listItem;
            this.userListener = userListener;
        }

        @Override
        public int getCount() {
            return listItem.size();
        }

        @Override
        public Object getItem(int i) {
            return listItem.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            User item = (User) getItem(i);

            view = LayoutInflater.from(context).inflate(R.layout.item_user_add_group, null);

            TextView title = view.findViewById(R.id.nameUserAddGroup);
            TextView description = view.findViewById(R.id.emailUserAddGroup);
            RoundedImageView imageView = view.findViewById(R.id.imageUserAddGroup);


            title.setText(item.getName() + " " + item.getLastname());
            description.setText(item.getEmail());

            try{
                imageView.setImageBitmap(decodeImage(item.getImage()));
            }catch (Exception e){
//                imageView.setImageBitmap(decodeImage(item.getImage()));
            }


            view.setOnClickListener(v -> userListener.onUserClicked(item));


            return view;
        }

        private static Bitmap decodeImage(String encodedImage){

            byte[] bytes = android.util.Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
}
