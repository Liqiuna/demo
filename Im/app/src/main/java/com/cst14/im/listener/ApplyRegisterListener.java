package com.cst14.im.listener;

import com.cst14.im.activity.LoginActivity;
import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.Model;

/**
 * Created by zxm on 2016/9/15.
 */
public class ApplyRegisterListener implements iPresenter {
    LoginLisener.LoginView v;
    LoginActivity activity;
    public ApplyRegisterListener(LoginLisener.LoginView view) {
        this.v = view;
        activity = (LoginActivity) v;
    }
    @Override
    public void onProcess(ProtoClass.Msg msg) {
        if( msg.getMsgType()!=ProtoClass.MsgType.CHECK_RGST_TYPE){
            return;
        }
        Model.setRGST(msg.getRgstType());

    }
}