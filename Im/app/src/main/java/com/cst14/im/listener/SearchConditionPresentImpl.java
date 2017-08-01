package com.cst14.im.listener;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.cst14.im.R;
import com.cst14.im.activity.SearchConditionActivity;
import com.cst14.im.activity.SearchResultActivity;
import com.cst14.im.adapter.ProvinceAdapter;
import com.cst14.im.baseClass.ISearchConditionPresenter;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.AddressData;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

/**
 * Created by hz on 2016/8/26.
 */

public class SearchConditionPresentImpl implements ISearchConditionPresenter{
    private ISearchConditionView mView;
    private SearchConditionActivity activity;

    private int windowWidth;
    private int windowHeight;
    //年龄范围
    private int[] ageLow = {0,0,18,23,27,36};
    private int[] ageHigh = {0,18,22,26,35,60};
    private int whichAgeRange = 0;

    public SearchConditionPresentImpl(ISearchConditionView mView) {
        this.mView = mView;
        activity = (SearchConditionActivity) mView;
    }

    @Override
    public void onProcess(ProtoClass.Msg msg) {

    }

    @Override
    public void initToolbar() {
        mView.initToolbar("按条件查找");
    }

    @Override
    public void initView() {

    }

    @Override
    public void initEvent() {
        mView.initEvent();
    }

    @Override
    public void onClick(View view) {

        int viewId = view.getId();
        switch (viewId){
            case R.id.search_online_layout:
                showOnlineDialog();
                break;
            case R.id.search_sex_layout:
                showSexDialog();
                break;
            case R.id.search_age_layout:
                showAgeDialog();
                break;
            case R.id.search_addr_layout:
                PopupWindow window = makeAddressPopwin();
                int[] xy = new int[2];
                mView.getWindowLayout().getLocationOnScreen(xy);
                window.showAtLocation(mView.getWindowLayout(), Gravity.BOTTOM,0,-windowHeight);
                break;
            case R.id.search_btn:
                clickSearchBnt();
                break;
        }

    }

    private void clickSearchBnt(){
        final ProtoClass.SearchInfo searchInfo = collectSearchInfo();

        Intent intent = new Intent(activity, SearchResultActivity.class);
        //将用户的查询信息传递到下一个activity
        intent.putExtra("search_info",searchInfo);
        activity.startActivity(intent);

    }

    private ProtoClass.SearchInfo collectSearchInfo(){

        ProtoClass.SearchInfo.Builder infoBuilder = ProtoClass.SearchInfo.newBuilder()
                .setSearchType(ProtoClass.MsgType.SEEK_CONDITION);//设置为按条件查找
        infoBuilder.setSrchAttrb(mView.getSearchWord());
        //设置性别
        if (TextUtils.equals("男", mView.getSexText())){
            infoBuilder.setSelectMale(true);
        }else if (TextUtils.equals("女",mView.getSexText())){
            infoBuilder.setSelectFemale(true);
        }
        //设置是否在线
        if (TextUtils.equals("仅在线",mView.getOnlineText())){
            infoBuilder.setOnlyOnline(true);
        }
        //设置年龄
        infoBuilder.setAgeLow(ageLow[whichAgeRange]);
        infoBuilder.setAgeHigh(ageHigh[whichAgeRange]);

        //设置地址
        String address = mView.getAddressText();
        if (!TextUtils.equals("不限",address)){
            address = address.replace("-","%");
            infoBuilder.setAddress(address);
        }
        return infoBuilder.build();
    }
    /**
     * show the dialog to select whether the users are online
     */
    private void showOnlineDialog(){
        final String[] options = {"不限","仅在线"};
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setSingleChoiceItems(options, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mView.setOnlineText(options[which]);
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    /**
     * show the dialog to choice gender
     */
    private void showSexDialog(){
        final String[] options = {"不限","男","女"};
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setSingleChoiceItems(options, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mView.setSexText(options[which]);
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    private void showAgeDialog(){
        final String[] options = {"不限","18岁以下","18-22岁","23-26岁","27-35岁","35岁以上"};
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setSingleChoiceItems(options, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        whichAgeRange = which;
                        mView.setAgeText(options[which]);
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    private boolean scrolling = false;


    /**
     * 生成一个PopupWindow用于选择所在地
     * @return
     */
    @SuppressWarnings("JavaDoc")
    private PopupWindow makeAddressPopwin(){
        final PopupWindow window;
        window = new PopupWindow(activity);
        //加载布局
        @SuppressLint("InflateParams") View contentView = LayoutInflater.from(activity)
                .inflate(R.layout.address_layout,null);
        //把布局添加到PopupWindow中
        window.setContentView(contentView);

        final WheelView provinceView = (WheelView) contentView.findViewById(R.id.province);
        final WheelView cityView = (WheelView) contentView.findViewById(R.id.city);
        provinceView.setVisibleItems(3);
        provinceView.setViewAdapter(new ProvinceAdapter(activity));

        final String[][] cities = AddressData.CITIES;
        cityView.setVisibleItems(0);

        provinceView.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (!scrolling) {
                    updateCities(cityView, cities, newValue);
                }
            }
        });

        provinceView.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelView wheel) {
                scrolling = true;
            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                scrolling = false;
                updateCities(cityView,cities,provinceView.getCurrentItem());
            }
        });

        provinceView.setCurrentItem(1);

        final Button addressBtn;
        addressBtn = (Button) contentView.findViewById(R.id.address_btn);
        addressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mView.setAddressText(AddressData.PROVINCES[provinceView.getCurrentItem()]
                        + "-" + AddressData.CITIES[provinceView.getCurrentItem()][cityView.getCurrentItem()]);
                window.dismiss();
            }
        });

        window.setWidth(windowWidth);
        window.setHeight(windowHeight/3);

        // 设置PopupWindow外部区域是否可触摸
        window.setFocusable(true); //设置PopupWindow可获得焦点
        window.setTouchable(true); //设置PopupWindow可触摸
        window.setOutsideTouchable(true); //设置非PopupWindow区域可触摸
        return window;
    }
    /**
     * Updates the city wheel
     */
    private void updateCities(WheelView city, String cities[][], int index) {
        ArrayWheelAdapter<String> adapter =
                new ArrayWheelAdapter<>(activity, cities[index]);
        adapter.setTextSize(18);
        city.setViewAdapter(adapter);
        city.setCurrentItem(cities[index].length / 2);
    }

    @Override
    public void setWindowSize(int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
    }

    @Override
    public void onDestroy() {

    }

    public interface ISearchConditionView {
        void initToolbar(String title);
        void initEvent();
        LinearLayout getWindowLayout();
        String getSearchWord();
        void sexSearchWord(String word);
        String getOnlineText();
        void setOnlineText(String word);
        String getSexText();
        void setSexText(String word);
        String getAgeText();
        void setAgeText(String word);
        String getAddressText();
        void setAddressText(String word);
    }
}
