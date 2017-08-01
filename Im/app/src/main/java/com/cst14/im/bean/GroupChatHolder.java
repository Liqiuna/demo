package com.cst14.im.bean;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ListView;

import com.cst14.im.adapter.GroupMsgAdapter;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.ImApplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by qi on 16-8-29.
 */
public class GroupChatHolder {
    private Context context;
    private static LinkedList<UserChat> list;
    private ListView lvMsgList;
    public static List<UserChat> userChatList;
    private GroupMsgAdapter adapter;
    private static volatile GroupChatHolder groupChatHolder;

    public GroupChatHolder() {
    }

    /*
        public static GroupChatHolder getGroupChatHolder() {
            if (groupChatHolder == null) {
                synchronized (GroupChatHolder.class) {
                    if (groupChatHolder == null) {
                        groupChatHolder = new GroupChatHolder();
                    }
                }
            }
            return groupChatHolder;
        }
    */
    public GroupChatHolder(Context context, LinkedList<UserChat> list) {
        this.context = context;
        this.list = list;
    }

    /**
     * @param list 对话列表
     * @return
     * @throws
     */
    /*
    private String mySerialize(List<UserChat> list) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                byteArrayOutputStream);
        objectOutputStream.writeObject(list);
        String serStr = byteArrayOutputStream.toString("ISO-8859-1");
        serStr = java.net.URLEncoder.encode(serStr, "UTF-8");
        objectOutputStream.close();
        byteArrayOutputStream.close();
        return serStr;
    }

    /**
     * 反序列化对象
     *
     * @param str
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     *
    private LinkedList<UserChat> myDeSerialization(String str) throws IOException,
            ClassNotFoundException {
        String redStr = java.net.URLDecoder.decode(str, "UTF-8");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                redStr.getBytes("ISO-8859-1"));
        ObjectInputStream objectInputStream = new ObjectInputStream(
                byteArrayInputStream);
        LinkedList<UserChat> list = (LinkedList<UserChat>) objectInputStream.readObject();
        objectInputStream.close();
        byteArrayInputStream.close();
        return list;
    }

    private void saveObject(String strObject, int groupID) {
        SharedPreferences sp = ImApplication.instance.getSharedPreferences("group_" + groupID, 0);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("person", strObject);
        edit.commit();
    }

    private String getObject(int groupID) {
        SharedPreferences sp = ImApplication.instance.getSharedPreferences("group_" + groupID, 0);
        return sp.getString("person", null);
    }

    public LinkedList<UserChat> getList(int groupID) {
        if (getObject(groupID) == null) {
            return list;
        }
        try {
            list = myDeSerialization(getObject(groupID));
            return list;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return list;
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return list;
        }
    }

    public synchronized LinkedList<UserChat> setMsg(ProtoClass.GroupMsg.Builder groupMsg, String filePath) {
        Log.i("qiqi", "更新信息啦");
        UserChat userChat = new UserChat();
        userChat.setMsgType(groupMsg.getDataType().toString());
        userChat.setName(groupMsg.getSenderName());
        userChat.setStrContent(groupMsg.getText());
        userChat.setFilePath(filePath);
        userChat.setUrl(ImApplication.FILE_SERVER_HOST + File.separator + groupMsg.getFileInfo().getPath());
        list.add(userChat);
        try {
            saveObject(mySerialize(list), groupMsg.getGroupID());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return list;
    }

    public LinkedList<UserChat> setVoiceMsg(int voiceTime, String filePath, int groupID) {
        Log.i("qiqi", "存入语音信息");
        UserChat userChat = new UserChat();
        userChat.setMsgType(ProtoClass.DataType.VOICE.toString());
        userChat.setName(ImApplication.User_id);
        userChat.setFilePath(filePath);
        userChat.setVoiceTime(voiceTime);
        list = this.getList(groupID);
        list.add(userChat);
        try {
            saveObject(mySerialize(list), groupID);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return list;
    }
*/
    public UserChat protoToUserchat(ProtoClass.Msg.Builder msg,String filePath){
        UserChat userChat = new UserChat();
        userChat.setMsgType(msg.getGroupMsg().getDataType().toString());
        userChat.setName(msg.getGroupMsg().getSenderName());
        userChat.setStrContent(msg.getGroupMsg().getText());
        userChat.setMsgUniqueTag(msg.getMsgUniqueTag());
        userChat.setFilePath(filePath);
        userChat.setVoiceTime(msg.getGroupMsg().getVoiceTime());
        userChat.setIsSendOK(0);
        userChat.setIsRead(1);
        return userChat;
    }
}
