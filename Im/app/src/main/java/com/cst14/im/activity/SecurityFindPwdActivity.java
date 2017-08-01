package com.cst14.im.activity;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.cst14.im.R;
import com.cst14.im.listener.SecurityFindPwdListener;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;

import java.util.regex.Pattern;

public class SecurityFindPwdActivity extends AppCompatActivity implements SecurityFindPwdListener.ISecurityFindPwdView,View.OnClickListener{

    private ActionBar actionBar;
    private LinearLayout findPwd_email_ll;
    private LinearLayout findPwd_question_ll;
    private LinearLayout findPwd_phone_ll;

    private EditText inputEmail;

    private SecurityFindPwdListener mSecurityFindPwdListener;
    private String account_app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_find_pwd);
        initView();

    }

    private void initView(){
        findPwd_email_ll = (LinearLayout)findViewById(R.id.find_pwd_email_ll);
        findPwd_phone_ll = (LinearLayout)findViewById(R.id.find_pwd_phone_ll);
        findPwd_question_ll = (LinearLayout)findViewById(R.id.find_pwd_question_ll);

        mSecurityFindPwdListener = new SecurityFindPwdListener(this);
        Tools.addPresenter(mSecurityFindPwdListener);
        account_app = ImApplication.User_id;
        mSecurityFindPwdListener.initToolbar();

        findPwd_question_ll.setOnClickListener(this);
        findPwd_email_ll.setOnClickListener(this);
        findPwd_phone_ll.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.find_pwd_email_ll:
                Intent intent0 = getIntent();
                final String email = intent0.getStringExtra("emailBindC");

                final AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(this);
                final LinearLayout findPwdDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.security_find_pwd_email,null);
                alertDialogBuilder.setView(findPwdDialog);
                alertDialogBuilder.setCancelable(true);
                final AlertDialog dialog=alertDialogBuilder.create();
                dialog.setTitle("找回密码");
                dialog.show();

                inputEmail = (EditText)findPwdDialog.findViewById(R.id.find_pwd_email_tv);
                final EditText inputEmail = (EditText)findPwdDialog.findViewById(R.id.find_pwd_email_tv);
                if (!email.equals("未绑定")&&!email.equals("为验证")){
                    inputEmail.setText(email);
                    inputEmail.setEnabled(false);
                }
                final String email_send = inputEmail.getText().toString();

                findPwdDialog.findViewById(R.id.find_pwd_email_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (matchEmailPattern(email_send)){
                            ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                            builder.setAccount(account_app).setMsgType(ProtoClass.MsgType.CHANGE_PWD_BY_EMAIL)
                                    .setToken(ImApplication.getLoginToken())
                            .setSecurityItem(ProtoClass.MsgSecurity.newBuilder().setEmailBind(
                                    ProtoClass.MsgEmail.newBuilder().setUserEmail(email_send)));
                            Tools.startTcpRequest(builder, new Tools.TcpListener() {
                                @Override
                                public void onSendFail(Exception e) {
                                    popDialog("发送邮箱失败");
                                }

                                @Override
                                public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                                    mSecurityFindPwdListener.onProcess(responseMsg);
                                    return true;
                                }
                            });

                        }else {
                            popDialog("邮箱格式有误，请重新输入");
                        }
                        dialog.dismiss();
                    }
                });

                findPwdDialog.findViewById(R.id.cancel_find_pwd_email_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                break;
            case R.id.find_pwd_phone_ll:
                Intent intent = new Intent();
                intent.setClass(SecurityFindPwdActivity.this,LoginPhoneVerifyActivity.class);
                startActivity(intent);
                break;
            case R.id.find_pwd_question_ll:
                Intent intentToChangePwd = new Intent();
                intentToChangePwd.setClass(SecurityFindPwdActivity.this,ChangePwdActivity.class);
                startActivity(intentToChangePwd);
                break;
        }
    }

    @Override
    public void initToolbar(String title) {
        actionBar = getSupportActionBar();
        if (actionBar == null) return;

        actionBar.setTitle(title);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDefaultDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void doFindByEmailResult(String result) {
        new AlertDialog.Builder(SecurityFindPwdActivity.this)
                .setTitle("提示")
                .setMessage("请访问邮件中给出的网址链接之地，根据页面提示完成密码重置")
                .setPositiveButton("确定", null)
                .show();
    }

    @Override
    public void doFailed(String errMsg) {
        popDialog("发送邮箱失败");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                Intent intent = new Intent();
                intent.setClass(SecurityFindPwdActivity.this,SettingSecurityActivity.class);
                startActivity(intent);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //弹出信息提示对话框
    private void popDialog(String msg){
        new AlertDialog.Builder(SecurityFindPwdActivity.this)
                .setTitle("提示")
                .setMessage(msg)
                .setPositiveButton("确定", null)
                .show();
    }

    private boolean matchEmailPattern(String email){
        String regexEmail = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        return Pattern.compile(regexEmail).matcher(email).matches();
    }
}
