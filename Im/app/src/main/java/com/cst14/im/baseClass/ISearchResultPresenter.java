package com.cst14.im.baseClass;

import com.cst14.im.protobuf.ProtoClass;

/**
 * Created by hz on 2016/8/24.
 */

public interface ISearchResultPresenter extends iPresenter{
    void initToolbar();
    void initViews();
    void success(ProtoClass.Msg msg);
    void fail();
    void onDestroy();
    void clickUserListItem(int pos);
    void scrollToLastItem();
    void sendMsg();
}
