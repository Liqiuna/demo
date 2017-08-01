package com.cst14.im.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.adapter.SearchHistoryMsgResultAdapter;
import com.cst14.im.bean.HistoryMsgBean;
import com.cst14.im.db.dao.GroupMsgDao;
import com.cst14.im.db.dao.PersonalMsgDao;
import com.cst14.im.utils.ImApplication;

import java.util.LinkedList;

/**
 * Created by Belinda Y on 2016/9/11.
 */
public class SearchHistoryMsgActivity extends Activity {
    private EditText mEditText;
    private Button mSearchButton;
    private TextView mTipTextView;
    private ListView mHistoryMsgListView;
    private  String id;
    private SearchHistoryMsgResultAdapter msgResultAdapter;
    private LinkedList<HistoryMsgBean>msgList=new LinkedList<>();
    private int CODE_SEARCH_MSG_RESULT=4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_historymsg);
        mEditText=(EditText)findViewById(R.id.et_msgKeyWord);
        mSearchButton=(Button)findViewById(R.id.btn_commit_msgKeyWord);
        mTipTextView=(TextView)findViewById(R.id.tv_tip_noResult);
        mHistoryMsgListView=(ListView)findViewById(R.id.msgListView);
        msgResultAdapter=new SearchHistoryMsgResultAdapter();
        msgResultAdapter.setContext(this);


        Intent intent=getIntent();
        final boolean isPerMsg=intent.getBooleanExtra("isPerMsg",false);
        if(isPerMsg) {
            id = ImApplication.instance.getCurSessionHolder().userAccount;
        }else {
            id=intent.getStringExtra("groupID");
            Log.e("groupid",id);
        }
        textChange tc = new textChange();
        //为编辑框增加监听器
        mEditText.addTextChangedListener(tc);

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HideKeyBoard();
                String msgKeyWord=mEditText.getText().toString().trim();
                if(isPerMsg) {
                    msgList = PersonalMsgDao.queryHistoryMsg(msgKeyWord, id);
                }else {
                    msgList= GroupMsgDao.queryHistoryMsg(msgKeyWord,id);
                }
                if(msgList.size()<=0){
                    mTipTextView.setVisibility(View.VISIBLE);
                    return;
                }
                for(int i=0;i<msgList.size();i++){
                   msgResultAdapter.setKeyWord(msgKeyWord);
                }
                msgResultAdapter.setmData(msgList);
                mHistoryMsgListView.setAdapter(msgResultAdapter);
                msgResultAdapter.notifyDataSetChanged();

            }
        });


        mHistoryMsgListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String sendTime=msgList.get(position).getSendTime();
                String textContent=msgList.get(position).getTextContent();
                Intent intent=new Intent();
                Bundle bundle=new Bundle();
                bundle.putString("sendTime",sendTime);
                bundle.putString("textContent",textContent);
                intent.putExtras(bundle);
                SearchHistoryMsgActivity.this.setResult(CODE_SEARCH_MSG_RESULT,intent);
                finish();
            }
        });
    }

    //EditText监听器  当编辑框内容不为空时按钮才可用
    class textChange implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence cs, int start, int before,
                                  int count) {
           if(mEditText.getText().length() > 0) {
               mSearchButton.setEnabled(true);
           } else {
                mSearchButton.setEnabled(false);
            }
        }
        @Override
        public void afterTextChanged(Editable s) {
        }

    }
    //隐藏键盘
    private void HideKeyBoard() {
        InputMethodManager inputMethodManager =(InputMethodManager) getApplicationContext().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mSearchButton.getWindowToken(), 0);
    }

}
