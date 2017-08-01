package com.cst14.im.listener;

import com.cst14.im.activity.ChangePwdActivity;
import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.protobuf.ProtoClass;

/**
 * Created by Administrator on 2016/8/25 0025.
 */
public class ChangePwdListener implements iPresenter {
    ChangePwdView v;
    ChangePwdActivity activity;
    public ChangePwdListener(ChangePwdView view){
        this.v = view;
        activity = (ChangePwdActivity)v;
    }
    public void onProcess(final ProtoClass.Msg msg){
        if (msg.getMsgType() == ProtoClass.MsgType.GET_PWD_QUETION){
            if (msg.getResponseState() != ProtoClass.StatusCode.SUCCESS) {
                final String errMsg = msg.getErrMsg();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.falseGetPswQuetion(errMsg);
                    }
                });
                return;
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    v.successGetPswQuetion(msg.getSecurityItem().getQuestionSet().getPswQuetion1(),
                            msg.getSecurityItem().getQuestionSet().getPswQuetion2(),
                            msg.getSecurityItem().getQuestionSet().getPswQuetion3());
                }
            });
        }

        if (msg.getMsgType() == ProtoClass.MsgType.CHANGE_PASSWORD){
            if (msg.getResponseState() != ProtoClass.StatusCode.SUCCESS) {
                final String errMsg = msg.getErrMsg();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.falseChangePwd(errMsg);
                    }
                });
                return;
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    v.successChangePwd();
                }
            });
        }
        if (msg.getMsgType() == ProtoClass.MsgType.VERIFY_PWD_QUETION){
            if (msg.getResponseState() != ProtoClass.StatusCode.SUCCESS) {
                final String errMsg = msg.getErrMsg();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.falseChangePwd(errMsg);
                    }
                });
                return;
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    v.successVerifyPwdQuetion();
                }
            });
        }
    }
    public interface ChangePwdView{
        public void successGetPswQuetion(String pswQue1, String pswQue2, String pswQue3);
        public void successVerifyPwdQuetion();
        public void falseGetPswQuetion(String errMsg);
        public void successChangePwd();
        public void falseChangePwd(String errMsg);
    }
}
