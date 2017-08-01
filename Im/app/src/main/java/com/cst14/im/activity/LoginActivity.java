package com.cst14.im.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cst14.im.Fragment.FriendFragment;
import com.cst14.im.Fragment.FriendInfo;
import com.cst14.im.R;
import com.cst14.im.bean.User;
import com.cst14.im.db.dao.FriendsDao;

import com.cst14.im.listener.ApplyRegisterListener;


import com.cst14.im.listener.Friend_Listener;

import com.cst14.im.listener.GetAttrTypeListener;
import com.cst14.im.listener.IMEItoRegisterPresenter;
import com.cst14.im.listener.LoginLisener;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Model;
import com.cst14.im.utils.Utils;
import com.cst14.im.utils.pictureUtils.httprequestPresenter;
import com.cst14.im.utils.sessionUtils.ChatMsgBean;

import java.util.LinkedList;
import java.util.Map;

import static com.cst14.im.db.dao.FriendsDao.KeepfriendsToDB;
import static com.cst14.im.db.dao.FriendsDao.loadFriendRequest;
import static com.cst14.im.db.dao.FriendsDao.loadFriendsFromDB;
import static com.cst14.im.db.dao.FriendsDao.loadGroupsFromDB;

/**
 * Created by MRLWJ on 2016/8/22.
 *socket初始化在myApplication中
 */
public class LoginActivity extends Activity implements  LoginLisener.LoginView,IMEItoRegisterPresenter.Registerview{
    private LoginLisener mLoginLisener;
    private IMEItoRegisterPresenter mIMEItoRegisterPresenter;//用于注册

    private ApplyRegisterListener mapplyRegisterListener;
    private GetAttrTypeListener   mgetAttrTypeListener;
    public ImApplication app;
    private TextView tv_tip;
    private ImageView  iv_head;
    public FriendsDao friendsDao;

    private  String account=null,pwd=null;
    private  Button btn_Regist;

    private TextView loginQuestionOpt_tv;
    private TextView loginMoreOpt_tv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        friendsDao=new FriendsDao(getBaseContext());
        mLoginLisener = new LoginLisener(this);
        mIMEItoRegisterPresenter=new IMEItoRegisterPresenter(this);

        mapplyRegisterListener=new ApplyRegisterListener(this);
        mgetAttrTypeListener=new GetAttrTypeListener();
        Tools.addPresenter(mLoginLisener); //如果需要接收消息推送的话，就要添加监听
        Tools.addPresenter( mIMEItoRegisterPresenter);
        app= (ImApplication) getApplication();

        initView();

    }

    //将account传至LoginPhoneVerifyActivity
    private void transAccountToPhoneVfAc(){
        account = mEdAccount.getText().toString().trim();
        app.User_id = account;

    }

    @Override
    protected void onResume() {
        super.onResume();
        new RegisterOnUI().start();
        autoLogin();
    }

    private class RegisterOnUI extends Thread {

        @Override
        public void run() {
            Looper.prepare();
            permitToRegister();

            Looper.loop();
        }
    }
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                switch ( Model.getRGST()) {
                    case Model.autoRgst:
                        btn_Regist.setVisibility(View.GONE);
                        Register();
                        break;
                    case Model.banRgst:
                        btn_Regist.setVisibility(View.GONE);
                        break;
                    case Model.fastRgst:
                        btn_Regist.setText("一键注册");
                        btn_Regist.setBackgroundResource(R.drawable.green);
                        btn_Regist.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Register();
                            }
                        });
                        break;
                }
            }
        }
    };
    public void permitToRegister(){
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setAccount("")
                .setMsgType(ProtoClass.MsgType.CHECK_RGST_TYPE);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Utils.showToast2(LoginActivity.this, "请求发送失败");
            }

            //第二个参数是服务器返回的响应消息
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                mapplyRegisterListener.onProcess(responseMsg);
                Message msg = new Message();
                msg.what =1;
                handler.sendMessage(msg);
                return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
            }
        });

    }


    private EditText mEdAccount,mEdPwd;
    private CheckBox rem_pw, auto_login;
    private Button Login;
    private void initView() {
        mEdAccount = (EditText) findViewById(R.id.ed_login_account);
        mEdPwd = (EditText) findViewById(R.id.ed_login_pwd);
        tv_tip = (TextView) findViewById(R.id.tip);
        iv_head=(ImageView)findViewById(R.id.iv_head);
        rem_pw = (CheckBox) findViewById(R.id.cb_mima);
        auto_login = (CheckBox) findViewById(R.id.cb_auto);
        btn_Regist=(Button)findViewById(R.id.btn_Regist);
        Login=(Button) findViewById(R.id.login);
        //监听记住密码多选框按钮事件
        rem_pw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (rem_pw.isChecked()) {

                    ImApplication.getSp().edit().putBoolean("ISCHECK", true).commit();

                }else {


                    ImApplication.getSp().edit().putBoolean("ISCHECK", false).commit();

                }

            }
        });
        //监听自动登录多选框事件
        auto_login.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (auto_login.isChecked()) {

                    ImApplication.getSp().edit().putBoolean("AUTO_ISCHECK", true).commit();
                } else {

                    ImApplication.getSp().edit().putBoolean("AUTO_ISCHECK", false).commit();
                }
            }
        });

        //登录遇到问题与其他方式（邮箱，手机号码等）登录
        loginMoreOpt_tv = (TextView)findViewById(R.id.login_more_opt_tv);
        loginQuestionOpt_tv = (TextView)findViewById(R.id.login_question_opt_tv);

        //登录遇到问题
        loginQuestionOpt_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("登录遇到问题")
                        .setMessage("你可以通过手机号+短信验证码登录微信")
                        .setPositiveButton("用短信验证码登录", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                transAccountToPhoneVfAc();
                                Intent intent = new Intent();
                                intent.setClass(LoginActivity.this,LoginPhoneVerifyActivity.class);
                                startActivity(intent);
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
        //登录之更多
        loginMoreOpt_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
                LinearLayout changePwdDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_security_more_opt_login,null);
                alertDialogBuilder.setView(changePwdDialog);
                alertDialogBuilder.setCancelable(true);
                final AlertDialog dialog=alertDialogBuilder.create();
                dialog.show();
                changePwdDialog.findViewById(R.id.change_account_ll).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(LoginActivity.this)
                                .setTitle("提示")
                                .setMessage("暂无")
                                .setPositiveButton("确定", null)
                                .show();
//                        Intent intent = new Intent();
//                        intent.setClass(LoginActivity.this,SecurityLoginByPhoneActivity.class);
//                        startActivity(intent);
                        dialog.dismiss();
                    }
                });
                changePwdDialog.findViewById(R.id.security_center_login_ll).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass(LoginActivity.this,SecurityCenterActivity.class);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });

            }
        });
    }
    public void autoLogin(){
        //获得实例对象
        //sp = this.getSharedPreferences("userInfo", Context.MODE_WORLD_READABLE);
        //判断记住密码多选框的状态
        if( ImApplication.getSp().getBoolean("ISCHECK", false))
        {
            //设置默认是记录密码状态
            rem_pw.setChecked(true);
            mEdAccount.setText(ImApplication.getSp().getString("currentUserName", ""));
            mEdPwd.setText(ImApplication.getSp().getString("currentUserPwd", ""));
            //判断自动登陆多选框状态
            if(ImApplication.getSp().getBoolean("AUTO_ISCHECK", false))
            {
                //设置默认是自动登录状态
                auto_login.setChecked(true);
                //跳转界面
                Login.performClick();

            }
        }
    }
    public void Register(){
        String IMEI=((TelephonyManager) getSystemService(TELEPHONY_SERVICE))
                .getDeviceId();

        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setAccount("")
                .setIMEI(IMEI)
                .setMsgType(ProtoClass.MsgType.REGISTER);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Utils.showToast2(LoginActivity.this, "请求发送失败");
            }
            //第二个参数是服务器返回的响应消息
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                mIMEItoRegisterPresenter.onProcess(responseMsg);
                return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
            }
        });


    }
    public void login(View v){
        account = mEdAccount.getText().toString().trim();
        pwd = mEdPwd.getText().toString().trim();
        app.User_id = account ;
        ImApplication.instance.setCurUser(new User());
        ImApplication.instance.getCurUser().setName(ImApplication.User_id);
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setAccount(account)
                .setPwd(pwd)
                .setMsgType(ProtoClass.MsgType.LOGIN);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Toast.makeText(getBaseContext(), "send failed", Toast.LENGTH_SHORT);
            }

            //第二个参数是服务器返回的响应消息
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                mLoginLisener.onProcess(responseMsg);
                return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
            }
        });
    }
    public void remPw(){
        if(rem_pw.isChecked())
        {
            //记住用户名、密码、
            SharedPreferences.Editor editor = ImApplication.getSp().edit();
            editor.putString("currentUserName", account);
            editor.putString("currentUserPwd",pwd);
            editor.commit();
        }
    }

    @Override
    public void sunccessLogin() {
        remPw();
        Log.e("登录成功", "----------");
        ImApplication.isFirstLoginInThisPhone=loadGroupsFromDB();//加载成功后进入页面  再申请最新版本的好友信息
        if(!ImApplication.isFirstLoginInThisPhone){   //失败表明该用户从未登陆过   没有历史 好友记录
            getFriendList();//先获取好友列表
            Log.e("获取好友列表成功", "----------");
            return;
        }
        loadFriendsFromDB();
        loadFriendRequest();
        getAttrType();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }
    public void getAttrType(){
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();

        builder.setAccount(ImApplication.User_id)
                .setToken(ImApplication.getLoginToken())
                .setMsgType(ProtoClass.MsgType.GET_ATTR_TYPE);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Toast.makeText(getBaseContext(), "send failed", Toast.LENGTH_SHORT);
            }

            //第二个参数是服务器返回的响应消息
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {

                mgetAttrTypeListener.onProcess(responseMsg);
                return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
            }
        });
    }


    //获取好友列表
    public  void getFriendList(){
        int id = Integer.parseInt(ImApplication.User_id);
        ProtoClass.User user = ProtoClass.User.newBuilder().setUserID(id).build();
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder().setMsgType(ProtoClass.MsgType.GET_FRIEND);
        builder.setUser(user).build();
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendSuccess(String msgId) {//发送成功
            }

            @Override
            public void onSendFail(Exception e) {//发送失败

            }

            //第二个参数是服务器返回的响应消息
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                mLoginLisener.onProcess(responseMsg);
                return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
            }
        });
    }
    public void sunccessGetFriends(){
        Log.e("获取新好友列表成功11111", "----------");
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        KeepfriendsToDB();
        finish();
    }
    @Override
    public void failedLogin(String errMsg) {
        Utils.showToast2(LoginActivity.this,errMsg);
    }

    @Override
    public void setSpannableString(String tip) {
        SpannableString sps = new SpannableString(tip);
        sps.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), 0, tip.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_tip.setText(sps);
    }

    @Override
    public void setUsername(String username) {
        mEdAccount.setText(username);
    }

    @Override
    public void setPwb(String password) {
        mEdPwd.setText(password);
    }


}
