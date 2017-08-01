package com.cst14.im.listener;

import android.content.Intent;
import android.view.View;

import com.cst14.im.R;
import com.cst14.im.activity.SecurityFindPwdActivity;
import com.cst14.im.baseClass.ISecurityFindPwdPresenter;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.ImApplication;

/**
 * Created by tying on 2016/9/19.
 * the methods of finding password
 */

public class SecurityFindPwdListener implements ISecurityFindPwdPresenter{

    private SecurityFindPwdActivity mSecurityFindPwdActivity;
    private ISecurityFindPwdView mView;
    public static final String CHANGEPWDBYEMAIL = "CHANGE_PWD_BY_EMAIL";
    public SecurityFindPwdListener(ISecurityFindPwdView view){
        this.mView = view;
        mSecurityFindPwdActivity = (SecurityFindPwdActivity)mView;
    }

    @Override
    public void onProcess(ProtoClass.Msg msg) {
        switch (msg.getMsgType().toString()){
            case CHANGEPWDBYEMAIL:
                if (msg.getMsgType()!= ProtoClass.MsgType.CHANGE_PWD_BY_EMAIL){
                    return;
                }
                if (msg.getResponseState()!=ProtoClass.StatusCode.SUCCESS){
                    final String errMsg = msg.getErrMsg();
                    mSecurityFindPwdActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mView.doFailed(errMsg);
                        }
                    });
                    return;
                }
                ImApplication.setLoginToken(msg.getToken());
                final String emailCP = msg.getResponseState().toString();
                mSecurityFindPwdActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mView.doFindByEmailResult(emailCP);
                    }
                });
                break;
        }
    }

    @Override
    public void onViewClick(View view) {
        int viewId = view.getId();
        switch(viewId){
        }
    }

    @Override
    public void initToolbar() {
        mView.initToolbar("找回账号密码");
    }

    @Override
    public void onDestroy() {

    }

    public interface ISecurityFindPwdView{
        void initToolbar(String title);
        void doFindByEmailResult(String result);
        void doFailed(String errMsg);
    }
}
