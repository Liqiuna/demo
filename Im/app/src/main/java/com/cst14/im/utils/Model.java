package com.cst14.im.utils;

import android.content.SharedPreferences;

import com.cst14.im.protobuf.ProtoClass;

/**
 * Created by zxm on 2016/8/27.
 */
public class Model {
    public static final int autoRgst = 1;
    public static final int fastRgst = 2;
    public static final int banRgst = 3;
    public static ProtoClass.UserCustomAttr mcustomAttrs;
    public static ProtoClass.UserDetail userDetail;
    public static int ID,role,RGST=1;
    public  static boolean  nickIsNeed=false, phoneIsNeed=false,
            photoIsNeed=false, addressIsNeed=false, ageIsNeed=false,
            sexIsNeed=false, birthIsNeed=false, mailIsNeed=false, qqIsNeed=false,
            wechatIsNeed=false;
   public static void setAttrType(boolean nick,boolean phone,boolean photo,
                                  boolean address,boolean age,boolean sex,
                                  boolean birth,boolean mail,boolean qq,
                                  boolean wechat)
   {nickIsNeed=nick;phoneIsNeed=phone;photoIsNeed=photo;addressIsNeed=address;
       ageIsNeed= age;sexIsNeed=sex;birthIsNeed=birth;mailIsNeed=mail;
       qqIsNeed=qq;wechatIsNeed=wechat;
   }
    public  static  boolean getwechatIsNeed(){
        return wechatIsNeed;
    }
    public  static  boolean getmailIsNeed(){
        return mailIsNeed;
    }
    public  static  boolean getqqIsNeed(){
        return qqIsNeed;
    }
    public  static  boolean getAddressIsNeed(){
        return addressIsNeed;
    }
    public  static  boolean getageIsNeed(){
        return  ageIsNeed;
    }
    public  static  boolean getsexIsNeed(){
        return sexIsNeed;
    }
    public  static  boolean getbirthIsNeed(){
        return birthIsNeed;
    }
    public  static  boolean getPhotoIsNeed(){
        return photoIsNeed;
    }
    public static boolean getNickIsNeed(){
        return nickIsNeed;
    }
    public static boolean getPhoneIsNeed(){
        return phoneIsNeed;
    }

    public void setUserpwd(String pwd){
        SharedPreferences.Editor editor = ImApplication.getSp().edit();
        editor.putString("currentUserPwd", pwd);
        editor.commit();
    }
    public static void setUickName(String nickName) {
        SharedPreferences.Editor editor = ImApplication.getSp().edit();
        editor.putString("currentUickName", nickName);
        editor.commit();
    }
    public static void setHeadimagePath(String headImage){
        SharedPreferences.Editor editor = ImApplication.getSp().edit();
        editor.putString("currentHeadImage",headImage);
        editor.commit();
    }
    public static void setRGST(int rgst){
        RGST=rgst;
    }
    public static int  getRGST(){
        return RGST;
    }
    public static void setDetail(ProtoClass.UserDetail UD){
        userDetail=UD;
    }
    public static  ProtoClass.UserDetail getUserDetail(){
        return userDetail;
    }
    public static void setCustomAttr( ProtoClass.UserCustomAttr myattr){
        mcustomAttrs=myattr;
    }
    public static  ProtoClass.UserCustomAttr getCustomAttr(){
        return   mcustomAttrs;
    }
    public static  void setUserID(int id){
        ID=id;
    }
    public static  int  getID(){
        return ID;
    }
    public static  void  setRole(int id){
        role=id;
    }
    public static  int  getRole(){
        return role;
    }
    public static String getHeadImage(){
        String Image=ImApplication.getSp().getString("currentHeadImage", "");
        return Image;
    }
    public static String getUsername(){
        String userName=ImApplication.getSp().getString("currentUserName", "");
        return userName;
    }
    public static String getnickName(){
        String nickName=ImApplication.getSp().getString("currentUickName", "");
        return nickName;
    }
    public static String getPwb(){
        String Pwb=ImApplication.getSp().getString("currentUserPwd", "");
        return Pwb;
    }
    public static void  setUsername(String username){
        SharedPreferences.Editor editor = ImApplication.getSp().edit();
        editor.putString("currentUserName",username);
        editor.commit();
    }


}
