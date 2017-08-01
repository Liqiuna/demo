package com.cst14.im.listener;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.cst14.im.activity.GroupInfoActivity;
import com.cst14.im.baseClass.iGroupInfoView;
import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.bean.UserGroup;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;

import java.util.List;

public class GroupInfoPresenter implements iPresenter {
    private static final String TAG = "GroupInfoPresenter";
    private Handler handler = new Handler(Looper.getMainLooper());

    private iGroupInfoView view;

    public GroupInfoPresenter(iGroupInfoView view) {
        this.view = view;
        Tools.addPresenter(this);
    }

    @Override
    public void onProcess(ProtoClass.Msg msg) {
        switch (msg.getMsgType()) {
            case GET_GROUP_PUBLIC_INFO:
                this.onProcessGetPubInfo(msg);
                break;
            case GET_GROUP_PERSONAL_INFO:
                this.onProcessGetPerInfo(msg);
                break;
            case DEL_GROUP:
                onProcessDelGroup(msg);
                break;
            case EXIT_GROUP:
                onProcessExitGroup(msg);
                break;
            case DEL_GROUP_MEMBER:
                onProcessRemoveGroupMember(msg);
                break;
            case INVITE_MEMBER_TO_JOIN_GROUP:
                onProcessInviteMember(msg);
                break;
            case EDIT_GROUP_INFO:
                onProcessModifyGroupInfo(msg);
                break;
            case EDIT_GROUP_NAMECARD:
                onProcessModifyGroupNameCard(msg);
                break;
            case SET_GROUP_ADMIN:
                onProcessSetAdmin(msg);
                break;
            case REMOVE_GROUP_ADMIN:
                onProcessRemoveAdmin(msg);
                break;
            default:
                break;
        }
    }

    //处理获取公共群资料的响应
    private void onProcessGetPubInfo(final ProtoClass.Msg msg) {
        Log.e(TAG, "onProcessGetPubInfo: " + msg.toString());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (msg.getResponseState() == ProtoClass.StatusCode.SUCCESS) {
                    view.showGetInfoSucceed(new UserGroup(msg.getGroupInfo(0)));
                } else {
                    view.showGetInfoFail();
                }
            }
        });
    }

    //处理获取个人群资料的响应
    private void onProcessGetPerInfo(final ProtoClass.Msg msg) {
        Log.e(TAG, "onProcessGetPerInfo: " + msg.toString());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (msg.getResponseState() == ProtoClass.StatusCode.SUCCESS) {
                    view.showGetInfoSucceed(new UserGroup(msg.getGroupInfo(0)));
                } else {
                    view.showGetInfoFail();
                }
            }
        });
    }

    //处理加入群的响应
    private void onProcessJoinGroup(final ProtoClass.Msg msg) {
        Log.e(TAG, "onProcessJoinGroup: " + msg.toString());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (msg.getResponseState() == ProtoClass.StatusCode.SUCCESS) {
                    view.showJoinGroupSucceed();
                } else {
                    view.showJoinGroupFail();
                }
            }
        });
    }

    //处理成员退群的响应
    private void onProcessExitGroup(final ProtoClass.Msg msg) {
        Log.e(TAG, "onProcessExitGroup: " + msg.toString());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (msg.getResponseState() == ProtoClass.StatusCode.SUCCESS) {
                    view.showExitGroupSucceed();
                } else {
                    view.showRequestFail();
                }
            }
        });
    }

    //处理移除成员的响应
    private void onProcessRemoveGroupMember(final ProtoClass.Msg msg) {
        Log.e(TAG, "onProcessRemoveGroupMember: " + msg.toString());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (msg.getResponseState() == ProtoClass.StatusCode.SUCCESS) {
                    view.showRemoveGroupMemberSucceed();
                } else {
                    view.showRequestFail();
                }
            }
        });
    }

    //处理解散群的响应
    private void onProcessDelGroup(final ProtoClass.Msg msg) {
        Log.e(TAG, "onProcessDelGroup: " + msg.toString());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (msg.getResponseState() == ProtoClass.StatusCode.SUCCESS) {
                    view.showDelGroupSucceed();
                } else {
                    view.showRequestFail();
                }
            }
        });
    }

    //处理添加成员的响应
    private void onProcessInviteMember(final ProtoClass.Msg msg) {
        Log.e(TAG, "onProcessInviteMember: " + msg.toString());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (msg.getResponseState() == ProtoClass.StatusCode.SUCCESS) {
                    view.showInviteMemberToJoinSucceed();
                } else {
                    view.showRequestFail();
                }
            }
        });
    }

    //处理修改群名，群简介的响应
    private void onProcessModifyGroupInfo(final ProtoClass.Msg msg) {
        Log.e(TAG, "onProcessModifyGroupInfo: " + msg.toString());
        final String type = msg.getStrkey();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (msg.getResponseState() == ProtoClass.StatusCode.SUCCESS) {
                    return;
                }

                if (type.equals("name")) {
                    view.showModifyGroupInfoFail(GroupInfoActivity.REQUEST_CODE_EDIT_GROUP_NAME);
                } else if (type.equals("intro")) {
                    view.showModifyGroupInfoFail(GroupInfoActivity.REQUEST_CODE_EDIT_GROUP_INTRO);
                }

            }
        });
    }

    //处理修改群名片的响应
    private void onProcessModifyGroupNameCard(final ProtoClass.Msg msg) {
        Log.e(TAG, "onProcessModifyGroupNameCard: " + msg.toString());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (msg.getResponseState() == ProtoClass.StatusCode.SUCCESS) {
                    return;
                }
                view.showModifyGroupInfoFail(GroupInfoActivity.REQUEST_CODE_EDIT_GROUP_NAMECARD);
            }
        });
    }

    //处理设置管理员的响应
    private void onProcessSetAdmin(final ProtoClass.Msg msg) {
        Log.e(TAG, "onProcessSetAdmin: " + msg.toString());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (msg.getResponseState() == ProtoClass.StatusCode.SUCCESS) {
                    view.showSetAdminSucceed();
                } else {
                    view.showRequestFail();
                }
            }
        });
    }


    //处理移除管理员的响应
    private void onProcessRemoveAdmin(final ProtoClass.Msg msg) {
        Log.e(TAG, "onProcessRemoveAdmin: " + msg.toString());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (msg.getResponseState() == ProtoClass.StatusCode.SUCCESS) {
                    view.showRemoveAdminSucceed();
                } else {
                    view.showRequestFail();
                }
            }
        });
    }

    //------

    //获取群公开资料
    public void getGroupPublicInfo(UserGroup userGroup) {
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setMsgType(ProtoClass.MsgType.GET_GROUP_PUBLIC_INFO);
        builder.setAccount(ImApplication.instance.getCurUser().getName()).setGroupID(userGroup.getID());

        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                onProcess(responseMsg);
                return true;
            }
        });
    }

    //获取群非公开资料，含群成员/名片等
    public void getGroupPerInfo(UserGroup userGroup) {
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setMsgType(ProtoClass.MsgType.GET_GROUP_PERSONAL_INFO);
        builder.setAccount(ImApplication.instance.getCurUser().getName()).setGroupID(userGroup.getID());

        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                onProcess(responseMsg);
                return true;
            }
        });
    }

    //加入群
    public void joinGroup(UserGroup userGroup) {
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setMsgType(ProtoClass.MsgType.JOIN_GROUP);
        builder.setAccount(ImApplication.instance.getCurUser().getName()).setGroupID(userGroup.getID());

        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                onProcess(responseMsg);
                return true;
            }
        });

    }

    //成员主动退出群
    public void exitGroup(UserGroup userGroup) {
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setMsgType(ProtoClass.MsgType.EXIT_GROUP);
        builder.setAccount(ImApplication.instance.getCurUser().getName()).setGroupID(userGroup.getID());

        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                onProcess(responseMsg);
                return true;
            }
        });
    }

    //管理员移除群成员
    public void removeGroupMember(UserGroup.Member member, UserGroup userGroup) {
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setMsgType(ProtoClass.MsgType.DEL_GROUP_MEMBER);
        builder.setAccount(ImApplication.instance.getCurUser().getName()).setGroupID(userGroup.getID());

        builder.setStrkey(member.getUserName());
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                onProcess(responseMsg);
                return true;
            }
        });
    }

    //群主解散群
    public void delGroup(UserGroup userGroup) {
        if (!userGroup.getOwner().getUserName().equals(ImApplication.instance.getCurUser().getName())) {
            return;
        }

        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setMsgType(ProtoClass.MsgType.DEL_GROUP);
        builder.setAccount(ImApplication.instance.getCurUser().getName()).setGroupID(userGroup.getID());

        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                onProcess(responseMsg);
                return true;
            }
        });
    }

    //添加新成员
    public void inviteMember(UserGroup userGroup, List<String> friends) {
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setMsgType(ProtoClass.MsgType.INVITE_MEMBER_TO_JOIN_GROUP);
        builder.setAccount(ImApplication.instance.getCurUser().getName()).setGroupID(userGroup.getID());

        for (String friendName : friends) {
            ProtoClass.User.Builder friendBuilder = ProtoClass.User.newBuilder();
            friendBuilder.setUserID(Integer.parseInt(friendName));
            friendBuilder.setUserName(friendName);
            builder.addFriends(friendBuilder);
        }

        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                onProcess(responseMsg);
                return true;
            }
        });
    }

    //修改群名
    public void modifyGroupName(UserGroup userGroup, String newName) {
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setMsgType(ProtoClass.MsgType.EDIT_GROUP_INFO);
        builder.setAccount(ImApplication.instance.getCurUser().getName()).setGroupID(userGroup.getID());
        builder.setStrkey("name");
        ProtoClass.GroupInfo.Builder groupBuild = ProtoClass.GroupInfo.newBuilder();
        groupBuild.setGroupID(userGroup.getID()).setGroupName(newName);

        builder.addGroupInfo(groupBuild);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                onProcess(responseMsg);
                return true;
            }
        });
    }

    //修改群简介
    public void modifyGroupIntro(UserGroup userGroup, String newIntro) {
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setMsgType(ProtoClass.MsgType.EDIT_GROUP_INFO);
        builder.setAccount(ImApplication.instance.getCurUser().getName()).setGroupID(userGroup.getID());
        builder.setStrkey("intro");
        ProtoClass.GroupInfo.Builder groupBuild = ProtoClass.GroupInfo.newBuilder();
        groupBuild.setGroupID(userGroup.getID()).setGroupIntro(newIntro);

        builder.addGroupInfo(groupBuild);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                onProcess(responseMsg);
                return true;
            }
        });
    }

    //修改群名片
    public void modifyGroupNameCard(UserGroup userGroup, String newNameCard) {
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setMsgType(ProtoClass.MsgType.EDIT_GROUP_NAMECARD);
        builder.setAccount(ImApplication.instance.getCurUser().getName()).setGroupID(userGroup.getID());

        builder.setStrkey(newNameCard);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                onProcess(responseMsg);
                return true;
            }
        });
    }

    //设置管理员
    public void setGroupAdmin(UserGroup.Member member, UserGroup userGroup) {
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setMsgType(ProtoClass.MsgType.SET_GROUP_ADMIN);
        builder.setAccount(ImApplication.instance.getCurUser().getName()).setGroupID(userGroup.getID());

        builder.setFriendID(member.getUserID());
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                onProcess(responseMsg);
                return true;
            }
        });
    }

    //移除管理员
    public void removeGrouopAdmin(UserGroup.Member member, UserGroup userGroup) {
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setMsgType(ProtoClass.MsgType.REMOVE_GROUP_ADMIN);
        builder.setAccount(ImApplication.instance.getCurUser().getName()).setGroupID(userGroup.getID());

        builder.setFriendID(member.getUserID());
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                onProcess(responseMsg);
                return true;
            }
        });
    }

    //转让该群
    public void transferGroup(UserGroup userGroup, int toMemberID) {
        //TODO 转让该群
    }
}
