package com.cst14.im.Fragment;

/**
 * Created by I_C_U on 2016/8/24.
 */

public class FriendInfo {

    private String User_id="";      //好友账号
    private String Name="";         //好友昵称
    private String Mark="";         //好友备注
    private int ListNo=1;         //好友分组
    private int isAgree=0;    //好友请求 是否同意    0未处理    1已同意  2拒绝

    private boolean isOnline;//好友是否在线    需要什么的可以增加
    public String getUser_id() {
        return User_id;
    }
    public int getListNo() {
        return ListNo;
    }
    public int getIsAgree(){return isAgree;}
    public String getName() {
        return Name;
    }
    public String getMark() {
        return Mark;
    }

    public void setListNo(int ListNo){
        this.ListNo=ListNo;
    }
    public void setMark(String Mark ) {
        this.Mark=Mark;
    }
    public void setUser_id(String User_id ) {
        this.User_id=User_id;
    }
    public void setName(String Name ) {
        this.Name=Name;
    }
    public void setAgree(int isAgree){
        this.isAgree=isAgree;
    }

    public FriendInfo(int User_id, String Name,String Mark,int ListNo) {
        this.User_id = String.valueOf(User_id);
        this.Name = Name;
        this.Mark=Mark;
        this.ListNo=ListNo;
    }
    public FriendInfo(int User_id, String Name,int isAgree) {
        this.User_id = String.valueOf(User_id);
        this.Name = Name;
        this.isAgree=isAgree;
    }
    public FriendInfo(int User_id, String Name) {
        this.User_id = String.valueOf(User_id);
        this.Name = Name;
    }
    public FriendInfo() {
    }

}