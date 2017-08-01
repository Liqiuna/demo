package com.cst14.im.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.bean.UserGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zl on 2016/8/26.
 */
public class GroupListAdapter extends ArrayAdapter<UserGroup> {
    private int resourceID;
    private final int TYPE_GROUP_CHAT = 0;
    private final int TYPE_GROUP_SEARCH_RESULT = 1;
    private int viewType = TYPE_GROUP_CHAT;

    public GroupListAdapter(Context context, int resource, List<UserGroup> objects) {
        super(context, resource, objects);
        this.resourceID = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        UserGroup userGroup = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceID, null);
            viewHolder = new ViewHolder();
            viewHolder.ivGroupIcon = (ImageView) view.findViewById(R.id.iv_item_grouplist_groupIcon);
            viewHolder.tvGroupName = (TextView) view.findViewById(R.id.tv_item_groupList_groupName);
            viewHolder.tvLastMsg = (TextView) view.findViewById(R.id.tv_item_groupList_lastMsg);
            viewHolder.tvTime = (TextView) view.findViewById(R.id.tv_item_groupList_time);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        //TODO load group Icon
        viewHolder.tvGroupName.setText(userGroup.getName());
        if (viewType == TYPE_GROUP_CHAT) {
            //viewHolder.tvLastMsg.setText(userGroup.getLastMsg());
            viewHolder.tvTime.setVisibility(View.VISIBLE);
            //viewHolder.tvTime.setText(userGroup.getLastMsgTime());
        } else if (viewType == TYPE_GROUP_SEARCH_RESULT) {
            viewHolder.tvLastMsg.setText(userGroup.getIntro());
            viewHolder.tvTime.setVisibility(View.GONE);
        }

        return view;
    }

    public void setViewTypeForGroupList() {
        this.viewType = TYPE_GROUP_CHAT;
    }

    public void setViewTypeForGroupSearchResult() {
        this.viewType = TYPE_GROUP_SEARCH_RESULT;
    }

    class ViewHolder {
        ImageView ivGroupIcon;
        TextView tvGroupName;
        TextView tvTime;
        TextView tvLastMsg;
    }

}
