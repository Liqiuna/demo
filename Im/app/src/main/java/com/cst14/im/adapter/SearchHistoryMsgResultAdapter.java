package com.cst14.im.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.bean.HistoryMsgBean;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Model;
import com.cst14.im.utils.StringParser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchHistoryMsgResultAdapter extends BaseAdapter {
    private List<HistoryMsgBean>mData;
    private  Context context;
    private  String textKeyWord;
    public Context context(){
        return  context;
    }
    public  void  setContext(Context context){ this.context=context;}
    public void setmData(List mData){
        this.mData=mData;
    }
    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(context, R.layout.item_historymsg, null);
        HistoryMsgBean bean = mData.get(position);
        TextView tv_nickName = (TextView) view.findViewById(R.id.tv_nickname);
        TextView tv_textContent = (TextView) view.findViewById(R.id.tv_textContent);
        TextView tv_time = (TextView) view.findViewById(R.id.tv_time);
        if (bean.getIsPerMsg()) {
            if (bean.getIsMineMsg()) {
                tv_nickName.setText(Model.getnickName());

            } else {
                tv_nickName.setText(ImApplication.mapFriends.get(bean.getFriendId()).getMark());

            }
        }else{
            tv_nickName.setText(bean.getGroupMemberId());
        }
        tv_textContent.setText(setKeyWordColor(bean.getTextContent(), textKeyWord));
        tv_time.setText(StringParser.getBrifTime(bean.getSendTime()));
        return view;
    }
    public void setKeyWord(String textKeyWord){
        this.textKeyWord=textKeyWord;
    }

    //设置搜索关键字为显示为蓝色
    private SpannableString setKeyWordColor(String textContent, String keyword){
        SpannableString s = new SpannableString(textContent);
        Pattern p = Pattern.compile(keyword);
        Matcher m = p.matcher(s);
        while (m.find()){
            int start = m.start();
            int end = m.end();
            s.setSpan(new ForegroundColorSpan(Color.BLUE),start,end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return s;
    }

}
