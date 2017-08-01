package com.cst14.im.activity;

import android.net.wifi.WifiConfiguration;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.cst14.im.R;
import com.cst14.im.listener.SetRoleListener;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SetRoleActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText setRole_user;
    private Button setRole_search;
    private RelativeLayout setRole_result;
    private Spinner setRole_spinner;
    private Button setRole_yes;

    private int search_userID;
    private int search_roleID;
    private int modify_roleID;
    private SetRoleListener setRoleListener = new SetRoleListener(this);
    private int mRoleID = ImApplication.getMyRoleID();
    private ArrayAdapter adapter;
    private static final List<String> super_manager_list = new ArrayList<String>(){{add("管理员");add("客服人员");add("普通用户");}};
    private static final int normal_manager_role = 2;
    private static final int customer_service_role = 3;
    private static final int normal_user_role = 4;
    private static final int normal_manager_role_sequence = normal_manager_role - 2;
    private static final int customer_service_role_sequence = customer_service_role - 2;
    private static final int normal_user_role_sequence = normal_user_role - 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_role);
        iniView();
        Tools.addPresenter(setRoleListener);
    }

    private void iniView(){
        setRole_user= (EditText) findViewById(R.id.set_role_user);
        setRole_search= (Button) findViewById(R.id.set_role_search);
        setRole_search.setOnClickListener((View.OnClickListener)this);
        setRole_result= (RelativeLayout) findViewById(R.id.set_role_result);
        setRole_spinner= (Spinner) findViewById(R.id.set_role_spinner);
        setRole_yes= (Button) findViewById(R.id.set_role_yes);
        setRole_yes.setOnClickListener((View.OnClickListener)this);
    }

    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.set_role_search:
                String strID = setRole_user.getText().toString();
                if (strID.equals("")){
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SetRoleActivity.this, "ID不可为空，请重新输入", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                if (!isNumeric(strID)){
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SetRoleActivity.this, "您所输入的ID不合法，请重新输入", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                final int userID = Integer.parseInt(strID);
                searchPackToSend(userID);
                break;
            case R.id.set_role_yes:
                modifyPackToSend(search_userID, modify_roleID);
                break;
        }
    }

    private void searchPackToSend(int userID){
        ProtoClass.User user = ProtoClass.User.newBuilder().setUserID(userID).build();
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setUser(user)
                .setMsgType(ProtoClass.MsgType.SELECT_USER_ROLE);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Toast.makeText(SetRoleActivity.this,"发送失败",Toast.LENGTH_LONG);
            }
            //第二个参数是服务器返回的响应消息
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                setRoleListener.onProcess(responseMsg);
                return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
            }
        });
    }

    public void spinnerChange(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                search_userID = SetRoleListener.getUserID();
                search_roleID = SetRoleListener.getRoleID();
                if (search_roleID <= mRoleID){
                    SetRoleActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SetRoleActivity.this,"您无法搜索并修改管理员的角色信息",Toast.LENGTH_LONG).show();
                            setRole_result.setVisibility(View.GONE);
                        }
                    });
                    return;
                }
                adapter = new ArrayAdapter<String>(SetRoleActivity.this, android.R.layout.simple_spinner_item, super_manager_list);
                //为适配器设置下拉列表下拉时的菜单样式
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //将适配器添加到下拉列表上
                setRole_spinner.setAdapter(adapter);
                setRole_spinner.setSelection(search_roleID - 2, true);
                setRole_result.setVisibility(View.VISIBLE);
                setRole_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        parent.setVisibility(View.VISIBLE);
                        switch (position) {
                            case normal_manager_role_sequence:
                                modify_roleID = normal_manager_role;
                                setRole_spinner.setSelection(normal_manager_role_sequence, true);
                                break;
                            case customer_service_role_sequence:
                                modify_roleID = customer_service_role;
                                setRole_spinner.setSelection(customer_service_role_sequence, true);
                                break;
                            case normal_user_role_sequence:
                                modify_roleID = normal_user_role;
                                setRole_spinner.setSelection(normal_user_role_sequence, true);
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

    private void modifyPackToSend(int userID, int roleID){
        ProtoClass.User user = ProtoClass.User.newBuilder()
                .setUserID(userID)
                .setUserRoleID(roleID)
                .build();
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setUser(user)
                .setMsgType(ProtoClass.MsgType.MODIFY_ROLE);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Toast.makeText(SetRoleActivity.this,"发送失败",Toast.LENGTH_LONG);
            }
            //第二个参数是服务器返回的响应消息
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                setRoleListener.onProcess(responseMsg);
                return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
            }
        });
    }
}
