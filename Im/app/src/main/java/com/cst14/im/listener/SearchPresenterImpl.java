package com.cst14.im.listener;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import com.cst14.im.R;
import com.cst14.im.activity.SearchActivity;
import com.cst14.im.activity.SearchConditionActivity;
import com.cst14.im.activity.SearchGroupResultActivity;
import com.cst14.im.activity.SearchResultActivity;
import com.cst14.im.baseClass.ISearchPresenter;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.LocationUtil;

/**
 * Created by hz on 2016/8/24.
 */

public class SearchPresenterImpl implements ISearchPresenter{

    private ISearchView mView;
    private SearchActivity activity;

    private ProtoClass.MsgType opt;

    public SearchPresenterImpl(ISearchView view) {
        this.mView = view;
        activity = (SearchActivity)view;
        LocationUtil.init(activity);
    }

    @Override
    public void onProcess(ProtoClass.Msg msg) {

    }

    @Override
    public void initToolbar() {
        mView.initToolbar("查找");
    }

    @Override
    public void onViewClick(View view) {
        int viewId = view.getId();
        switch(viewId){
            case R.id.search_edit_view:
                opt = ProtoClass.MsgType.SEEK_NAME;
                mView.setInputEditHint("输入用户名");
                mView.showInputLayout();
                mView.setTextInputEdit();
                break;
            case R.id.search_nearly_layout:
                opt = ProtoClass.MsgType.GET_NEARBY;
                if ( !LocationUtil.gPSIsOPen(activity)){
                    mView.showCustomToast("无法定位，请前往设置打开定位功能");
                    break;
                }
                clickSearchBtn();
                break;
            case R.id.search_attrb_layout:
                Intent intent = new Intent(activity, SearchConditionActivity.class);
                activity.startActivity(intent);
                break;
            case R.id.search_group_layout:
                opt = ProtoClass.MsgType.SEARCH_GROUP;
                mView.setInputEditHint("输入群号");
                mView.setDigitalEdit();
                mView.showInputLayout();
                break;
            case R.id.commit_name_search:
                clickSearchBtn();
                break;
            case R.id.exit_input_layout:
                mView.hideInputLayout();
                mView.hideKeyboard();
                break;
        }
    }

    private void clickSearchBtn() {
        if (opt != ProtoClass.MsgType.GET_NEARBY && TextUtils.equals("",mView.getInputEditText())){
            return;
        }
        final ProtoClass.SearchInfo searchInfo = collectSearchInfo();

        Intent intent;
        if (opt == ProtoClass.MsgType.SEEK_NAME || opt == ProtoClass.MsgType.GET_NEARBY){
            intent= new Intent(activity, SearchResultActivity.class);
        }else {
            intent = new Intent(activity, SearchGroupResultActivity.class);
        }

        //将用户的查询信息传递到下一个activity
        intent.putExtra("search_info",searchInfo);
        activity.startActivity(intent);
    }

    private ProtoClass.SearchInfo collectSearchInfo() {
        ProtoClass.SearchInfo.Builder infoBuilder = ProtoClass.SearchInfo.newBuilder()
                .setSearchType(opt)
                .setSrchName(mView.getInputEditText());
        //设置群号
        if (opt == ProtoClass.MsgType.SEARCH_GROUP){
            String groupNo = mView.getInputEditText();
            try {
                int num = Integer.parseInt(groupNo);
                infoBuilder.setGroupNO(num);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        //定位参数
        if (opt == ProtoClass.MsgType.GET_NEARBY) {
            String curUsername = ImApplication.instance.getCurUser().getName();
            infoBuilder.setSrchName(curUsername);
            infoBuilder.setLat(LocationUtil.latitude);
            infoBuilder.setLng(LocationUtil.longitude);
        }
        return infoBuilder.build();
    }
    @Override
    public void onDestroy() {

    }

    public interface ISearchView {
        void initToolbar(String title);
        void showInputLayout();
        void hideInputLayout();
        void setInputEditHint(String hint);
        String getInputEditText();
        void hideKeyboard();
        void setDigitalEdit();
        void setTextInputEdit();
        void showCustomToast(String tip);
    }
}
