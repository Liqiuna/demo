package com.cst14.im.baseClass;

import com.cst14.im.protobuf.ProtoClass;

import java.util.List;

public interface iGroupSearchView {
    void showSearchResult(List<ProtoClass.GroupInfo> groupInfos);

    void showNoReasult();

    void showEmptyInput();

    void showInvalidInput();

}
