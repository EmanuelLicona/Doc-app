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
import com.application.pm1_proyecto_final.models.GroupUser;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

public class InvitationAdapter extends BaseAdapter {



    private Context context;
    private ArrayList<GroupUser> listItem;
    private Invitationlistener invitationlistener;

    public InvitationAdapter(Context context, ArrayList<GroupUser> listItem, Invitationlistener invitationlistener) {
        this.context = context;
        this.listItem = listItem;
        this.invitationlistener = invitationlistener;
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

        GroupUser item = (GroupUser) getItem(i);

        view = LayoutInflater.from(context).inflate(R.layout.item_invitation, null);

        TextView title = view.findViewById(R.id.tileGroupInvitation);
        TextView description = view.findViewById(R.id.descriptionGroupInvitation);
        RoundedImageView imageView = view.findViewById(R.id.imageInvitationGroup);


        title.setText(item.getNameGroup());
        description.setText(item.getDescriptionGroup());
        imageView.setImageBitmap(decodeImage(item.getImage()));

        view.setOnClickListener(v -> invitationlistener.OnClickInvitation(item));


        return view;
    }

    private static Bitmap decodeImage(String encodedImage){

        byte[] bytes = android.util.Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
