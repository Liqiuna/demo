package com.cst14.im.bean;

import com.cst14.im.protobuf.ProtoClass;

/**
 * Created by Administrator on 2016/9/1 0001.
 */
public class NearbyBean {

    private byte[] avatar;
    private String name;
    private String nick;
    private String sex;
    private int age;
    private String intro;
    private double distance;
    private String updateTime;

    public NearbyBean(ProtoClass.Nearby nearby) {
        this.avatar = nearby.getAvatar().toByteArray();
        this.name = nearby.getName();
        this.nick = nearby.getNick();
        this.sex = nearby.getSex();
        this.age = nearby.getAge();
        this.intro = nearby.getIntro();
        this.distance = nearby.getDistance();
        this.updateTime = nearby.getUpdateTime();
    }

    public byte[] getAvatar() {
        return avatar;
    }

    public String getName() {
        return name;
    }

    public String getNick() {
        return nick;
    }

    public String getSex() {
        return sex;
    }

    public int getAge() {
        return age;
    }

    public String getIntro() {
        return intro;
    }

    public double getDistance() {
        return distance;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public boolean isMan() {
        return "男".equals(sex);
    }

    public boolean isWoman() {
        return "女".equals(sex);
    }
}
