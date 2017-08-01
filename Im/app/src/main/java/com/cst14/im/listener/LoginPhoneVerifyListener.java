package com.cst14.im.listener;

import android.view.View;

import com.cst14.im.R;
import com.cst14.im.activity.LoginPhoneVerifyActivity;
import com.cst14.im.baseClass.IChangePwdOriginPresenter;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.ImApplication;

/**
 * Created by tying on 2016/9/19.
 * login by phone
 */

public class LoginPhoneVerifyListener implements IChangePwdOriginPresenter {
    private ILoginPhoneVerifyView mView;
    private LoginPhoneVerifyActivity mLoginPhoneVerifyActivity;

    private ProtoClass.MsgType opt;

    private static final String GETSECURITYINFO = "GET_SECURITY_INFO";
    private static final String BINDPHONE = "BIND_PHONE";
    public LoginPhoneVerifyListener(ILoginPhoneVerifyView view){
        this.mView = view;
        mLoginPhoneVerifyActivity = (LoginPhoneVerifyActivity)mView;
    }

    @Override
    public void onProcess(ProtoClass.Msg msg) {
        switch (msg.getMsgType().toString()){
            case GETSECURITYINFO:
                if (msg.getMsgType()!=ProtoClass.MsgType.GET_SECURITY_INFO){
                    return;
                }
                if (msg.getResponseState()!=ProtoClass.StatusCode.SUCCESS){
                    final String errMsg = msg.getErrMsg();
                    mLoginPhoneVerifyActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mView.doFailed(errMsg);
                        }
                    });
                    return;
                }
                ImApplication.setLoginToken(msg.getToken());

                final String phone_get = msg.getSecurityItem().getPhoneBind().getUserPhone();
                mLoginPhoneVerifyActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mView.doGetPhoneResult(phone_get);
                    }
                });
                break;
            case BINDPHONE:
                if (msg.getMsgType() != ProtoClass.MsgType.BIND_PHONE) {
                    return;
                }
                ImApplication.setLoginToken(msg.getToken());
                final String phone_bind = msg.getSecurityItem().getPhoneBind().getUserPhone();
                if (msg.getResponseState() != ProtoClass.StatusCode.SUCCESS) {
                    if (msg.getSecurityItem().getPhoneBind().getUserPhoneStatus() == ProtoClass.EmailStauts.BOUND) {
                        mView.doResult(phone_bind,"FAILED");
                    }
                    final String errMsg = msg.getErrMsg();
                    mLoginPhoneVerifyActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mView.doFailed(errMsg);
                        }
                    });
                    return;
                }
                mLoginPhoneVerifyActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mView.doResult(phone_bind,"BOUND");
                    }
                });
                break;
        }
    }

    @Override
    public void onViewClick(View view) {
        int viewId = view.getId();
        switch(viewId){
            case R.id.change_pwd_finish_btn:
                break;
        }
    }

    @Override
    public void initToolbar() {
        mView.initToolbar("用短信验证码登录");
    }

    @Override
    public void onDestroy() {

    }

    public interface ILoginPhoneVerifyView{
        void initToolbar(String title);
        void doResult(String phone,String result);
        void doGetPhoneResult(String phone);
        void doFailed(String errMsg);

    }
}
