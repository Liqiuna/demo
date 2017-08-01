package com.cst14.im.layoutView;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;

public class MyScrollView extends ScrollView {

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setData(View title, int titleHeight) {
        mTitleHeight = titleHeight;
        mTvTitle = title;
    }

    private View mTvTitle;
    private int mTitleHeight;

    @Override
    protected void onScrollChanged(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        float flactor = (float) scrollY / (mTitleHeight * 1.5f);
        flactor = flactor > 1 ? 1 : flactor;
        mTvTitle.setAlpha(1 - flactor);
    }
}