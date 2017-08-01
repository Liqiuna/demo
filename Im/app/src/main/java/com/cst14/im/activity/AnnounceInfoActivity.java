package com.cst14.im.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cst14.im.R;
import com.cst14.im.bean.AnnounceBean;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;

/**
 * Created by Administrator on 2016/8/28 0028.
 */
public class AnnounceInfoActivity extends Activity {

    public static final int DELETE_ANNOUNCE_SUCCESS = 0x102;

    private Activity activity;
    private View ly_title;
    private Button btn_back;
    private Button btn_delAnnounce;

    private TextView tv_title;
    private TextView tv_sender;
    private TextView tv_sendTime;
    private TextView tv_content;

    private AnnounceBean bean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_announce);

        activity = this;
        bean = (AnnounceBean) getIntent().getExtras().get("announceBean");
        initView();
        initData();
    }

    private void initView() {
        btn_delAnnounce = (Button) findViewById(R.id.btn_delAnnounce);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_sender = (TextView) findViewById(R.id.tv_sender);
        tv_sendTime = (TextView) findViewById(R.id.tv_time);
        tv_content = (TextView) findViewById(R.id.tv_name);
        findViewById(R.id.ly_title).setVisibility(View.VISIBLE);
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });

        initHideWidget();
    }

    private void initHideWidget() {
        if (!ImApplication.User_id.equals(bean.getSender())) return;

        btn_delAnnounce = (Button) findViewById(R.id.btn_delAnnounce);
        btn_delAnnounce.setVisibility(View.VISIBLE);
        btn_delAnnounce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ProtoClass.GroupAnnounce.Builder announce = ProtoClass.GroupAnnounce.newBuilder();
                announce.setGroupID(bean.getGroupID());
                announce.setAnnounceID(bean.getAnnounceID());

                ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                builder.setAccount(ImApplication.User_id);
                builder.setMsgType(ProtoClass.MsgType.DEL_ANNOUNCE);
                builder.addAnnounce(announce);

                Tools.startTcpRequest(builder, new Tools.TcpListener() {
                    @Override
                    public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                        if (ProtoClass.MsgType.DEL_ANNOUNCE != responseMsg.getMsgType()) return false;
                        if (ProtoClass.StatusCode.SUCCESS   != responseMsg.getResponseState()) {
                            Toast.makeText(activity, responseMsg.getErrMsg(), Toast.LENGTH_SHORT).show();
                            return true;
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                               onDelAnnounceSuccess();
                            }
                        });
                        return true;
                    }
                });
            }
        });
    }

    private void initData() {
        tv_title.setText(bean.getTitle());
        tv_sender.setText(bean.getSender());
        tv_sendTime.setText(bean.getSendTime());
        tv_content.setText(bean.getContent());
    }

    private void onDelAnnounceSuccess() {
        Toast.makeText(activity, "删除成功", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra("announceID", bean.getAnnounceID());
        setResult(DELETE_ANNOUNCE_SUCCESS, intent);
        finish();
    }
}
