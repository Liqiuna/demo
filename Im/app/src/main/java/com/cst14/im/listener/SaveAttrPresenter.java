package com.cst14.im.listener;

import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.Model;

/**
 * Created by zxm on 2016/8/28.
 */
public class SaveAttrPresenter {
    public void onProcess(ProtoClass.Msg msg) {
        if (msg.getMsgType() != ProtoClass.MsgType.SAVE_ATTR) {
            return;
        }
        if (msg.getResponseState() != ProtoClass.StatusCode.SUCCESS) {

            final String errMsg = msg.getErrMsg();
            System.out.println(errMsg);
            return;
        }
        System.out.println("保存成功");

    }

}
