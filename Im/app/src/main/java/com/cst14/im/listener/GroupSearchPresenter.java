package com.cst14.im.listener;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.cst14.im.baseClass.iGroupSearchView;
import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;

public class GroupSearchPresenter implements iPresenter {
    private static final String TAG = "GroupSearchPresenter";

    private iGroupSearchView view;
    private Handler handler = new Handler(Looper.getMainLooper());

    public GroupSearchPresenter(iGroupSearchView view) {
        this.view = view;
    }

    @Override
    public void onProcess(final ProtoClass.Msg msg) {
        if (msg.getMsgType() != ProtoClass.MsgType.SEARCH_GROUP) {
            return;
        }
        Log.e(TAG, "onProcess: " + msg.toString());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (msg.getResponseState() == ProtoClass.StatusCode.SUCCESS) {
                    view.showSearchResult(msg.getGroupInfoList());
                } else {
                    view.showNoReasult();
                }
            }
        });
    }

    public void searchGroup(String searchKey) {
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setMsgType(ProtoClass.MsgType.SEARCH_GROUP).setStrkey(searchKey);

        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                onProcess(responseMsg);
                return true;
            }
        });
    }
}
