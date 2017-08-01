package com.cst14.im.baseClass;

import android.view.View;

/**
 * Created by tying on 2016/9/19.
 * change pwd by origin pwd
 */

public interface IChangePwdOriginPresenter extends iPresenter{
    void initToolbar();
    void onViewClick(View view);
    void onDestroy();
}
