package com.cst14.im.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.cst14.im.R;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.ImageUtil;

import java.util.ArrayList;
import java.util.List;


public class SearchUserResultAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<ProtoClass.User> userList = null;

    private List<Double> distanceList;

    private ProtoClass.MsgType opt;
    public SearchUserResultAdapter(Context context, List<ProtoClass.User> list, ProtoClass.MsgType opt){
        mInflater = LayoutInflater.from(context);
        this.userList = new ArrayList<>();
        userList.addAll(list);
        this.opt = opt;
    }

    public void setDistanceList(List<Double> list) {
        distanceList = list;
    }
    @Override
    public int getCount() {
        if (userList == null){
            return 0;
        }
        return userList.size();
    }

    @Override
    public Object getItem(int position) {
        if (userList == null){
            return null;
        }
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addData(List<ProtoClass.User> list){
        if (userList == null){
            this.userList = new ArrayList<>();
            userList.addAll(list);
        }
        else {
            userList.addAll(list);
        }
        notifyDataSetChanged();
    }
    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null){
            convertView = mInflater.inflate(R.layout.result_list_item,null);
            holder = new ViewHolder();
            holder.headerView = (ImageView) convertView.findViewById(R.id.user_header);
            holder.nickName = (TextView) convertView.findViewById(R.id.nick_name);
            holder.userName = (TextView) convertView.findViewById(R.id.user_name);
            holder.otherInfo = (TextView) convertView.findViewById(R.id.show_other);
            holder.sexView = (ImageView) convertView.findViewById(R.id.SexImageView);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        ProtoClass.User curUser = userList.get(position);
        //填充用户头像
        if (TextUtils.isEmpty(curUser.getIconName())){
            holder.headerView.setImageResource(R.drawable.initial_header);
        }else {
            Bitmap bitmap = ImageUtil.bytes2Bitmap(curUser.getIcon().toByteArray());
            holder.headerView.setImageBitmap(bitmap);
        }

        holder.nickName.setText(curUser.getNickName());
        holder.userName.setText(String.format("用户名：%s", curUser.getUserName()));
        if (this.opt == ProtoClass.MsgType.SEEK_ATTRB){ //如果是根据用户属性查找
            holder.otherInfo.setText("");
        }else if (this.opt == ProtoClass.MsgType.SEEK_NAME){ //如果根据用户名精确查询
            holder.userName.setText(String.format("用户名：%s", curUser.getUserName()));
        }else if (this.opt == ProtoClass.MsgType.SEEK_AROUD){ //如果是查找附近的人
            System.out.println();
        }
        //显示用户性别
        if (TextUtils.equals(curUser.getUserDetail().getSex(),"男")){
            holder.sexView.setImageResource(R.drawable.ic_male);
            holder.sexView.setVisibility(View.VISIBLE);
        }else if (TextUtils.equals(curUser.getUserDetail().getSex(),"女")){
            holder.sexView.setImageResource(R.drawable.ic_female);
            holder.sexView.setVisibility(View.VISIBLE);
        }else{
            holder.sexView.setVisibility(View.INVISIBLE);
        }
        //填充其他信息
        if (opt == ProtoClass.MsgType.GET_NEARBY) {
            //附近好友的距离
            holder.otherInfo.setText("距离"+distanceList.get(position)+"米");
        }
        return convertView;
    }

    private final class ViewHolder{
        ImageView headerView = null;
        TextView nickName = null;
        TextView userName = null;
        TextView otherInfo = null;
        ImageView sexView = null;
    }
}
