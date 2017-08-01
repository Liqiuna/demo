package com.cst14.im.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.cst14.im.R;
import com.cst14.im.listener.RgstPermissionListener;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;

public class RgstPermissionActivity extends AppCompatActivity implements View.OnClickListener {
    private RadioButton managerPermissions_autoRgst;
    private RadioButton managerPermissions_fastRgst;
    private RadioButton managerPermissions_banRgst;
    private Button managerPermissions_yes;

    private static final int autoRgst = 1;
    private static final int fastRgst = 2;
    private static final int banRgst = 3;

    private RgstPermissionListener rgstPermissionListener = new RgstPermissionListener(this);
    private int rgstType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rgst_permission);
        iniView();
        Tools.addPresenter(rgstPermissionListener);

        ProtoClass.Msg.Builder checkRgstType = ProtoClass.Msg.newBuilder();
        checkRgstType.setMsgType(ProtoClass.MsgType.CHECK_RGST_TYPE);
        sendPermissions(checkRgstType);
    }

    private void iniView(){
        managerPermissions_autoRgst = (RadioButton) findViewById(R.id.managerPermissions_autoRgst);
        managerPermissions_autoRgst.setOnClickListener((View.OnClickListener)this);
        managerPermissions_fastRgst = (RadioButton) findViewById(R.id.managerPermissions_fastRgst);
        managerPermissions_fastRgst.setOnClickListener((View.OnClickListener)this);
        managerPermissions_banRgst = (RadioButton) findViewById(R.id.managerPermissions_banRgst);
        managerPermissions_banRgst.setOnClickListener((View.OnClickListener)this);
        managerPermissions_yes = (Button) findViewById(R.id.managerPermissions_yes);
        managerPermissions_yes.setOnClickListener((View.OnClickListener)this);
    }

    public void onClick(View view){
        switch(view.getId()){
            case R.id.managerPermissions_autoRgst:
                autoRgstChecked();
                rgstType = autoRgst;
                break;
            case R.id.managerPermissions_fastRgst:
                fastRgstChecked();
                rgstType = fastRgst;
                break;
            case R.id.managerPermissions_banRgst:
                banRgstChecked();
                rgstType = banRgst;
                break;
            case R.id.managerPermissions_yes:
                ProtoClass.Msg.Builder msg = ProtoClass.Msg.newBuilder();
                msg.setRgstType(rgstType)
                        .setMsgType(ProtoClass.MsgType.SET_RGST_TYPE);
                sendPermissions(msg);
                break;
        }
    }

    private void sendPermissions(ProtoClass.Msg.Builder builder){
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Toast.makeText(RgstPermissionActivity.this,"发送失败",Toast.LENGTH_LONG);
            }
            //第二个参数是服务器返回的响应消息
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                rgstPermissionListener.onProcess(responseMsg);
                return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
            }
        });
    }

    public void autoRgstChecked(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                managerPermissions_autoRgst.setChecked(true);
                managerPermissions_fastRgst.setChecked(false);
                managerPermissions_banRgst.setChecked(false);
            }
        });
    }

    public void fastRgstChecked(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                managerPermissions_fastRgst.setChecked(true);
                managerPermissions_autoRgst.setChecked(false);
                managerPermissions_banRgst.setChecked(false);
            }
        });
    }

    public void banRgstChecked(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                managerPermissions_banRgst.setChecked(true);
                managerPermissions_autoRgst.setChecked(false);
                managerPermissions_fastRgst.setChecked(false);
            }
        });
    }

    public int getRgstType(){
        return this.rgstType;
    }
}
