package com.cst14.im.listener;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.cst14.im.activity.SetRoleActivity;
import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.ImApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/10 0010.
 */
public class SetRoleListener implements iPresenter {
    private RelativeLayout resultView;
    private static int userID;
    private static int roleID;
    private SetRoleActivity setRoleActivity;

    public SetRoleListener(SetRoleActivity setRoleActivity){
        this.setRoleActivity = setRoleActivity;
    }

    public void onProcess(ProtoClass.Msg msg){
        if (msg.getMsgType() == ProtoClass.MsgType.SELECT_USER_ROLE) {
            if (msg.getUser().getUserID() == 0 && msg.getUser().getUserRoleID() == 0) {
                showMsg("您所搜索的用户不存在");
                return;
            }
            userID = msg.getUser().getUserID();
            roleID = msg.getUser().getUserRoleID();
            setRoleActivity.spinnerChange();
        }else if (msg.getMsgType() == ProtoClass.MsgType.MODIFY_ROLE){
            userID = msg.getUser().getUserID();
            roleID = msg.getUser().getUserRoleID();
            showMsg("修改该用户角色成功");
        }
        return;
    }

    public void showMsg(final String str){
        setRoleActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(setRoleActivity,str,Toast.LENGTH_LONG).show();
            }
        });
    }

    public static int getUserID(){
        return userID;
    }

    public static int getRoleID(){
        return roleID;
    }
}
