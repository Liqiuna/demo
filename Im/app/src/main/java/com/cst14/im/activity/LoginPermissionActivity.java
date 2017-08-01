package com.cst14.im.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.cst14.im.R;
import com.cst14.im.listener.LoginPermissionListener;
import com.cst14.im.listener.SetRoleListener;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class LoginPermissionActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText loginPrms_userID;
    private Button loginPrms_search;
    private RelativeLayout loginPrms_result;
    private Spinner loginPrms_spinner;
    private Button loginPrms_yes;

    private int search_userID;
    private int search_roleID;
    private boolean search_ifLogin;
    private int mRoleID = ImApplication.getMyRoleID();
    private boolean modify_ifLogin;
    private LoginPermissionListener loginPermissionListener = new LoginPermissionListener(this);

    private ArrayAdapter adapter;
    private static final List<String> manager_list = new ArrayList<String>(){{add("允许登录");add("禁止登录");}};
    private static final int allowLogin = 0;
    private static final int banLogin = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_permission);
        iniView();
        Tools.addPresenter(loginPermissionListener);
    }

    private void iniView(){
        loginPrms_userID = (EditText) findViewById(R.id.loginPrms_userID);
        loginPrms_search = (Button) findViewById(R.id.loginPrms_search);
        loginPrms_search.setOnClickListener((View.OnClickListener)this);
        loginPrms_result = (RelativeLayout) findViewById(R.id.loginPrms_result);
        loginPrms_spinner = (Spinner) findViewById(R.id.loginPrms_spinner);
        loginPrms_yes = (Button) findViewById(R.id.loginPrms_yes);
        loginPrms_yes.setOnClickListener((View.OnClickListener)this);
    }

    public void onClick(View view){
        switch(view.getId()){
            case R.id.loginPrms_search:
                String strID = loginPrms_userID.getText().toString();
                if (strID.equals("")){
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginPermissionActivity.this, "ID不可为空，请重新输入", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                if (!isNumeric(strID)){
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginPermissionActivity.this, "您所输入的ID不合法，请重新输入", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                final int userID = Integer.parseInt(strID);
                searchPackToSend(userID);
                break;
            case R.id.loginPrms_yes:
                modifyPackToSend(search_userID, modify_ifLogin);
                break;
        }
    }

    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    private void searchPackToSend(int userID){
        ProtoClass.User user = ProtoClass.User.newBuilder().setUserID(userID).build();
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setUser(user)
                .setMsgType(ProtoClass.MsgType.CHECK_IF_LOGIN);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Toast.makeText(LoginPermissionActivity.this,"发送失败",Toast.LENGTH_LONG);
            }
            //第二个参数是服务器返回的响应消息
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                loginPermissionListener.onProcess(responseMsg);
                return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
            }
        });
    }

    private void modifyPackToSend(int userID, boolean ifLogin){
        ProtoClass.User user = ProtoClass.User.newBuilder()
                .setUserID(userID)
                .build();
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setUser(user)
                .setIfLogin(ifLogin)
                .setMsgType(ProtoClass.MsgType.SET_LOGIN_TYPE);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Toast.makeText(LoginPermissionActivity.this,"发送失败",Toast.LENGTH_LONG);
            }
            //第二个参数是服务器返回的响应消息
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                loginPermissionListener.onProcess(responseMsg);
                return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
            }
        });
    }

    public void spinnerChange(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                search_userID = LoginPermissionListener.getUserID();
                search_roleID = LoginPermissionListener.getRoleID();
                search_ifLogin = LoginPermissionListener.getIfLogin();
                System.out.println("search_roleID: " + search_roleID);
                System.out.println("mRoleID: " + mRoleID);
                if (search_roleID <= mRoleID){
                    LoginPermissionActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginPermissionActivity.this,"您无法搜索并修改该用户的登录类型",Toast.LENGTH_LONG).show();
                            loginPrms_result.setVisibility(View.GONE);
                        }
                    });

                    return;
                }
                adapter = new ArrayAdapter<String>(LoginPermissionActivity.this, android.R.layout.simple_spinner_item, manager_list);
                //为适配器设置下拉列表下拉时的菜单样式
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //将适配器添加到下拉列表上
                loginPrms_spinner.setAdapter(adapter);
                if (search_ifLogin){
                    loginPrms_spinner.setSelection(allowLogin);
                }else if (!search_ifLogin){
                    loginPrms_spinner.setSelection(banLogin);
                }
                loginPrms_result.setVisibility(View.VISIBLE);
                loginPrms_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        parent.setVisibility(View.VISIBLE);
                        switch (position) {
                            case allowLogin:
                                modify_ifLogin = true;
                                loginPrms_spinner.setSelection(allowLogin);
                                break;
                            case banLogin:
                                modify_ifLogin = false;
                                loginPrms_spinner.setSelection(banLogin);
                                break;
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        parent.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    public void resultGone(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loginPrms_result.setVisibility(View.GONE);
            }
        });
    }
}
