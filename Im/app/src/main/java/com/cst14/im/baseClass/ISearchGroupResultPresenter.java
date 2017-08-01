package com.cst14.im.baseClass;

import android.view.View;

/**
 * Created by hz on 2016/8/31.
 */

public interface ISearchGroupResultPresenter extends iPresenter {
    void init();
    void onClick(View v);
    void sendMsg();
    void onDestroy();
}
