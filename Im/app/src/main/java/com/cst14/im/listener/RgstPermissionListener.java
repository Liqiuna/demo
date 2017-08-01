package com.cst14.im.listener;

import android.widget.Toast;

import com.cst14.im.activity.RgstPermissionActivity;
import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.protobuf.ProtoClass;

/**
 * Created by Administrator on 2016/9/13 0013.
 */
public class RgstPermissionListener implements iPresenter {
    private RgstPermissionActivity rgstPermissionActivity;
    private int rgstType;

    private static final int autoRgst = 1;
    private static final int fastRgst = 2;
    private static final int banRgst = 3;

    public RgstPermissionListener(RgstPermissionActivity activity){
        rgstPermissionActivity = activity;
    }

    public void onProcess(final ProtoClass.Msg msg){
        if (msg.getMsgType() != ProtoClass.MsgType.CHECK_RGST_TYPE && msg.getMsgType() != ProtoClass.MsgType.SET_RGST_TYPE){
            return;
        }
        if (msg.getMsgType() == ProtoClass.MsgType.CHECK_RGST_TYPE){
            rgstType = msg.getRgstType();
            switch (rgstType){
                case autoRgst:
                    rgstPermissionActivity.autoRgstChecked();
                    break;
                case fastRgst:
                    rgstPermissionActivity.fastRgstChecked();
                    break;
                case banRgst:
                    rgstPermissionActivity.banRgstChecked();
                    break;
            }
        }else if (msg.getMsgType() == ProtoClass.MsgType.SET_RGST_TYPE) {
            rgstType = rgstPermissionActivity.getRgstType();
            if (rgstType == msg.getRgstType()) {
                rgstPermissionActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(rgstPermissionActivity, "修改系统用户注册类型成功", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }
}
