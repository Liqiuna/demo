package com.cst14.im.layoutView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cst14.im.Fragment.SessionsFragment;
import com.cst14.im.R;

/**
 *  一个布局展现菜单
 */
public class MenuLayout extends RelativeLayout {

    private Context context;

    public MenuOptionAdapter getMenuOptionAdapter() {
        return menuOptionAdapter;
    }

    private MenuOptionAdapter menuOptionAdapter = new MenuOptionAdapter();

    public void setContactFragment(SessionsFragment contactFragment) {
        this.contactFragment = contactFragment;
    }


    private SessionsFragment contactFragment = null;

    public MenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        contactFragment.dismissMenu();
        return true;
    }

    public String[] getOptionArr() {
        return optionArr;
    }

    private String[] optionArr = {"消息置顶","删除该聊天","标记为未读","消息免打扰"};

    private class MenuOptionAdapter extends BaseAdapter{

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
