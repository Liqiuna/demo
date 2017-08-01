package com.cst14.im.activity;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.cst14.im.R;
import com.cst14.im.baseClass.iGroupCreateView;
import com.cst14.im.listener.GroupCreatePresenter;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.Utils;

public class GroupCreateActivity extends AppCompatActivity implements View.OnClickListener, iGroupCreateView {

    private EditText etGroupName;
    private EditText etGroupInfo;
    private Button btCommit;
    private Toolbar toolbar;
    private GroupCreatePresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_group_create);

        //init view
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("创建群");
        setSupportActionBar(toolbar);
        etGroupInfo = (EditText) findViewById(R.id.et_activity_create_groupInfo);
        etGroupName = (EditText) findViewById(R.id.et_activity_create_groupName);
        btCommit = (Button) findViewById(R.id.bt_activity_create_commit);
        btCommit.setOnClickListener(this);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        presenter = new GroupCreatePresenter(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Tools.removePresenter(presenter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_activity_create_commit:
                //check input
                String groupName = etGroupName.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    showGroupNameInputError();
                    break;
                }
                String groupInfo = etGroupInfo.getText().toString();
                if (TextUtils.isEmpty(groupInfo)) {
                    showGroupInfoInputError();
                    break;
                }
                presenter.createGroup(groupName, groupInfo);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showGroupNameInputError() {
        Utils.showToast2(getApplicationContext(), "群名不能为空");
    }

    public void showGroupInfoInputError() {
        Utils.showToast2(getApplicationContext(), "群简介不能为空");
    }

    public void onCreateSucceed() {
        Utils.showToast2(getApplicationContext(), "创建群成功");
        Intent intent = new Intent();
        setResult(GroupActivity.CODE_RESULT_UPDATE_LIST, intent);
        finish();
    }

    public void onCreateFail() {
        Utils.showToast2(getApplicationContext(), "创建群失败");

    }

}
