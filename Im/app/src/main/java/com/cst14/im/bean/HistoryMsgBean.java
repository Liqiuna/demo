package com.cst14.im.bean;

public class HistoryMsgBean {
    private  String msgId;
    private  String friendId;
    private  boolean isMineMsg;
    private  String textContent;
    private String sendTime;
    private  String groupMemberId;
    private  boolean isPerMsg;
   public HistoryMsgBean(String msgId, String friendId, boolean isMineMsg, String textContent, String sendTime,boolean isPerMsg){
       this.msgId=msgId;
       this.friendId=friendId;
        this .isMineMsg=isMineMsg;
       this.textContent=textContent;
       this.sendTime=sendTime;
       this.isPerMsg=isPerMsg;
    }
    public HistoryMsgBean(String msgId, String groupMemberId, String textContent,String sendTime,boolean isPerMsg){
        this.msgId=msgId;
        this.groupMemberId=groupMemberId;
        this.textContent=textContent;
        this.sendTime=sendTime;
        this.isPerMsg=isPerMsg;
    }
    public  String getMsgId(){
        return  msgId;
    }
    public  String getFriendId(){
        return  friendId;
    }
    public boolean getIsMineMsg(){
        return  isMineMsg;
    }

    public String getTextContent(){
        return  textContent;
    }

    public String getSendTime(){
        return  sendTime;
    }
    public String getGroupMemberId(){return groupMemberId;}
    public boolean getIsPerMsg(){
        return  isPerMsg;
    }
}
