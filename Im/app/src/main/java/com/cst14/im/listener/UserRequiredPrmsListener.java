package com.cst14.im.listener;

import android.widget.Toast;

import com.cst14.im.activity.UserRequiredPrmsActivity;
import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.protobuf.ProtoClass;

/**
 * Created by Administrator on 2016/9/24 0024.
 */
public class UserRequiredPrmsListener implements iPresenter {
    private UserRequiredPrmsActivity userRequiredPrmsActivity;

    private boolean nick_checked;
    private boolean phone_checked;
    private boolean address_checked;
    private boolean sex_checked;
    private boolean age_checked;
    private boolean birthday_checked;
    private boolean mail_checked;
    private boolean qq_checked;
    private boolean weChat_checked;
    private boolean idCard_checked;
    private boolean creCard_checked;
    private boolean debtCard_checked;
    private boolean stuNum_checked;
    private boolean realPhoto_checked;

    public UserRequiredPrmsListener(UserRequiredPrmsActivity activity){
        userRequiredPrmsActivity = activity;
    }

    public void onProcess(ProtoClass.Msg msg){
        if (msg.getMsgType() != ProtoClass.MsgType.CHECK_USER_REQUIRED && msg.getMsgType() != ProtoClass.MsgType.SET_USER_REQUIRED){
            return;
        }
        switch (msg.getMsgType()){
            case CHECK_USER_REQUIRED:
                nick_checked = msg.getAttrType().getNick();
                phone_checked = msg.getAttrType().getPhone();
                address_checked = msg.getAttrType().getAddress();
                sex_checked = msg.getAttrType().getSex();
                age_checked = msg.getAttrType().getAge();
                birthday_checked = msg.getAttrType().getBirthday();
                mail_checked = msg.getAttrType().getMail();
                qq_checked = msg.getAttrType().getQq();
                weChat_checked = msg.getAttrType().getWechat();
                idCard_checked = msg.getAttrType().getIdCard();
                creCard_checked = msg.getAttrType().getCreCard();
                debtCard_checked = msg.getAttrType().getDebtCard();
                stuNum_checked = msg.getAttrType().getStuNo();
                realPhoto_checked = msg.getAttrType().getRealPhoto();

                userRequiredPrmsActivity.userRequiredSetChecked(nick_checked,phone_checked,address_checked,sex_checked,age_checked, birthday_checked,
                        mail_checked,qq_checked,weChat_checked,idCard_checked,creCard_checked,debtCard_checked,stuNum_checked,realPhoto_checked);
                break;
            case SET_USER_REQUIRED:
                if (msg.getResponseState() == ProtoClass.StatusCode.SUCCESS){
                    userRequiredPrmsActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(userRequiredPrmsActivity,"修改用户必填属性项成功！",Toast.LENGTH_LONG).show();
                        }
                    });
                }
                break;
        }
    }
}
