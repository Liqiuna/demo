package com.cst14.im.listener;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.cst14.im.baseClass.iGroupCreateView;
import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;

public class GroupCreatePresenter implements iPresenter {
    private static final String TAG = "GroupCreatePresenter";

    private Handler handler = new Handler(Looper.getMainLooper());
    private iGroupCreateView view;

    public GroupCreatePresenter(iGroupCreateView view) {
        this.view = view;
        Tools.addPresenter(this);
    }

    @Override
    public void onProcess(final ProtoClass.Msg msg) {
        if (msg.getMsgType() != ProtoClass.MsgType.CREATE_GROUP) {
            return;
        }
        Log.e(TAG, "onProcess: " + msg.toString());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (msg.getResponseState() == ProtoClass.StatusCode.SUCCESS) {
                    view.onCreateSucceed();
                } else {
                    view.onCreateFail();
                }
            }
        });
    }

    public void createGroup(String groupName, String info) {
        //build groupData for msg
        ProtoClass.GroupInfo.Builder groupDataBuilder = ProtoClass.GroupInfo.newBuilder();
        groupDataBuilder.setOwnerID(Integer.parseInt(ImApplication.instance.getCurUser().getName()));
        groupDataBuilder.setGroupName(groupName).setGroupIntro(info).setOwnerName(ImApplication.instance.getCurUser().getName());

        //build msg and send
        ProtoClass.Msg.Builder msgBuilder = ProtoClass.Msg.newBuilder();

        msgBuilder.setMsgType(ProtoClass.MsgType.CREATE_GROUP).setAccount(ImApplication.instance.getCurUser().getName());
        msgBuilder.addGroupInfo(groupDataBuilder);

        Tools.startTcpRequest(msgBuilder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                onProcess(responseMsg);
                return true;
            }
        });
    }
}
