package com.cst14.im.listener;

import android.widget.Switch;

import com.cst14.im.activity.LoginActivity;
import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.Model;

/**
 * Created by zxm on 2016/9/22.
 */
public class GetAttrTypeListener implements iPresenter {
    public void onProcess(ProtoClass.Msg msg) {
        if (msg.getMsgType() != ProtoClass.MsgType.GET_ATTR_TYPE) {
            return;
        }

        if (msg.getResponseState() != ProtoClass.StatusCode.SUCCESS) {
            final String errMsg = msg.getErrMsg();
            return;
        }
        if(msg.getAttrType()!=null){
           //服务端默认每个字段都有值
            Model.setAttrType(msg.getAttrType().getNick(),msg.getAttrType().getPhone(),
                    msg.getAttrType().getRealPhoto(),msg.getAttrType().getAddress(),
                    msg.getAttrType().getAddress(),msg.getAttrType().getAge(),
                    msg.getAttrType().getSex(),msg.getAttrType().getMail(),msg.getAttrType().getQq(),
                    msg.getAttrType().getWechat());
        }
    }
}
