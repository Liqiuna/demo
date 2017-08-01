package com.cst14.im.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cst14.im.Dialog.NearbyTipDialog;
import com.cst14.im.Fragment.FriendFragment;
import com.cst14.im.Fragment.SessionsFragment;
import com.cst14.im.Fragment.UserFragment;
import com.cst14.im.R;
import com.cst14.im.adapter.FragmentAdapter;
import com.cst14.im.baseActionView.SessionView;
import com.cst14.im.baseClass.iGroupView;
import com.cst14.im.bean.UserGroup;
import com.cst14.im.listener.GroupPresenter;
import com.cst14.im.listener.NearbyTipListener;
import com.cst14.im.listener.SessionLisener;
import com.cst14.im.listener.getAttrListener;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Model;
import com.cst14.im.utils.Utils;
import com.cst14.im.utils.sessionUtils.SessionHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MRLWJ on 2016/8/22.
 */
public class MainActivity extends AppCompatActivity implements SessionView, iGroupView {
    private ViewPager mViewPage;
    private List<Fragment> mFragments;
    public UserFragment mUserFragment;
    public SessionsFragment mSessionsFragment;
    public FriendFragment mFriendFragment;
    private TextView tvRedCycle;
    private static TextView tv_red_cycle_new_friend;
    private List<ImageView> mIconList;
    private ViewGroup rootLayout;
    private FragmentAdapter mFragment;
    private int curPage = 1;
    public SessionLisener sessionLisener;
    private getAttrListener mgetAttrListener;
    private Button mChangePwd;
    private ArrayAdapter<String> adapter;
    private ListView listView;
    private static final String [] surper_manager = {"新建好友分组","管理员板块","客服板块","设置角色"};
    private static final String [] normal_manager ={"新建好友分组","管理员板块"};
    private static final String [] customer_service ={"新建好友分组","客服板块"};
    private static final String [] normal_user = {"新建好友分组"};

    private Context ctx;
    private Button btn_bulb;
    private String locProvider;
    private LocationManager locManager;
    private NearbyTipListener nearbyTipListener;
    private NearbyTipDialog nearbyTipDialog;

    private GroupPresenter groupPresenter = new GroupPresenter(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImApplication.mainActivity = this;
        mgetAttrListener=new getAttrListener();
        initView();
        initData();
        getAttr();
        ctx = this;
        initNearbyTipDialog();
        mViewPage.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mViewPage.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                iconStateChange(position, positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
                curPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        rootLayout = (ViewGroup) getWindow().getDecorView();

        new Thread(new Runnable() {
            @Override
            public void run() {
                groupPresenter.getAllGroupsUserJoined();
            }
        }).start();

        setFriendRequestCount(ImApplication.newFriendRequestNum);
    }

    //    初始化控件
    private void initView() {
        mViewPage = (ViewPager) findViewById(R.id.viewpager_in_main);
        tvRedCycle = (TextView) findViewById(R.id.tv_red_cycle_in_layout);
        tv_red_cycle_new_friend = (TextView) findViewById(R.id.tv_red_cycle_new_friend);
        listView = (ListView) findViewById(R.id.listView_in_main);
        tvRedCycle.setAlpha(0f);
        tv_red_cycle_new_friend.setAlpha(0f);
        initIcon();
    }

    //    初始化控件数据
    private void initData() {
        mFragments = new ArrayList<Fragment>();
        mUserFragment = new UserFragment();
        mSessionsFragment = new SessionsFragment();
        mFriendFragment = new FriendFragment();

        mFragments.add(mSessionsFragment);
        mFragments.add(mFriendFragment);
        mFragments.add(mUserFragment);
        sessionLisener = new SessionLisener(this);
        Tools.addPresenter(sessionLisener);
        mFragment = new FragmentAdapter(getSupportFragmentManager(), mFragments, this);
        mViewPage.setAdapter(mFragment);
        mViewPage.setCurrentItem(curPage);
        ImApplication.density = getResources().getDisplayMetrics().density;
    }

    // 好友提醒
    private void initNearbyTipDialog() {
        btn_bulb = (Button) findViewById(R.id.btn_bulb);
        nearbyTipDialog = new NearbyTipDialog(this);
        nearbyTipDialog.callOnCreated();
        nearbyTipListener = new NearbyTipListener(this, new NearbyTipListener.OnAcceptNearbyTipListener() {
            @Override
            public void onAccept(ProtoClass.Nearby nearby) {
                // 显示小灯泡提醒
                nearbyTipDialog.updateNearby(nearby);
                btn_bulb.setVisibility(View.VISIBLE);
            }
        });
        btn_bulb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_bulb.setVisibility(View.INVISIBLE);
                nearbyTipDialog.show();
            }
        });
        Tools.addPresenter(nearbyTipListener);

        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    private void updateLocationToServer(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            Toast.makeText(this, "你的纬度位置不正确", Toast.LENGTH_SHORT).show();
            return;
        }
        if (longitude < -180.0 || longitude > 180.0) {
            Toast.makeText(this, "你的经度位置不正确", Toast.LENGTH_SHORT).show();
            return;
        }
        ProtoClass.Location.Builder locBuilder = ProtoClass.Location.newBuilder();
        locBuilder.setLatitude(latitude).setLongitude(longitude);

        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setAccount(ImApplication.User_id);
        builder.setMsgType(ProtoClass.MsgType.UPDATE_LOCATION);
        builder.setLocation(locBuilder);

        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, final ProtoClass.Msg responseMsg) {
                if (ProtoClass.MsgType.UPDATE_LOCATION   != responseMsg.getMsgType()) return false;
                if (ProtoClass.StatusCode.SUCCESS   != responseMsg.getResponseState()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ctx, "更新位置失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return true;
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (locManager != null) {
            try
            {
                /** 先注册GPS位置监听, GPS未开启就注册Network监听, GPS重新开启后注销Network监听 **/
                locManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 10 * 1000, 1, gpsLocListener);
            }
            catch (SecurityException e) {e.printStackTrace();}
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (locManager != null) {
            try
            {
                locManager.removeUpdates(gpsLocListener);
                locManager.removeUpdates(netLocListener);
            }
            catch (SecurityException e) {e.printStackTrace();}
        }
    }

    // 设置显示总共多少条消息未读
    public void setTotalUnReadMsgCount(int totalUnReadMsgCount) {
        if (totalUnReadMsgCount <= 0) {
            tvRedCycle.setAlpha(0f);
            setTitle("我的应用");
            return;
        }
        setTitle("我的应用(" + totalUnReadMsgCount + ")");
        tvRedCycle.setText("" + totalUnReadMsgCount);
        tvRedCycle.setAlpha(1f);
    }

    // 设置显示多少个好友申请
    public static void setFriendRequestCount(int friendRequestCount) {
        if (friendRequestCount <= 0) {
            tv_red_cycle_new_friend.setAlpha(0f);
            return;
        }
        tv_red_cycle_new_friend.setText("" + friendRequestCount);
        tv_red_cycle_new_friend.setAlpha(1f);
    }
    private void initIcon() {
        mIconList = new ArrayList<ImageView>();
        mIconList.add((ImageView) findViewById(R.id.iv_f0));
        mIconList.add((ImageView) findViewById(R.id.iv_f1));
        mIconList.add((ImageView) findViewById(R.id.iv_f2));

        ImageView iv;
        for (int i = 0; i < mIconList.size(); i++) {
            iv = mIconList.get(i);
            if (i == curPage) {
                iv.setAlpha(1f);
            } else {
                iv.setAlpha(0f);
            }
            final int newCurPage = i;
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPage.setCurrentItem(newCurPage, true);
                    mIconList.get(curPage).setAlpha(0f);
                    mIconList.get(newCurPage).setAlpha(1f);
                    curPage = newCurPage;
                }
            });
        }
    }


    private void iconStateChange(int pos, float offset) {
        if (offset <= 0) {
            return;
        }
        mIconList.get(pos).setAlpha(1 - offset);
        mIconList.get(pos + 1).setAlpha(offset);
    }

    // 获取根布局
    public ViewGroup getRootViewGroup() {
        return rootLayout;
    }

    @Override
    public SessionHolder startSession(String account) {
        return mSessionsFragment.startSession(account, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_search:
                Intent intent = new Intent(this,SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.action_add:
                if (listView.getVisibility() == View.VISIBLE){
                    listView.setVisibility(View.GONE);
                    break;
                } else{
                    listView.setVisibility(View.VISIBLE);
                }
                //根据用户权限，为下拉列表定义一个适配器，这里就用到里前面定义的list
                switch (ImApplication.getMyRoleID()){
                    case ImApplication.SUPER_MANAGER:
                        adapter = new ArrayAdapter<String>(this, R.layout.main_listview_item, surper_manager);
                        break;
                    case ImApplication.NORMAL_MANAGER:
                        adapter = new ArrayAdapter<String>(this, R.layout.main_listview_item, normal_manager);
                        break;
                    case ImApplication.CUSTOMER_SERVICE:
                        adapter = new ArrayAdapter<String>(this, R.layout.main_listview_item, customer_service);
                        break;
                    case ImApplication.NORMAL_USER:
                        adapter = new ArrayAdapter<String>(this, R.layout.main_listview_item, normal_user);
                        break;
                }
                //为适配器设置下拉列表下拉时的菜单样式
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //将适配器添加到下拉列表上
                listView.setAdapter(adapter);
                listView.setVisibility(View.VISIBLE);
                listView.bringToFront();
                //spinnerListener(spinner);
                //为下拉列表设置各种事件的响应，这个事响应菜单被选中
                listViewListener(listView);                break;
            case R.id.action_secure:
                Intent intent0 = new Intent(this,SettingSecurityActivity.class);
                startActivity(intent0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void listViewListener(final ListView listView){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                System.out.println(position);
                switch (position){
                    case 0:
                        Add_friend_group();
                        break;
                    case 1:
                        startActivity(new Intent(MainActivity.this, ManagerPrmsActivity.class));
                        break;
                    case 2:
                        break;
                    case 3:
                        startActivity(new Intent(MainActivity.this, SetRoleActivity.class));
                        break;
                }
                listView.setVisibility(View.GONE);
            }
        });
    }

    public void Add_friend_group(){
        //加载布局
        View contentView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.add_group_or_remark, null);

        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setView(contentView).create();
        final TextView tv_add_or_remark_name=  (TextView)contentView.findViewById(R.id.tv_add_or_remark_name);
        final TextView tv_message_detail= (TextView)contentView.findViewById(R.id.tv_message_detail);
        final EditText ed_add_or_remark_namel= (EditText)contentView.findViewById(R.id.et_add_or_remark_name);
        final View btn_add_or_remark_name_cancle=contentView.findViewById(R.id.btn_add_or_remark_name_cancle);
        final View btn_add_or_remark_name_ok=contentView.findViewById(R.id.btn_add_or_remark_name_ok);
        tv_add_or_remark_name.setText("增加分组");
        tv_message_detail.setText("请输入新的分组名称");
        btn_add_or_remark_name_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btn_add_or_remark_name_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name=ed_add_or_remark_namel.getText().toString();
                int id = Integer.parseInt(ImApplication.User_id);
                ProtoClass.User user = ProtoClass.User.newBuilder().setUserID(id).build();
                ProtoClass.FriendList friendList = ProtoClass.FriendList.newBuilder().setListNO(++ImApplication.Friend_group_MAX_number).setListName(name).build();
                ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                builder.setUser(user)
                        .addFriendLists(friendList)
                        .setMsgType(ProtoClass.MsgType.ADD_Friend_GROUP);
                Tools.startTcpRequest(builder, new Tools.TcpListener() {
                    @Override
                    public void onSendFail(Exception e) {
                        Log.e("添加好友分组：", e.toString());
                    }

                    //第二个参数是服务器返回的响应消息
//                    @Override
//                    public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
//                        sessionLisener.onProcess(responseMsg);
//                        return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
//                    }
                });
                dialog.dismiss();
            }
        });
        dialog.show();

    }
    public void getAttr(){
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        System.out.println("用户名："+Model.getUsername());
        builder.setAccount(ImApplication.User_id)
                .setToken(ImApplication.getLoginToken())
                .setMsgType(ProtoClass.MsgType.GET_ATTR);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Toast.makeText(getBaseContext(), "send failed", Toast.LENGTH_SHORT);
            }

            //第二个参数是服务器返回的响应消息
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {

                mgetAttrListener.onProcess(responseMsg);

                return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
            }
        });
    }

    @Override
    protected void onDestroy() {
        Tools.tcpListenerList.clear();
        super.onDestroy();
    }

    //触碰屏幕事件发生
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        hideListViewWhenTouchOtherView(this, ev, getExcludeTouchHideInputViews());
        return super.dispatchTouchEvent(ev);
    }

    public List<View> getExcludeTouchHideInputViews(){
        List<View> excludeViews = new ArrayList<View>();
        excludeViews.add(listView);
        return excludeViews;
    }

    /**
     * 当点击其他View时隐藏软键盘
     * @param activity
     * @param ev
     * @param excludeViews  点击这些View不会触发隐藏软键盘动作
     */

    public final void hideListViewWhenTouchOtherView(Activity activity, MotionEvent ev, List<View> excludeViews){

        activity.onTouchEvent(ev);
        if (ev.getAction() == MotionEvent.ACTION_DOWN){
            if (excludeViews != null && !excludeViews.isEmpty()){
                for (int i = 0; i < excludeViews.size(); i++){
                    if (isTouchView(excludeViews.get(i), ev)){
                        return;
                    }
                }
            }
            View v = activity.getCurrentFocus();
            if (!isShouldHideListView(v, ev)){
                listView.setVisibility(View.GONE);
            }

        }
    }

    public static boolean isTouchView(View view, MotionEvent event){
        if (view == null || event == null){
            return false;
        }
        int[] leftTop = {0, 0};
        view.getLocationInWindow(leftTop);
        int left = leftTop[0];
        int top = leftTop[1];
        int bottom = top + view.getHeight();
        int right = left + view.getWidth();
        if (event.getRawX() > left && event.getRawX() < right
                && event.getRawY() > top && event.getRawY() < bottom){
            return true;
        }
        return false;
    }

    public static boolean isShouldHideListView(View v, MotionEvent event){
        return v != null && (v instanceof EditText) && !isTouchView(v, event);
    }

    @Override
    public void showGetGroupListFail() {
        Utils.showToast2(getApplicationContext(), "获取群列表失败");
    }

    @Override
    public void showGetGroupListSucceed(List<ProtoClass.GroupInfo> groupInfos) {
        List<UserGroup> userGroups = new ArrayList<>();
        for (ProtoClass.GroupInfo groupInfo : groupInfos) {
            UserGroup userGroup = new UserGroup(groupInfo);
            userGroups.add(userGroup);
        }
        //save group data to current user
        ImApplication.instance.getCurUser().setGroups(userGroups);
        ImApplication.instance.getCurUser().setLastGroupListUpdateTime(System.currentTimeMillis());
        Tools.removePresenter(this.groupPresenter);
    }

    private LocationListener gpsLocListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                updateLocationToServer(location.getLatitude(), location.getLongitude());
            }
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {
            try
            {
                if (locManager != null) {
                    locManager.removeUpdates(netLocListener);
                }
            }
            catch (SecurityException e)
            {
                e.printStackTrace();
            }
        }
        @Override
        public void onProviderDisabled(String provider) {
            try
            {
                if (locManager != null) {
                    locManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER, 10 * 1000, 1, netLocListener);
                }
            }
            catch (SecurityException e)
            {
                e.printStackTrace();
            }
        }
    };

    private LocationListener netLocListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                updateLocationToServer(location.getLatitude(), location.getLongitude());
            }
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) {}
    };
}
