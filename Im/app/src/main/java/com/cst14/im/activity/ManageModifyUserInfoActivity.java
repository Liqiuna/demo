package com.cst14.im.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.listener.ModifyUserInfoListener;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.StringParser;
import com.cst14.im.utils.UserDetailInfo;
import com.cst14.im.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Belinda Y on 2016/9/24.
 */
public class ManageModifyUserInfoActivity extends AppCompatActivity implements ModifyUserInfoListener.ModifyUserInforView{
    private TextView tv_account;
    private TextView tv_nick;
    private EditText et_modify_nick;
    private TextView tv_sex;
    private TextView tv_phone;
    private TextView tv_address;
    private TextView tv_mail;
    private EditText et_modify_phone;
    private EditText et_modify_address;
    private EditText et_modify_mail;
    private TextView tv_qq;
    private EditText et_modify_qq;
    private TextView tv_wechat;
    private TextView tv_birthday;
    private EditText et_modify_wechat;
    private ImageView iv_clear_text;
    private  DatePicker dp_modify_birthday;
    private  UserDetailInfo userDetailInfo;
    private ProtoClass.UserDetail.Builder userdetail;
    private String userNick=null;
    private  String userphone=null;
    private String userqq=null;
    private  String userwechat=null;
    private  String useraddress=null;
    private String usermail=null;
    private String userSex=null;
    private  String birthday=null;
    private SimpleDateFormat format;
    private  Calendar calendar;
    private ModifyUserInfoListener modifyUserInfoListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle("点击修改用户信息");
        initView();

    }

    private void initView() {
        tv_account=(TextView) findViewById(R.id.tv_account_content);
        tv_nick=(TextView)findViewById(R.id.et_nick);
        tv_sex=(TextView) findViewById(R.id.tv_sex_content);
        tv_phone=(TextView) findViewById(R.id.tv_phone_content);
        tv_address=(TextView) findViewById(R.id.tv_address_content);
        tv_qq=(TextView) findViewById(R.id.tv_qq_content);
        tv_wechat=(TextView)findViewById(R.id.tv_wechat_content);
        tv_address=(TextView) findViewById(R.id.tv_address_content);
        tv_mail=(TextView)findViewById(R.id.tv_email_content);
        tv_birthday=(TextView) findViewById(R.id.tv_birthday_content);
        userDetailInfo=(UserDetailInfo)getIntent().getSerializableExtra("userDetailInfo");
        tv_account.setText(String.valueOf(userDetailInfo.getuID()));
        tv_nick.setText(userDetailInfo.getNick());
        tv_sex.setText(userDetailInfo.getSex());
        tv_phone.setText(userDetailInfo.getPhone());
        tv_qq.setText(userDetailInfo.getQQ());
        tv_wechat.setText(userDetailInfo.getWechat());
        tv_address.setText(userDetailInfo.getAddress());
        tv_mail.setText(userDetailInfo.getMail());
        tv_birthday.setText(userDetailInfo.getBirthday());
        modifyUserInfoListener=new ModifyUserInfoListener(this);
        Tools.addPresenter(modifyUserInfoListener);

    }
    public  void  onClick(View view){
        switch (view.getId()) {
            case R.id.rl_account:
                showTipDialog();
                break;
            case R.id.rl_nick:
                modifyNick();
                break;
            case R.id.rl_sex:
                modifySex();
                break;
            case R.id.rl_phone:
                modifyPhone();
                break;
            case R.id.rl_qq:
                modifyQQ();
                break;
            case R.id.rl_wechat:
                modifyWeChat();
                break;
            case R.id.rl_address:
                modifyAddress();
                break;
            case R.id.rl_email:
                modifyMail();
                break;
            case R.id.rl_birthday:
                modifyBirthday();
                break;
        }


    }


    private void showTipDialog() {
        final AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("用户账号不可修改！");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();

    }

    private  void  modifyNick(){
        LayoutInflater factory=LayoutInflater.from(this);
        final View v=factory.inflate(R.layout.dialog_modify_nick,null);
       AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("修改昵称");
        builder.setView(v);
        et_modify_nick=(EditText)v.findViewById(R.id.et_dialog);
        iv_clear_text=(ImageView) v.findViewById(R.id.iv_clear_all_text);
        et_modify_nick.setText(tv_nick.getText());
        et_modify_nick.setSelection(tv_nick.length());//让编辑框光标移到文本最后
        iv_clear_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_modify_nick.setText("");
                iv_clear_text.setVisibility(View.INVISIBLE);
            }
        });
        builder.setPositiveButton("确认修改", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                    userNick=et_modify_nick.getText().toString().trim();
                    ProtoClass.User.Builder user = ProtoClass.User.newBuilder();
                    user.setUserID(userDetailInfo.getuID());
                    user.setNickName(userNick);
                    ProtoClass.Attrtype.Builder attrType= ProtoClass.Attrtype.newBuilder();
                    attrType.setNick(true);
                    ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                    builder.setUser(user)
                            .setAttrType(attrType)
                            .setMsgType(ProtoClass.MsgType.MODIFY_USER_INFOR);

                    Tools.startTcpRequest(builder, new Tools.TcpListener() {
                        @Override
                        public void onSendFail(Exception e) {
                            Utils.showToast2(ManageModifyUserInfoActivity.this, "请求发送失败");
                        }

                        //第二个参数是服务器返回的响应消息
                        @Override
                        public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                            modifyUserInfoListener.onProcess(responseMsg);
                            return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
                        }
                    });

                }

        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    //修改性别
    private void modifySex() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("选择性别");
        final String[] sex={"男","女","其他"};
        int checkedItem=0;
        if(tv_sex.getText().equals("男")) {
           checkedItem =0;
        }else if(tv_sex.getText().equals("女")){
            checkedItem=1;
        }else{
            checkedItem=2;
        }



        userdetail= ProtoClass.UserDetail.newBuilder();
        userdetail.setUID(userDetailInfo.getuID());
        builder.setSingleChoiceItems(sex, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userdetail.setSex(sex[which]);
                userSex=sex[which];
            }
        });

        builder.setPositiveButton("确定修改", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                ProtoClass.User.Builder user= ProtoClass.User.newBuilder();
                user.setUserID(userDetailInfo.getuID());
                user.setUserDetail(userdetail);
                ProtoClass.Attrtype.Builder attrType= ProtoClass.Attrtype.newBuilder();
                attrType.setSex(true);
                ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                builder.setUser(user)
                        .setAttrType(attrType)
                        .setMsgType(ProtoClass.MsgType.MODIFY_USER_INFOR);

                Tools.startTcpRequest(builder, new Tools.TcpListener() {
                    @Override
                    public void onSendFail(Exception e) {
                        Utils.showToast2(ManageModifyUserInfoActivity.this, "请求发送失败");
                    }


                    @Override
                    public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                        modifyUserInfoListener.onProcess(responseMsg);
                        return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
                    }
                });
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                  dialog.dismiss();
            }
        });
        builder.create().show();
    }


    private  void  modifyQQ(){
        LayoutInflater factory=LayoutInflater.from(this);
        final View v=factory.inflate(R.layout.dialog_modify_qq,null);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("修改qq");
        builder.setView(v);
        et_modify_qq=(EditText)v.findViewById(R.id.et_dialog_modify_qq);
        iv_clear_text=(ImageView) v.findViewById(R.id.iv_clear_all_text);
        et_modify_qq.setText(tv_qq.getText());
        et_modify_qq.setSelection(et_modify_qq.length());
        iv_clear_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_modify_qq.setText("");
                iv_clear_text.setVisibility(View.INVISIBLE);
            }
        });
        userdetail= ProtoClass.UserDetail.newBuilder();
        userdetail.setUID(userDetailInfo.getuID());

        builder.setPositiveButton("确认修改", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                userqq=et_modify_qq.getText().toString().trim();
                if(userqq.length()<5&&userqq.length()!=0){
                    Utils.showToast2(ManageModifyUserInfoActivity.this,"qq号格式错误！");
                    userqq=null;
                    return;
                }
                userdetail.setQQ(userqq);
                ProtoClass.Attrtype.Builder attrType= ProtoClass.Attrtype.newBuilder();
                attrType.setQq(true);
                ProtoClass.User.Builder user = ProtoClass.User.newBuilder();
                user.setUserID(userDetailInfo.getuID());
                user.setUserDetail(userdetail);
                ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                builder.setUser(user)
                        .setAttrType(attrType)
                        .setMsgType(ProtoClass.MsgType.MODIFY_USER_INFOR);

                Tools.startTcpRequest(builder, new Tools.TcpListener() {
                    @Override
                    public void onSendFail(Exception e) {
                        Utils.showToast2(ManageModifyUserInfoActivity.this, "请求发送失败");
                    }

                    //第二个参数是服务器返回的响应消息
                    @Override
                    public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                        modifyUserInfoListener.onProcess(responseMsg);
                        return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
                    }
                });

            }

        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }


    private  void  modifyPhone(){
        LayoutInflater factory=LayoutInflater.from(this);
        final View v=factory.inflate(R.layout.dialog_modify_phone,null);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("修改手机号");
        builder.setView(v);
        et_modify_phone=(EditText)v.findViewById(R.id.et_dialog_modify_phone);
        iv_clear_text=(ImageView) v.findViewById(R.id.iv_clear_all_text);
        et_modify_phone.setText(tv_phone.getText());
        et_modify_phone.setSelection(et_modify_phone.length());
        iv_clear_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_modify_phone.setText("");
                iv_clear_text.setVisibility(View.INVISIBLE);
            }
        });
        userdetail= ProtoClass.UserDetail.newBuilder();
        userdetail.setUID(userDetailInfo.getuID());

        builder.setPositiveButton("确认修改", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                userphone=et_modify_phone.getText().toString().trim();
                if( userphone.length()!=0&&userphone.length()<11){
                    Utils.showToast2(ManageModifyUserInfoActivity.this,"手机号必须为11位！");
                    userphone=null;
                    return;
                }
                userdetail.setPhone(userphone);
                ProtoClass.Attrtype.Builder attrType= ProtoClass.Attrtype.newBuilder();
                attrType.setPhone(true);
                ProtoClass.User.Builder user = ProtoClass.User.newBuilder();
                user.setUserID(userDetailInfo.getuID());
                user.setUserDetail(userdetail);
                ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                builder.setUser(user)
                        .setAttrType(attrType)
                        .setMsgType(ProtoClass.MsgType.MODIFY_USER_INFOR);

                Tools.startTcpRequest(builder, new Tools.TcpListener() {
                    @Override
                    public void onSendFail(Exception e) {
                        Utils.showToast2(ManageModifyUserInfoActivity.this, "请求发送失败");
                    }

                    //第二个参数是服务器返回的响应消息
                    @Override
                    public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                        modifyUserInfoListener.onProcess(responseMsg);
                        return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
                    }
                });

            }

        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }
    private  void  modifyWeChat(){
        LayoutInflater factory=LayoutInflater.from(this);
        final View v=factory.inflate(R.layout.dialog_modify_wechat,null);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("修改微信号");
        builder.setView(v);
        et_modify_wechat=(EditText)v.findViewById(R.id.et_dialog_modify_wechat);
        iv_clear_text=(ImageView) v.findViewById(R.id.iv_clear_all_text);
        et_modify_wechat.setText(tv_wechat.getText());
        et_modify_wechat.setSelection(et_modify_wechat.length());
        iv_clear_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_modify_wechat.setText("");
                iv_clear_text.setVisibility(View.INVISIBLE);
            }
        });
        userdetail= ProtoClass.UserDetail.newBuilder();
        userdetail.setUID(userDetailInfo.getuID());

        builder.setPositiveButton("确认修改", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                userwechat=et_modify_wechat.getText().toString().trim();
                userdetail.setWechat(userwechat);
                ProtoClass.Attrtype.Builder attrType= ProtoClass.Attrtype.newBuilder();
                attrType.setWechat(true);
                ProtoClass.User.Builder user = ProtoClass.User.newBuilder();
                user.setUserID(userDetailInfo.getuID());
                user.setUserDetail(userdetail);
                ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                builder.setUser(user)
                        .setAttrType(attrType)
                        .setMsgType(ProtoClass.MsgType.MODIFY_USER_INFOR);

                Tools.startTcpRequest(builder, new Tools.TcpListener() {
                    @Override
                    public void onSendFail(Exception e) {
                        Utils.showToast2(ManageModifyUserInfoActivity.this, "请求发送失败");
                    }

                    //第二个参数是服务器返回的响应消息
                    @Override
                    public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                        modifyUserInfoListener.onProcess(responseMsg);
                        return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
                    }
                });

            }

        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }


    private  void  modifyAddress(){
        LayoutInflater factory=LayoutInflater.from(this);
        final View v=factory.inflate(R.layout.dialog_modify_address,null);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("修改地址");
        builder.setView(v);
        et_modify_address=(EditText)v.findViewById(R.id.et_dialog_modify_address);
        iv_clear_text=(ImageView) v.findViewById(R.id.iv_clear_all_text);
        et_modify_address.setText(tv_address.getText());
        et_modify_address.setSelection(et_modify_address.length());
        iv_clear_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_modify_address.setText("");
                iv_clear_text.setVisibility(View.INVISIBLE);
            }
        });
        userdetail= ProtoClass.UserDetail.newBuilder();
        userdetail.setUID(userDetailInfo.getuID());

        builder.setPositiveButton("确认修改", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                useraddress=et_modify_address.getText().toString().trim();
                userdetail.setAddress(useraddress);
                ProtoClass.Attrtype.Builder attrType= ProtoClass.Attrtype.newBuilder();
                attrType.setAddress(true);
                ProtoClass.User.Builder user = ProtoClass.User.newBuilder();
                user.setUserID(userDetailInfo.getuID());
                user.setUserDetail(userdetail);
                ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                builder.setUser(user)
                        .setAttrType(attrType)
                        .setMsgType(ProtoClass.MsgType.MODIFY_USER_INFOR);

                Tools.startTcpRequest(builder, new Tools.TcpListener() {
                    @Override
                    public void onSendFail(Exception e) {
                        Utils.showToast2(ManageModifyUserInfoActivity.this, "请求发送失败");
                    }

                    //第二个参数是服务器返回的响应消息
                    @Override
                    public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                        modifyUserInfoListener.onProcess(responseMsg);
                        return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
                    }
                });

            }

        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private  void  modifyMail(){
        LayoutInflater factory=LayoutInflater.from(this);
        final View v=factory.inflate(R.layout.dialog_modify_mail,null);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("修改邮箱");
        builder.setView(v);
        et_modify_mail=(EditText)v.findViewById(R.id.et_dialog_modify_mail);
        iv_clear_text=(ImageView) v.findViewById(R.id.iv_clear_all_text);
        et_modify_mail.setText(tv_mail.getText());
        et_modify_mail.setSelection(et_modify_mail.length());
        iv_clear_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_modify_mail.setText("");
                iv_clear_text.setVisibility(View.INVISIBLE);
            }
        });
        userdetail= ProtoClass.UserDetail.newBuilder();
        userdetail.setUID(userDetailInfo.getuID());

        builder.setPositiveButton("确认修改", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                usermail=et_modify_mail.getText().toString().trim();
                if(!checkEmail(usermail)&&usermail.length()!=0){
                    Utils.showToast2(ManageModifyUserInfoActivity.this,"邮箱格式错误！");
                    usermail=null;
                    return;
                };
                userdetail.setMail(usermail);
                ProtoClass.Attrtype.Builder attrType= ProtoClass.Attrtype.newBuilder();
                attrType.setMail(true);
                ProtoClass.User.Builder user = ProtoClass.User.newBuilder();
                user.setUserID(userDetailInfo.getuID());
                user.setUserDetail(userdetail);
                ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                builder.setUser(user)
                        .setAttrType(attrType)
                        .setMsgType(ProtoClass.MsgType.MODIFY_USER_INFOR);

                Tools.startTcpRequest(builder, new Tools.TcpListener() {
                    @Override
                    public void onSendFail(Exception e) {
                        Utils.showToast2(ManageModifyUserInfoActivity.this, "请求发送失败");
                    }

                    //第二个参数是服务器返回的响应消息
                    @Override
                    public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                        modifyUserInfoListener.onProcess(responseMsg);
                        return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
                    }
                });

            }

        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private  void  modifyBirthday(){
        LayoutInflater factory=LayoutInflater.from(this);
        final View v=factory.inflate(R.layout.dialog_choose_date,null);
        calendar = Calendar.getInstance();
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("修改生日");
        builder.setView(v);
        dp_modify_birthday=(DatePicker)v.findViewById(R.id.dialog_modify_birthday);

        String birthdayString=tv_birthday.getText().toString().trim();
        int year=calendar.get(Calendar.YEAR);     //若原来的生日为空则设置对话框显示的初始日期为当前日期
        int  month=calendar.get(Calendar.MONTH);
        int dayOfMonth=calendar.get(Calendar.DAY_OF_MONTH);
        if(birthdayString.length()>0){    //设置对话框显示的初始日期为原来的生日
           String[] items=birthdayString.split("-");
            year= Integer.parseInt(items[0]);
            month=Integer.parseInt(items[1]);
            dayOfMonth=Integer.parseInt(items[2]);
        }
        dp_modify_birthday.setMaxDate(new Date().getTime());
        dp_modify_birthday.init(year, month-1, dayOfMonth, new DatePicker.OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                // 获取一个日历对象，并初始化为当前选中的时间

                calendar.set(year, monthOfYear, dayOfMonth);
               format = new SimpleDateFormat("yyyy-MM-dd");

            }
        });
        userdetail= ProtoClass.UserDetail.newBuilder();
        userdetail.setUID(userDetailInfo.getuID());

        builder.setPositiveButton("确认修改", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                birthday= format.format(calendar.getTime());
                userdetail.setBirthday(birthday);
                ProtoClass.Attrtype.Builder attrType= ProtoClass.Attrtype.newBuilder();
                attrType.setBirthday(true);
                ProtoClass.User.Builder user = ProtoClass.User.newBuilder();
                user.setUserID(userDetailInfo.getuID());
                user.setUserDetail(userdetail);
                ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                builder.setUser(user)
                        .setAttrType(attrType)
                        .setMsgType(ProtoClass.MsgType.MODIFY_USER_INFOR);

                Tools.startTcpRequest(builder, new Tools.TcpListener() {
                    @Override
                    public void onSendFail(Exception e) {
                        Utils.showToast2(ManageModifyUserInfoActivity.this, "请求发送失败");
                    }

                    //第二个参数是服务器返回的响应消息
                    @Override
                    public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                        modifyUserInfoListener.onProcess(responseMsg);
                        return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
                    }
                });

            }

        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }


    private boolean checkEmail(String email){
        boolean flag = false;
        try{
            String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(email);
            flag = matcher.matches();
        }catch(Exception e){

            flag = false;
        }

        return flag;
    }
    @Override
    public void successModifyUserInfor() {
        if(userSex!=null) tv_sex.setText(userSex);
        if(userNick!=null) tv_nick.setText(userNick);
        if(userphone!=null) tv_phone.setText(userphone);
        if (userqq!=null) tv_qq.setText(userqq);
        if(userwechat!=null) tv_wechat.setText(userwechat);
        if(useraddress!=null) tv_address.setText(useraddress);
        if (usermail!=null)  tv_mail.setText(usermail);
        if(birthday!=null)   tv_birthday.setText(birthday);
         Utils.showToast2(this,"修改成功");

    }

    @Override
    public void failedModifyUserInfor(String tipMsg) {
       Utils.showToast2(this,tipMsg);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

        }
        return true;
    }
}
