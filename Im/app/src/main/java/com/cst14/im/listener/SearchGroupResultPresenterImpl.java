package com.cst14.im.listener;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.cst14.im.R;
import com.cst14.im.activity.SearchGroupResultActivity;
import com.cst14.im.baseClass.ISearchGroupResultPresenter;
import com.cst14.im.bean.UserGroup;
import com.cst14.im.db.dao.FriendsDao;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;

import java.io.Serializable;
import java.util.List;

/**
 * Created by hz on 2016/8/31.
 */

public class SearchGroupResultPresenterImpl implements ISearchGroupResultPresenter{
    private static final String TAG = "SearchGroupResultPImpl";
    private Handler handler = new Handler(Looper.getMainLooper());
    ISearchGroupResultView mView;
    SearchGroupResultActivity activity;


    private int curGroupId;
    public SearchGroupResultPresenterImpl(ISearchGroupResultView mView) {
        this.mView = mView;
        activity = (SearchGroupResultActivity) mView;

    }

    @Override
    public void onProcess(final ProtoClass.Msg msg) {
        if (msg.getMsgType() == ProtoClass.MsgType.JOIN_GROUP){
            this.onProcessJoinGroup(msg);
            return;
        }
        if (msg.getMsgType() != ProtoClass.MsgType.SEARCH_GROUP){
            return;
        }
        if (msg.getResponseState() == ProtoClass.StatusCode.SUCCESS){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    success(msg);
                }
            });
        }else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fail();
                }
            });
        }
    }
    private void success(ProtoClass.Msg msg) {
        mView.hideWaitingBar();
        mView.hideSearchTip();

        List<ProtoClass.GroupInfo> groupList = msg.getGroupInfoList();
        if (groupList == null || groupList.size() == 0){
            fail();
            return;
        }
        ProtoClass.GroupInfo group = groupList.get(0);
        mView.setGroupNick(group.getGroupName());
        mView.setGroupNO("群号: "+ group.getGroupID());
        mView.setGroupCreateTime(group.getCreateTime());
        mView.setGroupIntro(group.getGroupIntro());
        mView.setGroupType("普通群");
        curGroupId = group.getGroupID();

        //判断该用户是否已经加入此群
        int groupId = group.getGroupID();
        if (ImApplication.instance.getCurUser().hasGroup(groupId)) {
            mView.hideJoinBtn();
        }else {
            mView.showJoinBtn();
        }
        mView.showGroupInfo();
    }
    private void fail(){
        mView.hideGroupInfo();
        mView.hideJoinBtn();
        mView.setTipText("不存在该群");
        mView.setTipClickable(false);
        mView.showSearchTip();
        mView.hideWaitingBar();
    }
    @Override
    public void init() {
        mView.initToolbar("查找群");
        mView.initViews();
        mView.hideGroupInfo();
        mView.hideJoinBtn();
        mView.hideWaitingBar();
        mView.showWaitingBar();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        //加入群
        if (id == R.id.group_join_btn){
            //群id是curGroupId
            joinGroup(curGroupId);
        }
    }

    @Override
    public void sendMsg() {
        new WaitingThread().start();
        init();
        ProtoClass.SearchInfo info = getSearchInfo();
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder().setMsgType(ProtoClass.MsgType.SEARCH_GROUP)
                .setSrchInfo(info);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {

            }

            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                return false;
            }
        });
    }
    /**
     * get the search info from previous activity
     * @return info
     */
    private ProtoClass.SearchInfo getSearchInfo(){
        Intent intent = mView.getSearchIntent();
        Serializable info = intent.getSerializableExtra("search_info");
        if (info instanceof ProtoClass.SearchInfo){
            return (ProtoClass.SearchInfo)info;
        }
        return null;
    }
    @Override
    public void onDestroy() {

    }

    public interface ISearchGroupResultView {
        void initToolbar(String title);
        void initViews();
        Intent getSearchIntent();
        void showWaitingBar();
        void hideWaitingBar();
        void showGroupInfo();
        void hideGroupInfo();
        void showSearchTip();
        void hideSearchTip();
        void showJoinBtn();
        void hideJoinBtn();
        boolean getProcessBarState();
        void setTipText(String tip);
        void setTipClickable(boolean clickable);
        void setTipOnClickListener(View.OnClickListener listener);

        void setGroupIcon(Bitmap bitmap);
        void setGroupNick(String nick);
        void setGroupNO(String no);
        void setGroupType(String type);
        void setGroupCreateTime(String time);
        void setGroupIntro(String intro);

        void showJoinGroupSucceed();
        void showJoinGroupFail();
    }

    private class WaitingThread extends Thread {
        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                SystemClock.sleep(1000);
            }
            if (!mView.getProcessBarState()){
                return;
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mView.hideWaitingBar();
                    mView.setTipClickable(true);
                    mView.setTipText("请求失败，点击重试");
                    mView.showSearchTip();
                    mView.setTipOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendMsg();
                            init();
                            mView.hideSearchTip();
                        }
                    });
                }
            });

        }
    }

    private void onProcessJoinGroup(final ProtoClass.Msg msg) {
        Log.e(TAG, "onProcessJoinGroup: " + msg.toString());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (msg.getResponseState() == ProtoClass.StatusCode.SUCCESS) {
                    mView.showJoinGroupSucceed();
                } else {
                    mView.showJoinGroupFail();
                }
            }
        });
    }

    //加入群
    public void joinGroup(int groupID) {
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setMsgType(ProtoClass.MsgType.JOIN_GROUP);
        builder.setAccount(ImApplication.instance.getCurUser().getName()).setGroupID(groupID);

        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                onProcess(responseMsg);
                return true;
            }
        });

    }
}
