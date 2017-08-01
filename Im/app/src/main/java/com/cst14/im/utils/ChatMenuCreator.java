package com.cst14.im.utils;

import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.activity.ChatActivity;
import com.yolanda.nohttp.NoHttp;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by MRLWJ on 2016/8/4.
 * 动态生成消息菜单
 */
public class ChatMenuCreator {
    private ChatActivity activity;
    private List<MenuItemEntry> list;
    public ChatMenuCreator(){
        //代表的是当前对话的activity
        activity =  ImApplication.instance.getCurSessionHolder().chatActivity;
        list = new LinkedList<MenuItemEntry>();
    }
    public void addAction(MenuItemEntry entry){
        list.add(entry);
    }
    public AlertDialog.Builder build(){
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.chat_menu_selector);
        for(final MenuItemEntry entry:list){
            View menuItem = View.inflate(activity,R.layout.item_menu2,null);
            TextView tv = (TextView) menuItem.findViewById(R.id.tv_menu_title);
            tv.setText(entry.title);
            //这里不知道为什么给 menuItem 设置点击监听 会无效
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    entry.action.run();
                    activity.chatMenu.dismiss();
                }
            });
            root.addView(menuItem);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(root);
        return builder;
    }
    public static class MenuItemEntry{
        private String title;
        private Runnable action;
        public MenuItemEntry(String title,Runnable action){
            this.title = title;
            this.action = action;
        }
    }
}
