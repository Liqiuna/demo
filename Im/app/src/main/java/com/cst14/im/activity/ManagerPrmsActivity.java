package com.cst14.im.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.cst14.im.R;

public class ManagerPrmsActivity extends AppCompatActivity implements View.OnClickListener {
    private RelativeLayout managerPrms_rgst;
    private RelativeLayout managerPrms_login;
    private RelativeLayout managerPrms_userRequired;
    private RelativeLayout managerPrms_groupLimit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_prms);
        iniView();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void iniView(){
        managerPrms_rgst = (RelativeLayout) findViewById(R.id.managerPrms_rgst);
        managerPrms_rgst.setOnClickListener((View.OnClickListener)this);
        managerPrms_login = (RelativeLayout) findViewById(R.id.managerPrms_login);
        managerPrms_login.setOnClickListener((View.OnClickListener)this);
        managerPrms_userRequired = (RelativeLayout) findViewById(R.id.managerPrms_user_required);
        managerPrms_userRequired.setOnClickListener((View.OnClickListener)this);
        managerPrms_groupLimit = (RelativeLayout) findViewById(R.id.managerPrms_group_limit);
        managerPrms_groupLimit.setOnClickListener((View.OnClickListener)this);
        setTitle("设置");

    }

    public void onClick(View view){
        switch(view.getId()){
            case R.id.managerPrms_rgst:
                startActivity(new Intent(ManagerPrmsActivity.this, RgstPermissionActivity.class));
                break;
            case R.id.managerPrms_login:
                startActivity(new Intent(ManagerPrmsActivity.this, LoginPermissionActivity.class));
                break;
            case R.id.managerPrms_user_required:
                startActivity(new Intent(ManagerPrmsActivity.this, UserRequiredPrmsActivity.class));
                break;
            case R.id.managerPrms_modify_user_info:
                startActivity(new Intent(ManagerPrmsActivity.this, SearchUserDetailsActivity.class));
                break;
            case R.id.managerPrms_group_limit:
                startActivity(new Intent(ManagerPrmsActivity.this, GroupLimitPrmsActivity.class));
                break;
        }
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
