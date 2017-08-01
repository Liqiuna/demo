package com.cst14.im.listener;

import com.cst14.im.activity.LoginActivity;
import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.ImApplication;

/**
 * Created by zxm on 2016/8/26.
 */
public class IMEItoRegisterPresenter implements iPresenter {
    IMEItoRegisterPresenter.Registerview  v;
    LoginActivity activity;

    public IMEItoRegisterPresenter(IMEItoRegisterPresenter.Registerview view) {
        this.v = view;
        activity = (LoginActivity) v;
    }

    @Override
    public void onProcess(ProtoClass.Msg msg) {


        if( msg.getMsgType()!=ProtoClass.MsgType.REGISTER ){
            return;
        }


        switch(msg.getResponseState()) {

            case  FAILED:
                Register(0,"绑定手机注册，该用户已注册",msg.getAccount());
                break;
            case  SUCCESS:
                Register(1,"绑定手机注册，注册成功,默认密码为1234。",msg.getAccount());
        }
        return;
    }




    public void Register(final int flag,final String tip, final String username) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String pwd="1234";
                v.setSpannableString(tip);
                v.setUsername(username);
                if(flag==1){
                    v.setPwb(pwd);
                }else{
                    if(username.equals(ImApplication.getSp().getString("currentUserName", ""))){
                        pwd=ImApplication.getSp().getString("currentUserPwd", "");
                    }else{
                        pwd="";
                    }
                    v.setPwb(pwd);
                }
            }
        });

    }


    public interface Registerview {
        void setSpannableString(String tip);
        void setUsername(String username);
        void setPwb(String password);
    }




}