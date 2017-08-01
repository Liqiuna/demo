package com.cst14.im.listener;

import android.content.ContentValues;
import android.util.Log;

import com.cst14.im.activity.MainActivity;
import com.cst14.im.baseActionView.SessionView;
import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.db.dao.PersonalMsgDao;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Utils;
import com.cst14.im.utils.sessionUtils.ChatMsgBean;
import com.cst14.im.utils.sessionUtils.SessionHolder;

public class SessionLisener implements iPresenter {
    SessionView v;
    MainActivity activity;

    public SessionLisener(SessionView view) {
        this.v = view;
        activity = (MainActivity) v;
    }

    public void onProcess(ProtoClass.Msg msg) {
        if (msg.getMsgType() != ProtoClass.MsgType.MsgType_SESSION) {
            return;
        }
        if (msg.getResponseState() != ProtoClass.StatusCode.SUCCESS) {
            final String errMsg = msg.getErrMsg();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try{
                        String[] arr = errMsg.split("#");
                        SessionHolder holder = v.startSession(arr[1]);
                        ChatMsgBean offLineMsg = holder.mapToSendingMsgBean.get(arr[2]);
                        offLineMsg.sentTime = arr[0];
                        offLineMsg.msgSentFinished = false;
                        offLineMsg.isSending = false;
                        offLineMsg.showMsgSentState("[离线]",true);
                        ContentValues values = new ContentValues();
                        values.put("sent_time",arr[0]);
                        int num = PersonalMsgDao.db.update("msg_data", values, "msg_id=?", new String[]{arr[2]});
                        if(num<=0){
                            Utils.showToast2(ImApplication.mainActivity,"数据更新失败");
                        }
                        return;
                    }catch (Exception e){
                        Log.e("in sessionListener",e.toString());
                    }
                    Utils.showToast(activity.getBaseContext(), errMsg);
                }
            });
            return;
        }
        ProtoClass.PersonalMsg personalMsg = msg.getPersonalMsg();
        final String msgId = personalMsg.getMsgId();
        final String from = personalMsg.getSenderID() + "";
        final ProtoClass.DataType dataType = personalMsg.getMsgType();
        ChatMsgBean bean = null;
        if (ProtoClass.DataType.TEXT == dataType) {
            bean = ChatMsgBean.newTextMsg(personalMsg.getContent(), from);
        } else if (ProtoClass.DataType.VOICE == dataType) {
            bean =ChatMsgBean.newVoiceMsg(personalMsg.getFileName(), personalMsg.getFileLen(), from);
            bean.setFileFingerPrint(personalMsg.getContent());
        } else if (ProtoClass.DataType.FILE == dataType) {
            bean = ChatMsgBean.newFileMsg(personalMsg.getFileName(), personalMsg.getFileLen(), from);
            bean.setFileFingerPrint(personalMsg.getContent());
        } else if (ProtoClass.DataType.FEED_BACK_SEND_OK == dataType) {
            String to = personalMsg.getRecverID(0) + "";
            // TODO 根据id找到自己发的消息，然后为这个消息打上时间戳，通知更新UI，消息发送成功，然后返回
            SessionHolder holder = v.startSession(to);
            final ChatMsgBean sendedMsg = holder.mapToSendingMsgBean.get(msgId);
            if(sendedMsg==null){
                return;
            }
            sendedMsg.sentTime = personalMsg.getSendTime();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sendedMsg.msgSentFinished = true;
                    sendedMsg.isSending = false;
                    sendedMsg.showMsgSentState("", true);
                    ContentValues values = new ContentValues();
                    values.put("sent_time",sendedMsg.sentTime);
                    values.put("msg_send_finished",1);
                    int num = PersonalMsgDao.db.update("msg_data", values, "msg_id=?", new String[]{sendedMsg.msgId});
                    if(num<=0){
                        Utils.showToast2(ImApplication.mainActivity,"数据更新失败");
                    }
                }
            });
            return;
        } else if (ProtoClass.DataType.VIDEO == dataType) {
            bean = ChatMsgBean.newVideoMsg(personalMsg.getFileName(), personalMsg.getFileLen(), from);
            bean.setFileFingerPrint(personalMsg.getContent());
            bean.setThumbFingerPrint(personalMsg.getThumbFingerPrint());
        }
        bean.msgId = msgId;
        bean.sentTime = personalMsg.getSendTime();
        final ChatMsgBean finalBean = bean;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SessionHolder holder = v.startSession(from);
                holder.acceptMsg(finalBean,false,true);
            }
        });
    }
}
