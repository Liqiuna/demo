package com.cst14.im.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.cst14.im.R;
import com.cst14.im.adapter.GroupListAdapter;
import com.cst14.im.baseClass.iGroupSearchView;
import com.cst14.im.bean.UserGroup;
import com.cst14.im.listener.GroupSearchPresenter;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class GroupSearchActivity extends AppCompatActivity implements iGroupSearchView, View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "GroupSearchActivity";

    private GroupSearchPresenter presenter = new GroupSearchPresenter(this);
    private List<UserGroup> searchResults = new ArrayList<>();
    private GroupListAdapter groupListAdapter;

    private Button btSearch;
    private EditText etSearchKey;
    private ListView lvSearchResult;
    private Toolbar toolbar;

    private boolean isNeedToUpdateGroupList = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_group_search);

        //init view
        btSearch = (Button) findViewById(R.id.bt_activity_search_group_doSearch);
        etSearchKey = (EditText) findViewById(R.id.et_activity_search_group_key);
        lvSearchResult = (ListView) findViewById(R.id.lv_activity_search_group_searchResult);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("查找群");

        btSearch.setOnClickListener(this);
        groupListAdapter = new GroupListAdapter(this, R.layout.item_group_list, this.searchResults);
        groupListAdapter.setViewTypeForGroupSearchResult();
        lvSearchResult.setAdapter(groupListAdapter);
        lvSearchResult.setOnItemClickListener(this);

        Utils.showToast2(getApplicationContext(), "输入h试试");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Tools.removePresenter(presenter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == GroupActivity.CODE_RESULT_UPDATE_LIST) {
            Intent intent = new Intent();
            setResult(GroupActivity.CODE_RESULT_UPDATE_LIST, intent);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_activity_search_group_doSearch) {
            String searchKey = etSearchKey.getText().toString().trim();
            if (TextUtils.isEmpty(searchKey)) {
                showEmptyInput();
                return;
            }
            if (!searchKey.matches("[^%&',;*=?$\\x22]+")) {
                showInvalidInput();
                return;
            }
            presenter.searchGroup(searchKey);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("usergroup", searchResults.get(position));
        bundle.putInt("origin", GroupInfoActivity.From_GROUP_SEARCH_ACTIVITY);

        Intent intent = new Intent();
        intent.setClass(this, GroupInfoActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, 10);
    }


    //implements iGroupSearchView

    @Override
    public void showSearchResult(List<ProtoClass.GroupInfo> groupInfos) {
        searchResults.clear();
        for (ProtoClass.GroupInfo groupInfo : groupInfos) {
            searchResults.add(new UserGroup(groupInfo));
        }
        groupListAdapter.notifyDataSetChanged();
    }

    @Override
    public void showNoReasult() {
        Utils.showToast2(getApplicationContext(), "结果为空");
    }

    @Override
    public void showEmptyInput() {
        Utils.showToast2(getApplicationContext(), "输入不能为空");

    }

    @Override
    public void showInvalidInput() {
        Utils.showToast2(getApplicationContext(), "不能输入^%&',;*=?$");
    }
}
