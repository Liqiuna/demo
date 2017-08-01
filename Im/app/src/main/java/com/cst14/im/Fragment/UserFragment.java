package com.cst14.im.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cst14.im.R;
import com.cst14.im.activity.ChangePwdActivity;

import com.cst14.im.activity.GameActivity;
import com.cst14.im.activity.NearbyActivity;
import com.cst14.im.activity.NearbyTipSettingAty;
import com.cst14.im.activity.UserInfoActivity;
import com.cst14.im.adapter.userfragementAdapter;
import com.cst14.im.utils.ImApplication;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 第三个界面的fagment
 */
public class UserFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "UserFragment";

    private RelativeLayout mRLMyInfo;
    private RelativeLayout mRLGame;
    private RelativeLayout mRLNearby;
    private RelativeLayout mRLNearbySetting;
    private RelativeLayout mRLAutoLoginSettring;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        //findView
        mRLMyInfo = (RelativeLayout) view.findViewById(R.id.rl_fragment_user_myinfo);
        mRLGame = (RelativeLayout) view.findViewById(R.id.rl_fragment_user_game);
        mRLNearby = (RelativeLayout) view.findViewById(R.id.rl_fragment_user_nearby);
        mRLNearbySetting = (RelativeLayout) view.findViewById(R.id.rl_fragment_user_nearby_setting);
        mRLAutoLoginSettring= (RelativeLayout) view.findViewById(R.id.rl_fragment_user_auto_setting);
        //setOnClickListener
        mRLNearbySetting.setOnClickListener(this);
        mRLNearby.setOnClickListener(this);
        mRLGame.setOnClickListener(this);
        mRLMyInfo.setOnClickListener(this);
        mRLAutoLoginSettring.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        int viewID = v.getId();
        switch (viewID) {
            case R.id.rl_fragment_user_game:
                startActivity(new Intent(getActivity(), GameActivity.class));
                break;

            case R.id.rl_fragment_user_myinfo:
                startActivity(new Intent(getActivity(), UserInfoActivity.class));
                break;

            case R.id.rl_fragment_user_nearby:
                startActivity(new Intent(getActivity(), NearbyActivity.class));
                break;

            case R.id.rl_fragment_user_nearby_setting:
                startActivity(new Intent(getActivity(), NearbyTipSettingAty.class));
                break;
            case R.id.rl_fragment_user_auto_setting:
                checkIsAuto();
                break;
        }
    }
    public void checkIsAuto(){
        boolean autoIstrue=false,pwbIstrue=false;
        //选项数组
        if(ImApplication.getSp().getBoolean("AUTO_ISCHECK", false)==true){
            autoIstrue=true;
        }else{
            autoIstrue=false;
        }
        if(ImApplication.getSp().getBoolean("ISCHECK", false)==true){
            pwbIstrue=true;
        }else{
            pwbIstrue=false;
        }
        String[] choices={"自动登录","记住密码"};
        //Check判断数组，与选项对应
        boolean[] chsBool = {autoIstrue,pwbIstrue};
        //包含多个选项及复选框的对话框

        AlertDialog.Builder dialog = new AlertDialog.Builder(this.getContext())
                .setIcon(android.R.drawable.btn_star_big_on)
                .setTitle("设置")
               .setMultiChoiceItems(choices,chsBool,autoClick);
        dialog.show();
    }

    DialogInterface.OnMultiChoiceClickListener autoClick = new DialogInterface.OnMultiChoiceClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
               if(which==0) {
                   if (isChecked == true) {
                       ImApplication.getSp().edit().putBoolean("AUTO_ISCHECK", true).commit();
                   } else {
                       ImApplication.getSp().edit().putBoolean("AUTO_ISCHECK", false).commit();
                   }
               }
                   if(which==1){
                       if (isChecked == true) {
                           ImApplication.getSp().edit().putBoolean("ISCHECK", true).commit();
                       } else {
                           ImApplication.getSp().edit().putBoolean("ISCHECK", false).commit();
                       }
                   }

        }

    };
}
