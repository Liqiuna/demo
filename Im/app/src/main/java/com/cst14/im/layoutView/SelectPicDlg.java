package com.cst14.im.layoutView;

import android.app.Activity;
import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.utils.ImApplication;

/**
 * Created by MRLWJ on 2016/8/18.
 */
public class SelectPicDlg extends LinearLayout {
    private Activity activity;
    private TextView title,yuantu,biaoqing,positive,negative;
    private ImageView ivYuantuChoose,ivBiaoqingChoose;
    private RelativeLayout rlYuantu,rlBiaoqing;
    private boolean isBiaoqingChoose;
    public SelectPicDlg(Context context) {
        super(context);
        activity = (Activity) context;
        View.inflate(context, R.layout.dialog_custom,this);
        title = (TextView) findViewById(R.id.tv_dlg_title);
        title.setText("请选择图片尺寸");

        ivBiaoqingChoose = (ImageView) findViewById(R.id.iv_biaoqing_choose);
        ivYuantuChoose = (ImageView) findViewById(R.id.iv_yuantu_choose);

        rlBiaoqing = (RelativeLayout) findViewById(R.id.rl_dlg_biaoqing_container);
        rlBiaoqing.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ivBiaoqingChoose.setVisibility(View.VISIBLE);
                ivYuantuChoose.setVisibility(View.GONE);
                isBiaoqingChoose = true;
            }
        });
        rlYuantu = (RelativeLayout) findViewById(R.id.rl_dlg_yuantu_container);
        rlYuantu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ivBiaoqingChoose.setVisibility(View.GONE);
                ivYuantuChoose.setVisibility(View.VISIBLE);
                isBiaoqingChoose = false;
            }
        });


        biaoqing = (TextView) findViewById(R.id.tv_dlg_biaoqing);
        yuantu = (TextView) findViewById(R.id.tv_dlg_yuantu);

        positive = (TextView) findViewById(R.id.tv_dlg_postive);
        positive.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onPositiveListener!=null){
                    onPositiveListener.onClick(isBiaoqingChoose);
                }
                dismiss();
            }
        });
        negative = (TextView) findViewById(R.id.tv_dlg_negative);
        negative.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setLayoutParams(params);
    }

    public void showYuanTuSize(long size){
        yuantu.setText("原图（"+Formatter.formatFileSize(activity,size)+"）");
    }
    public void showBiaoQingSize(long size){
        biaoqing.setText("标清（约"+Formatter.formatFileSize(activity,size)+"）");
    }
    public interface OnPositiveListener{
        void onClick(boolean isBiaoqingChoose);
    }
    public void setOnPositiveListener( OnPositiveListener onPositiveListener) {
        this.onPositiveListener = onPositiveListener;
    }
    private OnPositiveListener onPositiveListener;

    private void dismiss(){
        ImApplication.instance.getCurSessionHolder().chatActivity.rlRootView.removeView(this);
    }
}
