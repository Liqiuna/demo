package com.cst14.im.layoutView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cst14.im.adapter.myVpAdapter;
import com.cst14.im.baseActionView.GridViewCommon;

/**
 * Created by MRLWJ on 2016/7/12.
 */
public class EmoGridView extends GridViewCommon {
    private int pageIndex;
    private myVpAdapter.OnMyVPClickListener listener;

    private static final int EMO_COUNT = 42;
    private static final int COUNT_EACH_PAGE = 21;

    public EmoGridView(Context context) {
        super(context);
        setNumColumns(7);
        setVerticalSpacing(5 * density);
        setHorizontalSpacing(5 * density);
    }

    @Override
    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
        setAdapter(new MyAdapter());
    }

    @Override
    public void dispatchListener(myVpAdapter.OnMyVPClickListener listener) {
        this.listener = listener;
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            int a = (pageIndex + 1) * COUNT_EACH_PAGE;
            a = a > EMO_COUNT ? EMO_COUNT : a;
            int b = pageIndex * COUNT_EACH_PAGE;
            return a - b;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        //用线性布局把iv包住，这样才能设置宽和高，再把layout返回
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String indexStr;
            if (position == COUNT_EACH_PAGE - 1) {
                indexStr = "_del";
            } else {
                indexStr = (position + COUNT_EACH_PAGE * pageIndex) + "";
                if (indexStr.length() == 1) {
                    indexStr = "0" + indexStr;
                }
            }
            indexStr = "emo" + indexStr;
            Context context = parent.getContext();
            LinearLayout container = new LinearLayout(context);
            int size = (int) (40 * density);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            int index = context.getResources().getIdentifier(indexStr, "drawable", context.getPackageName());
            ImageView iv = new ImageView(context);
            iv.setBackgroundResource(index);
            iv.setLayoutParams(params);
            final String finalIndexStr = indexStr;
            final int finalIndex = index;
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onClick(finalIndex, finalIndexStr);
                    }
                }
            });
            container.addView(iv);
            return container;
        }
    }
}
