package com.cst14.im.Dialog;

import android.content.ContentValues;
import android.net.wifi.WifiEnterpriseConfig;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.db.dao.PersonalMsgDao;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Utils;
import com.cst14.im.utils.sessionUtils.ChatMsgBean;

import org.w3c.dom.Text;

/**
 * Created by MRLWJ on 2016/9/2.
 * 用来显示阅读记录的对话框布局
 */
public class PersonalMsgReadingDlg extends RelativeLayout {
    private ChatMsgBean mMsg;
    private ProgressBar mPbLoading;
    private static RelativeLayout.LayoutParams params;

    static {
        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(CENTER_IN_PARENT);
    }

    public PersonalMsgReadingDlg(ChatMsgBean msg) {
        super(ImApplication.mainActivity);
        setPadding(10 * (int) ImApplication.density, 10 * (int) ImApplication.density, 10 * (int) ImApplication.density, 10 * (int) ImApplication.density);
        mPbLoading = new ProgressBar(ImApplication.mainActivity);
        mPbLoading.setLayoutParams(params);
        mMsg = msg;
        if(TextUtils.isEmpty(mMsg.readTime)){
            sendRequest();
        }else{
            showReadTime(mMsg.readTime);
        }
    }

    private void sendRequest() {
        if(TextUtils.isEmpty(mMsg.sentTime)){
            Utils.showToast2(ImApplication.mainActivity,"消息还未发送完成");
            return;
        }
        addView(mPbLoading);
        ProtoClass.Msg.Builder msgBuilder = ProtoClass.Msg.newBuilder();
        msgBuilder.setToken(ImApplication.getLoginToken())
                .setMsgType(ProtoClass.MsgType.GET_PERSONAL_MSG_READING_RECORD)
                .setFriendID(Integer.parseInt(mMsg.getParentSessionHolder().userAccount))
                .setMsgTime(mMsg.sentTime);
        Tools.startTcpRequest(msgBuilder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Utils.showToast2(ImApplication.mainActivity,"发送请求失败");
            }
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                ProtoClass.PersonalMsgReadingRecord record = responseMsg.getRecord();
                final String readTime = record.getReadTime();
                ImApplication.mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PersonalMsgReadingDlg.this.removeView(mPbLoading);
                        if(TextUtils.isEmpty(readTime)||readTime.endsWith("00:00:00")){
                            showUnRead();
                        }else{
                            mMsg.readTime = readTime;
                            showReadTime(readTime);
                            ContentValues values = new ContentValues();
                            values.put("read_time",readTime);
                            int num = PersonalMsgDao.db.update("msg_data", values, "msg_id=?", new String[]{mMsg.msgId});
                            if(num<=0){
                                Utils.showToast2(ImApplication.mainActivity,"数据更新失败");
                            }
                        }
                    }
                });
                return true;
            }
        });
    }
    private void showUnRead(){
        View.inflate(ImApplication.mainActivity, R.layout.dlg_unread,this);
    }
    private void showReadTime(String readTime){
        View content = View.inflate(ImApplication.mainActivity,R.layout.dlg_readed,this);
        TextView tv = (TextView) content.findViewById(R.id.tv_dlg_read_time);
        tv.setText("阅读时间："+readTime);
    }
}
