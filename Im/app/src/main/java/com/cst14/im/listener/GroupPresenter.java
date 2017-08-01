package com.cst14.im.listener;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.cst14.im.baseClass.iGroupView;
import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;

public class GroupPresenter implements iPresenter {
    private static final String TAG = "GroupPresenter";

    private Handler handler = new Handler(Looper.getMainLooper());
    private iGroupView view;

    public GroupPresenter(iGroupView view) {
        this.view = view;
        Tools.addPresenter(this);
    }

    @Override
    public void onProcess(final ProtoClass.Msg msg) {
        if (msg.getMsgType() != ProtoClass.MsgType.GET_ALL_GROUP_JOINED) {
            return;
        }
        Log.e(TAG, "onProcess: " + msg.toString());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (msg.getResponseState() == ProtoClass.StatusCode.SUCCESS) {
                    view.showGetGroupListSucceed(msg.getGroupInfoList());
                } else {
                    view.showGetGroupListFail();
                }
            }
        });
    }

    public void getAllGroupsUserJoined() {
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setMsgType(ProtoClass.MsgType.GET_ALL_GROUP_JOINED).setAccount(ImApplication.instance.getCurUser().getName());

        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                onProcess(responseMsg);
                return true;
            }
        });
    }
}
