package com.cst14.im.baseClass;

import android.view.View;

/**
 * Created by hz on 2016/8/26.
 */

public interface ISearchConditionPresenter extends iPresenter {
    void initToolbar();
    void initView();
    void initEvent();
    void onClick(View view);
    void setWindowSize(int width,int height);
    void onDestroy();
}
