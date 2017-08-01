package com.cst14.im.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.baseClass.ISearchConditionPresenter;
import com.cst14.im.listener.SearchConditionPresentImpl;
import com.cst14.im.tools.Tools;

/**
 * Created by hz on 2016/8/26.
 */

public class SearchConditionActivity extends AppCompatActivity implements
        SearchConditionPresentImpl.ISearchConditionView,View.OnClickListener {

    private LinearLayout mWindowLayout;
    private EditText mAttrEdit;

    private View mOnlineView;
    private View mSexView;
    private View mAgeView;
    private View mAdderView;

    private TextView mOnlineText;
    private TextView mSexText;
    private TextView mAgeText;
    private TextView mAddrText;

    private Button mSearchBtn;

    private ISearchConditionPresenter mPresenter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_condition);
        mWindowLayout = (LinearLayout) findViewById(R.id.window_layout);
        mAttrEdit = (EditText) findViewById(R.id.search_edit_attr);
        mOnlineView = findViewById(R.id.search_online_layout);
        mSexView = findViewById(R.id.search_sex_layout);
        mAgeView = findViewById(R.id.search_age_layout);
        mAdderView = findViewById(R.id.search_addr_layout);
        mOnlineText = (TextView) findViewById(R.id.online_text);
        mSexText = (TextView) findViewById(R.id.sex_text);
        mAgeText = (TextView) findViewById(R.id.age_text);
        mAddrText = (TextView) findViewById(R.id.address_text);
        mSearchBtn = (Button) findViewById(R.id.search_btn);

        mPresenter = new SearchConditionPresentImpl(this);
        Tools.addPresenter(mPresenter);

        mPresenter.initView();
        mPresenter.initToolbar();
        mPresenter.initEvent();


        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        mPresenter.setWindowSize(outMetrics.widthPixels,outMetrics.heightPixels);
    }

    @Override
    public void initToolbar(String title) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDefaultDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void initEvent() {
        mOnlineView.setOnClickListener(this);
        mSexView.setOnClickListener(this);
        mAgeView.setOnClickListener(this);
        mAdderView.setOnClickListener(this);
        mSearchBtn.setOnClickListener(this);
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
    public LinearLayout getWindowLayout() {
        return mWindowLayout;
    }

    @Override
    public void onClick(View v) {
        mPresenter.onClick(v);
    }

    @Override
    public String getSearchWord() {
        return mAttrEdit.getText().toString();
    }

    @Override
    public void sexSearchWord(String word) {
        mAttrEdit.setText(word);
    }

    @Override
    public String getOnlineText() {
        return mOnlineText.getText().toString();
    }

    @Override
    public void setOnlineText(String word) {
        mOnlineText.setText(word);
    }

    @Override
    public String getSexText() {
        return mSexText.getText().toString();
    }

    @Override
    public void setSexText(String word) {
        mSexText.setText(word);
    }

    @Override
    public String getAgeText() {
        return mAgeText.getText().toString();
    }

    @Override
    public void setAgeText(String word) {
        mAgeText.setText(word);
    }

    @Override
    public String getAddressText() {
        return mAddrText.getText().toString();
    }

    @Override
    public void setAddressText(String word) {
        mAddrText.setText(word);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
