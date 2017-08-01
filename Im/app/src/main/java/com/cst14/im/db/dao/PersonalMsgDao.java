package com.cst14.im.db.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;

import com.cst14.im.bean.HistoryMsgBean;
import com.cst14.im.db.helper.DBHelper;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.sessionUtils.ChatMsgBean;
import com.cst14.im.utils.sessionUtils.SessionHolder;

import java.util.LinkedList;

/**
 * Created by MRLWJ on 2016/9/6.
 */
public class PersonalMsgDao {
    private static DBHelper helper;
    public static SQLiteDatabase db;

    static {
        helper = new DBHelper(ImApplication.mainActivity);
        db = helper.getWritableDatabase();
    }

    public static boolean addHolder(SessionHolder target) {
        ContentValues values = new ContentValues();
        values.put("my_id", ImApplication.User_id);
        values.put("friend_id", target.userAccount);
        values.put("fix_top", target.isFixTop());
        values.put("no_tip", target.isNoTip());
        values.put("unread_msg_count", target.getUnReadMsgCount());
        values.put("last_brif_msg", target.lastBrifMsg);
        values.put("last_brif_msg_time", target.lastBrifMsgTime);
        long lastId = db.insert("session_holders", null, values);
        return lastId > 0;
    }

    public static boolean upDateHolder(SessionHolder target) {
        ContentValues values = new ContentValues();
        values.put("fix_top", target.isFixTop());
        values.put("no_tip", target.isNoTip());
        values.put("unread_msg_count", target.getUnReadMsgCount());
        values.put("last_brif_msg", target.lastBrifMsg);
        values.put("last_brif_msg_time", target.lastBrifMsgTime);
        int num = db.update("session_holders",
                values,
                "my_id=? and friend_id=?",
                new String[]{ImApplication.User_id, target.userAccount});
        return num == 1;
    }

    public static boolean delHolder(String account) {
        int num = db.delete("session_holders",
                "my_id=? and friend_id=?",
                new String[]{ImApplication.User_id, account});

                 db.delete("msg_data",
                "my_id=? and friend_id=?",
                new String[]{ImApplication.User_id, account});
        return num == 1;
    }

    public static boolean addMsg(ChatMsgBean msg) {
        ContentValues values = new ContentValues();
        values.put("my_id", ImApplication.User_id);
        values.put("friend_id", msg.getParentSessionHolder().userAccount);
        values.put("is_mine_msg", msg.isMineMsg());
        values.put("text_content", msg.getTextContent());
        values.put("is_readed", msg.isReaded);
        values.put("data_type", msg.getDataType());
        values.put("msg_id", msg.msgId);
        values.put("msg_send_finished", msg.msgSentFinished);
        values.put("sent_time", msg.sentTime);
        values.put("read_time", msg.readTime);
        values.put("thumb_finger_print", msg.getThumbFingerPrint());
        values.put("file_name", msg.getFileName());
        values.put("file_size", msg.getFileSize());
        long lastId = db.insert("msg_data", null, values);
        return lastId > 0;
    }
    public static LinkedList<SessionHolder> getAllHolder() {
        Cursor cursor = db.query("session_holders", null, "my_id=?", new String[]{ImApplication.User_id}, null, null, null);
        LinkedList<SessionHolder> holders = new LinkedList<SessionHolder>();
        while (cursor.moveToNext()) {
            SessionHolder holder = new SessionHolder();
            String account = cursor.getString(2);
            boolean isFixTop = cursor.getInt(3) == 1;
            boolean isNoTip = cursor.getInt(4) == 1;
            int unReadCount = cursor.getInt(5);
            String lastBrifMsg = cursor.getString(6);
            String lastBrifMsgTime = cursor.getString(7);
            holder.userAccount = account;
            holder.setFixTop(isFixTop);
            holder.setNoTip(isNoTip);
            holder.unReadMsgCount = unReadCount;
            holder.lastBrifMsg = lastBrifMsg;
            holder.lastBrifMsgTime = lastBrifMsgTime;
            holders.add(holder);
        }
        cursor.close();
        return holders;
    }
    public static LinkedList<ChatMsgBean> getAllMsg(String friendId) {
        Cursor cursor = db.query("msg_data", null, "my_id=? and friend_id=?", new String[]{ImApplication.User_id, friendId}, null, null, null);
        SessionHolder curSessionHolder = ImApplication.instance.getCurSessionHolder();
        LinkedList<ChatMsgBean> msgList = new LinkedList<ChatMsgBean>();
        while (cursor.moveToNext()) {
            boolean isMineMsg = cursor.getInt(3) == 1;
            String textCopntent = cursor.getString(4);
            boolean isReaded = cursor.getInt(5) == 1;
            int dataType = cursor.getInt(6);
            String msgId = cursor.getString(7);
            boolean msgSendFinished = cursor.getInt(8) == 1;
            String sendTime = cursor.getString(9);
            String readTime = cursor.getString(10);
            String thumbFingerPrint = cursor.getString(11);
            String fileName = cursor.getString(12);
            String fileLen = cursor.getString(13);
            String from = isMineMsg ? "" : friendId;
            ChatMsgBean bean=null;
            switch (dataType) {
                case ProtoClass.DataType.TEXT_VALUE:
                    bean = ChatMsgBean.newTextMsg(textCopntent, from);
                    break;
                case ProtoClass.DataType.VOICE_VALUE:
                    bean = ChatMsgBean.newVoiceMsg(fileName, fileLen, from);
                    bean.setFileFingerPrint(textCopntent);
                    break;
                case ProtoClass.DataType.FILE_VALUE:
                    bean = ChatMsgBean.newFileMsg(fileName, fileLen, from);
                    bean.setFileFingerPrint(textCopntent);
                    break;
                case ProtoClass.DataType.VIDEO_VALUE:
                    bean = ChatMsgBean.newVideoMsg(fileName, fileLen, from);
                    bean.setFileFingerPrint(textCopntent);
                    bean.setThumbFingerPrint(thumbFingerPrint);
                    break;
            }
            bean.msgId = msgId;
            bean.isReaded = isReaded;
            bean.msgSentFinished = msgSendFinished;
            bean.sentTime = sendTime;
            bean.readTime = readTime;
            bean.setParentSessionHolder(curSessionHolder);
            bean.showMsgSentState(msgSendFinished?"":"[离线]",true);
            msgList.add(bean);
        }
        cursor.close();
        return msgList;
    }
    public  static LinkedList<HistoryMsgBean>queryHistoryMsg(String msgKeyWord, String friendId){

        String current_sql_sel = "SELECT  * FROM msg_data " +
                "where (my_id="+ImApplication.User_id +
                " and friend_id="+friendId+ ") " +
                " and  text_content like '%"+msgKeyWord+"%'"+
                "order by sent_time desc";
        Cursor cursor=db.rawQuery(current_sql_sel,null);
        LinkedList<HistoryMsgBean>msgList=new LinkedList<>();
        while (cursor.moveToNext()){
            String msgId = cursor.getString(7);
            boolean isMineMsg=cursor.getInt(3)==1;
            String textContent=cursor.getString(4);
            String sendTime = cursor.getString(9);
            HistoryMsgBean bean=new HistoryMsgBean(msgId,friendId,isMineMsg,textContent,sendTime,true);
            msgList.add(bean);
        }
        cursor.close();
        return  msgList;
    }
}
