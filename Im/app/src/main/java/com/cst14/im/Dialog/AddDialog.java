package com.cst14.im.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cst14.im.R;

/**
 * Created by zxm on 2016/8/31.
 */
public class AddDialog extends Dialog {
    private TextView tv_name ;
    private EditText edit_content,et_name;
    private Button btn_ok;


    public  AddDialog(Context context) {
        super(context, R.style.CommonDialog);

        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.add_dialog_layout, null);
        tv_name= (TextView) view.findViewById(R.id.tv_name);
        edit_content= (EditText)view.findViewById(R.id.edit_content);
        btn_ok = (Button) view.findViewById(R.id.btn_ok);
        et_name= (EditText)view.findViewById(R.id.et_name );

        super.setContentView(view);
    }




    public TextView getnameText(){
        return tv_name;
    }

    public EditText getcontentText(){
        return edit_content;
    }
    public EditText getnameEText(){
        return   et_name;
    }
    public void setOnClickCommitListener(View.OnClickListener listener){
        btn_ok.setOnClickListener(listener);
    }

}
