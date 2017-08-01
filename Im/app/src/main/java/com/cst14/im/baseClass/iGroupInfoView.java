package com.cst14.im.baseClass;

import com.cst14.im.bean.UserGroup;

public interface iGroupInfoView {
    void showGetInfoFail();

    void showGetInfoSucceed(UserGroup userGroup);

    void showJoinGroupSucceed();

    void showJoinGroupFail();

    void showSendJoinRequest();

    void showExitGroupSucceed();

    void showRemoveGroupMemberSucceed();

    void showDelGroupSucceed();

    void showRequestFail();

    void showInviteMemberToJoinSucceed();

    void showModifyGroupInfoFail(int type);

    void showSetAdminSucceed();

    void showRemoveAdminSucceed();
}
