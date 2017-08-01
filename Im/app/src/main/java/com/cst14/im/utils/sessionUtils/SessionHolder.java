package com.cst14.im.utils.sessionUtils;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cst14.im.Fragment.SessionsFragment;
import com.cst14.im.activity.ChatActivity;
import com.cst14.im.db.dao.PersonalMsgDao;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.StringParser;
import com.cst14.im.utils.Utils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * 存放 每个会话对应的所有相关数据，消息列表，控件等
 */
public class SessionHolder {
    public transient ImageView head;
    public transient TextView nickName;
    public transient TextView lastChatMsg;
    public transient TextView lastMsgTime;
    public transient View contentView;
    public transient TextView tvRedCycle;
    public transient ImageView ivNoTip;
    public transient ChatActivity chatActivity;
    public transient SessionsFragment sessionsFragment;
    public transient ImageView ivNoTipSmallRedCycle;
    public transient HashMap<String, ChatMsgBean> mapToSendingMsgBean;

    public String nickNameStr;
    public String userAccount;
    private boolean fixTop;
    private boolean noTip;

    public boolean isNoTip() {
        return noTip;
    }

    public void setNoTip(boolean noTip) {
        this.noTip = noTip;
        if (noTip) {
            unReadMsgCount = 0;
            if(ivNoTip!=null)
                ivNoTip.setAlpha(1f);
        } else if(ivNoTip!=null){
            ivNoTip.setAlpha(0f);
        }
    }

    public boolean isFixTop() {
        return fixTop;
    }

    public void setFixTop(boolean fixTop) {
        this.fixTop = fixTop;
    }

    public void initListeners() {
        acceptMsgListeners = new ArrayList<OnAcceptMsgListener>();
        msgReadedListeners = new ArrayList<OnMsgReadedListener>();
    }
    public boolean isMsgListVisable() {
        return msgListVisable;
    }

    public void setMsgListVisable(boolean msgListVisable) {
        this.msgListVisable = msgListVisable;
    }

    private boolean msgListVisable;

    public int getUnReadMsgCount() {
        return unReadMsgCount;
    }

    public int unReadMsgCount;
    private transient ArrayList<OnAcceptMsgListener> acceptMsgListeners;
    private transient ArrayList<OnMsgReadedListener> msgReadedListeners;

    //尾巴是最新的消息
    public LinkedList<ChatMsgBean> msgList = new LinkedList<ChatMsgBean>();

    public String lastBrifMsg;
    public String lastBrifMsgTime;

    public void updateLastMsg() {
        try {
            ChatMsgBean lastMsg = msgList.getLast();
            if(!TextUtils.isEmpty(lastMsg.sentTime)){
                lastMsgTime.setText(StringParser.getBrifTime(lastMsg.sentTime));
            }
            lastBrifMsg = StringParser.convertToBirfMsg(sessionsFragment.getContext(), lastMsg);
            lastChatMsg.setText(lastBrifMsg);
        } catch (NoSuchElementException noE) {  //没有最后一个元素（开启一个新会话）
            lastMsgTime.setText(StringParser.getBrifTime(lastBrifMsgTime));
        } catch (Exception e) {   //第一次启动，无法获取bean中的视图对象
            lastChatMsg.setText(lastBrifMsg);
        } finally {
            if(lastChatMsg!=null)
                lastBrifMsg = lastChatMsg.getText().toString();
        }
    }

    public void updateRedCycle() {
        if (unReadMsgCount > 0) {
            if (isNoTip()) {
                ivNoTipSmallRedCycle.setImageAlpha(255);
                tvRedCycle.setAlpha(0f);
            } else {
                tvRedCycle.setText("" + unReadMsgCount);
                tvRedCycle.setAlpha(1f);
                ivNoTipSmallRedCycle.setImageAlpha(0);
            }
            return;
        }
        tvRedCycle.setAlpha(0f);
        ivNoTipSmallRedCycle.setImageAlpha(0);
    }
    // 返回当前客户端保存的最旧的成功发送的消息的发送时间,返回""代表获取失败
    public String getOldestMsgSentTime(){
       for(ChatMsgBean oldestMsg:msgList){
           if(!TextUtils.isEmpty(oldestMsg.sentTime)){
               return oldestMsg.sentTime;
           }
       }
      return "";
    }

    //可能会多线程调用
    public synchronized void acceptMsg(ChatMsgBean bean,boolean addToTop,boolean keepMsg) {
        bean.setParentSessionHolder(this);
        if (bean.isMineMsg()) {
            mapToSendingMsgBean.put(bean.msgId, bean);
        }
        if(keepMsg){
            boolean ok = PersonalMsgDao.addMsg(bean);
            if(!ok){
                Utils.showToast2(ImApplication.mainActivity,"本地保存失败");
            }
        }
        msgList.add(addToTop?0:msgList.size(),bean);
        if (!bean.isReaded) {
            unReadMsgCount++;
        }
        for (OnAcceptMsgListener listener : acceptMsgListeners) {
            if (listener != null) {
                listener.onAccept(bean);
            }
        }
    }

    public void setUnReadTag() {
        if (unReadMsgCount != 0 || msgList.size() == 0) {
            return;
        }
        ChatMsgBean lastMsg = msgList.getLast();
        lastMsg.isReaded = false;
        msgReaded(0);
    }

    public void msgReaded(int count) {
        unReadMsgCount-=count;
        unReadMsgCount = unReadMsgCount < 0 ? 0 : unReadMsgCount;
        for (OnMsgReadedListener listener : msgReadedListeners) {
            if (listener != null) {
                listener.onMsgReaded(unReadMsgCount,count);
            }
        }
    }

    public static String getCurTime(String format) {
        SimpleDateFormat sDateFormat = new SimpleDateFormat(format);
        String curTime = sDateFormat.format(new java.util.Date());
        return curTime;
    }

    public void setOnAcceptMsgListener(OnAcceptMsgListener acceptMsgListener) {
        acceptMsgListeners.add(acceptMsgListener);
    }

    public interface OnAcceptMsgListener {
        void onAccept(ChatMsgBean bean);
    }

    public void addOnMsgReadedListener(OnMsgReadedListener msgReadedListener) {
        msgReadedListeners.add(msgReadedListener);
    }

    public interface OnMsgReadedListener {
        void onMsgReaded(int unReadMsgCount,int count);
    }
    // 用于判断是否有重复数据
    public boolean has(String sendTime){
        for(ChatMsgBean item:msgList){
            if(item.sentTime.equals(sendTime)){
                return true;
            }
        }
        return false;
    }
}
