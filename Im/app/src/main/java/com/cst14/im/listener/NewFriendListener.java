package com.cst14.im.listener;

import com.cst14.im.Fragment.FriendInfo;
import com.cst14.im.activity.NewFriendActivity;
import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.protobuf.ProtoClass;

/**
 * Created by I_C_U on 2016/9/16.
 */

public class NewFriendListener implements iPresenter {
    NewFriendView v;
    NewFriendActivity activity;

    public NewFriendListener(NewFriendView view) {
        this.v = view;
        activity = (NewFriendActivity) v;
    }
    public void onProcess(final ProtoClass.Msg msg) {
        if (msg.getMsgType() != ProtoClass.MsgType.ADD_FRIEND_Response)
            return;
        if(!msg.hasResponseState())return;
        final String errMsg=msg.getErrMsg();//运行到这里表明是自己之前点过同意或者拒绝
        if (msg.getResponseState().equals(ProtoClass.StatusCode.FAILED)) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    v.failedResponse(errMsg);
                }
            });
            return;
        }
        int IsAgree=1;
        if(msg.getFriends(0).getIsAgree()==false)IsAgree=2;
        final FriendInfo friend=new FriendInfo(msg.getFriends(0).getUserID(),msg.getFriends(0).getNickName(),IsAgree);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v.sucessResponse(friend);
            }
        });

    }
    public interface NewFriendView {
        void failedResponse(String errMsg);
        void sucessResponse(FriendInfo friend);

    }
}
