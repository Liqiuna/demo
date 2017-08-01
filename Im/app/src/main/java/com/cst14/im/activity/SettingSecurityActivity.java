package com.cst14.im.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cst14.im.R;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.listener.SettingSecurityListener;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Utils;

import java.util.regex.Pattern;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class SettingSecurityActivity extends AppCompatActivity implements SettingSecurityListener.SettingSecurityView,View.OnClickListener {

    private LinearLayout email_ll;
    private LinearLayout question_ll;
    private LinearLayout phone_ll;
    private LinearLayout pwd_change_ll;
    private LinearLayout security_center_ll;
    private RelativeLayout mBindEmailLayout;
    private LinearLayout mExitEBindLayout;
    private LinearLayout mExitEChangeLayout;
    private LinearLayout mExitPBindLayout;
    private LinearLayout mExitPCancelLayout;
    private LinearLayout mExitQCancelLayout;
    private LinearLayout mExitQSetLayout;
    private ActionBar actionBar;

    private TextView email_status_tv;
    private TextView question_status_tv;
    private TextView phone_status_tv;
    //    MyApplicationUtil appUtil;
//    private EmailAutoCompleteTextView email_bind_et;
    private EditText email_bind_et;
    private Button email_bind_bn;
    private Button email_remove_bn;

    private EditText phone_bind_et;
    private Button phone_bind_bn;

    private Button phone_cancel_bn;

    private SettingSecurityListener mSettingSecurityListener;

    EditText question_set1_et;
    EditText answer_set1_et;
    EditText question_set2_et;
    EditText answer_set2_et;
    EditText question_set3_et;
    EditText answer_set3_et;

    private ProgressBar bindProgressBar;
    private ProgressBar removeProgressBar;
    private ProgressBar setQuestionProgressBar;
    private ProgressBar cancelQuestionProgressBar;

    private String account_app;
    private ImApplication app;

    //短信验证
    String APPKEY = "172ee88c1b5d0";
    String APPSECRETE = "91203cb4d89acc85cf872e9791fd68e1";

    // 验证码输入框
    private EditText inputCodeEt;
    // 获取验证码按钮
    private Button requestCodeBtn;

    int i = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_security);
        mSettingSecurityListener = new SettingSecurityListener(this);
        Tools.addPresenter(mSettingSecurityListener); //如果需要接收消息推送的话，就要添加监听
        account_app = app.User_id;
        initView();
    }

    private void initView(){
        email_ll = (LinearLayout)findViewById(R.id.email_ll);
        question_ll = (LinearLayout)findViewById(R.id.question_ll);
        phone_ll = (LinearLayout)findViewById(R.id.phone_ll);
        pwd_change_ll = (LinearLayout)findViewById(R.id.pwd_change_ll);
        security_center_ll = (LinearLayout)findViewById(R.id.security_center_ll);

        email_status_tv = (TextView)findViewById(R.id.email_status_tv);
        question_status_tv = (TextView)findViewById(R.id.question_status_tv);
        phone_status_tv = (TextView)findViewById(R.id.phone_status_tv);

        mBindEmailLayout = (RelativeLayout) findViewById(R.id.email_bind_layout);

        mExitQCancelLayout = (LinearLayout) findViewById(R.id.exit_qcancel_layout);
        mExitQSetLayout = (LinearLayout) findViewById(R.id.exit_qset_layout);

        //First make the progressbar invisible
        email_ll.setOnClickListener(this);
        question_ll.setOnClickListener(this);
        phone_ll.setOnClickListener(this);
        pwd_change_ll.setOnClickListener(this);
        security_center_ll.setOnClickListener(this);

        mSettingSecurityListener.initToolbar();
        getSecurityInfo();
    }
    private void getSecurityInfo(){
        //get security info auto
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setAccount(account_app).setMsgType(ProtoClass.MsgType.GET_SECURITY_INFO)
                .setToken(ImApplication.getLoginToken());
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Utils.showToast(SettingSecurityActivity.this,"更新信息失败");
            }

            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                mSettingSecurityListener.onProcess(responseMsg);
                return true;
            }
        });
    }
    @Override
    public void onClick(View v) {

        switch (v.getId()){
            //the case is settling email
            case R.id.email_ll:
//                mInputLayout.setVisibility(View.GONE);
                if (actionBar!=null){
                    actionBar.hide();
                }
                if (email_status_tv.getText().equals("未绑定")){
                    mBindEmailLayout.setVisibility(View.VISIBLE);
                    jumpToEmailBindLayout();
                }else {

                    jumpToEmailChangeLayout();
                }

                break;
            //the case is setting question
            case R.id.question_ll:
                if (question_status_tv.getText().equals("未设置")){
                    jumpToQuestionSetLayout();
                } else {
                    Intent intent = new Intent(this,ChangePwdActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.phone_ll:
                if (actionBar!=null){
                    actionBar.hide();
                }
                if (phone_status_tv.getText().equals("未绑定")){
                    jumpToPhoneBindLayout();
                }else {
                    jumpPhoneCancelLayout();
                }
                break;

            case R.id.pwd_change_ll:
                final AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(this);
                final LinearLayout changePwdDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.security_origin_pwd_verify,null);
                alertDialogBuilder.setView(changePwdDialog);
                alertDialogBuilder.setCancelable(true);
                final AlertDialog dialog = alertDialogBuilder.create();
                dialog.show();
                changePwdDialog.findViewById(R.id.verify_pwd_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText inputPwd = (EditText)changePwdDialog.findViewById(R.id.verify_pwd_tv);
                        if (inputPwd.getText().toString().equals("")){
                            popDialog("密码不能为空，请输入密码");
                            return;
                        }
                        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                        builder.setAccount(account_app).setMsgType(ProtoClass.MsgType.CHANGE_PWD_BY_ORIGIN_PWD)
                                .setToken(ImApplication.getLoginToken())
                                .setSecurityItem(ProtoClass.MsgSecurity.newBuilder()
                                        .setPwd(inputPwd.getText().toString()));

                        Tools.startTcpRequest(builder, new Tools.TcpListener() {
                            @Override
                            public void onSendFail(Exception e) {
                                popDialog("密码错误，请重新输入");
                            }

                            @Override
                            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                                mSettingSecurityListener.onProcess(responseMsg);
                                return true;
                            }
                        });
                        dialog.dismiss();
                    }
                });

                changePwdDialog.findViewById(R.id.cancel_verify_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                break;
            case R.id.security_center_ll:
                Intent intent = new Intent();
                intent.setClass(SettingSecurityActivity.this,SecurityCenterActivity.class);
                intent.putExtra("emailBind",email_status_tv.getText().toString());
                startActivity(intent);
                break;

            case R.id.email_bind_bn:

                email_bind_et.setEnabled(false);
                email_bind_bn.setEnabled(false);
                if (!matchEmailPattern(email_bind_et.getText().toString())){
                    Utils.showToast(SettingSecurityActivity.this, "邮箱格式有误");
                    email_bind_et.setEnabled(true);
                    email_bind_bn.setEnabled(true);
                }else {
                    ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                    builder.setAccount(account_app).setMsgType(ProtoClass.MsgType.BIND_EMAIL)
                            .setToken(ImApplication.getLoginToken())
                            .setSecurityItem(ProtoClass.MsgSecurity.newBuilder()
                                    .setEmailBind(ProtoClass.MsgEmail.newBuilder()
                                            .setUserEmail(email_bind_et.getText().toString())));
                    Tools.startTcpRequest(builder, new Tools.TcpListener() {
                        @Override
                        public void onSendFail(Exception e) {
                            Utils.showToast(SettingSecurityActivity.this, "邮箱绑定请求失败");
                        }

                        @Override
                        public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                            mSettingSecurityListener.onProcess(responseMsg);
                            return true;
                        }
                    });

                }
                break;
            case R.id.email_remove_bn:
                ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                builder.setAccount(account_app).setMsgType(ProtoClass.MsgType.REMOVE_EMAIL)
                        .setToken(ImApplication.getLoginToken());
                Tools.startTcpRequest(builder, new Tools.TcpListener() {
                    @Override
                    public void onSendFail(Exception e) {
                        Utils.showToast(SettingSecurityActivity.this,"解除邮箱绑定失败");
                    }

                    @Override
                    public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                        mSettingSecurityListener.onProcess(responseMsg);
                        return true;
                    }
                });
                break;

            case R.id.question_set_bt:
                String quetion1 = question_set1_et.getText().toString().trim();
                String quetion2 = question_set2_et.getText().toString().trim();
                String quetion3 = question_set3_et.getText().toString().trim();
                String answer1 = answer_set1_et.getText().toString().trim();
                String answer2 = answer_set2_et.getText().toString().trim();
                String answer3 = answer_set3_et.getText().toString().trim();
                String answer = answer1 + ";" + answer2 + ";" + answer3;
                if (quetion1.equals("") ||quetion2.equals("") ||answer1.equals("") ||answer2.equals("")) {
                    new android.support.v7.app.AlertDialog.Builder(SettingSecurityActivity.this)
                            .setTitle("提示")
                            .setMessage("至少输入两个密保问题及其答案")
                            .setPositiveButton("确定", null)
                            .show();
                    break;
                } else if (quetion1.equals(quetion2)) {
                    new android.support.v7.app.AlertDialog.Builder(SettingSecurityActivity.this)
                            .setTitle("提示")
                            .setMessage("密保问题不能重复")
                            .setPositiveButton("确定", null)
                            .show();
                    break;
                }
                ProtoClass.MsgQuestion.Builder quetionBuilder = ProtoClass.MsgQuestion.newBuilder();
                quetionBuilder.setPswQuetion1(quetion1).setPswQuetion2(quetion2).setPswQuetion3(quetion3).setPswAnswer(answer);

                ProtoClass.MsgSecurity.Builder securityBuilder = ProtoClass.MsgSecurity.newBuilder();
                securityBuilder.setQuestionSet(quetionBuilder);

                ProtoClass.Msg.Builder msgBuilder = ProtoClass.Msg.newBuilder();
                msgBuilder.setAccount(account_app)
                        .setSecurityItem(securityBuilder)
                        .setMsgType(ProtoClass.MsgType.SET_PWD_QUETION);
                Tools.startTcpRequest(msgBuilder, new Tools.TcpListener() {
                    @Override
                    public void onSendFail(Exception e) {
                        Utils.showToast2(SettingSecurityActivity.this, "请求发送失败");
                    }

                    //第二个参数是服务器返回的响应消息
                    @Override
                    public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                        mSettingSecurityListener.onProcess(responseMsg);
                        return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
                    }
                });
                break;
            //短信验证码

            case R.id.login_request_code_btn:
                String phoneNum = phone_bind_et.getText().toString();
                // 1. 通过规则判断手机号
                if (!judgePhoneNums(phoneNum)) {
                    return;
                } // 2. 通过sdk发送短信验证
                SMSSDK.getVerificationCode("86", phoneNum);

                // 3. 把按钮变成不可点击，并且显示倒计时（正在获取）
                requestCodeBtn.setClickable(false);
                requestCodeBtn.setText("重新发送(" + i + ")");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (; i > 0; i--) {
                            handler.sendEmptyMessage(-9);
                            if (i <= 0) {
                                break;
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        handler.sendEmptyMessage(-8);
                    }
                }).start();

                inputCodeEt.setEnabled(true);
                phone_bind_bn.setEnabled(true);
                break;

            case R.id.phone_bind_bn:
                //将收到的验证码和手机号提交再次核对
                String phoneNum1 = phone_bind_et.getText().toString();
                SMSSDK.submitVerificationCode("86", phoneNum1, inputCodeEt.getText().toString());
                createProgressBar();
                break;
            case R.id.phone_cancel_bn:
                ProtoClass.Msg.Builder builderPC = ProtoClass.Msg.newBuilder();
                builderPC.setAccount(account_app).setMsgType(ProtoClass.MsgType.CANCEL_PHONE)
                        .setToken(ImApplication.getLoginToken());
                Tools.startTcpRequest(builderPC, new Tools.TcpListener() {
                    @Override
                    public void onSendFail(Exception e) {
                        Utils.showToast(SettingSecurityActivity.this,"解除手机号码绑定失败");
                    }

                    @Override
                    public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                        mSettingSecurityListener.onProcess(responseMsg);
                        return true;
                    }
                });
                break;

            case R.id.question_cancel_bt:
                break;
            case R.id.exit_ebind_layout:
                mBindEmailLayout.setVisibility(View.GONE);
                if (actionBar != null) {
                    actionBar.show();
                }
                refresh();
//                getSecurityInfo();
                break;
            case R.id.exit_echange_layout:
                refresh();
                break;
            case R.id.exit_pbind_layout:
                refresh();
                break;
            case R.id.exit_pcancel_layout:
                refresh();
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
    public void doResult(String email, String question_status, String phone, String answer) {
        if (!email.equals("")){
            email_status_tv.setText(email);
        }else {
            email_status_tv.setText("未绑定");
        }
        if (!phone.equals("")){
            phone_status_tv.setText(phone);
        }else {
            phone_status_tv.setText("未绑定");
        }
        question_status_tv.setText(question_status);
    }

    @Override
    public void doEmailBindSuccess(String email_bind) {
//        Utils.showToast(SettingSecurityActivity.this, "已成功发送信息到"+email_bind+"邮箱，请尽快登录此邮箱进行确认绑定！");
        popDialog("已成功发送信息到"+email_bind+"邮箱，请尽快登录此邮箱进行确认绑定！");
    }

    @Override
    public void doEmailBindFailed(String errMsg) {
        Utils.showToast(SettingSecurityActivity.this,"绑定失败");
    }

    @Override
    public void doEmailRemoveResult(String email_bind, String result) {
        Utils.showToast(SettingSecurityActivity.this,"解除绑定此邮箱"+email_bind+" :"+result);
        if (result.equals("SUCCESS")){
//            mBindEmailLayout.setVisibility(View.VISIBLE);
//            jumpToEmailBindLayout();
//            getSecurityInfo();
            refresh();
        }
    }

    @Override
    public void doQuestionSetResult() {
        Utils.showToast(SettingSecurityActivity.this, "成功设置密保");
        Intent intent = new Intent(this,SettingSecurityActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void doQuestionCancelResult(String question, String answer, String result) {
        Utils.showToast(SettingSecurityActivity.this,"绑定失败");
    }

    //短信方面
    @Override
    public void doPhoneBindSuccess(String phone_bind) {
        Utils.showToast(SettingSecurityActivity.this,"成功绑定此手机号码"+phone_bind);
        refresh();
    }

    @Override
    public void doPhoneBindFailed(String errMsg) {

    }

    @Override
    public void doPhoneCancelResult(String phone_bind, String result) {
        Utils.showToast(SettingSecurityActivity.this,"解除绑定此手机号码"+phone_bind+" :"+result);
        if (result.equals("SUCCESS")){
//            mBindEmailLayout.setVisibility(View.VISIBLE);
//            jumpToEmailBindLayout();
//            getSecurityInfo();
            refresh();
        }
    }

    //找回密码
    @Override
    public void doChangePwdByOriginPwdResult(String result) {
        Intent intent = new Intent();
        intent.setClass(SettingSecurityActivity.this,SecurityChangePwdActivity.class);
        startActivity(intent);
    }

    @Override
    public void doLoginByPhoneResult(String phone) {

    }

    @Override
    public void doChangePwdResult(String result) {

    }

    @Override
    public void doFailed(String errMsg) {
        if (errMsg.equals("WRONGPWD")){
            popDialog("密码错误,请重新输入");
        }
    }



    //Email
    private void jumpToEmailBindLayout(){
//        setContentView(R.layout.security_layout_email_bind);
        email_bind_bn = (Button)findViewById(R.id.email_bind_bn);
//        email_bind_et = (EmailAutoCompleteTextView)findViewById(R.id.email_bind_et);
        email_bind_et = (EditText)findViewById(R.id.email_bind_et);
        mExitEBindLayout = (LinearLayout) findViewById(R.id.exit_ebind_layout);
        mExitEBindLayout.setOnClickListener(this);
        email_bind_bn.setOnClickListener(this);

    }
    private void jumpToEmailChangeLayout(){
        setContentView(R.layout.security_layout_email_change);
        email_remove_bn = (Button)findViewById(R.id.email_remove_bn);
        TextView email_remove_tv = (TextView)findViewById(R.id.email_remove_tv);
        email_remove_tv.setText(email_status_tv.getText());

        mExitEChangeLayout = (LinearLayout) findViewById(R.id.exit_echange_layout);
        mExitEChangeLayout.setOnClickListener(this);
        //First remove the email bound and change to layout_email_bind layout auto at the same time renew the activity_setting
        email_remove_bn.setOnClickListener(this);
    }

    private void jumpToSettingLayout(){
        refresh();
    }

    //Question
    private void jumpToQuestionSetLayout(){
        setContentView(R.layout.security_layout_question_set);
        Button question_set_bn = (Button)findViewById(R.id.question_set_bt);
        question_set1_et = (EditText) findViewById(R.id.question_set1_et);
        answer_set1_et = (EditText)findViewById(R.id.answer_set1_et);
        question_set2_et = (EditText) findViewById(R.id.question_set2_et);
        answer_set2_et = (EditText)findViewById(R.id.answer_set2_et);
        question_set3_et = (EditText) findViewById(R.id.question_set3_et);
        answer_set3_et = (EditText)findViewById(R.id.answer_set3_et);

        question_set_bn.setOnClickListener(this);
    }

    private void jumpQuestionChangeLayout(){
        setContentView(R.layout.security_layout_question_cancel);
        Button question_cancel_bn = (Button)findViewById(R.id.question_cancel_bt);
        //TextView question_cancel_tv = (TextView) findViewById(R.id.question_cancel_tv);
        //TextView answer_cancel_tv = (TextView)findViewById(R.id.answer_cancel_tv);

        question_cancel_bn.setOnClickListener(this);
    }
    //Phone
    private void jumpToPhoneBindLayout(){
        setContentView(R.layout.security_layout_phone_bind);
        inputCodeEt = (EditText) findViewById(R.id.login_input_code_et);
        requestCodeBtn = (Button) findViewById(R.id.login_request_code_btn);
        phone_bind_bn = (Button)findViewById(R.id.phone_bind_bn);
        phone_bind_et = (EditText)findViewById(R.id.phone_bind_et);
        phone_bind_bn.setEnabled(false);

        mExitPBindLayout = (LinearLayout) findViewById(R.id.exit_pbind_layout);
        requestCodeBtn.setOnClickListener(this);
        mExitPBindLayout.setOnClickListener(this);
        phone_bind_bn.setOnClickListener(this);

        // 启动短信验证sdk
        SMSSDK.initSDK(this, APPKEY, APPSECRETE);
        EventHandler eventHandler = new EventHandler(){
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
            }
        };
        //注册回调监听接口
        SMSSDK.registerEventHandler(eventHandler);
    }



    private void jumpPhoneCancelLayout(){
        setContentView(R.layout.security_layout_phone_cancel);
        TextView phone_bind_tv = (TextView)findViewById(R.id.phone_cancel_tv);
        mExitPCancelLayout = (LinearLayout)findViewById(R.id.exit_pcancel_layout);
        phone_cancel_bn = (Button)findViewById(R.id.phone_cancel_bn);
        phone_bind_tv.setText(phone_status_tv.getText());
        mExitPCancelLayout.setOnClickListener(this);
        phone_cancel_bn.setOnClickListener(this);
    }

    //refresh activity
    private void refresh(){
        Intent intent = new Intent(this,SettingSecurityActivity.class);
        //clear all activity and open new LoginActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void jumpToMainActivity(){
        Intent intent = new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    //match the form of the email
    private boolean matchEmailPattern(String email){
        String regexEmail = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        return Pattern.compile(regexEmail).matcher(email).matches();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void hideKeyboard() {
        //隐藏键盘
        InputMethodManager manager = ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
        View view = SettingSecurityActivity.this.getCurrentFocus();
        if (view == null){
            return;
        }
        manager.hideSoftInputFromWindow(view.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     *对短信绑定的handle处理
     */
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == -9) {
                requestCodeBtn.setText("重新发送(" + i + ")");
            } else if (msg.what == -8) {
                requestCodeBtn.setText("获取验证码");
                requestCodeBtn.setClickable(true);
                i = 30;
            } else {
                int event = msg.arg1;
                int result = msg.arg2;
                Object data = msg.obj;
                Log.e("event", "event=" + event);
                if (result == SMSSDK.RESULT_COMPLETE) {
                    // 短信注册成功后，返回Activity,然后提示
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {// 提交验证码成功
                        Toast.makeText(getApplicationContext(), "提交验证码成功",
                                Toast.LENGTH_SHORT).show();
                        //createProgressBar();

                        //提交成功，则将手机号码保存到数据库进行最终绑定
                        ProtoClass.Msg.Builder builderPB = ProtoClass.Msg.newBuilder();
                        builderPB.setAccount(account_app).setMsgType(ProtoClass.MsgType.BIND_PHONE)
                                .setToken(ImApplication.getLoginToken())
                                .setSecurityItem(ProtoClass.MsgSecurity.newBuilder()
                                        .setPhoneBind(ProtoClass.MsgPhone.newBuilder()
                                                .setUserPhone(phone_bind_et.getText().toString())));
                        Tools.startTcpRequest(builderPB, new Tools.TcpListener() {
                            @Override
                            public void onSendFail(Exception e) {
                                Utils.showToast(SettingSecurityActivity.this, "手机号码绑定请求失败");
                            }

                            @Override
                            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                                mSettingSecurityListener.onProcess(responseMsg);
                                return true;
                            }
                        });

                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        Toast.makeText(getApplicationContext(), "正在获取验证码",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        ((Throwable) data).printStackTrace();
                    }
                }
            }
        }
    };


    /**
     * 判断手机号码是否合理
     *
     * @param phoneNums
     */
    private boolean judgePhoneNums(String phoneNums) {
        if (isMatchLength(phoneNums, 11)
                && isMobileNO(phoneNums)) {
            return true;
        }
        Toast.makeText(this, "手机号码输入有误！",Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * 判断一个字符串的位数
     * @param str
     * @param length
     * @return
     */
    public static boolean isMatchLength(String str, int length) {
        if (str.isEmpty()) {
            return false;
        } else {
            return str.length() == length ? true : false;
        }
    }

    /**
     * 验证手机格式
     */
    public static boolean isMobileNO(String mobileNums) {
        /*
         * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
         * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
         * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
         */
        String telRegex = "[1][358]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobileNums))
            return false;
        else
            return mobileNums.matches(telRegex);
    }

    /**
     * progressbar
     */
    private void createProgressBar() {
        FrameLayout layout = (FrameLayout) findViewById(android.R.id.content);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        ProgressBar mProBar = new ProgressBar(this);
        mProBar.setLayoutParams(layoutParams);
        mProBar.setVisibility(View.VISIBLE);
        layout.addView(mProBar);
    }

    //弹出信息提示对话框
    private void popDialog(String msg){
        new AlertDialog.Builder(SettingSecurityActivity.this)
                .setTitle("提示")
                .setMessage(msg)
                .setPositiveButton("确定", null)
                .show();
    }
}
