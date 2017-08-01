package com.cst14.im.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.listener.GetUserDetailListener;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Model;
import com.cst14.im.utils.UserDetailInfo;
import com.cst14.im.utils.Utils;

/**
 * Created by Belinda Y on 2016/9/24.
 */
public class SearchUserDetailsActivity extends Activity implements  GetUserDetailListener.GetUserDetailView{
    private Button btn_search;
    private EditText et_userMark;
    private TextView tv_tipMsg;
    private GetUserDetailListener getUserDetailListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);
        btn_search=(Button)findViewById(R.id.btn_search);
        et_userMark=(EditText) findViewById(R.id.et_account);
        tv_tipMsg=(TextView) findViewById(R.id.tv_tipMsg);

        getUserDetailListener=new GetUserDetailListener(this);
        Tools.addPresenter(getUserDetailListener);
        textChange tc = new textChange();
        //为编辑框增加监听器
        et_userMark.addTextChangedListener(tc);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HideKeyBoard();
                String userMark=et_userMark.getText().toString().trim();
                if(userMark.equals(ImApplication.User_id)||(userMark.equals(Model.getUsername()))){
                    tv_tipMsg.setVisibility(View.VISIBLE);
                    tv_tipMsg.setText("您可到主界面->我->我的资料 修改您自己的信息");
                    return;
                }
                ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                builder.setUserMark(userMark)
                        .setMsgType(ProtoClass.MsgType.GET_USER_DETAIL);
                Tools.startTcpRequest(builder, new Tools.TcpListener() {
                    @Override
                    public void onSendFail(Exception e) {
                        Utils.showToast2(SearchUserDetailsActivity.this, "请求发送失败");
                    }

                    //第二个参数是服务器返回的响应消息
                    @Override
                    public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                        getUserDetailListener.onProcess(responseMsg);
                        return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
                    }
                });

            }
        });
    }
    //EditText监听器  当编辑框内容不为空时按钮才可用
    class textChange implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            tv_tipMsg.setVisibility(View.INVISIBLE);
        }
        @Override
        public void onTextChanged(CharSequence cs, int start, int before,
                                  int count) {
            if(et_userMark.getText().length() > 0) {
                btn_search.setEnabled(true);
            } else {
                btn_search.setEnabled(false);
            }
        }
        @Override
        public void afterTextChanged(Editable s) {
        }

    }
    //隐藏键盘
    private void HideKeyBoard() {
        InputMethodManager inputMethodManager =(InputMethodManager) getApplicationContext().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(btn_search.getWindowToken(), 0);
    }


    @Override
    public void successUserDetail(UserDetailInfo userDetailInfor) {

        Intent intent=new Intent();
        Bundle bundle=new Bundle();
        bundle.putSerializable("userDetailInfo",userDetailInfor);
        intent.setClass(SearchUserDetailsActivity.this,ManageModifyUserInfoActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
    @Override
    public void failedGetUserDetail(String tipMsg,boolean isServerError) {
        if( isServerError) {
            Utils.showToast2(this,tipMsg);
        }else{
            tv_tipMsg.setVisibility(View.VISIBLE);
            tv_tipMsg.setText(tipMsg);
        }
    }
}
