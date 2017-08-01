package com.cst14.im.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cst14.im.R;
import com.cst14.im.bean.AnnounceBean;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;

/**
 * Created by Administrator on 2016/8/28 0028.
 */
public class AnnounceCreateActivity extends Activity {

    public static final int CREATE_ANNOUNCE_SUCCESS = 0x101;

    private int groupID;
    private Activity activity;

    private Button btn_back;
    private Button btn_create;
    private EditText et_title;
    private EditText et_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_announce);

        activity = this;
        groupID = getIntent().getIntExtra("groupID", -1);

        initView();
    }

    private void initView() {
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_create = (Button) findViewById(R.id.btn_announceCreate);
        et_title  = (EditText) findViewById(R.id.et_title);
        et_content = (EditText) findViewById(R.id.et_content);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = et_title.getText().toString().trim();
                String content = et_content.getText().toString().trim();
                if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
                    Toast.makeText(activity, "标题或内容不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (title.length() < 4 || title.length() > 20) {
                    Toast.makeText(activity, "标题必须为4-20字", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (content.length() < 15 || content.length() > 200) {
                    Toast.makeText(activity, "正文必须为15-200字", Toast.LENGTH_SHORT).show();
                    return;
                }

                ProtoClass.GroupAnnounce.Builder announceBuilder = ProtoClass.GroupAnnounce.newBuilder();
                announceBuilder.setGroupID(groupID).setTitle(title).setContent(content);
                announceBuilder.setSender(ImApplication.User_id);

                ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                builder.setMsgType(ProtoClass.MsgType.CREATE_ANNOUNCE);
                builder.setAccount(ImApplication.User_id);
                builder.addAnnounce(announceBuilder);

                Tools.startTcpRequest(builder, new Tools.TcpListener() {
                    @Override
                    public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                        if (ProtoClass.MsgType.CREATE_ANNOUNCE != responseMsg.getMsgType()) return false;
                        if (ProtoClass.StatusCode.SUCCESS   != responseMsg.getResponseState()) {
                            Toast.makeText(activity, responseMsg.getErrMsg(), Toast.LENGTH_SHORT).show();
                            return true;
                        }

                        final ProtoClass.GroupAnnounce announce = responseMsg.getAnnounce(0);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onCreateAnnounceSucces(announce);
                            }
                        });
                        return true;
                    }
                });
            }
        });
    }

    private void onCreateAnnounceSucces(ProtoClass.GroupAnnounce announce) {
        Toast.makeText(this, "发布成功", Toast.LENGTH_SHORT).show();

        AnnounceBean bean = new AnnounceBean(announce);
        Intent intent = new Intent();
        intent.putExtra("announceBean", bean);
        setResult(CREATE_ANNOUNCE_SUCCESS, intent);
        finish();
    }
}
