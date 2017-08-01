package com.cst14.im.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;

/**
 * Created by Administrator on 2016/8/29 0029.
 */
public class GroupMoreFuncActivity extends Activity {

    private Activity activity;
    private int groupID;
    private ImageButton ib_announce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_more_func);

        groupID = getIntent().getIntExtra("groupID", -1);
        activity = this;
        initView();
    }

    private void initView() {
        ib_announce = (ImageButton) findViewById(R.id.ib_groupAnnounce);
        ib_announce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipToGroupAnnounce();
            }
        });
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ib_announce.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ib_announce.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                isHaveNewAnnounce();
            }
        });
    }

    private void skipToGroupAnnounce() {
        Intent intent = new Intent(activity, GroupAnnounceActivity.class);
        intent.putExtra("groupID", groupID);
        startActivity(intent);
    }

    private void showAnnouncePopup(ProtoClass.GroupAnnounce announce) {
        View view = getLayoutInflater().inflate(R.layout.popup_announce, null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        TextView tv_sender = (TextView) view.findViewById(R.id.tv_sender);
        TextView tv_time = (TextView) view.findViewById(R.id.tv_time);
        TextView tv_content = (TextView) view.findViewById(R.id.tv_name);
        tv_title.setText(announce.getTitle());
        tv_sender.setText(announce.getSender());
        tv_time.setText(announce.getSendTime());
        tv_content.setText(announce.getContent());

        final PopupWindow popupWindow = new PopupWindow(view,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.showAtLocation(findViewById(R.id.ly_title), Gravity.TOP, 0, 150);

        view.findViewById(R.id.btn_acknowledge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        view.findViewById(R.id.tv_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               skipToGroupAnnounce();
            }
        });
    }

    private void isHaveNewAnnounce() {
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setAccount(ImApplication.User_id);
        builder.setMsgType(ProtoClass.MsgType.IS_HAVE_NEW_ANNOUNCE);
        builder.setGroupID(groupID);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                if (ProtoClass.MsgType.IS_HAVE_NEW_ANNOUNCE != responseMsg.getMsgType()) return false;
                if (ProtoClass.StatusCode.SUCCESS   != responseMsg.getResponseState()) {
                    return true;
                }

                final ProtoClass.GroupAnnounce announce = responseMsg.getAnnounce(0);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAnnouncePopup(announce);
                    }
                });
                return true;
            }
        });
    }
}
