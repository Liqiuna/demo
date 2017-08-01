package com.cst14.im.listener;

import android.util.Log;
import android.widget.Toast;

import com.cst14.im.Fragment.FriendInfo;
import com.cst14.im.activity.LoginActivity;
import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.db.dao.FriendsDao;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Model;
import com.cst14.im.utils.sessionUtils.ChatMsgBean;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LoginLisener implements iPresenter {

    LoginView v;
    LoginActivity activity;
    private  ProtoClass.MsgType [] op = new ProtoClass.MsgType[] {ProtoClass.MsgType.LOGIN, ProtoClass.MsgType.BAN_LOGIN,ProtoClass.MsgType.GET_FRIEND};
    public LoginLisener(LoginView view) {
        this.v = view;
        activity = (LoginActivity) v;
    }

    public void onProcess(final ProtoClass.Msg msg) {
        if (!Arrays.asList(op).contains(msg.getMsgType())) return;
        switch (msg.getMsgType()){
            case LOGIN:{
                if (msg.getResponseState() == ProtoClass.StatusCode.FAILED) {
                    final String errMsg = msg.getErrMsg();
                    activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    v.failedLogin(errMsg);
                }
            });return;}
                ImApplication.setLoginToken(msg.getToken());
                ImApplication.setMyRoleID(msg.getUser().getUserRoleID());
                ImApplication.User_id=msg.getAccount();
                Model.setUsername(msg.getAccount());
                System.out.println("in loginLisener"+msg.getAccount());
          activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v.sunccessLogin();
            }
        });}break;
            case GET_FRIEND:{
                final String errMsg=msg.getErrMsg();
                if (msg.getResponseState().equals(ProtoClass.StatusCode.FAILED)) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("GET_FRIEND failed :", errMsg);
                        }
                    });
                    break;
                }

                List<ProtoClass.FriendList>FriendGroup=msg.getFriendListsList();   //先取出好友分组
                //ImApplication.clearFriend();//从服务器获得最新版本的好友信息 直接清除以前的
                for(ProtoClass.FriendList element:FriendGroup){
                    //从服务器获得最新版本的好友信息 直接清除以前的
                    ImApplication.mapFriendGroup.put(element.getListNO(),element.getListName());
                    LinkedList<FriendInfo> friendlist= new LinkedList<FriendInfo>();
                    ImApplication.mapGroupFriends.put(element.getListNO(),friendlist);
                    if(ImApplication.Friend_group_MAX_number<element.getListNO())
                        ImApplication.Friend_group_MAX_number=element.getListNO();//记录好友分组的最大组号
                }

                //获取到的好友加到全局变量 APP。好友map 里面
                List<ProtoClass.User> frendInfoList = msg.getFriendsList();
                for (ProtoClass.User element:frendInfoList){             //FriendInfo的构造函数  取出来并且新建好友信息   加到map里面的 list
                    int ListNo=element.getListNO();//默认在分组0;我的好友
                    if(!element.hasListNO()||!ImApplication.mapFriendGroup.containsKey(ListNo))
                        ListNo=0;
                    FriendInfo friend_item=new FriendInfo(element.getUserID(),element.getNickName(),element.getRemark(),ListNo);
                    ImApplication.mapGroupFriends.get(ListNo).add(friend_item);
                    ImApplication.mapFriends.put(friend_item.getUser_id(), friend_item);
                }
                //获取离线好友添加请求
                List<ProtoClass.User> frendRequestList = msg.getNewFriendRequestList();
                for (ProtoClass.User element:frendRequestList){             //FriendInfo的构造函数  取出来并且新建好友信息   加到map里面的 list
                    FriendInfo friend_item=new FriendInfo(element.getUserID(),element.getNickName(),0);
                    FriendsDao.addFriendRequest(friend_item);
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.sunccessGetFriends();
                    }
                });
            }break;
            case BAN_LOGIN:
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity,"很抱歉，您所登录的账号已被禁止登录",Toast.LENGTH_LONG).show();
                    }
                });
                break;

        }
    }


    public interface LoginView {
        void sunccessLogin();
        void failedLogin(String errMsg);
        void sunccessGetFriends();
    }
}
