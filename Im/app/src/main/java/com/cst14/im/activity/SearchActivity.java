package com.cst14.im.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cst14.im.R;
import com.cst14.im.baseClass.ISearchPresenter;
import com.cst14.im.listener.SearchPresenterImpl;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.Utils;

/**
 * Created by hz on 2016/8/24.
 */

public class SearchActivity extends AppCompatActivity implements SearchPresenterImpl.ISearchView,
        View.OnClickListener{
    private ActionBar actionBar;
    private EditText mSearchEdit;
    private LinearLayout mSearchAroud;
    private LinearLayout mSearchWithAttrb;
    private LinearLayout mSearchGroup;

    private RelativeLayout mInputLayout;
    private EditText mInputEdit;
    private Button mCommitInputBtn;
    private LinearLayout mExitInputLayout;

    private ISearchPresenter mSearchPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);
        mSearchEdit = (EditText) findViewById(R.id.search_edit_view);
        mSearchAroud = (LinearLayout) findViewById(R.id.search_nearly_layout);
        mSearchWithAttrb = (LinearLayout) findViewById(R.id.search_attrb_layout);
        mSearchGroup = (LinearLayout) findViewById(R.id.search_group_layout);
        mInputLayout = (RelativeLayout) findViewById(R.id.search_input_layout);
        mInputEdit = (EditText) findViewById(R.id.search_name);
        mCommitInputBtn = (Button) findViewById(R.id.commit_name_search);
        mExitInputLayout = (LinearLayout) findViewById(R.id.exit_input_layout);

        mSearchPresenter = new SearchPresenterImpl(this);
        Tools.addPresenter(mSearchPresenter);
        mSearchPresenter.initToolbar();
    }

    @Override
    public void onClick(View v) {
        mSearchPresenter.onViewClick(v);
    }

    @Override
    public void initToolbar(String title) {
        actionBar = getSupportActionBar();
        if (actionBar == null) return;

        actionBar.setTitle(title);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDefaultDisplayHomeAsUpEnabled(true);

        mSearchEdit.setOnClickListener(this);
        mSearchAroud.setOnClickListener(this);
        mSearchWithAttrb.setOnClickListener(this);
        mSearchGroup.setOnClickListener(this);
        mCommitInputBtn.setOnClickListener(this);
        mExitInputLayout.setOnClickListener(this);

        mSearchEdit.clearFocus();
    }
    @Override
    public void hideKeyboard() {
        //隐藏键盘
        InputMethodManager manager = ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
        View view = SearchActivity.this.getCurrentFocus();
        if (view == null){
            return;
        }
        manager.hideSoftInputFromWindow(view.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void showInputLayout() {
        mInputLayout.setVisibility(View.VISIBLE);
        mSearchEdit.clearFocus();
        mExitInputLayout.requestFocus();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    public void hideInputLayout() {
        mInputLayout.setVisibility(View.GONE);
        if (actionBar != null) {
            actionBar.show();
        }
    }

    @Override
    public void setInputEditHint(String hint) {
        mInputEdit.setHint(hint);
    }

    @Override
    public String getInputEditText() {
        return mInputEdit.getText().toString();
    }

    @Override
    public void showCustomToast(String tip) {
        Toast.makeText(this,tip,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setDigitalEdit() {
        mInputEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        mInputEdit.setText("");
    }

    @Override
    public void setTextInputEdit() {
        mInputEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        mInputEdit.setText("");
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
