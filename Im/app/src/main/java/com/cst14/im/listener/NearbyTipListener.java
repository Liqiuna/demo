package com.cst14.im.listener;

import android.app.Activity;

import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.protobuf.ProtoClass;

/**
 * Created by Administrator on 2016/9/12 0012.
 */
public class NearbyTipListener implements iPresenter {

    private Activity aty;
    private OnAcceptNearbyTipListener listener;

    public NearbyTipListener(Activity aty, OnAcceptNearbyTipListener listener) {
        this.aty = aty;
        this.listener = listener;
    }

    @Override
    public void onProcess(final ProtoClass.Msg msg) {
        if (ProtoClass.MsgType.GET_NEARBY_TIP != msg.getMsgType())  return;
        if (ProtoClass.StatusCode.SUCCESS != msg.getResponseState()) return;

        if (listener == null) return;

        aty.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listener.onAccept(msg.getNearBy(0));
            }
        });
    }

    public interface OnAcceptNearbyTipListener {
        void onAccept(ProtoClass.Nearby nearby);
    }
}
