package com.cst14.im.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Toast;

import com.cst14.im.R;
import com.cst14.im.listener.UserRequiredPrmsListener;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;


public class UserRequiredPrmsActivity extends AppCompatActivity implements View.OnClickListener{
    private CheckBox userRequired_nick;
    private CheckBox userRequired_phone;
    private CheckBox userRequired_address;
    private CheckBox userRequired_sex;
    private CheckBox userRequired_age;
    private CheckBox userRequired_birthday;
    private CheckBox userRequired_mail;
    private CheckBox userRequired_qq;
    private CheckBox userRequired_weChat;
    private CheckBox userRequired_idCard;
    private CheckBox userRequired_creCard;
    private CheckBox userRequired_debtCard;
    private CheckBox userRequired_stuNum;
    private CheckBox userRequired_realPhoto;
    private Button userRequired_yes;

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

    private UserRequiredPrmsListener userRequiredPrmsListener = new UserRequiredPrmsListener(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_required_prms);
        iniView();
        Tools.addPresenter(userRequiredPrmsListener);
        ProtoClass.Msg.Builder msg = ProtoClass.Msg.newBuilder()
                .setMsgType(ProtoClass.MsgType.CHECK_USER_REQUIRED);
        userRequired_sendMsg(msg);
    }

    private void iniView(){
        userRequired_nick = (CheckBox) findViewById(R.id.userRequired_nick);
        userRequired_nick.setOnClickListener((View.OnClickListener) this);
        userRequired_phone = (CheckBox) findViewById(R.id.userRequired_phone);
        userRequired_phone.setOnClickListener((View.OnClickListener) this);
        userRequired_address = (CheckBox) findViewById(R.id.userRequired_address);
        userRequired_address.setOnClickListener((View.OnClickListener) this);
        userRequired_sex = (CheckBox) findViewById(R.id.userRequired_sex);
        userRequired_sex.setOnClickListener((View.OnClickListener) this);
        userRequired_age = (CheckBox) findViewById(R.id.userRequired_age);
        userRequired_age.setOnClickListener((View.OnClickListener) this);
        userRequired_birthday = (CheckBox) findViewById(R.id.userRequired_birthday);
        userRequired_birthday.setOnClickListener((View.OnClickListener) this);
        userRequired_mail = (CheckBox) findViewById(R.id.userRequired_mail);
        userRequired_mail.setOnClickListener((View.OnClickListener) this);
        userRequired_qq = (CheckBox) findViewById(R.id.userRequired_qq);
        userRequired_qq.setOnClickListener((View.OnClickListener) this);
        userRequired_weChat = (CheckBox) findViewById(R.id.userRequired_weChat);
        userRequired_weChat.setOnClickListener((View.OnClickListener) this);
        userRequired_idCard = (CheckBox) findViewById(R.id.userRequired_idCard);
        userRequired_idCard.setOnClickListener((View.OnClickListener) this);
        userRequired_creCard = (CheckBox) findViewById(R.id.userRequired_creCard);
        userRequired_creCard.setOnClickListener((View.OnClickListener) this);
        userRequired_debtCard = (CheckBox) findViewById(R.id.userRequired_debtCard);
        userRequired_debtCard.setOnClickListener((View.OnClickListener) this);
        userRequired_stuNum = (CheckBox) findViewById(R.id.userRequired_stuNum);
        userRequired_stuNum.setOnClickListener((View.OnClickListener) this);
        userRequired_realPhoto = (CheckBox) findViewById(R.id.userRequired_realPhoto);
        userRequired_realPhoto.setOnClickListener((View.OnClickListener) this);
        userRequired_yes = (Button) findViewById(R.id.userRequired_yes);
        userRequired_yes.setOnClickListener((View.OnClickListener) this);
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.userRequired_nick:
                if (userRequired_nick.isChecked()){
                    nick_checked = true;
                }else{
                    nick_checked = false;
                }
                break;
            case R.id.userRequired_phone:
                if (userRequired_phone.isChecked()){
                    phone_checked = true;
                }else{
                    phone_checked = false;
                }
                break;
            case R.id.userRequired_address:
                if (userRequired_address.isChecked()){
                    address_checked = true;
                }else{
                    address_checked = false;
                }
                break;
            case R.id.userRequired_sex:
                if (userRequired_sex.isChecked()){
                    sex_checked = true;
                }else{
                    sex_checked = false;
                }
                break;
            case R.id.userRequired_age:
                if (userRequired_age.isChecked()){
                    age_checked = true;
                }else{
                    age_checked = false;
                }
                break;
            case R.id.userRequired_birthday:
                if (userRequired_birthday.isChecked()){
                    birthday_checked = true;
                }else{
                    birthday_checked = false;
                }
                break;
            case R.id.userRequired_mail:
                if (userRequired_mail.isChecked()){
                    mail_checked = true;
                }else{
                    mail_checked = false;
                }
                break;
            case R.id.userRequired_qq:
                if (userRequired_qq.isChecked()){
                    qq_checked = true;
                }else{
                    qq_checked = false;
                }
                break;
            case R.id.userRequired_weChat:
                if (userRequired_weChat.isChecked()){
                    weChat_checked = true;
                }else{
                    weChat_checked = false;
                }
                break;
            case R.id.userRequired_idCard:
                if (userRequired_idCard.isChecked()){
                    idCard_checked = true;
                }else{
                    idCard_checked = false;
                }
                break;
            case R.id.userRequired_creCard:
                if (userRequired_creCard.isChecked()){
                    creCard_checked = true;
                }else{
                    creCard_checked = false;
                }
                break;
            case R.id.userRequired_debtCard:
                if (userRequired_debtCard.isChecked()){
                    debtCard_checked = true;
                }else{
                    debtCard_checked = false;
                }
                break;
            case R.id.userRequired_stuNum:
                if (userRequired_stuNum.isChecked()){
                    stuNum_checked = true;
                }else{
                    stuNum_checked = false;
                }
                break;
            case R.id.userRequired_realPhoto:
                if (userRequired_realPhoto.isChecked()){
                    realPhoto_checked = true;
                }else{
                    realPhoto_checked = false;
                }
                break;
            case R.id.userRequired_yes:
                System.out.println("nick_checked:" + nick_checked);
                ProtoClass.Attrtype.Builder attrType = ProtoClass.Attrtype.newBuilder()
                        .setNick(nick_checked)
                        .setPhone(phone_checked)
                        .setAddress(address_checked)
                        .setSex(sex_checked)
                        .setAge(age_checked)
                        .setBirthday(birthday_checked)
                        .setMail(mail_checked)
                        .setQq(qq_checked)
                        .setWechat(weChat_checked)
                        .setIdCard(idCard_checked)
                        .setCreCard(creCard_checked)
                        .setDebtCard(debtCard_checked)
                        .setStuNo(stuNum_checked)
                        .setRealPhoto(realPhoto_checked);
                ProtoClass.Msg.Builder msg = ProtoClass.Msg.newBuilder()
                        .setMsgType(ProtoClass.MsgType.SET_USER_REQUIRED)
                        .setAttrType(attrType);
                userRequired_sendMsg(msg);
                break;
        }
    }

    private void userRequired_sendMsg(ProtoClass.Msg.Builder builder){
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Toast.makeText(UserRequiredPrmsActivity.this,"发送失败",Toast.LENGTH_LONG);
            }
            //第二个参数是服务器返回的响应消息
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                userRequiredPrmsListener.onProcess(responseMsg);
                return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
            }
        });
    }

    public void userRequiredSetChecked(final boolean nick,final boolean phone,final boolean address,final boolean sex,final boolean age, final boolean birthday, final boolean mail,
                                       final boolean qq,final boolean weChat,final boolean idCard,final boolean creCard,final boolean debtCard,final boolean stuNum,final boolean realPhoto){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                nick_checked = nick;
                phone_checked = phone;
                address_checked = address;
                sex_checked = sex;
                age_checked = age;
                birthday_checked = birthday;
                mail_checked = mail;
                qq_checked = qq;
                weChat_checked = weChat;
                idCard_checked = idCard;
                creCard_checked = creCard;
                debtCard_checked = debtCard;
                stuNum_checked = stuNum;
                realPhoto_checked = realPhoto;

                userRequired_nick.setChecked(nick);
                userRequired_phone.setChecked(phone);
                userRequired_address.setChecked(address);
                userRequired_sex.setChecked(sex);
                userRequired_age.setChecked(age);
                userRequired_birthday.setChecked(birthday);
                userRequired_mail.setChecked(mail);
                userRequired_qq.setChecked(qq);
                userRequired_weChat.setChecked(weChat);
                userRequired_idCard.setChecked(idCard);
                userRequired_creCard.setChecked(creCard);
                userRequired_debtCard.setChecked(debtCard);
                userRequired_stuNum.setChecked(stuNum);
                userRequired_realPhoto.setChecked(realPhoto);
            }
        });
    }
}
