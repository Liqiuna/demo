package com.cst14.im.activity;

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
import com.cst14.im.listener.GroupLimitPrmsListener;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GroupLimitPrmsActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText groupLimit_userID;
    private Button groupLimit_checkUserID;
    private RelativeLayout groupLimit_checkResult;
    private Spinner groupLimit_spinner;
    private Button groupLimit_yes;

    private int search_userID;
    private int search_roleID;
    private int mRoleID = ImApplication.getMyRoleID();
    private int search_oldGroupLimit;
    private int search_newGroupLimit;
    private GroupLimitPrmsListener groupLimitPrmsListener = new GroupLimitPrmsListener(this);

    private ArrayAdapter adapter;
    private static final List<String> group_limit_list = new ArrayList<String>(){{add("1");add("2");add("3");add("4");add("5");add("6");add("7");add("8");add("9");add("10");}};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_limit_prms);
        iniView();
        Tools.addPresenter(groupLimitPrmsListener);
    }

    private void iniView(){
        groupLimit_userID = (EditText) findViewById(R.id.groupLimit_userID);
        groupLimit_checkUserID = (Button) findViewById(R.id.groupLimit_checkUserID);
        groupLimit_checkUserID.setOnClickListener((View.OnClickListener)this);
        groupLimit_checkResult = (RelativeLayout) findViewById(R.id.groupLimit_checkResult);
        groupLimit_spinner = (Spinner) findViewById(R.id.groupLimit_spinner);
        groupLimit_yes = (Button) findViewById(R.id.groupLimit_yes);
        groupLimit_yes.setOnClickListener((View.OnClickListener)this);
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.groupLimit_checkUserID:
                String strID = groupLimit_userID.getText().toString();
                if (strID.equals("")){
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GroupLimitPrmsActivity.this, "ID不可为空，请重新输入", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                if (!isNumeric(strID)){
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GroupLimitPrmsActivity.this, "您所输入的ID不合法，请重新输入", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                search_userID = Integer.parseInt(strID);

                ProtoClass.User.Builder userMsg_check = ProtoClass.User.newBuilder()
                        .setUserID(search_userID);
                ProtoClass.Msg.Builder builder_check = ProtoClass.Msg.newBuilder()
                        .setMsgType(ProtoClass.MsgType.CHECK_CREATE_GROUP_LIMIT)
                        .setUser(userMsg_check);
                sendMsg(builder_check);
                break;
            case R.id.groupLimit_yes:
                ProtoClass.User.Builder userMsg_set = ProtoClass.User.newBuilder()
                        .setUserID(search_userID)
                        .setCreGroupLimit(search_newGroupLimit);
                ProtoClass.Msg.Builder builder_set = ProtoClass.Msg.newBuilder()
                        .setMsgType(ProtoClass.MsgType.SET_CREATE_GROUP_LIMIT)
                        .setUser(userMsg_set);
                sendMsg(builder_set);
                break;
        }
    }

    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    public void sendMsg(ProtoClass.Msg.Builder builder){
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Toast.makeText(GroupLimitPrmsActivity.this,"发送失败",Toast.LENGTH_LONG);
            }
            //第二个参数是服务器返回的响应消息
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                groupLimitPrmsListener.onProcess(responseMsg);
                return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
            }
        });
    }

    public void spinnerShow(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                search_roleID = groupLimitPrmsListener.getRoleID();
                search_oldGroupLimit = groupLimitPrmsListener.getOldGroupLimit();
                if (search_roleID <= mRoleID){
                    GroupLimitPrmsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GroupLimitPrmsActivity.this,"您无法搜索并修改管理员的创建群上限",Toast.LENGTH_LONG).show();
                            groupLimit_checkResult.setVisibility(View.GONE);
                        }
                    });
                    return;
                }
                adapter = new ArrayAdapter<String>(GroupLimitPrmsActivity.this, android.R.layout.simple_spinner_item, group_limit_list);
                //为适配器设置下拉列表下拉时的菜单样式
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //将适配器添加到下拉列表上
                groupLimit_spinner.setAdapter(adapter);
                groupLimit_spinner.setSelection(search_oldGroupLimit - 1);
                groupLimit_checkResult.setVisibility(View.VISIBLE);
                groupLimit_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        parent.setVisibility(View.VISIBLE);
                        switch (position) {
                            case 0:
                                search_newGroupLimit = 1;
                                groupLimit_spinner.setSelection(0);
                                break;
                            case 1:
                                search_newGroupLimit = 2;
                                groupLimit_spinner.setSelection(1);
                                break;
                            case 2:
                                search_newGroupLimit = 3;
                                groupLimit_spinner.setSelection(2);
                                break;
                            case 3:
                                search_newGroupLimit = 4;
                                groupLimit_spinner.setSelection(3);
                                break;
                            case 4:
                                search_newGroupLimit = 5;
                                groupLimit_spinner.setSelection(4);
                                break;
                            case 5:
                                search_newGroupLimit = 6;
                                groupLimit_spinner.setSelection(5);
                                break;
                            case 6:
                                search_newGroupLimit = 7;
                                groupLimit_spinner.setSelection(6);
                                break;
                            case 7:
                                search_newGroupLimit = 8;
                                groupLimit_spinner.setSelection(7);
                                break;
                            case 8:
                                search_newGroupLimit = 9;
                                groupLimit_spinner.setSelection(8);
                                break;
                            case 9:
                                search_newGroupLimit = 10;
                                groupLimit_spinner.setSelection(9);
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
}
