package com.cst14.im.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cst14.im.R;
import com.cst14.im.adapter.GroupListAdapter;
import com.cst14.im.baseClass.iGroupView;
import com.cst14.im.bean.User;
import com.cst14.im.bean.UserGroup;
import com.cst14.im.listener.GroupPresenter;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, iGroupView {
    private static final String TAG = "GroupActivity";

    private Toolbar toolbar;
    private ActionBar myActionBar;
    private ActionBar actionBar;
    private MenuItem itemCreate;
    private MenuItem itemInfo;
    private MenuItem itemJoin;

    private ListView groupList;
    private List<UserGroup> userGroups = new ArrayList<>();
    private GroupListAdapter adapter;

    private GroupPresenter presenter = new GroupPresenter(this);

    public final static int CODE_RESULT_UPDATE_LIST = 10;

    public final static int CODE_REQUEST_CREATE = 100;
    public final static int CODE_REQUEST_INFO = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_group);

        initToolbar();

        groupList = (ListView) findViewById(R.id.lv_group_groupList);
        adapter = new GroupListAdapter(this, R.layout.item_group_list, this.userGroups);
        adapter.setViewTypeForGroupList();
        groupList.setAdapter(adapter);
        groupList.setOnItemClickListener(this);
        groupList.setOnCreateContextMenuListener(onCreateContextMenuListener);
        this.updataGroupList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Tools.removePresenter(presenter);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("我的群");
        toolbar.inflateMenu(R.menu.base_toolbar_menu);
        setSupportActionBar(toolbar);
        myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.base_toolbar_menu, menu);
        //创建右上角菜单
        itemCreate = menu.findItem(R.id.action_create_group);
        itemInfo = menu.findItem(R.id.action_group_info);
        itemJoin = menu.findItem(R.id.action_join_group);

        itemCreate.setVisible(true);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int menuItemID = item.getItemId();
        //处理菜单项的点击事件
        switch (menuItemID) {
            case R.id.action_create_group:
                skipToActivity(GroupCreateActivity.class, CODE_REQUEST_CREATE, null);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private static final int CONTECT_MENU_GROUP_INFO = 1;
    //上下文菜单
    private View.OnCreateContextMenuListener onCreateContextMenuListener = new View.OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(0, CONTECT_MENU_GROUP_INFO, 0, "查看群资料");
        }
    };

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int itemID = item.getItemId();
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (itemID) {
            case CONTECT_MENU_GROUP_INFO:
                Bundle bundle = new Bundle();
                bundle.putSerializable("usergroup", userGroups.get(menuInfo.position));
                bundle.putInt("origin", GroupInfoActivity.From_GROUP_ACTIVITY);
                skipToActivity(GroupInfoActivity.class, CODE_REQUEST_INFO, bundle);
                break;
        }

        return super.onContextItemSelected(item);
    }

    /***
     * 封装了Activity 的跳转
     *
     * @param aimClass    目标Activity 类名
     * @param requestCode 请求码，若为0则直接跳转
     * @param bundle      附加传递的Bundle
     */
    public void skipToActivity(Class aimClass, int requestCode, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(this, aimClass);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        if (requestCode == 0) {
            startActivity(intent);
        } else {
            startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, GroupChatActivity.class);
        intent.putExtra("groupID", userGroups.get(position).getID());
        intent.putExtra("groupName",userGroups.get(position).getName());
        startActivity(intent);
        finish();
        //获取选中的群资料： this.userGroups.get(position).getID();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //当其他Ativity执行的操作会响应群列表时更新，如 加群成功
        if (resultCode == CODE_RESULT_UPDATE_LIST) {
            ImApplication.instance.getCurUser().setLastGroupListUpdateTime(0);
            updataGroupList();
        }
    }

    public void updataGroupList() {

        if (isMoreThanOneDay()) {
            new Thread() {
                @Override
                public void run() {
                    //调用presenter获取用户加入的所有群
                    presenter.getAllGroupsUserJoined();
                }
            }.start();
        } else {
            this.userGroups.clear();
            this.userGroups.addAll(ImApplication.instance.getCurUser().getGroups());
            adapter.notifyDataSetChanged();
        }

    }

    //implements iGroupView:
    @Override
    public void showGetGroupListFail() {
        Utils.showToast2(getApplicationContext(), "获取群列表失败");
    }

    @Override
    public void showGetGroupListSucceed(List<ProtoClass.GroupInfo> groupInfos) {

        this.userGroups.clear();
        for (ProtoClass.GroupInfo groupInfo : groupInfos) {
            UserGroup userGroup = new UserGroup(groupInfo);
            //TODO 获取最后一条消息显示在群名下

            this.userGroups.add(userGroup);
        }
        adapter.notifyDataSetChanged();

        //save group data to current user
        ImApplication.instance.getCurUser().setGroups(userGroups);
        ImApplication.instance.getCurUser().setLastGroupListUpdateTime(System.currentTimeMillis());
    }

    private static boolean isMoreThanOneDay() {
        long nowTime = System.currentTimeMillis();
        long diff = (nowTime - ImApplication.instance.getCurUser().getLastGroupListUpdateTime()) / (1000 * 60);
        return diff > 24 * 60;
    }
}
