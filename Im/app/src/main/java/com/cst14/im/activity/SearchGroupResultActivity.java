package com.cst14.im.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cst14.im.R;
import com.cst14.im.baseClass.ISearchGroupResultPresenter;
import com.cst14.im.listener.SearchGroupResultPresenterImpl;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Utils;
import com.cst14.im.views.CircleImageView;

/**
 * Created by hz on 2016/8/31.
 */

public class SearchGroupResultActivity extends AppCompatActivity implements SearchGroupResultPresenterImpl.ISearchGroupResultView{
    private ScrollView mGroupInfoLayout;


    private CircleImageView mGroupIcon;
    private TextView mGroupNick;
    private TextView mGroupNO;
    private TextView mGroupCreateTime;
    private TextView mGroupType;
    private TextView mGroupIntro;

    private Button mJoinGroupBtn;

    private ProgressBar mWaitingBar;
    private TextView mSearchResultTip;

    private ISearchGroupResultPresenter mPresenter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_result);
        mGroupInfoLayout = (ScrollView) findViewById(R.id.group_detail_layout);
        mGroupIcon = (CircleImageView) findViewById(R.id.group_icon);
        mGroupNick = (TextView) findViewById(R.id.group_nick);
        mGroupNO = (TextView) findViewById(R.id.group_no);
        mGroupCreateTime = (TextView) findViewById(R.id.group_create_time);
        mGroupType = (TextView) findViewById(R.id.group_type);
        mGroupIntro = (TextView) findViewById(R.id.group_intro);
        mJoinGroupBtn = (Button) findViewById(R.id.group_join_btn);
        mWaitingBar = (ProgressBar) findViewById(R.id.group_searching_bar);
        mSearchResultTip = (TextView) findViewById(R.id.group_no_search);

        mPresenter = new SearchGroupResultPresenterImpl(this);
        Tools.addPresenter(mPresenter);
        mPresenter.init();
        mPresenter.sendMsg();

    }
    @Override
    public void initToolbar(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setTitle(title);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDefaultDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void initViews() {
        mJoinGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onClick(v);
            }
        });
    }

    @Override
    public Intent getSearchIntent() {
        return getIntent();
    }

    @Override
    public void showWaitingBar() {
        mWaitingBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideWaitingBar() {
        mWaitingBar.setVisibility(View.GONE);
    }

    @Override
    public void showGroupInfo() {
        mGroupInfoLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideGroupInfo() {
        mGroupInfoLayout.setVisibility(View.GONE);
    }

    @Override
    public void showSearchTip() {
        mSearchResultTip.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideSearchTip() {
        mSearchResultTip.setVisibility(View.GONE);
    }

    @Override
    public void setGroupIcon(Bitmap bitmap) {
        mGroupIcon.setImageBitmap(bitmap);
    }

    @Override
    public void setGroupNick(String nick) {
        mGroupNick.setText(nick);
    }

    @Override
    public void setGroupNO(String no) {
        mGroupNO.setText(no);
    }

    @Override
    public void setGroupType(String type) {
        mGroupType.setText(type);
    }

    @Override
    public void setGroupCreateTime(String time) {
        mGroupCreateTime.setText(time);
    }

    @Override
    public void setGroupIntro(String intro) {
        mGroupIntro.setText(intro);
    }

    @Override
    public void showJoinGroupSucceed() {
//        Utils.showToast2(getApplicationContext(), "加入群成功");
        Toast.makeText(this,"加入群成功",Toast.LENGTH_SHORT).show();
        ImApplication.instance.getCurUser().setLastGroupListUpdateTime(0);
        mJoinGroupBtn.setVisibility(View.GONE);
    }

    @Override
    public void showJoinGroupFail() {
        Toast.makeText(this,"加入群失败",Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean getProcessBarState() {
        return mWaitingBar.getVisibility() == View.VISIBLE;
    }

    @Override
    public void showJoinBtn() {
        mJoinGroupBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideJoinBtn() {
        mJoinGroupBtn.setVisibility(View.GONE);
    }

    @Override
    public void setTipText(String tip) {
        mSearchResultTip.setText(tip);
    }

    @Override
    public void setTipClickable(boolean clickable) {
        mSearchResultTip.setClickable(clickable);
    }

    @Override
    public void setTipOnClickListener(View.OnClickListener listener) {
        mSearchResultTip.setOnClickListener(listener);
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
    protected void onDestroy() {
        super.onDestroy();
    }
}
