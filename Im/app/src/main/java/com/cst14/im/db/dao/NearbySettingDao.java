package com.cst14.im.db.dao;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2016/9/22 0022.
 */
public class NearbySettingDao {

    private final int spMode = Context.MODE_PRIVATE;
    private final String SP_NAME = "nearbySetting";
    private final String KEY_IS_CAN_FIND = "isCanFind";
    private final String KEY_IS_NEED_TIP = "isNeedTip";
    private final String KEY_TIP_DISTANCE = "tipDistance";
    private final boolean DEF_VALUE_IS_CAN_FIND = true;
    private final boolean DEF_VALUE_IS_NEED_TIP = true;
    private final float DEF_VALUES_TIP_DISTANCE = 2000;

    private Context ctx;
    public NearbySettingDao(Context ctx) {
        this.ctx = ctx;
    }

    public void SaveSetting(boolean isCanFind, boolean isNeedTip, double tipDistance) {
        SharedPreferences sp = ctx.getSharedPreferences(SP_NAME, spMode);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(KEY_IS_CAN_FIND, isCanFind);
        editor.putBoolean(KEY_IS_NEED_TIP, isNeedTip);
        editor.putFloat(KEY_TIP_DISTANCE, (float) tipDistance);
        editor.apply();
    }

    public void SaveSetting(boolean isCanFind, boolean isNeedTip) {
        SharedPreferences sp = ctx.getSharedPreferences(SP_NAME, spMode);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(KEY_IS_CAN_FIND, isCanFind);
        editor.putBoolean(KEY_IS_NEED_TIP, isNeedTip);
        editor.apply();
    }

    /** 加载成功返回true, 失败返回false **/
    public boolean LoadSetting(OnLoadListener listener) {
        SharedPreferences sp = ctx.getSharedPreferences(SP_NAME, spMode);
        if (!sp.contains(KEY_IS_CAN_FIND) || !sp.contains(KEY_IS_NEED_TIP) || !sp.contains(KEY_TIP_DISTANCE)) {
            return false;
        }
        boolean isCanFind = sp.getBoolean(KEY_IS_CAN_FIND, DEF_VALUE_IS_CAN_FIND);
        boolean isNeedTip = sp.getBoolean(KEY_IS_NEED_TIP, DEF_VALUE_IS_NEED_TIP);
        double tipDistance = sp.getFloat(KEY_TIP_DISTANCE, DEF_VALUES_TIP_DISTANCE);
        if (listener != null) {
            listener.onLoadSuccess(isCanFind, isNeedTip, tipDistance);
        }
        return true;
    }

    public interface OnLoadListener {
        void onLoadSuccess(boolean isCanFind, boolean isNeedTip, double tipDistance);
    }
}
