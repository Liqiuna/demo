package com.cst14.im.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cst14.im.Fragment.FriendInfo;
import com.cst14.im.R;
import com.cst14.im.utils.ImApplication;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by I_C_U on 2016/8/24.
 */

public class FriendListAdapter extends BaseExpandableListAdapter {
    private final Context context;

    public FriendListAdapter(Context context){
        this.context = context;
    }

    public Object getChild(int groupPosition, int childPosition) {
        int groupID=(int)getGroupId(groupPosition);
        return  ImApplication.mapGroupFriends.get(groupID).get(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public View getChildView(int groupPosition, int childPosition, boolean arg2, View convertView,
                             ViewGroup arg4) {
        int groupID=(int)getGroupId(groupPosition);
        FriendInfo item_friend =  ImApplication.mapGroupFriends.get(groupID).get(childPosition);
        ChildViewHolder holder;
        holder = new ChildViewHolder();
        convertView = convertView.inflate(context,R.layout.friend_item, null);
        holder.img_avatar = (ImageView) convertView.findViewById(R.id.image_head);//头像
        holder.tv_FrindNick = (TextView) convertView.findViewById(R.id.tv_friend_item_name);//名字
        holder.img_avatar.setImageResource(R.drawable.a6a);
        holder.tv_FrindNick.setText(item_friend.getName());//好友名字
        if(!item_friend.getMark().equals(""))holder.tv_FrindNick.setText(item_friend.getMark());//好友名字
        convertView.setTag(R.id.friend_Group_continer,groupID);
        convertView.setTag(R.id.friend_list_continer,childPosition);//长按子类
        return convertView;
    }

    public int getChildrenCount(int groupPosition) {
        int groupID=(int)getGroupId(groupPosition);
        return  ImApplication.mapGroupFriends.get(groupID).size();
    }

    public String getGroup(int groupPosition) {
        int groupID=(int)getGroupId(groupPosition);
        return ImApplication.mapFriendGroup.get(groupID);
    }

    public int getGroupCount() {
        return ImApplication.mapFriendGroup.size();
    }

    public long getGroupId(int groupPosition) {
        int count=0;//用来计数   解决map中 key 与 好友列表中第几个不同步
        int key=0;//组号
        Iterator iter = ImApplication.mapFriendGroup.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            if(count==groupPosition){
                key = (int)entry.getKey();
                break; }
            count++;
        }
        return key;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup arg3) {
        int groupID=(int)getGroupId(groupPosition);
        convertView = convertView.inflate(context,R.layout.friend_group_item, null);
        TextView groupNameTextView=(TextView) convertView.findViewById(R.id.buddy_listview_group_name);
        groupNameTextView.setText(ImApplication.mapFriendGroup.get(groupID));
        String groupName=ImApplication.mapFriendGroup.get(groupID);
        Log.e("groupName", groupName);
        ImageView image = (ImageView) convertView.findViewById(R.id.buddy_listview_image);
        convertView.setTag(R.id.friend_Group_continer,groupID);
        convertView.setTag(R.id.friend_list_continer, -1); //长按子类标记

        //展开、折叠
        if(!isExpanded){
            image.setImageResource(R.drawable.split_vertical);
            return convertView;
        }

        image.setImageResource(R.drawable.merge_horizontal);
        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }

    // 子选项是否可以选择
    public boolean isChildSelectable(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return true;
    }

    static class ChildViewHolder {
        ImageView img_avatar;//对方头像
        TextView tv_FrindNick;//对方昵称
    }


}