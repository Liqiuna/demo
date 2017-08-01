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
 * Created by zxm on 2016/8/27.
 */
public class ModifyDialog  extends Dialog {
    private TextView tv_name ;
    private EditText edit_content;
    private Button btn_ok;



    public ModifyDialog(Context context) {
        super(context, R.style.ModifyDialog);

        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.modify_dialog_layout, null);
        tv_name= (TextView) view.findViewById(R.id.tv_name);
        edit_content= (EditText)view.findViewById(R.id.et_name);
        btn_ok = (Button) view.findViewById(R.id.btn_ok);
        super.setContentView(view);
    }



    public ModifyDialog(Context context,String content) {
        super(context, R.style.ModifyDialog);

        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.modify_dialog_layout, null);
        tv_name= (TextView) view.findViewById(R.id.tv_name);
        edit_content= (EditText)view.findViewById(R.id.et_name);
        btn_ok = (Button) view.findViewById(R.id.btn_ok);
        edit_content.setText(content);
        super.setContentView(view);
    }




    public TextView getnameText(){
        return tv_name;
    }
    public EditText getcontentText(){
        return edit_content;
    }
    public Button   getBtn_ok(){
        return btn_ok;
    }
    public void setOnClickCommitListener(View.OnClickListener listener){
        btn_ok.setOnClickListener(listener);
    }

}
