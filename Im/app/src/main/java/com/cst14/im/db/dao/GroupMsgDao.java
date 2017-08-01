package com.cst14.im.db.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cst14.im.bean.HistoryMsgBean;
import com.cst14.im.bean.UserChat;
import com.cst14.im.db.helper.DBHelper;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.ImApplication;

import java.util.LinkedList;

/**
 * Created by qi on 16-9-13.
 */
public class GroupMsgDao {
    private static DBHelper helper;
    public static SQLiteDatabase db;

    static {
        helper = new DBHelper(ImApplication.mainActivity);
        db = helper.getWritableDatabase();
    }

    /*
        "      1 msgid integer primary key autoincrement," +
               2"group_id int not null," +
                3"send_id  varchar(15) not null," +
                4"msg_type varchar(10) not null," +
               5 "str_content text," +
               6 "file_path varchar(60)," +
               7 "file_name varchar(30)"+
              8  "file_size varchar(10)"+
              9 "thumb_finger_print varchar(30)," +
              10"voice_time int," +
              11  "msg_unique_tag varchar(10) not null"+
              12  "send_time varchar(15)," +
              13 "read_time varchar(15)," +
              14  "is_send_ok int default 0," +
              15  "is_read int not null default 0"+
                ");");
    */
    public static LinkedList<UserChat> getMsgList(int groupID) {
        Log.i("groupMsgDao", "1");
        LinkedList<UserChat> msgList = new LinkedList<>();
        Cursor cursor = db.query("group_msg", null, "group_id=?", new String[]{"" + groupID}, null, null, null);
        Log.i("groupMsgDao", "crusor total is " + cursor.getCount());
        if (cursor.getCount() == 0) {
            return msgList;
        }
        cursor.moveToLast();
        int sumMsg = 0;
        while (true) {
            UserChat userChat = new UserChat();
            userChat.setName(cursor.getString(2));
            userChat.setMsgType(cursor.getString(3));
            userChat.setStrContent(cursor.getString(4));
            userChat.setFilePath(cursor.getString(5));
            userChat.setFileName(cursor.getString(6));
            userChat.setFileSize(cursor.getString(7));
            userChat.setFileFingerPrint(cursor.getString(8));
            userChat.setVoiceTime(cursor.getInt(9));
            userChat.setMsgUniqueTag(cursor.getString(10));
            userChat.setMsgSendTime(cursor.getString(11));
            userChat.setIsSendOK(cursor.getInt(13));
            userChat.setIsRead(cursor.getInt(14));
            msgList.addFirst(userChat);
            sumMsg++;
            if (sumMsg == 10 || cursor.isFirst()) {
                break;
            }
            cursor.moveToPrevious();

        }
        Log.i("groupMsgDao", "sunMsg is " + sumMsg);
        return msgList;
    }



    public static void addMsg(ProtoClass.Msg.Builder msg, String filePath, int who) {
        ContentValues cValue = new ContentValues();
        cValue.put("group_id", Integer.valueOf(msg.getGroupMsg().getGroupID()));
        cValue.put("send_id", msg.getGroupMsg().getSenderName());
        cValue.put("msg_type", msg.getGroupMsg().getDataType().toString());
        cValue.put("str_content", msg.getGroupMsg().getText());
        cValue.put("file_name", msg.getGroupMsg().getFileInfo().getName());
        cValue.put("file_size", msg.getGroupMsg().getFileInfo().getName());
        cValue.put("file_path", filePath);
        cValue.put("thumb_finger_print", msg.getToken());
        cValue.put("voice_time", msg.getGroupMsg().getVoiceTime());
        cValue.put("msg_unique_tag", msg.getMsgUniqueTag());
        cValue.put("send_time", msg.getGroupMsg().getMsgTime());
        Log.e("sqlite time",msg.getGroupMsg().getMsgTime());
        if(who==0){
            cValue.put("is_read",1);
        }
        if(who==1){
            cValue.put("is_send_ok", 1);
        }
        db.insert("group_msg", null, cValue);
    }

    public static LinkedList<HistoryMsgBean> queryHistoryMsg(String msgKeyWord, String groupId) {
        String current_sql_sel = "SELECT  * FROM group_msg" +
                " where group_id = " + groupId +
                " and  str_content like '%" + msgKeyWord + "%'" +
                "order by send_time desc";
        Cursor cursor = db.rawQuery(current_sql_sel, null);
        LinkedList<HistoryMsgBean> msgList = new LinkedList<>();
        while (cursor.moveToNext()) {
            String msgId = cursor.getString(0);
            String groupMemberId = cursor.getString(2);
            String StrContent = cursor.getString(4);
            String sendTime = cursor.getString(11);
            HistoryMsgBean bean = new HistoryMsgBean(msgId, groupMemberId, StrContent, sendTime, false);
            msgList.add(bean);
        }
        cursor.close();
        return msgList;
    }
   public  static  String getGroupMsgOldestTime(int groupId){
       String current_sql_sel="select send_time" +
                             " from group_msg \n" +
                               " where group_id="+groupId +
                              " order by send_time asc limit 1";
       Cursor cursor=db.rawQuery(current_sql_sel,null);
       String groupMsgOldestTime="";
       while (cursor.moveToNext()){
           groupMsgOldestTime=cursor.getString(0);
       }
       cursor.close();
       return groupMsgOldestTime;
   }

    public static void updateMsgFeedBack(ProtoClass.Msg.Builder msg, int isSendOK) {
        ContentValues cValue = new ContentValues();
        cValue.put("send_time", msg.getGroupMsg().getMsgTime());
        cValue.put("is_send_ok",isSendOK);
        String whereClause = " group_id=? and send_id=? and msg_unique_tag=?";
        String[] whereArgs = {Integer.toString(msg.getGroupMsg().getGroupID()), msg.getGroupMsg().getSenderName(), msg.getMsgUniqueTag()};
        db.update("group_msg", cValue, whereClause, whereArgs);
    }

    public static void updateMsgFeedBackRead(ProtoClass.Msg.Builder msg) {
        Log.i("dbhelp","update msg feed back read");
        Log.i("",msg.toString());

        ContentValues cValue = new ContentValues();
        cValue.put("is_read",1);
        String whereClause = " group_id=? and send_id=? and msg_unique_tag=?";
        String[] whereArgs = {Integer.toString(msg.getGroupMsg().getGroupID()), msg.getGroupMsg().getSenderName(), msg.getMsgUniqueTag()};
        db.update("group_msg", cValue, whereClause, whereArgs);
    }

    public static void deleteMsg(UserChat userChat, int groupID) {
        String whereClause = " group_id=? and send_id=? and msg_unique_tag=?";
        String[] whereArgs = {""+groupID,userChat.getName(),userChat.getMsgUniqueTag()};
        db.delete("group_msg",whereClause,whereArgs);
    }


}
