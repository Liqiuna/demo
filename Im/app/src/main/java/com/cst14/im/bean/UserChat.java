package com.cst14.im.bean;

import java.io.Serializable;

/**
 * Created by qi on 16-8-28.
 */
   /*
        "msgid integer primary key autoincrement," +
                "group_id int not null," +
                "send_id  varchar(15) not null," +
                "msg_type varchar(10) not null," +
                "str_content text," +
                "file_path varchar(60)," +
                "file_name varchar(30)"+
                "file_size varchar(10)"+
                "thumb_finger_print varchar(30)," +
                "voice_time int," +
                "msg_send_time string," +
                "msg_unique_tag varchar(10) not null"+
                "send_time varchar(15)," +
                "read_time varchar(15)," +
                "is_send_ok int default 0," +
                "is_read int not null default 0"+
                ");")
    */
public class UserChat {
    private String name, strContent, msgType,msgUniqueTag,url;
    private String filePath,fileName,fileSize,fileFingerPrint,msgSendTime;
    private int voiceTime,isSendOK,isRead;

    public UserChat(){}

    public void setName(String name){this.name = name;}
    public void setStrContent(String strContent){this.strContent = strContent;}
    public void setFilePath(String filePath){this.filePath = filePath;}
    public void setMsgType(String msgType){this.msgType = msgType;}
    public void setUrl(String url){this.url = url;}
    public void setVoiceTime(int voiceTime){this.voiceTime = voiceTime;}
    public void setMsgUniqueTag(String msgUniqueTag){this.msgUniqueTag=msgUniqueTag;}
    public void setIsSendOK(int isSendOK){this.isSendOK=isSendOK;}
    public void setFileName(String fileName){this.fileName=fileName;}
    public void setFileSize(String fileSize){this.fileSize=fileSize;}
    public void setFileFingerPrint(String fileFingerPrint){this.fileFingerPrint=fileFingerPrint;}
    public void setMsgSendTime(String msgSendTime){this.msgSendTime=msgSendTime;}
    public void setIsRead(int isRead){this.isRead=isRead;}


    public String getName(){return name;}
    public String getStrContent(){return strContent;}
    public String getFilePath(){return filePath;}
    public String getMsgType(){return msgType;}
    public int getVoiceTime(){return voiceTime;}
    public String getMsgUniqueTag(){return msgUniqueTag;}
    public int getIsSendOK(){return isSendOK;}
    public String getFileName(){return fileName;}
    public String getFileSize(){return fileSize;}
    public String getFileFingerPrint(){return fileFingerPrint;}
    public String getMsgSendTime(){return msgSendTime;}
    public int getIsRead(){return isRead;}
}
