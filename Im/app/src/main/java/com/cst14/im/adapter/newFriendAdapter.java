package com.cst14.im.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cst14.im.Fragment.FriendInfo;
import com.cst14.im.R;
import com.cst14.im.activity.NewFriendActivity;

import java.util.LinkedList;
import java.util.List;

import static com.cst14.im.db.dao.FriendsDao.updateFriendRequest;

/**
 * Created by I_C_U on 2016/9/16.
 */
public class newFriendAdapter extends BaseAdapter {
    private final Context context;
    private List<FriendInfo> requestFriendList = null;
    public newFriendAdapter(Context context, List<FriendInfo> requestFriendList) {
        this.context = context;
        if (requestFriendList == null) {
            requestFriendList = new LinkedList<FriendInfo>();
        }
        this.requestFriendList = requestFriendList;
    }
    public void refresh(List<FriendInfo> requestFriendList) {
        if (requestFriendList == null) {
            requestFriendList = new LinkedList<FriendInfo>();
        }
        this.requestFriendList = requestFriendList;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return requestFriendList.size();
    }

    @Override
    public Object getItem(int position) {
        return requestFriendList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }
    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public View getView(final int position, View v, ViewGroup parent) {
        final ViewHolder holder;
        final FriendInfo new_friend = requestFriendList.get(position);
        if (v == null) {
            holder = new ViewHolder();
            v = View.inflate(context, R.layout.new_friend_item, null);
            holder.new_friend_continer = (LinearLayout) v.findViewById(R.id.new_friend_continer);
            holder.new_friend_image_head = (ImageView) v.findViewById(R.id.new_friend_image_head);//头像
            holder.tv_new_friend_item_name = (TextView) v.findViewById(R.id.tv_new_friend_item_name);//昵称
            holder.tv_is_agree_in_ragment =(TextView)v.findViewById(R.id.tv_is_agree_in_ragment);// 显示是否已同意文字
            holder.btn_new_friend_agree =(Button)v.findViewById(R.id.btn_new_friend_agree);//同意
            holder.btn_new_friend_disagree =(Button)v.findViewById(R.id.btn_new_friend_disagree);//拒绝
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        holder.new_friend_image_head.setImageResource(R.drawable.a6a);
        holder.tv_new_friend_item_name.setText(new_friend.getName());//昵称
        switch (new_friend.getIsAgree()) {
            case 0:
                holder.btn_new_friend_agree.setVisibility(View.VISIBLE);
                holder.btn_new_friend_disagree.setVisibility(View.VISIBLE);
                holder.tv_is_agree_in_ragment.setVisibility(View.GONE);
                holder.btn_new_friend_agree.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        new_friend.setAgree(1);//同意  1
                        NewFriendActivity.response(new_friend);
                    }
                });
                holder.btn_new_friend_disagree.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        new_friend.setAgree(2);//拒绝  2
                        NewFriendActivity.response(new_friend);

                    }
                });break;
            case 1:
                holder.btn_new_friend_agree.setVisibility(View.GONE);
                holder.btn_new_friend_disagree.setVisibility(View.GONE);
                holder.tv_is_agree_in_ragment.setVisibility(View.VISIBLE);
                holder.tv_is_agree_in_ragment.setText("已同意");break;
            case 2:
                holder.btn_new_friend_agree.setVisibility(View.GONE);
                holder.btn_new_friend_disagree.setVisibility(View.GONE);
                holder.tv_is_agree_in_ragment.setVisibility(View.VISIBLE);
                holder.tv_is_agree_in_ragment.setText("已拒绝");break;
        }

        return v;
    }


    static class ViewHolder {
        ImageView new_friend_image_head;//头像
        TextView tv_new_friend_item_name;//昵称
        TextView tv_is_agree_in_ragment;// 显示是否已同意文字
        LinearLayout new_friend_continer;
        Button btn_new_friend_agree,btn_new_friend_disagree;  //按钮 是否同意
    }
}
