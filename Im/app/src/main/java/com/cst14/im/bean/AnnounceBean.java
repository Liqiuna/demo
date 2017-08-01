package com.cst14.im.bean;

import com.cst14.im.protobuf.ProtoClass;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/8/28 0028.
 */
public class AnnounceBean implements Serializable {

    private int groupID;
    private int announceID;
    private String title;
    private String sender;
    private String sendTime;
    private String content;

    public AnnounceBean() {}

    public AnnounceBean(ProtoClass.GroupAnnounce announce) {
        setGroupID(announce.getGroupID());
        setAnnounceID(announce.getAnnounceID());
        setTitle(announce.getTitle());
        setSender(announce.getSender());
        setSendTime(announce.getSendTime());
        setContent(announce.getContent());
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public void setAnnounceID(int announceID) {
        this.announceID = announceID;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getGroupID() {
        return groupID;
    }

    public int getAnnounceID() {
        return announceID;
    }

    public String getSender() {
        return sender;
    }

    public String getSendTime() {
        return sendTime;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
