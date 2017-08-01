package com.cst14.im.listener;

import android.widget.Toast;

import com.cst14.im.activity.LoginPermissionActivity;
import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.protobuf.ProtoClass;

/**
 * Created by Administrator on 2016/9/15 0015.
 */
public class LoginPermissionListener implements iPresenter {
    private static int userID;
    private static int roleID;
    private static boolean ifLogin;
    private LoginPermissionActivity loginPermissionActivity;

    public LoginPermissionListener(LoginPermissionActivity activity){
        loginPermissionActivity = activity;
    }

    public void onProcess(ProtoClass.Msg msg) {
        if (msg.getMsgType() != ProtoClass.MsgType.CHECK_IF_LOGIN && msg.getMsgType() != ProtoClass.MsgType.SET_LOGIN_TYPE){
            return;
        }
        switch (msg.getMsgType()){
            case CHECK_IF_LOGIN:
                if (msg.getUser().getUserID() == 0 && msg.getUser().getUserRoleID() == 0) {
                    showMsg("您所搜索的用户不存在");
                    loginPermissionActivity.resultGone();
                    return;
                }
                userID = msg.getUser().getUserID();
                roleID = msg.getUser().getUserRoleID();
                ifLogin = msg.getIfLogin();
                loginPermissionActivity.spinnerChange();
                break;
            case SET_LOGIN_TYPE:
                userID = msg.getUser().getUserID();
                ifLogin = msg.getIfLogin();
                showMsg("修改该用户登录类型成功");
                break;
        }
    }

    public void showMsg(final String str){
        loginPermissionActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(loginPermissionActivity,str,Toast.LENGTH_LONG).show();
            }
        });
    }

    public static int getUserID(){
        return userID;
    }

    public static int getRoleID(){
        return roleID;
    }

    public static boolean getIfLogin(){
        return ifLogin;
    }
}
