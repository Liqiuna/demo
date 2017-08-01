package com.cst14.im.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.cst14.im.R;
import com.cst14.im.db.dao.NearbySettingDao;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;

/**
 * Created by Administrator on 2016/9/11 0011.
 */
public class NearbyTipSettingAty extends Activity implements CompoundButton.OnCheckedChangeListener {

    private boolean isCanFind = true;
    private boolean isNeedTip = true;
    private double tipDistance = 2000;

    private Context ctx;
    private Switch swh_canFind;
    private Switch swh_needTip;
    private EditText et_tipDistance;
    private View ly_dis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_nearby_tip);

        ctx = this;
        initView();
        initData();
    }

    private void initView() {
        swh_canFind = (Switch) findViewById(R.id.swh_canFind);
        swh_needTip = (Switch) findViewById(R.id.swh_needTip);
        swh_canFind.setOnCheckedChangeListener(this);
        swh_needTip.setOnCheckedChangeListener(this);

        et_tipDistance = (EditText) findViewById(R.id.et_tipDistance);
        ly_dis = findViewById(R.id.ly_dis);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave();
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == null) return;

        switch (buttonView.getId()) {
            case R.id.swh_canFind:
                break;

            case R.id.swh_needTip:
                if(buttonView.isChecked()) {
                    ly_dis.setVisibility(View.VISIBLE);
                } else {
                    ly_dis.setVisibility(View.INVISIBLE);
                }
                break;
        }
    }

    private void initData() {
        /** 先从本地获得数据, 不成功再从服务器获取 **/
        if (loadSettingFromSP()) {
            return;
        }
        askSettingDataFromSrv();
    }

    private boolean loadSettingFromSP() {
        NearbySettingDao settingDao = new NearbySettingDao(getApplicationContext());
        boolean isOK = settingDao.LoadSetting(new NearbySettingDao.OnLoadListener() {
            @Override
            public void onLoadSuccess(boolean isCanFind, boolean isNeedTip, double tipDistance) {
                loadSettingView(isCanFind, isNeedTip, tipDistance);
            }
        });
        return isOK;
    }

    private void askSettingDataFromSrv() {
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setAccount(ImApplication.User_id);
        builder.setMsgType(ProtoClass.MsgType.GET_NEARBY_SETTING);

        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, final ProtoClass.Msg responseMsg) {
                if (ProtoClass.MsgType.GET_NEARBY_SETTING != responseMsg.getMsgType()) return false;
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
                        onAskSettingSuccess(responseMsg.getNearbySetting());
                    }
                });
                return true;
            }
        });
    }

    private void onAskSettingSuccess(ProtoClass.NearbySetting setting) {
        /** 服务器传过来的是 km, 需要转化为 m **/
        tipDistance = (int)(setting.getTipDistance()*1000d);
        loadSettingView(setting.getIsCanBeFind(), setting.getIsNeedTip(), tipDistance);
    }

    private void loadSettingView(boolean _isCanFind, boolean _isNeedTip, double _tipDistance) {
        isCanFind = _isCanFind;
        isNeedTip = _isNeedTip;
        tipDistance = _tipDistance;

        swh_canFind.setChecked(isCanFind);
        swh_needTip.setChecked(isNeedTip);
        et_tipDistance.setText(String.valueOf((int)tipDistance));
        if (isNeedTip) {
            ly_dis.setVisibility(View.VISIBLE);
        } else {
            ly_dis.setVisibility(View.INVISIBLE);
        }
    }

    private boolean onPreSave() {
        if (swh_needTip.isChecked()) {
            String text = et_tipDistance.getText().toString();
            if (!TextUtils.isEmpty(text)) {
                double dis = Double.valueOf(text);
                if (dis <= 0 || dis > 2000) {
                    Toast.makeText(ctx, "请输入正确的距离(0< 距离 <= 2000)", Toast.LENGTH_SHORT).show();
                    return false;
                }
                tipDistance = dis;
            } else {
                tipDistance = 2000;
            }
        }
        isCanFind = swh_canFind.isChecked();
        isNeedTip = swh_needTip.isChecked();
        return true;
    }

    private void onSave() {
        if (!onPreSave()) return;

        ProtoClass.NearbySetting.Builder settingBuilder = ProtoClass.NearbySetting.newBuilder();
        settingBuilder.setIsCanBeFind(isCanFind);
        settingBuilder.setIsNeedTip(isNeedTip);
        double distance_km = tipDistance / 1000d;
        settingBuilder.setTipDistance(distance_km);

        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setAccount(ImApplication.User_id);
        builder.setMsgType(ProtoClass.MsgType.UPDATE_NEARBY_SETTING);
        builder.setNearbySetting(settingBuilder);

        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, final ProtoClass.Msg responseMsg) {
                if (ProtoClass.MsgType.UPDATE_NEARBY_SETTING != responseMsg.getMsgType()) return false;
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
                        onSaveSuccess();
                    }
                });
                return true;
            }
        });
    }

    private void onSaveSuccess() {
        NearbySettingDao settingDao = new NearbySettingDao(getApplicationContext());
        settingDao.SaveSetting(isCanFind, isNeedTip, tipDistance);

        Toast.makeText(ctx, "设置成功", Toast.LENGTH_SHORT).show();
        finish();
    }
}
