package com.cst14.im.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.adapter.NearbyAdapter;
import com.cst14.im.protobuf.ProtoClass;

import java.util.Locale;

/**
 * Created by Administrator on 2016/9/12 0012.
 */
public class NearbyTipDialog extends Dialog {

    private ProtoClass.Nearby curNearby;

    private TextView tv_nick;
    private TextView tv_age_man;
    private TextView tv_age_woman;
    private TextView tv_intro;
    private TextView tv_distance;
    private TextView tv_recentTime;

    public NearbyTipDialog(Context ctx) {
        super(ctx, R.style.nearby_tip_dialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_nearby_tip);

        initView();
    }

    private void initView() {
        tv_nick = (TextView) findViewById(R.id.tv_nick);
        tv_age_man = (TextView) findViewById(R.id.tv_age_man);
        tv_age_woman = (TextView) findViewById(R.id.tv_age_woman);
        tv_intro = (TextView) findViewById(R.id.tv_intro);
        tv_distance = (TextView) findViewById(R.id.tv_distance);
        tv_recentTime = (TextView) findViewById(R.id.tv_recentTime);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (curNearby == null) return;

        tv_nick.setText(curNearby.getNick());
        tv_intro.setText(curNearby.getIntro());
        tv_distance.setText(String.format(Locale.CHINA, "%.3fkm", curNearby.getDistance()));
        tv_recentTime.setText(NearbyAdapter.RelativeDateFormat.format(curNearby.getUpdateTime()));

        tv_age_woman.setVisibility(View.INVISIBLE);
        tv_age_man.setVisibility(View.INVISIBLE);
        if ("男".equals(curNearby.getSex())) {
            tv_age_man.setVisibility(View.VISIBLE);
            tv_age_man.setText(String.valueOf(curNearby.getAge()));
        } else {
            tv_age_woman.setVisibility(View.VISIBLE);
            tv_age_woman.setText(String.valueOf(curNearby.getAge()));
        }
    }

    public void updateNearby(ProtoClass.Nearby nearby) {
        if (nearby != null) {
            curNearby = nearby;
        }
    }

    public void setDialogOnClickListener(View.OnClickListener listener) {
        getWindow().getDecorView().setOnClickListener(listener);
    }

    public void callOnCreated() {
        onCreate(null);
    }
}
