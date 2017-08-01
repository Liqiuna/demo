package com.cst14.im.listener;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cst14.im.Fragment.FriendFragment;
import com.cst14.im.Fragment.FriendInfo;
import com.cst14.im.R;
import com.cst14.im.activity.SearchResultActivity;
import com.cst14.im.adapter.SearchUserResultAdapter;
import com.cst14.im.baseClass.ISearchResultPresenter;
import com.cst14.im.db.dao.FriendsDao;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by hz on 2016/8/24.
 */

public class SearchResultPresenterImpl implements ISearchResultPresenter {
    private  ProtoClass.MsgType [] op = new ProtoClass.MsgType[] {ProtoClass.MsgType.SEEK_AROUD, ProtoClass.MsgType.SEEK_CONDITION,ProtoClass.MsgType.SEEK_NAME,ProtoClass.MsgType.ADD_FRIEND_Rquest};
    public FriendsDao friendsDao;//数据库接口
    private ISearchResultView mView;
    private SearchResultActivity activity;

    //是否查找更多结果
    private boolean searchMore = false;

    //还有没有更多结果可以查询
    private boolean hasMoreToSearch = true;

    //用户进行查找的信息
    private ProtoClass.SearchInfo mSearchInfo;

    //更新listView的数据时，应该指向的位置，即新添加的位置
    private int posAfterUpdate = 0;

    private SearchUserResultAdapter adapter;


    public SearchResultPresenterImpl(ISearchResultView view) {
        this.mView = view;
        activity = (SearchResultActivity) view;
        friendsDao=new FriendsDao(activity.getBaseContext());
    }

    @Override
    public void onProcess(final ProtoClass.Msg msg) {
        if (!Arrays.asList(op).contains(msg.getMsgType())) return;
        switch (msg.getMsgType()) {
            case ADD_FRIEND_Rquest:
                if (msg.getResponseState().equals(ProtoClass.StatusCode.FAILED)) {
                    final String errMsg = msg.getErrMsg();
                    activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, errMsg, Toast.LENGTH_SHORT).show();
                    }
                    });
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                Toast.makeText(activity,"发送成功，等待对方同意",Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case GET_NEARBY:
                if (msg.getResponseState().equals(ProtoClass.StatusCode.FAILED)) {
                    final String errMsg = msg.getErrMsg();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, errMsg, Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        success(msg);
                    }
                });
                break;
            case SEEK_NAME:
                if (msg.getResponseState() == ProtoClass.StatusCode.FAILED) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fail();
                        }
                    });
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Search success");
                        success(msg);
                    }
                });
                break;
            case SEEK_CONDITION:
                if (msg.getResponseState() == ProtoClass.StatusCode.FAILED) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fail();
                        }
                    });
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Search success");
                        success(msg);
                    }
                });
                break;


        }
    }

    @Override
    public void initToolbar() {
        mView.initToolbar("查找结果");
    }

    @Override
    public void initViews() {
        mView.showProcessBar();
        if (!searchMore) mView.hideListView();
        mView.hideTipText();
    }

    @Override
    public void success(ProtoClass.Msg msg) {
        mView.hideProcessBar();
        mView.hideTipText();
        List<ProtoClass.User> list = msg.getFriendsList();
        List<Double> distanceList = new LinkedList<>();

        //if data is from nearby
        if (msg.getMsgType() == ProtoClass.MsgType.GET_NEARBY) {
            List<ProtoClass.Nearby> nearbyList = msg.getNearByList();
            if (nearbyList == null || nearbyList.size() == 0) {
                fail();
                return;
            }
            list = new LinkedList<>();
            distanceList = new LinkedList<>();
            for (ProtoClass.Nearby nearby : nearbyList) {
                ProtoClass.User user = ProtoClass.User.newBuilder()
                        .setUserName(nearby.getName())
                        .setNickName(nearby.getNick())
                        .setUserID(0)
                        .setIcon(nearby.getAvatar())
                        .setUserDetail(ProtoClass.UserDetail.newBuilder()
                            .setAge(nearby.getAge())
                            .setSex(nearby.getSex()))
                        .build();
                //add to user list
                list.add(user);
                //add distance to the list
                distanceList.add(nearby.getDistance());
            }
        }
        hasMoreToSearch = (list != null && list.size() >= 15);

        if(searchMore){ //if the result from search more way
            if (adapter == null){
                adapter = new SearchUserResultAdapter(activity,msg.getFriendsList(),msg.getMsgType());
            }else {
                adapter.addData(msg.getFriendsList());
            }
        }else{
            adapter = new SearchUserResultAdapter(activity,msg.getFriendsList(),msg.getMsgType());
        }
        if (msg.getMsgType() == ProtoClass.MsgType.GET_NEARBY && distanceList != null) {
            adapter.setDistanceList(distanceList);
        }
        mView.setAdapter(adapter);
        mView.setListViewSelection(posAfterUpdate);
        mView.showListView();
    }

    @Override
    public void fail() {
        if (searchMore){
            mView.hideTipText();
            mView.hideProcessBar();
            mView.showListView();
            Toast.makeText(activity,"无更多结果",Toast.LENGTH_LONG).show();
        }else {
            mView.hideListView();
            mView.hideProcessBar();
            mView.setTipString("无查找结果");
            mView.setTipClickable(false);
            mView.showTipText();
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void clickUserListItem(int pos) {
        showMenuDialog(pos);
    }

    private void showMenuDialog(final int pos) {

        //加载布局
        View contentView = LayoutInflater.from(activity)
                .inflate(R.layout.click_user_menu,null);

        final AlertDialog dialog = new AlertDialog.Builder(activity).setView(contentView).create();

        final View addFriendLayout = contentView.findViewById(R.id.menu_add_layout);
        final View sendmsgLayout = contentView.findViewById(R.id.menu_sendMsg_layout);
        final View informLayout = contentView.findViewById(R.id.menu_jubao_layout);

        addFriendLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProtoClass.User user = (ProtoClass.User)adapter.getItem(pos);
                if (user == null) return;
                int id = user.getUserID();
                if (id == 0) {
                    try {
                        id = Integer.parseInt(user.getUserName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                addFriendRequest(id);
                dialog.dismiss();
            }
        });
        sendmsgLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        informLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void addFriendRequest(int friendID) {
        int curUserId = Integer.parseInt(ImApplication.User_id);
        ProtoClass.User user = ProtoClass.User.newBuilder().setUserID(curUserId).setNickName(Model.getUsername()).build();
        ProtoClass.User friend= ProtoClass.User.newBuilder().setUserID(friendID).build();
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setUser(user)
                .addFriends(friend)
                .setMsgType(ProtoClass.MsgType.ADD_FRIEND_Rquest);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Log.e("发送失败", "\"发送失败\"");
            }
//            第二个参数是服务器返回的响应消息
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                onProcess(responseMsg);
                return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
            }
        });
    }
    @Override
    public void scrollToLastItem() {
        searchMore = true;

        if (!hasMoreToSearch) {
            return;
        }
        mView.showProcessBar();
        //send to server
        new Thread() {
            @Override
            public void run() {
                posAfterUpdate = adapter.getCount() - 1;
                ProtoClass.User lastUser = (ProtoClass.User) adapter.getItem(posAfterUpdate);
                ProtoClass.SearchInfo.Builder builder = ProtoClass.SearchInfo.newBuilder(mSearchInfo)
                        .setSinceId(lastUser.getUserID());
                ProtoClass.Msg.Builder msg = ProtoClass.Msg.newBuilder()
                        .setMsgType(mSearchInfo.getSearchType())
                        .setSrchInfo(builder);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //write to server
                Tools.startTcpRequest(msg, new Tools.TcpListener() {
                    @Override
                    public boolean onResponse(String msgId, final ProtoClass.Msg responseMsg) {
                        if (responseMsg.getResponseState() == ProtoClass.StatusCode.FAILED){
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    fail();
                                }
                            });
                        }
                        else if (responseMsg.getResponseState() == ProtoClass.StatusCode.SUCCESS) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    System.out.println("Search success");
                                    success(responseMsg);
                                }
                            });
                        }
                        return true;
                    }
                });
            }
        };
    }
    @Override
    public void sendMsg() {
        if (!searchMore) mSearchInfo = getSearchInfo();
        ProtoClass.Msg.Builder msg = ProtoClass.Msg.newBuilder()
                .setMsgType(mSearchInfo.getSearchType())
                .setSrchInfo(mSearchInfo);
        //if the action is get nearby
        if (mSearchInfo.getSearchType() == ProtoClass.MsgType.GET_NEARBY){
            msg.setAccount(mSearchInfo.getSrchName());
            ProtoClass.Location location = ProtoClass.Location.newBuilder()
                    .setLatitude(mSearchInfo.getLat())
                    .setLongitude(mSearchInfo.getLng()).build();
            msg.setLocation(location);
        }


        new WaitingResponseThread().start();
        initViews();
        Tools.startTcpRequest(msg, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, final ProtoClass.Msg responseMsg) {
                if (responseMsg.getResponseState() == ProtoClass.StatusCode.FAILED){
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fail();
                            }
                        });
                }
                else if (responseMsg.getResponseState() == ProtoClass.StatusCode.SUCCESS) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("Search success");
                            success(responseMsg);
                        }
                    });
                }
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
    public interface ISearchResultView {
        void initToolbar(String title);
        void hideProcessBar();
        void showProcessBar();
        void showTipText();
        void hideTipText();
        void setAdapter(BaseAdapter adapter);
        void hideListView();
        void showListView();
        RelativeLayout getWindowLayout();
        int getWindowWidth();
        int getWindowHeight();
        Intent getSearchIntent();
        boolean getProcessBarState();
        void setListViewSelection(int pos);
        void setTipString(String tip);
        void setTipClickable(boolean clickable);
        void setTipOnClickListener(View.OnClickListener listener);
    }
    //等待服务端响应的线程，时长为5s
    private class WaitingResponseThread extends Thread {
        @Override
        public void run() {
            for(int i = 0; i < 5; i++){
                SystemClock.sleep(1000);
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!activity.getProcessBarState()) {
                        return;
                    }
                    activity.hideProcessBar();
                    mView.setTipString("无法响应请求，点击重试");
                    mView.setTipOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendMsg();
                        }
                    });
                    mView.setTipClickable(true);
                    mView.showTipText();
                }
            });
        }
    }
}
