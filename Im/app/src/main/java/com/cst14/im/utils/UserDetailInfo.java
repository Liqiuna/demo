package com.cst14.im.utils;

import java.io.Serializable;

/**
 * Created by Belinda Y on 2016/9/25.
 */
public class UserDetailInfo implements Serializable {
    int uID;
    String nick="";
    String phone="";
    String address="";
    String QQ ="";
    String wechat="";
    String sex="";
    int  age ;
    String idCard="" ;
    String mail="" ;
    String creCard="" ;
    String debtCard ="";
    String stdNo ="";
    String birthday="";
    public UserDetailInfo(int uID, String nick, String phone, String address, String QQ,
                          String wechat, String sex, int age, String idCard,
                          String mail, String creCard, String debtCard, String stdNo, String birthday){
       this.uID=uID;
        this.nick=nick;
        this.phone=phone;
        this.address=address;

        this.QQ=QQ;
        this.wechat=wechat;
        this.sex=sex;

        this.age=age;
        this.idCard=idCard;
        this.mail=mail;

        this.creCard=creCard;
        this.debtCard=debtCard;
        this.stdNo=stdNo;

        this.birthday=birthday;
    }
    public int getuID(){
        return  uID;
    }
    public String getNick(){
        return  nick;
    }
    public  int getAge(){
        return  age;
    }
    public  String getPhone(){
        return  phone;
    }
    public  String getAddress(){
        return  address;
    }
    public  String getQQ(){
        return  QQ;
    }
    public String getWechat(){
        return  wechat;
    }
    public  String getSex(){
        return  sex;
    }
    public  String getIdCard(){
        return  idCard;
    }
    public  String getMail(){
        return mail;
    }
    public  String getCreCard(){
        return creCard;
    }
    public  String getDebtCard(){
        return  debtCard;
    }
    public  String getStdNo(){
        return  stdNo;
    }
    public  String getBirthday(){
        return  birthday;
    }

}
