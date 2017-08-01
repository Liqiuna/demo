package com.cst14.im.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.utils.Utils;

import java.util.Timer;
import java.util.TimerTask;

public class EditInfoActivity extends AppCompatActivity {
    private static final String TAG = "EditInfoActivity";

    private Toolbar toolbar;
    private EditText etContent;
    private TextView tvMarginNum;
    private TextView tvTips;
    private Button btSave;

    private int maxLength;
    private String defaultContent;

    public static final int RESULT_CODE_EDITINFO = 175;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_edit_info);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        etContent = (EditText) findViewById(R.id.et_activity_edit_info_content);
        tvMarginNum = (TextView) findViewById(R.id.tv_activity_edit_info_margin);
        tvTips = (TextView) findViewById(R.id.tv_activity_edit_info_tips);
        btSave= (Button) findViewById(R.id.bt_save);
        btSave.setOnClickListener(this.onClickListener);

        //get intent extra from origin activity
        //title/tip/content/maxlength
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        if (title != null) {
            toolbar.setTitle(title);
        }
        setSupportActionBar(toolbar);
        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        String tip = intent.getStringExtra("tip");
        if (tip != null) {
            tvTips.setText(tip);
        } else {
            tvTips.setVisibility(View.GONE);
        }

        maxLength = intent.getIntExtra("maxlength", 20);
        Log.e(TAG, "onCreate: " + maxLength);
        if (maxLength == 0) {
            tvMarginNum.setVisibility(View.GONE);
        } else {
            etContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
            etContent.addTextChangedListener(textWatcher);
        }

        defaultContent = intent.getStringExtra("content");
        if (defaultContent != null) {
            etContent.setText(defaultContent);
        }

        etContent.requestFocus();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) etContent.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(etContent, 0);
            }
        }, 300);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (etContent.getText().toString().equals(defaultContent)) {
                btSave.setEnabled(false);
            } else {
                btSave.setEnabled(true);
            }

            int len = etContent.getText().length();
            tvMarginNum.setText(String.valueOf(maxLength - len));
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId()==R.id.bt_save){
                if (etContent.getText().toString().isEmpty()) {
                    Utils.showToast2(getApplicationContext(), "不能为空");
                    etContent.setText(defaultContent);
                }
                Intent intent = new Intent();
                intent.putExtra("result", etContent.getText().toString());
                setResult(RESULT_CODE_EDITINFO, intent);
                finish();
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
