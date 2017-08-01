package com.cst14.im.activity;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.cst14.im.R;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.Utils;

public class SecurityCenterActivity extends AppCompatActivity implements View.OnClickListener{

    private LinearLayout findPwd_ll;
    private ActionBar actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_center);
        initToolbar();
        findPwd_ll = (LinearLayout)findViewById(R.id.find_pwd_ll);
        findPwd_ll.setOnClickListener(this);

    }
    private void initToolbar(){
        actionBar = getSupportActionBar();
        if (actionBar == null) return;

        actionBar.setTitle("安全中心");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDefaultDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.find_pwd_ll:
                Intent intentGet = getIntent();
                String email = intentGet.getStringExtra("emailBind");
                Intent intent = new Intent();
                intent.setClass(SecurityCenterActivity.this,SecurityFindPwdActivity.class);
                intent.putExtra("emailBindC",email);
                startActivity(intent);
                finish();
                break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
