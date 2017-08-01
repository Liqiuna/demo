package com.cst14.im.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.baseClass.ISearchResultPresenter;
import com.cst14.im.listener.SearchResultPresenterImpl;
import com.cst14.im.tools.Tools;

/**
 * Created by hz on 2016/8/24.
 */

public class SearchResultActivity extends AppCompatActivity implements
        SearchResultPresenterImpl.ISearchResultView {

    private RelativeLayout mBaseLayout;
    private ListView mResultListView;
    private ProgressBar mWaitingBar;
    private TextView mTipText;

    private ISearchResultPresenter mPresenter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        mBaseLayout = (RelativeLayout) findViewById(R.id.search_result_base);
        mResultListView = (ListView) findViewById(R.id.search_user_list);
        mWaitingBar = (ProgressBar) findViewById(R.id.search_progressBar);
        mTipText = (TextView) findViewById(R.id.no_result_tip);

        mPresenter = new SearchResultPresenterImpl(this);
        Tools.addPresenter(mPresenter);

        mPresenter.initToolbar();
        mPresenter.initViews();
        mPresenter.sendMsg();
    }

    @Override
    public void initToolbar(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;

        actionBar.setTitle(title);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDefaultDisplayHomeAsUpEnabled(true);

        //单击监听
        mResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.clickUserListItem(position);
            }
        });

        //滑动监听
        mResultListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            //记录上一次的数量
            private int preLastItem = 0;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastItem = firstVisibleItem + visibleItemCount;
                if(lastItem == 0 || lastItem == preLastItem){
                    return;
                }
                //scroll to the listview last item
                if (lastItem == totalItemCount){
                    preLastItem = lastItem;
                    mPresenter.scrollToLastItem();
                }
            }
        });
    }

    @Override
    public RelativeLayout getWindowLayout() {
        return mBaseLayout;
    }

    @Override
    public int getWindowWidth() {
        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    @Override
    public void setTipString(String tip) {
        mTipText.setText(tip);
    }

    @Override
    public void setTipClickable(boolean clickable) {
        mTipText.setClickable(clickable);
    }

    @Override
    public void setTipOnClickListener(View.OnClickListener listener) {
        mTipText.setOnClickListener(listener);
    }

    @Override
    public int getWindowHeight() {
        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    @Override
    public Intent getSearchIntent() {
        return getIntent();
    }

    @Override
    public boolean getProcessBarState() {
        int state = mWaitingBar.getVisibility();
        return state == View.VISIBLE;
    }

    @Override
    public void hideProcessBar() {
        mWaitingBar.setVisibility(View.GONE);
    }

    @Override
    public void showProcessBar() {
        mWaitingBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void showTipText() {
        mTipText.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideTipText() {
        mTipText.setVisibility(View.GONE);
    }

    @Override
    public void setAdapter(BaseAdapter adapter) {
        mResultListView.setAdapter(adapter);
    }

    @Override
    public void hideListView() {
        mResultListView.setVisibility(View.GONE);
    }

    @Override
    public void showListView() {
        mResultListView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setListViewSelection(int pos) {
        mResultListView.setSelection(pos);
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
}
