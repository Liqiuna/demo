package com.cst14.im.activity;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.cst14.im.Dialog.AddDialog;
import com.cst14.im.Dialog.ModifyDialog;
import com.cst14.im.R;
import com.cst14.im.adapter.DetailAdapter;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Model;

import java.util.ArrayList;
import java.util.HashMap;

public class CuttomAttrActivity extends Activity {

    ListView list;
    private TextView tv_name;
    private EditText edit_content,et_name;
    DetailAdapter detailAdapter;
    public ImApplication app;
    private ModifyDialog dialog;
    private AddDialog adialog;
    ArrayList<HashMap<String, Object>> listitem = new ArrayList<HashMap<String, Object>>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuttom_attr);
        Initview();
    }
    private Button btn_add;
    public void Initview() {

        btn_add = (Button) findViewById(R.id.btn_add);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertAddDialog(CuttomAttrActivity.this);
            }
        });
        list = (ListView) findViewById(R.id.lv_mod);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view.findViewById(R.id.tv_title);
                alertModifyDialog(CuttomAttrActivity.this, position);

            }

        });
        detailAdapter = new DetailAdapter(this, getData());
        list.setAdapter(detailAdapter);
    }
    public ArrayList<HashMap<String, Object>> getData() {
        ProtoClass.UserCustomAttr mAttr;

        HashMap<String, Object> map=new HashMap<String,Object>();
        mAttr= Model.getCustomAttr();
        if( mAttr==null){
            return this.listitem;
        }
        for(int i=0;i<mAttr.getAttrNameCount();i++){
            map = new HashMap<String, Object>();
            map.put("title",mAttr.getAttrName(i) );
            map.put("headimage","无");
            map.put("content", mAttr.getAttrContent(i));
            this.listitem.add(map);


        }

        return this.listitem;//返回数据源

    }
    public void alertModifyDialog(Context context, final int item) {
        dialog = new ModifyDialog(context);
        tv_name  = dialog.getnameText();
        edit_content = dialog.getcontentText();
        tv_name.setText("修改"+listitem.get(item).get("title").toString());
        edit_content.setText(listitem.get(item).get("content").toString());
        dialog.setOnClickCommitListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                listitem.get(item).put("content", edit_content.getText().toString());
                dialog.dismiss();
            }
        });

        dialog.show();

    }
    public void alertAddDialog(Context context) {
        adialog = new AddDialog(context);
        et_name = adialog.getnameEText();
        edit_content = adialog.getcontentText();
        adialog.setOnClickCommitListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("title", et_name.getText().toString());
                map.put("headimage", "无");
                map.put("content", edit_content.getText().toString());
                listitem.add(map);
                adialog.dismiss();
            }
        });
        adialog.show();
    }
    public void SaveData() {

        ProtoClass.UserCustomAttr.Builder attrbuilder = ProtoClass.UserCustomAttr.newBuilder();


        for (int i = 0; i < listitem.size(); i++) {
            attrbuilder.addAttrName(listitem.get(i).get("title").toString());
            attrbuilder.addAttrContent(listitem.get(i).get("content").toString());
        }
        System.out.println("get attr from CuttomAttrActivity ");
        System.out.println("getid:"+Model.getID());
        attrbuilder.setUserID(Model.getID());
        ProtoClass.UserCustomAttr mattr = attrbuilder.build();

        Model.setCustomAttr(mattr);



    }

    /**
     * 监听Back键按下事件,方法1:
     * 注意:
     * super.onBackPressed()会自动调用finish()方法,关闭
     * 当前Activity.
     * 若要屏蔽Back键盘,注释该行代码即可
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SaveData();
        System.out.println("按下了back键   onBackPressed()");
    }
}
