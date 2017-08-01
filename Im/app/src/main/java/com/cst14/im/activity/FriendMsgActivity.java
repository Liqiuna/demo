package com.cst14.im.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import com.cst14.im.R;
import com.cst14.im.bean.FriendMsgBean;
import com.cst14.im.layoutView.MyScrollView;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Utils;

/**
 * Created by MRLWJ on 2016/9/12.
 */
public class FriendMsgActivity extends Activity implements View.OnClickListener {
    private TextView mTvRole,mTvNickName, mTvSex, mTvAge, mTvAddr, mTvIntro;
    private String friendId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR); //隐藏标题栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); //状态栏透明
        setContentView(R.layout.activity_friend_msg);
        friendId = ImApplication.instance.getCurSessionHolder().userAccount;
        initView();
        //开始请求数据
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setView(new ProgressBar(this));
        final AlertDialog dlg = dlgBuilder.show();
        getFriendMsg(friendId, new OnGetFriendMsg() {
            @Override
            public void success(FriendMsgBean friendMsgBean) {
                mTvRole.setText(friendMsgBean.role);
                mTvNickName.setText(friendMsgBean.nickName);
                mTvAge.setText(friendMsgBean.age);
                mTvAddr.setText(friendMsgBean.addr);
                mTvSex.setText(friendMsgBean.sex);
                mTvIntro.setText(friendMsgBean.intro);
                Log.e("接受到了数据",friendMsgBean.intro);
                dlg.dismiss();
            }

            @Override
            public void failed(String err) {
                dlg.dismiss();
                Utils.showToast2(FriendMsgActivity.this,"获取好友信息失败"+err);
            }
        });
    }

    /**
     * 根据id获取用户数据，获取完成时会自动调用callback，去看OnGetFriendMsg的简介
     * @param account 被请求的好友账号
     * @return 返回好友信息信息对象
     */
    public static void getFriendMsg(final String account, final OnGetFriendMsg callBack) {
        ProtoClass.Msg.Builder msgBuilder = ProtoClass.Msg.newBuilder();
        msgBuilder.setAccount(account)
                .setToken(ImApplication.getLoginToken())
                .setMsgType(ProtoClass.MsgType.GET_FRIEND_MSG);

        Tools.startTcpRequest(msgBuilder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                try {
                    if (responseMsg.getResponseState() != ProtoClass.StatusCode.SUCCESS) {
                        final String errMsg = responseMsg.getErrMsg();
                        ImApplication.mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callBack.failed(errMsg);
                            }
                        });
                        return true;
                    }
                    ProtoClass.User userMsg = responseMsg.getUser();
                    ProtoClass.UserDetail userDetail = userMsg.getUserDetail();
                    final FriendMsgBean bean = new FriendMsgBean();
                    bean.account = account;
                    bean.addr = userDetail.getAddress();
                    bean.age = userDetail.getAge() + "";
                    bean.intro = userMsg.getUesrIntro();
                    bean.sex = userDetail.getSex();
                    bean.nickName = userMsg.getNickName();
                    switch (userMsg.getUserRoleID()){
                        case 1:
                            bean.role = "普通用户";
                            break;
                        case 2:
                            bean.role = "管理员";
                            break;
                        case 3:
                            bean.role = "超级管理员";
                            break;
                        default:
                            bean.role = "未知角色";
                    }
                    ImApplication.mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callBack.success(bean);
                        }
                    });
                } catch (final Exception e) {
                    ImApplication.mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callBack.failed(e.toString());
                        }
                    });
                }
                return true;
            }
        });
    }

    /**
     * 响应成功的回掉函数
     * success（）和failed（）都是运行在主线程中的
     */
    public interface OnGetFriendMsg {
        void success(FriendMsgBean friendMsgBean);
        void failed(String err);
    }

    private View mTvTitle;
    private int mTitleHeight;
    private MyScrollView mSvMsgContainer;
    private View mTvBack, mTvMore;
    private void initView() {
        mTvBack = findViewById(R.id.tv_back);
        mTvMore = findViewById(R.id.tv_more);
        mSvMsgContainer = (MyScrollView) findViewById(R.id.sv_friend_msg);
        mTvTitle = findViewById(R.id.ll_title_bg);
        mTvTitle.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mTvTitle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mTitleHeight = mTvTitle.getHeight();
                mSvMsgContainer.setData(mTvTitle,mTitleHeight);
            }
        });
        mTvBack.setOnClickListener(this);
        mTvMore.setOnClickListener(this);
        mTvRole = (TextView) findViewById(R.id.tv_msg_role);
        TextView mTvId = (TextView) findViewById(R.id.tv_msg_id);
        mTvId.setText(friendId);
        mTvNickName = (TextView) findViewById(R.id.tv_msg_nick_name);
        mTvSex = (TextView) findViewById(R.id.tv_msg_sex);
        mTvAge = (TextView) findViewById(R.id.tv_msg_age);
        mTvAddr = (TextView) findViewById(R.id.tv_msg_addr);
        mTvIntro = (TextView) findViewById(R.id.tv_msg_intro);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_more:
                Utils.showToast(this, "点击了更多");
                break;
            case R.id.tv_back:
                finish();
                break;
        }
    }

}
