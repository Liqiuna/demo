package com.cst14.im.activity;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.listener.ChangePwdOriginListener;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Utils;


public class SecurityChangePwdActivity extends AppCompatActivity implements ChangePwdOriginListener.IChangePwdOriginView,View.OnClickListener{

    private ActionBar actionBar;
    private EditText pwdChangeEt;
    private EditText pwdChangeConfirmEt;
    private Button pwdChangeFinishBtn;

    private ChangePwdOriginListener mChangePwdOriginPresenter;
    private String account_app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_change_pwd);

        initView();

    }

    private void initView(){
        TextView account_tv = (TextView)findViewById(R.id.account_scp_tv);
        pwdChangeEt = (EditText)findViewById(R.id.pwd_change_et);
        pwdChangeConfirmEt = (EditText)findViewById(R.id.pwd_change_confirm_et);
        pwdChangeFinishBtn = (Button)findViewById(R.id.change_pwd_finish_btn);

        mChangePwdOriginPresenter = new ChangePwdOriginListener(this);
        Tools.addPresenter(mChangePwdOriginPresenter);
        account_app = ImApplication.User_id;
        mChangePwdOriginPresenter.initToolbar();

        if (!account_app.equals("")){
            account_tv.setText(account_app);
        }
        pwdChangeFinishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = pwdChangeEt.getText().toString();
                String pwd_confirm = pwdChangeConfirmEt.getText().toString();
                if ((pwd.length()>0)&&pwd.equals(pwd_confirm)){
                    ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                    builder.setAccount(account_app).setMsgType(ProtoClass.MsgType.CHANGE_PASSWORD)
                            .setToken(ImApplication.getLoginToken())
                    .setSecurityItem(ProtoClass.MsgSecurity.newBuilder().setPwd(pwd));
                    Tools.startTcpRequest(builder, new Tools.TcpListener() {
                        @Override
                        public void onSendFail(Exception e) {
                            popDialog("重新设置密码失败");
                        }

                        @Override
                        public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                            mChangePwdOriginPresenter.onProcess(responseMsg);
                            return true;
                        }
                    });
                }else if (!pwdChangeEt.equals(pwdChangeConfirmEt)){
                    popDialog("两次填写的密码不一致");
                }

            }
        });

    }


    @Override
    public void doChangePwdResult(String result) {
        Utils.showToast(SecurityChangePwdActivity.this,"密码设置成功");
        finish();
    }

    @Override
    public void doFailed(String errMsg) {
        popDialog("密码修改失败");
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
    //弹出信息提示对话框
    private void popDialog(String msg){
        new AlertDialog.Builder(SecurityChangePwdActivity.this)
                .setTitle("提示")
                .setMessage(msg)
                .setPositiveButton("确定", null)
                .show();
    }
}
