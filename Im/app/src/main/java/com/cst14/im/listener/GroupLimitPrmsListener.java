package com.cst14.im.listener;

import android.view.View;
import android.widget.Toast;

import com.cst14.im.activity.GroupLimitPrmsActivity;
import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.protobuf.ProtoClass;

/**
 * Created by Administrator on 2016/9/27 0027.
 */
public class GroupLimitPrmsListener implements iPresenter {
    private GroupLimitPrmsActivity groupLimitPrmsActivity;
    private int roleID;
    private int oldGroupLimit;

    public GroupLimitPrmsListener(GroupLimitPrmsActivity activity){
        groupLimitPrmsActivity = activity;
    }

    public void onProcess(ProtoClass.Msg msg){
        if (msg.getMsgType() != ProtoClass.MsgType.CHECK_CREATE_GROUP_LIMIT && msg.getMsgType() != ProtoClass.MsgType.SET_CREATE_GROUP_LIMIT){
            return;
        }
        switch (msg.getMsgType()){
            case CHECK_CREATE_GROUP_LIMIT:
                if (msg.getResponseState() == ProtoClass.StatusCode.FAILED && msg.getUser().getUserID() == 0 && msg.getUser().getUserRoleID() == 0){
                    showMsg("该用户不存在");
                    return;
                }else if (msg.getResponseState() == ProtoClass.StatusCode.SUCCESS) {
                    roleID = msg.getUser().getUserRoleID();
                    oldGroupLimit = msg.getUser().getCreGroupLimit();
                    groupLimitPrmsActivity.spinnerShow();
                }
                break;
            case SET_CREATE_GROUP_LIMIT:
                if (msg.getResponseState() == ProtoClass.StatusCode.SUCCESS){
                    showMsg("修改该用户创建群的上限成功");
                }
                break;
        }
    }

    public void showMsg(final String str){
        groupLimitPrmsActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(groupLimitPrmsActivity,str,Toast.LENGTH_LONG).show();
            }
        });
    }

    public int getRoleID(){
        return roleID;
    }

    public int getOldGroupLimit(){
        return oldGroupLimit;
    }
}
