package com.cst14.im.layoutView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cst14.im.Fragment.FriendFragment;
import com.cst14.im.R;

import java.util.HashMap;

/**
 * Created by I_C_U on 2016/9/11.
 */
public class FriendMenuLayout extends RelativeLayout {
    private Context context;
    private FriendFragment contactFragment = null;
    private String[] GroupOptionArr = {"删除该分组","重命名该分组"};
    private String[] FriendOptionArr = {"删除该好友","修改备注"};
    private String[] optionArr=GroupOptionArr ;
    public FriendMenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }
    public void setContactFragment(FriendFragment contactFragment,int groupPosition) {
        this.contactFragment = contactFragment;
        if(groupPosition==-1){
            optionArr=GroupOptionArr;  //运行在这里表示长按的是分组子项
            return;
        }
        optionArr=FriendOptionArr;//运行在这里表示长按的是好友子项
    }

    public String[] getOptionArr() {
        return optionArr;
    }
    public MenuOptionAdapter getMenuOptionAdapter() {
        return menuOptionAdapter;
    }

    private MenuOptionAdapter menuOptionAdapter = new MenuOptionAdapter();


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        contactFragment.dismissMenu();
        return true;
    }
    private class MenuOptionAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return optionArr.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = View.inflate(context, R.layout.item_menu,null);
            TextView tv = (TextView) v.findViewById(R.id.tv_item_menu);
            tv.setText(optionArr[position]);
            return v;
        }
    }
}
