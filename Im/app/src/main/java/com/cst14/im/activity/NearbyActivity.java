package com.cst14.im.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.cst14.im.Dialog.NearbyFilterDialog;
import com.cst14.im.R;
import com.cst14.im.adapter.NearbyAdapter;
import com.cst14.im.bean.NearbyBean;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/1 0001.
 */
public class NearbyActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "NearbyActivity";

    private LocationManager locManager;
    private Double latitude;
    private Double longitude;
    private ProtoClass.NearbyCondition.Builder conditionBuilder;
    private int curPage = -1;
    private boolean isHaveMoreData = false;

    private Context ctx;
    private SwipeRefreshLayout ly_swipe;
    private ListView lv_nearby;
    private NearbyFilterDialog filterDialog;

    private MyHandler handler;
    private List<NearbyBean> nearbyList;
    private NearbyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);

        ctx = this;
        initView();
        initData();
    }
    private void initView() {
        ly_swipe = (SwipeRefreshLayout) findViewById(R.id.ly_swipe);
        lv_nearby = (ListView) findViewById(R.id.lv_nearby);

        ly_swipe.setOnRefreshListener(this);
        ly_swipe.setColorSchemeResources(android.R.color.holo_blue_bright);
        ly_swipe.setProgressBackgroundColorSchemeColor(Color.parseColor("#ffffffff"));

        lv_nearby.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (lv_nearby == null) return;

                /** 判断是否可下拉刷新 **/
                boolean enable = false;
                if (lv_nearby.getChildCount() > 0) {
                    // check if the first item of the list is visible
                    boolean firstItemVisible = firstVisibleItem == 0;
                    // check if the top of the first item is visible
                    boolean topOfFirstItemVisible = lv_nearby.getChildAt(0).getTop() == 0;
                    // enabling or disabling the refresh layout
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
                if (lv_nearby.getChildCount() == 0) {
                    enable = true;
                }
                ly_swipe.setEnabled(enable);

                /** 如果已经到最后一个Item, 获得下一页数据 **/
                if (isHaveMoreData && firstVisibleItem+visibleItemCount == totalItemCount) {
                    isHaveMoreData = false;
                    getNearbyByPage();
                }
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.ib_filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterDialog.show();
            }
        });

        filterDialog = new NearbyFilterDialog(ctx, new NearbyFilterDialog.OnAcknowListener() {
            @Override
            public void onAcknow(ProtoClass.NearbyCondition.Builder builder) {
                conditionBuilder = builder;
                ly_swipe.setRefreshing(true);
                onRefresh();
            }
        });
    }

    private void initData() {
        handler = new MyHandler(ctx);
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        nearbyList = new ArrayList<>();
        adapter = new NearbyAdapter(ctx, nearbyList);
        lv_nearby.setAdapter(adapter);

        initDefCondition();
    }

    private void initDefCondition() {
        conditionBuilder = ProtoClass.NearbyCondition.newBuilder();
        conditionBuilder.setIsAllAge(true);
        conditionBuilder.setActive(ProtoClass.Active.ONE_DAY);
        conditionBuilder.setSex(ProtoClass.Sex.ALL);
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

    private void updateLocationToServer() {
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
                            Log.e(TAG, "updateLocationToServer:"+responseMsg.getErrMsg());
                        }
                    });
                    return true;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (nearbyList.isEmpty() || nearbyList.size() == 0) {
                            getNearby();
                        }
                    }
                });
                return true;
            }
        });
    }

    private void getNearby() {
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
        builder.setMsgType(ProtoClass.MsgType.GET_NEARBY);
        builder.setNearbyCondition(conditionBuilder);
        builder.setLocation(locBuilder);

        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, final ProtoClass.Msg responseMsg) {
                if (ProtoClass.MsgType.GET_NEARBY   != responseMsg.getMsgType()) return false;
                if (ProtoClass.StatusCode.SUCCESS   != responseMsg.getResponseState()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ctx, responseMsg.getErrMsg(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    return true;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onGetNearbySuccess(responseMsg.getNearByList());
                    }
                });
                return true;
            }
        });
    }

    private void getNearbyByPage() {
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setAccount(ImApplication.User_id);
        builder.setMsgType(ProtoClass.MsgType.GET_NEARBY_BY_PAGE);
        builder.setCurNearbyPage(curPage);

        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                if (ProtoClass.MsgType.GET_NEARBY_BY_PAGE != responseMsg.getMsgType()) return false;
                if (ProtoClass.StatusCode.SUCCESS   != responseMsg.getResponseState()) {
                    Toast.makeText(ctx, responseMsg.getErrMsg(), Toast.LENGTH_SHORT).show();
                    return true;
                }

                final List<ProtoClass.Nearby> nearbys = responseMsg.getNearByList();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onGetNearbySuccess(nearbys);
                    }
                });
                return true;
            }
        });
    }

    private void onGetNearbySuccess(List<ProtoClass.Nearby> nearbys) {
        if (nearbys == null || nearbys.isEmpty()) {
            isHaveMoreData = false;
            Toast.makeText(ctx, "没有更多数据", Toast.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged();
            return;
        }
        curPage++;
        isHaveMoreData = true;
        for (ProtoClass.Nearby nearby : nearbys) {
            NearbyBean bean = new NearbyBean(nearby);
            nearbyList.add(bean);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ly_swipe.setRefreshing(false);
            }
        }, 500);
        if (latitude == null || longitude == null) {
            Toast.makeText(ctx, "请打开定位服务或重新尝试", Toast.LENGTH_SHORT).show();
            return;
        }
        nearbyList.clear();
        curPage = -1;
        getNearby();
    }

    private LocationListener gpsLocListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                updateLocationToServer();
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
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                updateLocationToServer();
            }
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) {}
    };

    static class MyHandler extends android.os.Handler {
        private WeakReference<Context> ctxRefer;

        public MyHandler(Context ctx) {
            ctxRefer = new WeakReference<>(ctx);
        }

        @Override
        public void handleMessage(Message msg) {
            Context ctx = ctxRefer.get();
            if (ctx == null)
                throw new RuntimeException("ctx is null");
        }
    }
}
