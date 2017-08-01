package com.cst14.im.listener;

import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.Model;
import com.cst14.im.utils.pictureUtils.httprequestPresenter;

import java.net.MalformedURLException;

/**
 * Created by zxm on 2016/8/27.
 */
public class getAttrListener implements iPresenter {

    public void onProcess(ProtoClass.Msg msg) {
        if (msg.getMsgType() != ProtoClass.MsgType.GET_ATTR) {
            return;
        }

        if (msg.getResponseState() != ProtoClass.StatusCode.SUCCESS) {

            final String errMsg = msg.getErrMsg();
            System.out.println(errMsg);
            return;
        }
        Model.setUserID(msg.getUser().getUserID());
        Model.setRole(msg.getUser().getUserRoleID());
        Model.setUsername(msg.getAccount());
        System.out.println("在getttrlistener"+msg.getAccount());
        Model.setUickName(msg.getUser().getNickName());
        Model.setHeadimagePath(msg.getUser().getIconName());



        if(!msg.getUser().hasUserDetail()){
            ProtoClass.UserCustomAttr.Builder attrbuilder = ProtoClass.UserCustomAttr.newBuilder();
            attrbuilder.setUserID(Model.getID());
            ProtoClass.UserCustomAttr mattr=  attrbuilder.build();

            ProtoClass.UserDetail.Builder builder = ProtoClass.UserDetail.newBuilder();
            builder.setUID(Model.getID())
            .setAddress("")
            .setCreCard("")
            .setMail("")
            .setPhone("")
            .setQQ("")
            .setCustomAttr(mattr)
            .setWechat("")
            .setAge(0)
            .setSex("");
            ProtoClass.UserDetail UD=builder.build();
            Model.setDetail(UD);

        }else{
            if (!msg.getUser().getUserDetail().hasCustomAttr()){
                ProtoClass.UserCustomAttr.Builder attrbuilder = ProtoClass.UserCustomAttr.newBuilder();

                attrbuilder.setUserID(Model.getID());
                ProtoClass.UserCustomAttr mattr=  attrbuilder.build();
                Model.setCustomAttr(mattr);
            }else{
                System.out.println(msg.getUser().getUserDetail().getCustomAttr().getUserID());
                Model.setCustomAttr(msg.getUser().getUserDetail().getCustomAttr());

            }
            Model.setDetail(msg.getUser().getUserDetail());



        }



    }

}