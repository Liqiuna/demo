package com.cst14.im.bean;

import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.ImApplication;

import java.io.Serializable;
import java.util.ArrayList;

public class UserGroup implements Serializable {

    public static int MEMBER_ROLE_CREATER = 0;
    public static int MEMBER_ROLE_ADMIN = 1;
    public static int MEMBER_ROLE_COMMON = 2;

    private int ID;
    private String name;
    private String intro;
    private ArrayList<Member> members;
    private Member owner;

    //the info for current account
    private Member myInfo;
    public boolean isMeOwner = false;
    public boolean isMeAdmin = false;

    public UserGroup() {
    }

    public UserGroup(ProtoClass.GroupInfo groupData) {
        ID = groupData.getGroupID();
        name = groupData.getGroupName();
        intro = groupData.getGroupIntro();

        members = new ArrayList<>();
        for (ProtoClass.GroupMemberInfo memberInfo : groupData.getMemberListList()) {
            Member newMember = new Member(memberInfo);
            members.add(newMember);
            if (isOwner(newMember)) {
                this.owner = newMember;
            }
        }
        myInfo = new Member(groupData.getMyInfo());

        isMeOwner = isOwner(myInfo);
        isMeAdmin = isAdmin(myInfo);
    }

    public ArrayList<Member> getMembers() {
        return members;
    }

    public int getID() {
        return ID;
    }

    public String getIntro() {
        return intro;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Member getOwner() {
        return owner;
    }

    public Member getMyInfo() {
        return myInfo;
    }

    public static boolean isOwner(Member member) {
        return member.role == MEMBER_ROLE_CREATER;
    }

    public static boolean isAdmin(Member member) {
        return member.role == MEMBER_ROLE_ADMIN;
    }

    public static boolean isOwnerOrAdmin(Member member) {
        return isOwner(member) || isAdmin(member);
    }

    public static class Member implements Serializable {
        int userID;
        String userName;
        String nameCard;
        int role;

        public Member() {
        }

        public Member(ProtoClass.GroupMemberInfo memberInfo) {
            this.userID = memberInfo.getUserID();
            this.userName = memberInfo.getUserName();
            this.nameCard = memberInfo.getNamecard();
            this.role = MEMBER_ROLE_COMMON;
            this.role = memberInfo.getRole();
        }

        public String getNameCard() {
            return nameCard;
        }

        public int getUserID() {
            return userID;
        }

        public String getUserName() {
            return userName;
        }
    }
}


