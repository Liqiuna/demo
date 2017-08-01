package com.cst14.im.baseClass;

import com.cst14.im.protobuf.ProtoClass;

import java.util.List;

public interface iGroupView {
    void showGetGroupListFail();
    void showGetGroupListSucceed(List<ProtoClass.GroupInfo> groupInfos);
}
