package com.cst14.im.baseClass;

import android.view.View;

import com.cst14.im.protobuf.ProtoClass;

/**
 * Created by tying on 2016/9/10.
 */

public interface ISecurityPresenter extends iPresenter{
    void initToolbar();
    void onViewClick(View view);
    void onDestroy();
}
