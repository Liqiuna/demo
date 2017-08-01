package com.cst14.im.listener;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.cst14.im.activity.GroupChatActivity;
import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.db.dao.GroupMsgDao;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Utils;
import com.cst14.im.utils.groupChatUtils.DownloadFile;

import java.io.File;

/**
 * Created by qi on 16-8-29.
 */
public class GroupChatListener implements iPresenter {
    private static final String TAG = "GroupChatListener";
    String filePath = "";
    private Handler handler = new Handler(Looper.getMainLooper());
    private iGroupChatView view;
    GroupChatActivity activity;

    public GroupChatListener(iGroupChatView view) {
        this.view = view;
        activity = (GroupChatActivity) view;
    }

    @Override
    public void onProcess(final ProtoClass.Msg msg) {
        if (msg.getMsgType() != ProtoClass.MsgType.GROUP_CHAT) {
            return;
        }
        Log.i(TAG, "-----------------GroupChat msg:" + msg.toString());
        if (msg.getResponseState() != ProtoClass.StatusCode.SUCCESS) {
            final String errMsg = msg.getErrMsg();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.showToast(activity.getBaseContext(), errMsg);
                }
            });
            return;
        }

        if (msg.getGroupMsg().getDataType() == ProtoClass.DataType.FEED_BACK_SEND_OK) {
            Log.i(TAG, "FEED——BACE");
            GroupMsgDao.updateMsgFeedBack(msg.toBuilder(), 1);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.msgFeedBack(msg);
                }
            });
            return;
        }

        if (msg.getGroupMsg().getDataType() == ProtoClass.DataType.FEED_BACK_ONE_READED) {
            Log.i(TAG, "FEED_BACK_ONE_READED");
            GroupMsgDao.updateMsgFeedBackRead(msg.toBuilder());
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.msgFeedBackRead(msg);
                }
            });
            return;
        }


        final ProtoClass.GroupMsg groupMsg = msg.getGroupMsg();
        if (groupMsg.getDataType() == ProtoClass.DataType.TEXT) {
            Log.i(TAG, "文字信息");

        } else if (groupMsg.getDataType() == ProtoClass.DataType.IMAGE) {
            Log.e(TAG, "image-----------------------------");
            filePath = ImApplication.photoDir + File.separator + groupMsg.getFileInfo().getName();
            new DownloadFile().Download(ImApplication.FILE_SERVER_HOST + File.separator + groupMsg.getFileInfo().getPath(), filePath);
        } else if (groupMsg.getDataType() == ProtoClass.DataType.VOICE) {
            filePath = ImApplication.photoDir + File.separator + groupMsg.getFileInfo().getName();
            new DownloadFile().Download(ImApplication.FILE_SERVER_HOST + File.separator + groupMsg.getFileInfo().getPath(), filePath);

        } else if (groupMsg.getDataType() == ProtoClass.DataType.VIDEO) {

        } else if (groupMsg.getDataType() == ProtoClass.DataType.FILE)

            Log.i(TAG, "有新消息");
        Log.i(TAG, msg.toString());
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.startSession(msg, filePath);
            }
        });
    }

    public interface iGroupChatView {
        void startSession(final ProtoClass.Msg msg, String filePath);

        void msgFeedBack(final ProtoClass.Msg msg);

        void msgFeedBackRead(final ProtoClass.Msg msg);
    }
}
