package com.cst14.im.listener;

import android.util.Log;

import com.cst14.im.activity.ChatActivity;
import com.cst14.im.activity.GroupChatActivity;
import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.bean.UserChat;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.sessionUtils.ChatMsgBean;

import java.util.LinkedList;

/**
 * Created by Belinda Y on 2016/8/26.
 */
public class ViewHistoryListener implements iPresenter {
    PerHistoryMsgView perHistoryMsgView;
    GroupHistoryMsgView groupHistoryMsgView;
    ChatActivity perChatActivity;
    GroupChatActivity groupChatActivity;

    public ViewHistoryListener(PerHistoryMsgView historyMsgView) {
        this.perHistoryMsgView= historyMsgView;
        perChatActivity = (ChatActivity)historyMsgView;

    }
     public ViewHistoryListener(GroupHistoryMsgView historyMsgView){
         this.groupHistoryMsgView=historyMsgView;
         groupChatActivity=(GroupChatActivity)historyMsgView;
     }
    @Override
    public void onProcess(final ProtoClass.Msg msg) {
        if (msg.getMsgType() != ProtoClass.MsgType.VIEW_HISTOTY_MSG) {
            return;
        }

        if (msg.getResponseState() != ProtoClass.StatusCode.SUCCESS) {
            final String errMsg = msg.getErrMsg();
            Log.e("获取历史消息失败", errMsg);
            return;
        }

        if(msg.getIsPerMsg()&&msg.getPersonMsgCount()==0){
            perChatActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    perHistoryMsgView.successGetPerHistoryMsg(null);
                }
            });
            return;
        }

        if(!msg.getIsPerMsg()&&msg.getGroupHistoryMsgCount()==0){
            groupChatActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   groupHistoryMsgView.successGetGroupHistoryMsg(null);
                }
            });
            return;
        }

        if (msg.getPersonMsgCount() >0) {
            ChatMsgBean bean = null;
            boolean isMineMsg;
            final LinkedList<ChatMsgBean> historyMsgList = new LinkedList<>();
            for (int i = 0; i < msg.getPersonMsgCount(); i++) {
                ProtoClass.PersonalMsg personalMsg = msg.getPersonMsg(i);
                //final String msgId = personalMsg.getMsgId();
//            if(String.valueOf(personalMsg.getSenderID()).equals(ImApplication.User_id)){
//                isMineMsg=true;
//            }else{
//                isMineMsg=false;
//            }
                switch (personalMsg.getMsgType()) {
                    case TEXT:
                        bean = ChatMsgBean.newTextMsg(personalMsg.getContent(), personalMsg.getSenderID() + "");
                        break;
                    case VOICE:
                        bean = ChatMsgBean.newVoiceMsg(personalMsg.getFileName(), personalMsg.getFileLen(), personalMsg.getSenderID() + "");
                        bean.setFileFingerPrint(personalMsg.getContent());
                        break;
                    case IMAGE:
                    case FILE:
                        bean = ChatMsgBean.newFileMsg(personalMsg.getFileName(), personalMsg.getFileLen(), personalMsg.getSenderID() + "");
                        bean.setFileFingerPrint(personalMsg.getContent());
                        break;
                    case VIDEO:
                        bean = ChatMsgBean.newVideoMsg(personalMsg.getFileName(), personalMsg.getFileLen(), personalMsg.getSenderID() + "");
                        bean.setFileFingerPrint(personalMsg.getContent());
                        bean.setThumbFingerPrint(personalMsg.getThumbFingerPrint());
                }
                bean.sentTime = personalMsg.getSendTime();
                historyMsgList.add(bean);//历史消息列表
            }

            perChatActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    perHistoryMsgView.successGetPerHistoryMsg(historyMsgList);
                }
            });

        }
        if(msg.getGroupHistoryMsgCount()> 0){
            UserChat userChat=null;
            final LinkedList<UserChat> historyMsgList = new LinkedList<>();

            for(int i=0;i<msg.getGroupHistoryMsgCount();i++){
                ProtoClass.GroupMsg groupMsg = msg.getGroupHistoryMsg(i);
                userChat=new UserChat();
                userChat.setName(groupMsg.getSenderName());
                userChat.setMsgType(String.valueOf(groupMsg.getDataType()));
                userChat.setMsgSendTime(groupMsg.getMsgTime());
                userChat.setVoiceTime(groupMsg.getVoiceTime());
                userChat.setFileName(groupMsg.getFileInfo().getName());
                userChat.setStrContent(groupMsg.getText());
                userChat.setFileSize(String.valueOf(groupMsg.getFileInfo().getSize()));
                userChat.setFilePath(groupMsg.getFileInfo().getPath());
                userChat.setIsSendOK(1);
                userChat.setMsgUniqueTag(msg.getMsgUniqueTag());
                historyMsgList.add(userChat);
            }
            groupChatActivity.runOnUiThread(new Runnable() {
              @Override
              public void run() {
                  groupHistoryMsgView.successGetGroupHistoryMsg(historyMsgList);
              }
          });
        }
    };

    public interface PerHistoryMsgView {
        void successGetPerHistoryMsg(LinkedList<ChatMsgBean> historyMsgList);
        void failedGetHistoryMsg(String errMsg);
    }
   public  interface GroupHistoryMsgView{
       void successGetGroupHistoryMsg(LinkedList<UserChat> groupHistoryMsgList);
   }

}
