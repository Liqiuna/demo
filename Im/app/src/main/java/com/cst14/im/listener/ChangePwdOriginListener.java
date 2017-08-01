package com.cst14.im.listener;

import android.view.View;

import com.cst14.im.R;
import com.cst14.im.activity.SecurityChangePwdActivity;
import com.cst14.im.baseClass.IChangePwdOriginPresenter;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.ImApplication;

/**
 * Created by tying on 2016/9/19.
 * Change pwd by origin pwd
 */

public class ChangePwdOriginListener implements IChangePwdOriginPresenter{
    private IChangePwdOriginView mView;
    private SecurityChangePwdActivity mSecurityChangePwdActivity;

    public static final String CHANGEPWD = "CHANGE_PASSWORD";


    public ChangePwdOriginListener(IChangePwdOriginView view){
        this.mView = view;
        mSecurityChangePwdActivity = (SecurityChangePwdActivity)mView;
    }

    @Override
    public void onProcess(ProtoClass.Msg msg) {
        switch (msg.getMsgType().toString()){
            case CHANGEPWD:
                if (msg.getMsgType()!= ProtoClass.MsgType.CHANGE_PASSWORD){
                    return;
                }
                if (msg.getResponseState()!=ProtoClass.StatusCode.SUCCESS){
                    final String errMsg = msg.getErrMsg();
                    mSecurityChangePwdActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mView.doFailed(errMsg);
                        }
                    });
                    return;
                }
                ImApplication.setLoginToken(msg.getToken());
                final String resultCP = msg.getResponseState().toString();
                mSecurityChangePwdActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mView.doChangePwdResult(resultCP);
                    }
                });
                break;


        }

    }

    @Override
    public void onViewClick(View view) {
    }

    @Override
    public void initToolbar() {
        mView.initToolbar("设置密码");
    }

    @Override
    public void onDestroy() {

    }

    public interface IChangePwdOriginView {
        void initToolbar(String title);
        void doChangePwdResult(String result);
        void doFailed(String errMsg);
    }
}
