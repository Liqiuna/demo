package com.cst14.im.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cst14.im.Fragment.UserFragment;
import com.cst14.im.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zxm on 2016/8/27.
 */
public class userfragementAdapter extends BaseAdapter {
    private UserFragment fragement;
    private LayoutInflater mInflater;
    ArrayList<HashMap<String, String>> listitem =new ArrayList<HashMap<String, String>>();

    public userfragementAdapter(UserFragment fragement,ArrayList<HashMap<String, String>> map){


        this.mInflater=LayoutInflater.from(fragement.getContext());
        this.listitem=map;
    }


    @Override
    public int getCount() {
       return listitem.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {

            view =  this.mInflater.inflate(R.layout.user_adapter_layout, null);
        }
        TextView title = (TextView) view.findViewById(R.id.tv_item);
        title.setText(listitem.get(position).get("title").toString());
        title.getPaint().setFakeBoldText(true);
        return view;
    }
}
