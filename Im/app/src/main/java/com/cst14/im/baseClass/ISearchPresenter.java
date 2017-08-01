package com.cst14.im.baseClass;

import android.view.View;

/**
 * Created by hz on 2016/8/24.
 */

public interface ISearchPresenter extends iPresenter {
    void initToolbar();
    void onViewClick(View view);
    void onDestroy();
}
