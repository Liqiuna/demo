package com.cst14.im.layoutView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cst14.im.R;
import com.cst14.im.adapter.myVpAdapter;
import com.cst14.im.baseActionView.GridViewCommon;

/**
 * Created by MRLWJ on 2016/7/12.
 */
public class FuncGridView extends GridViewCommon {

    private int pageIndex;
    private int gapWidth;
    private myVpAdapter.OnMyVPClickListener listener;

    public FuncGridView(Context context) {
        super(context);
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);

        int width = wm.getDefaultDisplay().getWidth();
        gapWidth = width/13;
        setNumColumns(4);
        setVerticalSpacing(20 * density);
        setHorizontalSpacing(gapWidth);
        setPadding(gapWidth,gapWidth/4,gapWidth,gapWidth/4);
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
        setAdapter(new MyAdapter());
    }

    public void dispatchListener(myVpAdapter.OnMyVPClickListener listener) {
        this.listener = listener;
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        /**
         *
         * @param position
         * @param convertView
         * @param parent
         * @return
         *
         * 返回的对象的层级：
         *  相对布局
         *     - 相对布局   //控制大小
         *         - ImageView
         */
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final String indexStr = "func64_" + position;
            Context context = parent.getContext();
            RelativeLayout container = new RelativeLayout(context);

            RelativeLayout innerContainer = new RelativeLayout(context);
            innerContainer.setBackgroundResource(R.drawable.round16);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(gapWidth*2, 2*gapWidth);
            innerContainer.setLayoutParams(params);

            int index = context.getResources().getIdentifier(indexStr, "drawable", context.getPackageName());
            ImageView iv = new ImageView(context);
            iv.setBackgroundResource(index);
            RelativeLayout.LayoutParams centerInParent = new RelativeLayout.LayoutParams((int)(gapWidth*1.5), (int)(gapWidth*1.5));
            centerInParent.addRule(RelativeLayout.CENTER_IN_PARENT);
            iv.setLayoutParams(centerInParent);
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onClick(position, indexStr);
                    }
                }
            });
            innerContainer.addView(iv);
            container.addView(innerContainer);
            return container;
        }
    }
}
