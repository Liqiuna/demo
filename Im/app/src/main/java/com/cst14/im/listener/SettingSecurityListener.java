package com.cst14.im.listener;

import android.view.View;

import com.cst14.im.R;
import com.cst14.im.baseClass.ISecurityPresenter;
import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.activity.SettingSecurityActivity;
import com.cst14.im.utils.ImApplication;

/**
 * Created by tying on 2016/8/24.
 *
 */

public class SettingSecurityListener implements ISecurityPresenter {
    SettingSecurityView v;
    SettingSecurityActivity activity;

    public static final String EMAILBIND = "BIND_EMAIL";
    public static final String EMAILREMOVE = "REMOVE_EMAIL";
    public static final String QUESTIONSET = "SET_PWD_QUETION";
    public static final String QUESTIONCANCEL = "CANCEL_QUESTION";
    public static final String GETSECURITYINFO = "GET_SECURITY_INFO";
    public static final String PHONEBIND = "BIND_PHONE";
    public static final String PHONECANCEL = "CANCEL_PHONE";
    public static final String SUCCESS = "SUCCESS";
    public static final String CHANGEPWDBYORIGIN = "CHANGE_PWD_BY_ORIGIN_PWD";
    public static final String LOGINBYPHONE = "LOGIN_PWD_BY_PHONE";
    public static final String FAILED = "FAILED";
    public SettingSecurityListener(SettingSecurityView view) {
        this.v = view;
        activity = (SettingSecurityActivity) v;
    }

    @Override
    public void onProcess(ProtoClass.Msg msg) {
        switch (msg.getMsgType().toString()){
            case EMAILBIND:
                if (msg.getMsgType() != ProtoClass.MsgType.BIND_EMAIL) {
                    return;
                }
                if (msg.getResponseState() != ProtoClass.StatusCode.SUCCESS) {
                    final String errMsg = msg.getErrMsg();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v.doEmailBindFailed(errMsg);
                        }
                    });
                    return;
                }
                ImApplication.setLoginToken(msg.getToken());
                final String email_bind = msg.getSecurityItem().getEmailBind().getUserEmail();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.doEmailBindSuccess(email_bind);
                    }
                });
                break;
            case EMAILREMOVE:
                if (msg.getMsgType()!=ProtoClass.MsgType.REMOVE_EMAIL){
                    return;
                }
                if (msg.getResponseState()!=ProtoClass.StatusCode.SUCCESS){
                    final String errMsg = msg.getErrMsg();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v.doEmailBindFailed(errMsg);
                        }
                    });
                    return;
                }
                ImApplication.setLoginToken(msg.getToken());
                final String email_remove = msg.getSecurityItem().getEmailBind().getUserEmail();
                final String remove_result = msg.getResponseState().toString();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.doEmailRemoveResult(email_remove,remove_result);
                    }
                });
                break;
            case GETSECURITYINFO:
                if (msg.getMsgType()!=ProtoClass.MsgType.GET_SECURITY_INFO){
                    return;
                }
                if (msg.getResponseState()!=ProtoClass.StatusCode.SUCCESS){
                    final String errMsg = msg.getErrMsg();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v.doEmailBindFailed(errMsg);
                        }
                    });
                    return;
                }
                ImApplication.setLoginToken(msg.getToken());
                final String email_get = msg.getSecurityItem().getEmailBind().getUserEmail();
                final String phone_get = msg.getSecurityItem().getPhoneBind().getUserPhone();
                final String quetion_get = msg.getSecurityItem().getQuestionSet().getPswQuetion1();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (quetion_get.equals("")){
                            v.doResult(email_get,"未设置",phone_get,"");
                        } else {
                            v.doResult(email_get,"已设置",phone_get,"");
                        }
                    }
                });
                break;
            case QUESTIONSET:
                if (msg.getMsgType() != ProtoClass.MsgType.SET_PWD_QUETION) {
                    return;
                }
                if (msg.getResponseState()!=ProtoClass.StatusCode.SUCCESS){
                    final String errMsg = msg.getErrMsg();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v.doEmailBindFailed(errMsg);
                        }
                    });
                    return;
                }
                ImApplication.setLoginToken(msg.getToken());
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.doQuestionSetResult();
                    }
                });
                break;
            case QUESTIONCANCEL:
                break;
            case PHONEBIND:
                if (msg.getMsgType() != ProtoClass.MsgType.BIND_PHONE) {
                    return;
                }
                if (msg.getResponseState() != ProtoClass.StatusCode.SUCCESS) {
                    final String errMsg = msg.getErrMsg();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v.doPhoneBindFailed(errMsg);
                        }
                    });
                    return;
                }
                ImApplication.setLoginToken(msg.getToken());
                final String phone_bind = msg.getSecurityItem().getPhoneBind().getUserPhone();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.doPhoneBindSuccess(phone_bind);
                    }
                });
                break;
            case PHONECANCEL:
                if (msg.getMsgType()!=ProtoClass.MsgType.CANCEL_PHONE){
                    return;
                }
                if (msg.getResponseState()!=ProtoClass.StatusCode.SUCCESS){
                    final String errMsg = msg.getErrMsg();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v.doEmailBindFailed(errMsg);
                        }
                    });
                    return;
                }
                ImApplication.setLoginToken(msg.getToken());
                final String phone_cancel = msg.getSecurityItem().getPhoneBind().getUserPhone();
                final String cancel_result = msg.getResponseState().toString();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.doEmailRemoveResult(phone_cancel,cancel_result);
                    }
                });
                break;

            case CHANGEPWDBYORIGIN:
                if (msg.getMsgType()!= ProtoClass.MsgType.CHANGE_PWD_BY_ORIGIN_PWD){
                    return;
                }
                if (msg.getResponseState()!=ProtoClass.StatusCode.SUCCESS){
                    final String errMsg = msg.getErrMsg();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v.doFailed(errMsg);
                        }
                    });
                    return;
                }
                ImApplication.setLoginToken(msg.getToken());
                final String result = msg.getResponseState().toString();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.doChangePwdByOriginPwdResult(result);
                    }
                });
                break;

            case LOGINBYPHONE:
                if (msg.getMsgType()!= ProtoClass.MsgType.LOGIN_PWD_BY_PHONE){
                    return;
                }
                if (msg.getResponseState()!=ProtoClass.StatusCode.SUCCESS){
                    final String errMsg = msg.getErrMsg();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v.doFailed(errMsg);
                        }
                    });
                    return;
                }
                ImApplication.setLoginToken(msg.getToken());
                final String phoneL= msg.getSecurityItem().getEmailBind().getUserEmail();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.doLoginByPhoneResult(phoneL);
                    }
                });
                break;
            default:
                break;
        }
    }

    @Override
    public void onViewClick(View view) {

    }

    @Override
    public void initToolbar() {
        v.initToolbar("账号与安全");
    }

    @Override
    public void onDestroy() {

    }

    public interface SettingSecurityView{
        void doResult(String email_status, String question_status, String phone, String answer);
        void doEmailBindSuccess(String email_bind);
        void doEmailBindFailed(String errMsg);
        void initToolbar(String title);

        void doPhoneBindSuccess(String phone_bind);
        void doPhoneBindFailed(String errMsg);
        void doPhoneCancelResult(String phone_bind,String result);

        void doEmailRemoveResult(String email_bind,String result);
        void doQuestionSetResult();
        void doQuestionCancelResult(String question,String answer,String result);

        void doChangePwdByOriginPwdResult(String result);
        void doLoginByPhoneResult(String phone);
        void doChangePwdResult(String result);
        void doFailed(String errMsg);

        void hideKeyboard();
    }

}
