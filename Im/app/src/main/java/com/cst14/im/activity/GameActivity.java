package com.cst14.im.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.cst14.im.R;

public class GameActivity extends AppCompatActivity implements View.OnClickListener{
    LinearLayout view_flappy_bird,view_fruit_ninja;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        initControl();
        setListener();
    }

    private void setListener() {
        view_flappy_bird.setOnClickListener(this);
        view_fruit_ninja.setOnClickListener(this);
    }

    private void initControl() {
        view_flappy_bird=(LinearLayout)findViewById(R.id.view_flappy_bird);
        view_fruit_ninja=(LinearLayout)findViewById(R.id.view_fruit_ninja);
    }

    @Override
    public void onClick(View v) {
        Intent intent=new Intent();
        intent.setClass(GameActivity.this,WebViewActivity.class);
        switch (v.getId()){
            case R.id.view_fruit_ninja:
                intent.putExtra("game","fruit_ninja");
                break;
            case R.id.view_flappy_bird:
                intent.putExtra("game","flappy_bird");
                break;
        }
        startActivity(intent);
    }
}
