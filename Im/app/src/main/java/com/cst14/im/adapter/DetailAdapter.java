package com.cst14.im.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.utils.pictureUtils.LocalCacheUtils;
import com.cst14.im.utils.pictureUtils.httprequestPresenter;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zxm on 2016/8/27.
 */
public class DetailAdapter extends BaseAdapter  {
    private Context context1;
    private LayoutInflater mInflater;
    LocalCacheUtils mLocalCacheUtils;

    ArrayList<HashMap<String, Object>> listitem =new ArrayList<HashMap<String, Object>>();

    public DetailAdapter(Context context1, ArrayList<HashMap<String, Object>> map){

     mLocalCacheUtils=new   LocalCacheUtils();
        this.mInflater=LayoutInflater.from(context1);
        this.listitem=map;
    }
    @Override
    public int getCount() {
        return listitem.size();
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
    public View getView(int groupPosition,
                        View convertView, ViewGroup parent) {
        View view = convertView;
        Bitmap bitmap = null;
        if (view == null) {

            view =  this.mInflater.inflate(R.layout.mod_list_item_layout, null);
        }
        if(listitem.get(groupPosition).get("headimage").toString()!="无"&&listitem.get(groupPosition).get("headimage").toString()!=null&&
           listitem.get(groupPosition).get("headimage").toString()!=""     ){
            System.out.println("头像路径la："+listitem.get(groupPosition).get("headimage").toString());
        /*    try {
          //      httprequestPresenter.httpdownload(listitem.get(groupPosition).get("headimage").toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }*/
            System.out.println("下载成功： " + listitem.get(groupPosition).get("headimage").toString());

            bitmap= mLocalCacheUtils.getBitmapFromLocal(listitem.get(groupPosition).get("headimage").toString());


        }
        ImageView headview=(ImageView)view.findViewById(R.id.iv_headimage);
        TextView title = (TextView) view.findViewById(R.id.tv_title);
        TextView content = (TextView) view.findViewById(R.id.tv_info);
        content.setText(listitem.get(groupPosition).get("content").toString());
        title.setText(listitem.get(groupPosition).get("title").toString());
        if(bitmap!=null&&listitem.get(groupPosition).get("headimage").toString()!="无"){
            headview.setImageBitmap(bitmap);}else{headview.setImageBitmap(null);}
        return view;
    }


}
