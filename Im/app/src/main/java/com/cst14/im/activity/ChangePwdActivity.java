package com.cst14.im.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.cst14.im.R;
import com.cst14.im.listener.ChangePwdListener;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/25 0025.
 */
public class ChangePwdActivity extends Activity implements ChangePwdListener.ChangePwdView {
    private ChangePwdListener mChangePwdListener;
    private String userName;
    public ImApplication app;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changpwd);
        app = (ImApplication) getApplication();
        mChangePwdListener = new ChangePwdListener(this);
        Tools.addPresenter(mChangePwdListener);
        initView();
        getPswQuetion();
    }
    private void getPswQuetion(){
        userName = app.User_id;
        if(userName.equals("")){
            LinearLayout inputUserNameDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.input_username,null);
            userNameEt = (EditText)inputUserNameDialog.findViewById(R.id.input_username_et);
            Dialog dialogBuilder = new AlertDialog.Builder(ChangePwdActivity.this)
                    .setView(inputUserNameDialog)
                    .setTitle("请先输入账号")
                    .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            finish();
                        }
                    })
                    .setNegativeButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            userName = userNameEt.getText().toString().trim();
                            ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                            builder.setAccount(userName)
                                    .setMsgType(ProtoClass.MsgType.GET_PWD_QUETION);
                            Tools.startTcpRequest(builder, new Tools.TcpListener() {
                                @Override
                                public void onSendFail(Exception e) {
                                    Utils.showToast2(ChangePwdActivity.this, "请求发送失败");
                                }

                                //第二个参数是服务器返回的响应消息
                                @Override
                                public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                                    mChangePwdListener.onProcess(responseMsg);
                                    return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
                                }
                            });
                        }
                    })
                    .create();
            dialogBuilder.show();
        } else {
            ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
            builder.setAccount(userName)
                    .setMsgType(ProtoClass.MsgType.GET_PWD_QUETION);
            Tools.startTcpRequest(builder, new Tools.TcpListener() {
                @Override
                public void onSendFail(Exception e) {
                    Utils.showToast2(ChangePwdActivity.this, "请求发送失败");
                }

                //第二个参数是服务器返回的响应消息
                @Override
                public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                    mChangePwdListener.onProcess(responseMsg);
                    return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
                }
            });
        }
    }
    private Button mChangePwd,mReturn;
    private EditText mAns;
    private Spinner spinner;
    private ArrayAdapter<String> arr_adapter;
    private List<String> data_list;
    private int QueNum;
    private LayoutInflater layoutInflater;
    private View myVerifyPwdQuetion;
    private EditText newPwd_ET;
    private AlertDialog.Builder dialogBuilder;
    private EditText userNameEt;
    private void initView(){
        mChangePwd = (Button)findViewById(R.id.btn_change);
        mReturn = (Button)findViewById(R.id.btn_return);
        mAns = (EditText)findViewById(R.id.edt_ans);
        spinner = (Spinner) findViewById(R.id.spinner);
        data_list = new ArrayList<String>();
        data_list.add("请选择密保问题");
        arr_adapter= new ArrayAdapter<String>(this, R.layout.spinner_layout, data_list);
        //设置样式
        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //加载适配器
        spinner.setAdapter(arr_adapter);
        spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
        mChangePwd.setOnClickListener(mListener);
        mReturn.setOnClickListener(mListener);
    }
    View.OnClickListener mListener = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_change:
				    if (QueNum == 0 ){
                        new android.support.v7.app.AlertDialog.Builder(ChangePwdActivity.this)
                                .setTitle("提示")
                                .setMessage("请选择密保问题")
                                .setPositiveButton("确定", null)
                                .show();
                        break;
                    }
                    String ans = mAns.getText().toString().trim();

                    ProtoClass.MsgQuestion.Builder quetionBuilder = ProtoClass.MsgQuestion.newBuilder();
                    quetionBuilder.setPswQuetionNumber(QueNum).setPswAnswer(ans);

                    ProtoClass.MsgSecurity.Builder securityBuilder = ProtoClass.MsgSecurity.newBuilder();
                    securityBuilder.setQuestionSet(quetionBuilder);

                    ProtoClass.Msg.Builder msgBuilder = ProtoClass.Msg.newBuilder();
                    msgBuilder.setAccount(userName)
                            .setSecurityItem(securityBuilder)
                            .setMsgType(ProtoClass.MsgType.VERIFY_PWD_QUETION);
                    Tools.startTcpRequest(msgBuilder, new Tools.TcpListener() {
                        @Override
                        public void onSendFail(Exception e) {
                            Utils.showToast2(ChangePwdActivity.this, "请求发送失败");
                        }

                        //第二个参数是服务器返回的响应消息
                        @Override
                        public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                            mChangePwdListener.onProcess(responseMsg);
                            return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
                        }
                    });
                    break;
                case R.id.btn_return:
                    finish();
                    break;


            }
        }
    };
    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
            QueNum = arg2;
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
    public void successGetPswQuetion(String pswQue1,String pswQue2,String pswQue3){
        data_list.add(pswQue1);
        data_list.add(pswQue2);
        if (pswQue3.equals("")){

        } else {
            data_list.add(pswQue3);
        }
    }
    public void falseGetPswQuetion(String errMsg) {
        new android.support.v7.app.AlertDialog.Builder(ChangePwdActivity.this)
                .setTitle("提示")
                .setMessage(errMsg)
                .setPositiveButton("确定", null)
                .show();
    }
    public void successChangePwd(){
        new android.support.v7.app.AlertDialog.Builder(ChangePwdActivity.this)
                .setTitle("提示")
                .setMessage("密码修改成功")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //finish();
                    }
                })
                .show();
    }
    public void falseChangePwd(String errMsg) {
        new android.support.v7.app.AlertDialog.Builder(ChangePwdActivity.this)
                .setTitle("提示")
                .setMessage(errMsg)
                .setPositiveButton("确定", null)
                .show();
    }
    @Override
    public void successVerifyPwdQuetion(){
        final LinearLayout findPwdDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.verify_pwd_quetions,null);
        newPwd_ET = (EditText) findPwdDialog.findViewById(R.id.new_pwd_set_et);
        Dialog dialogPwdBuilder = new AlertDialog.Builder(ChangePwdActivity.this)
        .setTitle("修改密码")
        .setView(findPwdDialog)
        .setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        })
        .setNegativeButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String newPwd = newPwd_ET.getText().toString().trim();
                ProtoClass.Msg.Builder msgBuilder = ProtoClass.Msg.newBuilder();
                msgBuilder.setPwd(newPwd)
                        .setAccount(userName)
                        .setMsgType(ProtoClass.MsgType.CHANGE_PASSWORD_BY_QUETIONS );
                Tools.startTcpRequest(msgBuilder, new Tools.TcpListener() {
                    @Override
                    public void onSendFail(Exception e) {
                        Utils.showToast2(ChangePwdActivity.this, "请求发送失败");
                    }

                    //第二个参数是服务器返回的响应消息
                    @Override
                    public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                        mChangePwdListener.onProcess(responseMsg);
                        return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
                    }
                });
                findPwdDialog.removeAllViews();
            }
        }).create();
        dialogPwdBuilder.show();
                //dialogBuilder.show();
    }
}
