package com.cst14.im.listener;

import com.cst14.im.activity.ManageModifyUserInfoActivity;
import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.protobuf.ProtoClass;

/**
 * Created by Belinda Y on 2016/9/25.
 */
public class ModifyUserInfoListener implements iPresenter {
    ManageModifyUserInfoActivity activity;
    ModifyUserInforView modifyUserInforView;
    public ModifyUserInfoListener(ModifyUserInforView modifyUserInforView) {
        this.modifyUserInforView= modifyUserInforView;
        activity = (ManageModifyUserInfoActivity)modifyUserInforView;
    }


    @Override
    public void onProcess(ProtoClass.Msg msg) {
        if (msg.getMsgType() != ProtoClass.MsgType.MODIFY_USER_INFOR) {
            return;
        }


        if (msg.getResponseState() != ProtoClass.StatusCode.SUCCESS) {
            final String tipMsg="修改失败，请稍后再试";
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    modifyUserInforView.failedModifyUserInfor(tipMsg);

                }
            });
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                modifyUserInforView.successModifyUserInfor();
            }
        });

    }
    public interface ModifyUserInforView {
        void successModifyUserInfor();
        void failedModifyUserInfor(String tipMsg);
    }
}
