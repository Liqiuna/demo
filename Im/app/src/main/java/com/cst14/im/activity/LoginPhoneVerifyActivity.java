package com.cst14.im.activity;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cst14.im.R;
import com.cst14.im.listener.LoginPhoneVerifyListener;
import com.cst14.im.listener.SettingSecurityListener;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Utils;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class LoginPhoneVerifyActivity extends AppCompatActivity implements LoginPhoneVerifyListener.ILoginPhoneVerifyView,View.OnClickListener{
    private ActionBar actionBar;

    //号码输入框
    private EditText inputPhoneEt;
    // 验证码输入框
    private EditText inputCodeEt;
    // 获取验证码按钮
    private Button requestCodeBtn;
    // 登录按钮
    private Button login_btn;
    private String account_app;
    private String phone_bind;
    int i = 30;

    //登入为true，需要绑定为false
    private Boolean opt_result= false;

    public static final String KEY = "com.cst14.im";

    //短信验证
    String APPKEY = "172ee88c1b5d0";
    String APPSECRETE = "91203cb4d89acc85cf872e9791fd68e1";

    private LoginPhoneVerifyListener mLoginPhoneVerifyListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone_verify);
        initView();
    }

    private void initView(){
        inputCodeEt = (EditText)findViewById(R.id.login_input_code_et);
        inputPhoneEt = (EditText)findViewById(R.id.phone_login_et);
        requestCodeBtn = (Button)findViewById(R.id.login_request_code_btn);
        login_btn = (Button)findViewById(R.id.phone_bind_bn);
        inputPhoneEt.setEnabled(true);
        requestCodeBtn.setEnabled(true);
        login_btn.setEnabled(false);
        inputCodeEt.setEnabled(false);

        mLoginPhoneVerifyListener = new LoginPhoneVerifyListener(this);
        Tools.addPresenter(mLoginPhoneVerifyListener);
        account_app = ImApplication.User_id;
        //通过SharePreferences取出资料
//        SharedPreferences spref = getSharedPreferences(KEY, 0);
//        account_app = spref.getString("account",null);

        mLoginPhoneVerifyListener.initToolbar();

        requestCodeBtn.setOnClickListener(this);
        login_btn.setOnClickListener(this);
        getPhoneInfo();

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

    private void getPhoneInfo(){
        //get security info auto
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        if (account_app==null){
            return;
        }
        builder.setAccount(account_app).setMsgType(ProtoClass.MsgType.GET_SECURITY_INFO)
                .setToken(ImApplication.getLoginToken());
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Utils.showToast(LoginPhoneVerifyActivity.this,"更新信息失败");
            }

            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                mLoginPhoneVerifyListener.onProcess(responseMsg);
                return true;
            }
        });
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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_request_code_btn:
                String phoneNum = inputPhoneEt.getText().toString();
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
                login_btn.setEnabled(true);
                break;
            case R.id.phone_bind_bn:
                login_btn.setEnabled(false);
                //将收到的验证码和手机号提交再次核对
                String phoneNum1 = inputPhoneEt.getText().toString();
                SMSSDK.submitVerificationCode("86", phoneNum1, inputCodeEt.getText().toString());
                createProgressBar();
                break;
        }
    }

    @Override
    public void doResult(String phone,String result) {
        if (result.equals("SUCEESS")){
            Utils.showToast(LoginPhoneVerifyActivity.this,"成功绑定此手机号码"+phone_bind);
        }
        Intent intent = new Intent();
        intent.setClass(LoginPhoneVerifyActivity.this,SecurityChangePwdActivity.class);
        startActivity(intent);
        finish();

    }

    @Override
    public void doFailed(String errMsg) {
        Utils.showToast(LoginPhoneVerifyActivity.this,"失败");
    }

    @Override
    public void doGetPhoneResult(String phone) {
        if (!phone.equals("")){
            inputPhoneEt.setText(phone);
            inputPhoneEt.setEnabled(false);
            login_btn.setText("绑定");
            opt_result = true;
        }
        login_btn.setText("登入");
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
                        Toast.makeText(getApplicationContext(), "提交验证码成功，登陆中...",
                                Toast.LENGTH_SHORT).show();

                        if (opt_result){
                            //createProgressBar();
                            //提交成功，则将手机号码保存到数据库进行最终绑定
                            ProtoClass.Msg.Builder builderPB = ProtoClass.Msg.newBuilder();
                            builderPB.setAccount(account_app).setMsgType(ProtoClass.MsgType.BIND_PHONE)
                                    .setToken(ImApplication.getLoginToken())
                                    .setSecurityItem(ProtoClass.MsgSecurity.newBuilder()
                                            .setPhoneBind(ProtoClass.MsgPhone.newBuilder()
                                                    .setUserPhone(inputPhoneEt.getText().toString())));
                            Tools.startTcpRequest(builderPB, new Tools.TcpListener() {
                                @Override
                                public void onSendFail(Exception e) {
                                    Utils.showToast(LoginPhoneVerifyActivity.this, "手机号码绑定请求失败");
                                }

                                @Override
                                public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                                    mLoginPhoneVerifyListener.onProcess(responseMsg);
                                    return true;
                                }
                            });
                        }


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
    //弹出信息提示对话框
    private void popDialog(String msg){
        new AlertDialog.Builder(LoginPhoneVerifyActivity.this)
                .setTitle("提示")
                .setMessage(msg)
                .setPositiveButton("确定", null)
                .show();
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

}
