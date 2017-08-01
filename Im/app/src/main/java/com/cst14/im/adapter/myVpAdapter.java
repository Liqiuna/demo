package com.cst14.im.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.cst14.im.baseActionView.GridViewCommon;
import com.cst14.im.layoutView.EmoGridView;
import com.cst14.im.layoutView.FuncGridView;

/**
 * Created by MRLWJ on 2016/7/12.
 */
public class myVpAdapter extends PagerAdapter {

    private int gvType;
    public static final int EMO = 0;
    public static final int FUNC = 1;
    @Override
    public int getCount() {
        return gvType==EMO?2:1;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        GridViewCommon gv ;
        if(gvType == EMO){
            gv = new EmoGridView(container.getContext());
        }else{
            gv = new FuncGridView(container.getContext());
        }
        gv.dispatchListener(myVPClickListener);
        gv.setPageIndex(position);
        container.addView(gv);
        return gv;
    }
    public void setGVType(int type){
        gvType = type;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public interface OnMyVPClickListener {
        void onClick(int id, String strName);
    }
    private OnMyVPClickListener myVPClickListener;

    public void setOnMyVPClickListener(OnMyVPClickListener myVPClickListener){
        this.myVPClickListener = myVPClickListener;
    }
    public OnMyVPClickListener getOnEmoClickListener(){
        return myVPClickListener;
    }
}
