package com.application.pm1_proyecto_final.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.models.GroupUser;

import java.util.ArrayList;

public class InvitationAdapter extends BaseAdapter {



    private Context context;
    private ArrayList<GroupUser> listItem;

    public InvitationAdapter(Context context, ArrayList<GroupUser> listItem) {
        this.context = context;
        this.listItem = listItem;
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
        TextView date = view.findViewById(R.id.timeGroupInvitation);


        title.setText(item.getNameGroup());
        date.setText(item.getDate().toString());


        return view;
    }
}
