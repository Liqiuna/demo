package com.cst14.im.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cst14.im.Fragment.FriendInfo;
import com.cst14.im.activity.MainActivity;
import com.cst14.im.db.helper.DBHelper;
import com.cst14.im.utils.ImApplication;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;


/**
 * Created by I_C_U on 2016/9/10.
 */
public class FriendsDao {
    private static DBHelper helper;
    public static SQLiteDatabase db;

    public  FriendsDao(Context context) {
        helper = new DBHelper(context);
        db = helper.getWritableDatabase();
    }

    public static boolean addFriend(FriendInfo friend) {
        ContentValues values = new ContentValues();
        values.put("ownerID", Integer.parseInt(ImApplication.User_id));
        values.put("friendID",Integer.parseInt(friend.getUser_id()));
        values.put("Nick", friend.getName());
        values.put("remark", friend.getMark());
        values.put("listNO", friend.getListNo());
        long lastId = db.insert("friends", null, values);
        return lastId > 0;
    }
    public static boolean updateFriendInfo(FriendInfo friend) {
        ContentValues values = new ContentValues();
        values.put("ownerID", Integer.parseInt(ImApplication.User_id));
        values.put("friendID",Integer.parseInt(friend.getUser_id()));
        values.put("Nick", friend.getName());
        values.put("remark", friend.getMark());
        values.put("listNO", friend.getListNo());
        int num = db.update("friends",
                values,
                "ownerID=? and friendID=?",
                new String[]{ImApplication.User_id, friend.getUser_id()});
        return num == 1;
    }
    public static boolean delFriend(String friendID) {
        int num = db.delete("friends",
                "ownerID=? and friendID=?",
                new String[]{ImApplication.User_id, friendID});
        return num == 1;
    }

    public static boolean addGroup(int listNO,String listName) {
        ContentValues values = new ContentValues();
        values.put("UID", Integer.parseInt(ImApplication.User_id));
        values.put("listNO",listNO);
        values.put("listName",listName);
        long lastId = db.insert("friendGroup", null, values);
        return lastId > 0;
    }
    public static boolean updateGroup(int listNO,String listName) {
        ContentValues values = new ContentValues();
        values.put("UID", Integer.parseInt(ImApplication.User_id));
        values.put("listNO",listNO);
        values.put("listName",listName);
        int num = db.update("friendGroup",
                values,
                "UID=? and listNO=?",
                new String[]{ImApplication.User_id, String.valueOf(listNO)});
        return num == 1;
    }
    public static boolean delGroup(int listNO) {
        int num = db.delete("friendGroup",
                "UID=? and listNO=?",
                new String[]{ImApplication.User_id, String.valueOf(listNO)});
        return num == 1;
    }

    public static boolean loadFriendsFromDB() {
        Cursor cursor = db.query("friends", null, "ownerID=?", new String[]{ImApplication.User_id}, null, null, null);
        Log.e("cursor2:",cursor.toString());
        if(cursor.getCount()==0)return false;//第一次登陆  好友列表为空
        while (cursor.moveToNext()) {
            FriendInfo friend_item=new FriendInfo();
            friend_item.setUser_id(cursor.getString(cursor.getColumnIndex("friendID")));
            friend_item.setName(cursor.getString(cursor.getColumnIndex("Nick")));
            friend_item.setMark(cursor.getString(cursor.getColumnIndex("remark")));
            friend_item.setListNo(cursor.getInt(cursor.getColumnIndex("listNO")));
            ImApplication.mapGroupFriends.get(friend_item.getListNo()).add(friend_item);
            ImApplication.mapFriends.put(friend_item.getUser_id(), friend_item);
        }
        cursor.close();
        return true;
    }
    public static boolean loadGroupsFromDB() {
        Cursor cursor = db.query("friendGroup", null, "UID=?", new String[]{ImApplication.User_id}, null, null, null);
        Log.e("cursor1:",cursor.toString());
        if(cursor.getCount()==0)return false;//第一次登陆  好友分组为空
        while (cursor.moveToNext()) {
            String listName= cursor.getString(cursor.getColumnIndex("listName"));
            int listNO=cursor.getInt(cursor.getColumnIndex("listNO"));
            ImApplication.mapFriendGroup.put(listNO, listName);
            LinkedList<FriendInfo> friendlist= new LinkedList<FriendInfo>();
            ImApplication.mapGroupFriends.put(listNO,friendlist);
        }
        cursor.close();
        return true;
    }

    public static boolean KeepfriendsToDB() {
        int num1 = db.delete("friends", "ownerID=?",new String[]{ImApplication.User_id});//清除好友
        int num2 = db.delete("friendGroup", "UID=?", new String[]{ImApplication.User_id});//清除分组   先清除再插入
        Iterator iter1 = ImApplication.mapFriendGroup.entrySet().iterator();
        while (iter1.hasNext()) {
            Map.Entry entry = (Map.Entry) iter1.next();
            Integer key = (Integer)entry.getKey();
            String val = (String)entry.getValue();
            addGroup(key,val);
        }
        Iterator iter2 = ImApplication.mapFriends.entrySet().iterator();
        while (iter2.hasNext()) {
            Map.Entry entry = (Map.Entry) iter2.next();
            String key = (String)entry.getKey();
            FriendInfo friend_Item = (FriendInfo)entry.getValue();
            addFriend(friend_Item);
        }
        return true;
    }

    public static LinkedList<FriendInfo> loadFriendRequest(){
        LinkedList<FriendInfo>FriendRequestList=new LinkedList<FriendInfo>();
        Cursor cursor = db.query("friendRequest", null, "ownerID=?", new String[]{ImApplication.User_id}, null, null, null);
        Log.e("cursor:",cursor.toString());
        if(cursor.getCount()==0)return FriendRequestList;//第一次登陆  好友列表为空
        ImApplication.newFriendRequestNum=0;
        while (cursor.moveToNext()) {
            if(cursor.getInt(cursor.getColumnIndex("isAgree"))==0)++ImApplication.newFriendRequestNum;
            FriendInfo friend_item=new FriendInfo();
            friend_item.setUser_id(cursor.getString(cursor.getColumnIndex("friendID")));
            friend_item.setName(cursor.getString(cursor.getColumnIndex("Nick")));
            friend_item.setAgree(cursor.getInt(cursor.getColumnIndex("isAgree")));
            FriendRequestList.add(friend_item);
        }
        cursor.close();
        return FriendRequestList;
    }
    public static boolean addFriendRequest(FriendInfo friend){
        ContentValues values = new ContentValues();
        values.put("friendID",friend.getUser_id());
        values.put("ownerID", Integer.parseInt(ImApplication.User_id));
        values.put("Nick",friend.getName());
        values.put("isAgree", friend.getIsAgree());
        long lastId = db.insert("friendRequest", null, values);
        return lastId > 0;
    }
    public static boolean updateFriendRequest(FriendInfo friend) {
        ContentValues values = new ContentValues();
        values.put("friendID",friend.getUser_id());
        values.put("ownerID", Integer.parseInt(ImApplication.User_id));
        values.put("Nick",friend.getName());
        values.put("isAgree", friend.getIsAgree());
        int num = db.update("friendRequest",
                values,
                "ownerID=? and friendID=?",
                new String[]{ImApplication.User_id,friend.getUser_id()});
        return num == 1;
    }


}
