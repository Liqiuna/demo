package com.cst14.im.listener;


import com.cst14.im.activity.SearchUserDetailsActivity;
import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.UserDetailInfo;

/**
 * Created by Belinda Y on 2016/9/24.
 */
public class GetUserDetailListener implements iPresenter {
    SearchUserDetailsActivity activity;
    GetUserDetailView userDetailView;
    String tipMsg;
    public GetUserDetailListener(GetUserDetailView userDetailView) {
        this.userDetailView= userDetailView;
        activity = (SearchUserDetailsActivity)userDetailView;
    }

    @Override
    public void onProcess(final ProtoClass.Msg msg) {
        if (msg.getMsgType() != ProtoClass.MsgType.GET_USER_DETAIL) {
            return;
        }


        if (msg.getResponseState() != ProtoClass.StatusCode.SUCCESS) {
            final String errMsg = msg.getErrMsg();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    userDetailView.failedGetUserDetail(errMsg,true);
                }
            });
            return;
        }

        if(!msg.getIsUserExists()){
            tipMsg="用户不存在，请确认输入的用户名/账号是否正确";

        }else if(msg.getUser().getUserRoleID()== ProtoClass.UserRole.SupAdmin_VALUE ){
            tipMsg="该用户为超级管理员，您没有权限修改其信息";

        }else if(msg.getUser().getUserRoleID()== ProtoClass.UserRole.Admin_VALUE&&
                ImApplication.getMyRoleID()== ProtoClass.UserRole.Admin_VALUE){
            tipMsg="该用户为管理员，您没有权限修改其信息";

        }

        if (tipMsg!=null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    userDetailView.failedGetUserDetail(tipMsg, false);
                    tipMsg=null;
                }
            });

            return;
        }



        //查到的用户信息可以被修改时
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ProtoClass.User user=msg.getUser();
                ProtoClass.UserDetail userDetail=msg.getUser().getUserDetail();
                UserDetailInfo userDetailInfo=new UserDetailInfo(user.getUserID(),user.getNickName(),userDetail.getPhone(),
                        userDetail.getAddress(),userDetail.getQQ(),userDetail.getWechat(),userDetail.getSex(),userDetail.getAge(),userDetail.getIdCard(),
                        userDetail.getMail(),userDetail.getCreCard(),userDetail.getDebtCard(),userDetail.getStdNo(),userDetail.getBirthday());
                userDetailView.successUserDetail(userDetailInfo);
            }
        });



    }

    public interface GetUserDetailView {
        void successUserDetail(UserDetailInfo userDetailInfo);
        void failedGetUserDetail(String tipMsg, boolean isServerError);
    }
}
