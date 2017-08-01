package com.cst14.im.baseActionView;

import android.content.Context;
import android.widget.GridView;

import com.cst14.im.adapter.myVpAdapter;

/**
 * Created by MRLWJ on 2016/7/24.
 */
public abstract class GridViewCommon extends GridView{
    protected int density;
    public GridViewCommon(Context context) {
        super(context);
        density = (int) context.getResources().getDisplayMetrics().density;
        setPadding(3*density,0,3*density,0);
    }
    abstract public void  setPageIndex(int pageIndex);
    abstract public void dispatchListener(myVpAdapter.OnMyVPClickListener listener);
}
