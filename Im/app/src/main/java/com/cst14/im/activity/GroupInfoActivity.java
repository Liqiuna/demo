package com.cst14.im.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cst14.im.Fragment.FriendInfo;
import com.cst14.im.R;
import com.cst14.im.baseClass.iGroupInfoView;
import com.cst14.im.bean.UserGroup;
import com.cst14.im.listener.GroupInfoPresenter;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class GroupInfoActivity extends AppCompatActivity implements iGroupInfoView, Toolbar.OnMenuItemClickListener, AdapterView.OnItemClickListener, View.OnClickListener {
    private static final String TAG = "GroupInfoActivity";

    private Toolbar toolbar;
    private TextView tvGroupID;
    private TextView tvGroupName;
    private TextView tvGroupInfo;
    private TextView tvMemberNum;
    private TextView tvNameCard;
    private ListView lvGroupMembers;
    private ImageButton imgbtnAnnounce;
    private ImageButton imgbtnGroupFile;
    private RelativeLayout rLayoutMembetTip;

    private RelativeLayout rLayoutGroupName;
    private RelativeLayout rLayoutGroupIntro;
    private RelativeLayout rLayoutNameCard;

    private MenuItem itemJoin;
    private MenuItem itemExit;
    private MenuItem itemDel;
    private MenuItem itemInviteMember;
    private MenuItem itemTransfer;

    private ArrayAdapter<String> adapter;
    List<String> memberNames = new ArrayList<>();

    GroupInfoPresenter presenter = new GroupInfoPresenter(this);

    //可GroupActivity显示个人加入的群资料，也可显示GroupSearchActivity搜索结果的资料
    //故分类型显示公开/隐藏的群资料，如群成员/名片/公告等
    public static final int From_GROUP_ACTIVITY = 1;
    public static final int From_GROUP_SEARCH_ACTIVITY = 2;
    private int ActivityType = From_GROUP_SEARCH_ACTIVITY;

    public static final int REQUEST_CODE_EDIT_GROUP_NAME = 100;
    public static final int REQUEST_CODE_EDIT_GROUP_INTRO = 110;
    public static final int REQUEST_CODE_EDIT_GROUP_NAMECARD = 120;

    private UserGroup userGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_group_info);

        //init view
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvGroupID = (TextView) findViewById(R.id.tv_activity_info_group_id);
        tvGroupInfo = (TextView) findViewById(R.id.tv_activity_info_groupInfo);
        tvGroupName = (TextView) findViewById(R.id.tv_activity_info_group_name);
        tvMemberNum = (TextView) findViewById(R.id.tv_activity_info_members_number);
        tvNameCard = (TextView) findViewById(R.id.tv_activity_group_info_namecard);
        imgbtnAnnounce=(ImageButton)findViewById(R.id.imgbtn_group_info_announce);
        imgbtnGroupFile=(ImageButton)findViewById(R.id.imgbtn_group_info_file);
        lvGroupMembers = (ListView) findViewById(R.id.lv_activity_info_members);
        rLayoutMembetTip = (RelativeLayout) findViewById(R.id.layout_group_info_member_tips);
        rLayoutGroupName = (RelativeLayout) findViewById(R.id.rl_activity_group_info_group_name);
        rLayoutGroupIntro = (RelativeLayout) findViewById(R.id.rl_activity_group_info_group_intro);
        rLayoutNameCard = (RelativeLayout) findViewById(R.id.rl_activity_group_info_namecard);

        rLayoutNameCard.setOnClickListener(this);
        imgbtnAnnounce.setOnClickListener(new imgbtnOnClickListener());
        imgbtnGroupFile.setOnClickListener(new imgbtnOnClickListener());

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, memberNames);
        lvGroupMembers.setAdapter(adapter);

        //get data from intent
        Intent intent = getIntent();
        userGroup = (UserGroup) intent.getSerializableExtra("usergroup");
        ActivityType = intent.getExtras().getInt("origin");

        toolbar.setTitle(userGroup.getName());
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(this);
        showGroupInfo(userGroup);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (ActivityType == From_GROUP_ACTIVITY) {
                    presenter.getGroupPerInfo(userGroup);
                } else {
                    presenter.getGroupPublicInfo(userGroup);
                }
            }
        }).start();
    }

    private class imgbtnOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view){
            switch (view.getId()){
                case R.id.imgbtn_group_info_announce:
                    Intent intent = new Intent(GroupInfoActivity.this, GroupAnnounceActivity.class);
                    intent.putExtra("groupID", userGroup.getID());
                    startActivity(intent);
                    break;
                case R.id.imgbtn_group_info_file:
                    intent = new Intent(GroupInfoActivity.this, GroupFileActivity.class);
                    intent.putExtra("groupID", userGroup.getID());
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Tools.removePresenter(presenter);
    }

    private String tempGroupName = "";
    private String tempGroupIntro = "";
    private String tempNameCard = "";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == EditInfoActivity.RESULT_CODE_EDITINFO) {
            String result = data.getStringExtra("result");
            Log.e(TAG, "onActivityResult: " + data);
            if (result == null) return;
            switch (requestCode) {
                case REQUEST_CODE_EDIT_GROUP_INTRO:
                    tempGroupIntro = result;
                    tvGroupInfo.setText(tempGroupIntro);
                    presenter.modifyGroupIntro(userGroup, tempGroupIntro);
                    break;

                case REQUEST_CODE_EDIT_GROUP_NAME:
                    tempGroupName = result;
                    tvGroupName.setText(tempGroupName);
                    presenter.modifyGroupName(userGroup, tempGroupName);
                    break;

                case REQUEST_CODE_EDIT_GROUP_NAMECARD:
                    tempNameCard = result;
                    tvNameCard.setText(tempNameCard);
                    presenter.modifyGroupNameCard(userGroup, tempNameCard);
                    break;
            }
        }
    }

    //获取群资料后各种setText
    public void showGroupInfo(UserGroup argGroup) {
        userGroup = argGroup;

        tvGroupID.setText(userGroup.getID() + "");
        tvGroupName.setText(userGroup.getName());
        tvGroupInfo.setText(userGroup.getIntro());
        tvNameCard.setText(userGroup.getMyInfo().getNameCard());

        if (ActivityType != From_GROUP_ACTIVITY) {
            //隐藏布局
            lvGroupMembers.setVisibility(View.GONE);
            rLayoutMembetTip.setVisibility(View.GONE);
            rLayoutNameCard.setVisibility(View.GONE);
            return;
        }

        lvGroupMembers.setOnItemClickListener(this);
        registerForContextMenu(lvGroupMembers);

        tvMemberNum.setText(userGroup.getMembers().size() + "");
        memberNames.clear();
        for (UserGroup.Member member : userGroup.getMembers()) {
            if (UserGroup.isOwner(member)) {
                memberNames.add("[群主]" + member.getUserName());
            } else if (UserGroup.isAdmin(member)) {
                memberNames.add("[管理员]" + member.getUserName());
            } else {
                memberNames.add(member.getUserName());
            }
        }
        adapter.notifyDataSetChanged();
    }

    private static final int CONTEXT_MENU_ITEM_DEL_MEMBER = 1;
    private static final int CONTEXT_MENU_ITEM_SET_ADMIN = 2;
    private static final int CONTEXT_MENU_ITEM_REMOVE_ADMIN = 3;

    //长按出现上下文菜单
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo contextMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
        UserGroup.Member curMember = userGroup.getMembers().get(contextMenuInfo.position);

        if (UserGroup.isOwner(curMember)) {
            return;
        }
        if (userGroup.isMeOwner) {
            if (UserGroup.isAdmin(curMember)) {
                menu.add(0, CONTEXT_MENU_ITEM_REMOVE_ADMIN, 0, "移除管理员");
            } else {
                menu.add(0, CONTEXT_MENU_ITEM_SET_ADMIN, 0, "设置管理员");
            }
            menu.add(0, CONTEXT_MENU_ITEM_DEL_MEMBER, 0, "移除成员");
        } else if (userGroup.isMeAdmin) {
            menu.add(0, CONTEXT_MENU_ITEM_DEL_MEMBER, 0, "移除成员");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int itemID = item.getItemId();
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        UserGroup.Member member = userGroup.getMembers().get(menuInfo.position);

        switch (itemID) {
            case CONTEXT_MENU_ITEM_DEL_MEMBER:
                presenter.removeGroupMember(member, userGroup);
                break;

            case CONTEXT_MENU_ITEM_REMOVE_ADMIN:
                presenter.removeGrouopAdmin(member, userGroup);
                break;

            case CONTEXT_MENU_ITEM_SET_ADMIN:
                presenter.setGroupAdmin(member, userGroup);
                break;
        }
        return super.onContextItemSelected(item);
    }

    //TODO 单击群成员，跳转至用户信息页
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Utils.showToast2(getApplicationContext(), "跳转至用户详情");
    }

    //看情况显示菜单项
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.e(TAG, "onCreateOptionsMenu: ");
        getMenuInflater().inflate(R.menu.base_toolbar_menu, menu);
        itemJoin = menu.findItem(R.id.action_join_group);
        itemExit = menu.findItem(R.id.action_exit_group);
        itemDel = menu.findItem(R.id.action_del_group);
        itemInviteMember = menu.findItem(R.id.action_invite_member);
        itemTransfer = menu.findItem(R.id.action_transfer_group);

        if (!ImApplication.instance.getCurUser().hasGroup(userGroup.getID())) {
            itemJoin.setVisible(true);
        } else {
            itemInviteMember.setVisible(true);
            itemExit.setVisible(true);
        }

        //群主，则显示解散群、转让群，隐藏退出群，同时可修改群名群简介
        if (userGroup.isMeOwner) {
            itemDel.setVisible(true);
            itemTransfer.setVisible(false);
            itemExit.setVisible(false);

            rLayoutGroupName.setOnClickListener(this);
            rLayoutGroupIntro.setOnClickListener(this);
        }

        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int itemID = item.getItemId();
        switch (itemID) {
            case R.id.action_join_group:
                presenter.joinGroup(userGroup);
                break;

            case R.id.action_exit_group:
                presenter.exitGroup(userGroup);
                break;

            case R.id.action_del_group:
                presenter.delGroup(userGroup);
                break;

            case R.id.action_invite_member:
                showInviteMemberDialog();
                break;

            case R.id.action_transfer_group:
                showTransferToWhoDialog();
                break;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //点击布局，跳转到对应的群资料修改
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.rl_activity_group_info_group_name:
                skipToEditInfoActivity(REQUEST_CODE_EDIT_GROUP_NAME, "修改群名", tvGroupName.getText().toString(), "", 20);
                break;
            case R.id.rl_activity_group_info_group_intro:
                skipToEditInfoActivity(REQUEST_CODE_EDIT_GROUP_INTRO, "修改群简介", tvGroupInfo.getText().toString(), "", 30);
                break;
            case R.id.rl_activity_group_info_namecard:
                skipToEditInfoActivity(REQUEST_CODE_EDIT_GROUP_NAMECARD, "修改群名片", tvNameCard.getText().toString(), "", 10);
                break;

        }

    }

    private void skipToEditInfoActivity(int requestCode, String title, String defaultContent, String tip, int maxLength) {
        Intent intent = new Intent();
        intent.setClass(this, EditInfoActivity.class);
        if (!TextUtils.isEmpty(title)) {
            intent.putExtra("title", title);
        }
        if (!TextUtils.isEmpty(defaultContent)) {
            intent.putExtra("content", defaultContent);
        }
        if (!TextUtils.isEmpty(tip)) {
            intent.putExtra("tip", tip);
        }
        intent.putExtra("maxlength", maxLength);
        startActivityForResult(intent, requestCode);
    }

    private void showInviteMemberDialog() {
        final List<String> friends = new ArrayList<>();
        for (FriendInfo friendInfo : ImApplication.mapFriends.values()) {
            if (!this.memberNames.contains(friendInfo.getUser_id())) {
                friends.add(friendInfo.getUser_id());
            }
        }
        final List<String> resultsList = new ArrayList<>();

        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("选择好友").setIcon(R.drawable.a6a)
                .setNegativeButton("取消", null)
                .setMultiChoiceItems(friends.toArray(new String[0]), null, new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        String friendTemp = friends.get(which);
                        if (isChecked) {
                            resultsList.add(friendTemp);
                        } else {
                            if (resultsList.contains(friendTemp)) {
                                resultsList.remove(friendTemp);
                            }
                        }
                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        presenter.inviteMember(userGroup, resultsList);
                    }
                }).create();

        dialog.show();
    }

    private void showTransferToWhoDialog() {
        final List<String> friends = new ArrayList<>();
        for (FriendInfo friendInfo : ImApplication.mapFriends.values()) {
            if (!this.memberNames.contains(friendInfo.getUser_id())) {
                friends.add(friendInfo.getUser_id());
            }
        }
        final int[] resultChosed = {-1};
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("选择好友").setIcon(R.drawable.a6a)
                .setNegativeButton("取消", null)
                .setSingleChoiceItems(friends.toArray(new String[0]), 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resultChosed[0] = which;
                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        presenter.transferGroup(userGroup, Integer.parseInt(friends.get(resultChosed[0])));
                    }
                }).create();
        dialog.show();
    }

    @Override
    public void showGetInfoFail() {
        Utils.showToast2(getApplicationContext(), "获取群资料失败");
    }

    @Override
    public void showGetInfoSucceed(UserGroup userGroup) {
        this.showGroupInfo(userGroup);
    }

    @Override
    public void showJoinGroupSucceed() {
        Utils.showToast2(getApplicationContext(), "加入群成功");
        Intent intent = new Intent();
        setResult(GroupActivity.CODE_RESULT_UPDATE_LIST, intent);
        finish();
    }

    @Override
    public void showJoinGroupFail() {
        Utils.showToast2(getApplicationContext(), "加入群失败");
    }

    @Override
    public void showSendJoinRequest() {
        Utils.showToast2(getApplicationContext(), "已发送加群请求");
    }

    @Override
    public void showExitGroupSucceed() {
        Utils.showToast2(getApplicationContext(), "退出群聊成功");
        Intent intent = new Intent();
        setResult(GroupActivity.CODE_RESULT_UPDATE_LIST, intent);
        finish();
    }

    @Override
    public void showRemoveGroupMemberSucceed() {
        Utils.showToast2(getApplicationContext(), "移除群成员成功");
        presenter.getGroupPerInfo(userGroup);
    }


    @Override
    public void showDelGroupSucceed() {
        Utils.showToast2(getApplicationContext(), "解散群成功");
        Intent intent = new Intent();
        setResult(GroupActivity.CODE_RESULT_UPDATE_LIST, intent);
        finish();
    }

    @Override
    public void showRequestFail() {
        Utils.showToast2(getApplicationContext(), "请求失败，请稍后重试");
    }

    @Override
    public void showInviteMemberToJoinSucceed() {
        Utils.showToast2(getApplicationContext(), "添加群成员成功");
        presenter.getGroupPerInfo(userGroup);
    }

    @Override
    public void showModifyGroupInfoFail(int type) {
        switch (type) {
            case REQUEST_CODE_EDIT_GROUP_INTRO:
                tvGroupInfo.setText(tempGroupIntro);
                break;
            case REQUEST_CODE_EDIT_GROUP_NAME:
                tvGroupName.setText(tempGroupName);
                break;
            case REQUEST_CODE_EDIT_GROUP_NAMECARD:
                tvNameCard.setText(tempNameCard);
                break;
        }
        Utils.showToast2(getApplicationContext(), "修改失败，请重试");
    }

    @Override
    public void showSetAdminSucceed() {
        Utils.showToast2(getApplicationContext(), "设置成功");
        presenter.getGroupPerInfo(userGroup);
    }

    @Override
    public void showRemoveAdminSucceed() {
        Utils.showToast2(getApplicationContext(), "移除成功");
        presenter.getGroupPerInfo(userGroup);
    }

}
