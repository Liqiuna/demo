package com.cst14.im.activity;

import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.widget.PullRefreshLayout;
import com.cst14.im.R;
import com.cst14.im.adapter.myVpAdapter;
import com.cst14.im.db.dao.PersonalMsgDao;
import com.cst14.im.layoutView.SelectPicDlg;
import com.cst14.im.layoutView.VoiceRecordingLayout;
import com.cst14.im.listener.ViewHistoryListener;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.MsgViewCache;
import com.cst14.im.utils.StringParser;
import com.cst14.im.utils.Utils;
import com.cst14.im.utils.fileUtils.FileUtils;
import com.cst14.im.utils.fileUtils.Recorder;
import com.cst14.im.utils.fileUtils.UploadFile;
import com.cst14.im.utils.sessionUtils.ChatMsgBean;
import com.cst14.im.utils.sessionUtils.SessionHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;


/**
 * Created by MRLWJ on 2016/7/2.
 */
public class ChatActivity extends AppCompatActivity implements ViewHistoryListener.PerHistoryMsgView{

    private static final int MSG_TIP_FADE_IN = 0;
    private static final int TIP_CHANGE_GAP = 10;
    private static final float TIP_ALPHA_GAP = 0.05f;
    private static final int MSG_TIP_FADE_OUT = 2;
    private static final int VOICE_PLAYING = 77;
    private static final int CODE_FILE_SELECT = 2;
    private static final int CODE_TAKE_PHOTO = 3;
    private static  final  int CODE_SEARCH_MSG_RESULT=4;
    private static final String CARMER_FUNC_CALL = "func64_2";
    private static final String VIDEO_FUNC_CALL = "func64_1";
    private static final String FILE_FUNC_CALL = "func64_0";

    private EditText edInput;
    private Button btnSend;
    private ListView lvMsgList;
    private Button btnUnReadTip;
    public ClipboardManager cbm;
    private SessionHolder holder;
    private LinkedList<ChatMsgBean> msgList;
    private ChatMsgAdapter msgListAdapter;
    private ImageView ivEmo;
    private ImageView ivVoice;
    private ViewPager vp;
    private LinearLayout llMainContainer;
    public float density;
    private boolean isTipFadeIn;
    private boolean isVoiceIconClicked;
    private RelativeLayout rlTextInput;
    private View voiceInput;
    public RelativeLayout rlRootView;
    private LinearLayout llInputContainer;
    private VoiceRecordingLayout rlVoiceRecordingView;
    private Recorder recorder;
    private boolean isVpShow;
    private boolean isVoiceRecording;
    private boolean shouldCancelVoice;
    private boolean emoVpShow;
    private boolean funcVpShow;
    private TextView tvVoiceTip;
    private View root;
    public String token;
    private PullRefreshLayout refreshLayout;
    private String picFilePath;
    private myVpAdapter vpFuncAdapter, vpEmoAdapter;
    private Queue<ChatMsgBean> fileUploadQueue;   //执行上传指令之前必须把本地路径添加到队列中
    private ViewHistoryListener viewHistoryListener;
    public static final String DRAG_POSITION = "drag_position";

    public Queue<ChatMsgBean> getFileUploadQueue() {
        return fileUploadQueue;
    }

    //点击语音图标会触发这个方法,
    public void voiceIconClick(View v) {
        if (isVpShow) {
            // 以下是关闭viewpage 的逻辑
            llMainContainer.removeView(vp);
            isVpShow = false;
        }
        if (isVoiceIconClicked) {
            v.setBackgroundResource(R.drawable.voice64);
            llInputContainer.removeView(voiceInput);
            llInputContainer.addView(rlTextInput);
        } else {
            v.setBackgroundResource(R.drawable.key_board64);
            llInputContainer.removeView(rlTextInput);
            llInputContainer.addView(voiceInput);
        }
        isVoiceIconClicked = !isVoiceIconClicked;
    }

    private GestureDetector voiceGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float dis = e1.getY() - e2.getY();
            if (dis > 100) {
                shouldCancelVoice = true;
                rlVoiceRecordingView.setVoiceRecordingMsg("松开 取消语音");
            } else {
                shouldCancelVoice = false;
                rlVoiceRecordingView.setVoiceRecordingMsg("");
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initViewAction();
        recorder = new Recorder();
        cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ImApplication.instance.getCurSessionHolder().chatActivity = this;
         viewHistoryListener=new ViewHistoryListener(this);
        Tools.addPresenter(viewHistoryListener);
        token = ImApplication.getLoginToken();
        fileUploadQueue = new LinkedList<ChatMsgBean>();
    }

    private void initView() {
        setContentView(R.layout.activity_chat);
        rlRootView = (RelativeLayout) findViewById(R.id.rl_chat_main_ui);
        LinearLayout.LayoutParams paramsMatchParent = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        root = getWindow().getDecorView();
        llInputContainer = (LinearLayout) findViewById(R.id.ll_text_input_container);
        ivVoice = (ImageView) findViewById(R.id.iv_voice);
        ivVoice.setBackgroundResource(R.drawable.voice64);
        rlVoiceRecordingView = new VoiceRecordingLayout(this);
        rlTextInput = (RelativeLayout) findViewById(R.id.rl_input);
        //R.layout.layout_chat_voice_bar 中 clickable 不能为true 否则会拦截页面的down事件
        voiceInput = View.inflate(this, R.layout.layout_chat_voice_bar, null);
        tvVoiceTip = (TextView) voiceInput.findViewById(R.id.tv_voice_trigger);
        voiceInput.setLayoutParams(paramsMatchParent);
        holder = ImApplication.instance.getCurSessionHolder();
        holder.setMsgListVisable(true);
        setTitle(holder.nickName.getText().toString());
        edInput = (EditText) findViewById(R.id.et_msg_input);
        btnSend = (Button) findViewById(R.id.btn_msg_send);
        btnUnReadTip = (Button) findViewById(R.id.iv_msg_tip);
        ivEmo = (ImageView) findViewById(R.id.iv_emo);
        llMainContainer = (LinearLayout) findViewById(R.id.ll_chat_main_container);
        btnUnReadTip.setAlpha(0f);
        vp = new ViewPager(this);
        density = getResources().getDisplayMetrics().density;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (150 * density));
        vp.setLayoutParams(params);
        vpEmoAdapter = new myVpAdapter();
        vpEmoAdapter.setGVType(myVpAdapter.EMO);
        vpFuncAdapter = new myVpAdapter();
        vpFuncAdapter.setGVType(myVpAdapter.FUNC);
        lvMsgList = (ListView) findViewById(R.id.lv_chat_msg);
        lvMsgList.setDivider(null);
        lvMsgList.setOverScrollMode(View.OVER_SCROLL_NEVER);
        msgListAdapter = new ChatMsgAdapter();
        msgList = holder.msgList;
        //TODO 如果是空的话，就去查数据库
        if(msgList.size()==0){
            msgList.addAll(0, PersonalMsgDao.getAllMsg(holder.userAccount));
        }
        lvMsgList.setAdapter(msgListAdapter);
        lvMsgList.setSelection(msgListAdapter.getCount());
        lvMsgList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(visibleItemCount==0) return;
                for(int i = 0;i<msgList.size();i++){
                    if(i == firstVisibleItem){
                        i+=(visibleItemCount-1);
                    }else{
                        if(msgList.get(i).root!=null){
                            Log.e("回收","第"+i+"個");
                            msgList.get(i).recycleRootView();
                        }
                    }
                }
            }
        });

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        refreshLayout = (PullRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        refreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_WATER_DROP);

    }

    private void initViewAction() {
        //点击语音按钮会触发这个方法
        voiceInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                voiceGestureDetector.onTouchEvent(event);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        if (isVoiceRecording) {
                            handleVoiceMsg();
                        }
                        break;
                    case MotionEvent.ACTION_DOWN:
                        tvVoiceTip.setText("松开 结束");
                        shouldCancelVoice = false;
                        isVoiceRecording = true;
                        rlRootView.removeView(rlVoiceRecordingView);
                        rlRootView.addView(rlVoiceRecordingView);
                        levelTest = true;
                        recorder.start();
                        handler.sendEmptyMessage(88);
                        break;
                }
                return true;
            }
        });

        // 设置的点击事件，在元素被显示出来之前会被 adapter传递到gridView
        // gridView 会为每个单元设置 以上的点击事件

        vpEmoAdapter.setOnMyVPClickListener(new myVpAdapter.OnMyVPClickListener() {
            @Override
            public void onClick(int id, String strName) {
                if (strName.equals("emo_del")) {
                    //点击了删除按钮
                    edInput.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                    edInput.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
                    return;
                }
                //点击了表情
                String name = "[" + strName + "]";
                Editable text = edInput.getText();
                text.insert(edInput.getSelectionEnd(), name);
            }
        });
        vpFuncAdapter.setOnMyVPClickListener(new myVpAdapter.OnMyVPClickListener() {
            @Override
            public void onClick(int id, String strName) {
                switch (strName) {
                    case FILE_FUNC_CALL:
                        FileUtils.showFileChooser(ChatActivity.this, CODE_FILE_SELECT); //打开文件选择
                        break;
                    case VIDEO_FUNC_CALL:   //录像
                        Intent videoIntent = new Intent(ChatActivity.this, VideoActivity.class);
                        startActivity(videoIntent);
                        overridePendingTransition(R.anim.fade_in, 0);
                        break;
                    case CARMER_FUNC_CALL:   //调用系统相机
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        String name = new DateFormat().format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
                        picFilePath = ImApplication.photoDir + File.separator + name;
                        Uri uri = Uri.fromFile(new File(picFilePath));
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        startActivityForResult(intent, CODE_TAKE_PHOTO);
                        break;
                }
            }
        });
        //viewPager 切换或关闭或打开 逻辑
        ivEmo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVpShow) {
                    if (emoVpShow) {
                        llMainContainer.removeView(vp);
                        isVpShow = false;
                        emoVpShow = false;
                    } else {
                        vp.setAdapter(vpEmoAdapter);
                        emoVpShow = true;
                        funcVpShow = false;

                    }
                } else {
                    vp.setAdapter(vpEmoAdapter);
                    llMainContainer.addView(vp);
                    emoVpShow = true;
                    funcVpShow = false;
                    isVpShow = true;
                }
            }
        });


        edInput.addTextChangedListener(new TextWatcher() {
            int befInsertIndex;
            int befInsertLen;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                befInsertIndex = edInput.getSelectionEnd();
                befInsertLen = edInput.getText().length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    btnSend.setText("发送");
                    btnSend.setBackgroundResource(R.drawable.login_btn_selector);
                } else {
                    btnSend.setText("");
                    btnSend.setBackgroundResource(R.drawable.chat_send_def);
                }
                edInput.removeTextChangedListener(this);
                int insertLen = edInput.getText().length() - befInsertLen;
                if (insertLen > 0) {
                    edInput.setText(StringParser.convert(getBaseContext(), s.toString(), (int) (25 * density)));
                    edInput.setSelection(befInsertIndex + insertLen);
                }
                edInput.addTextChangedListener(this);
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String msg = edInput.getText().toString();
                if (TextUtils.isEmpty(msg)) {
                    //viewPager 切换或关闭或打开 逻辑
                    if (isVpShow) {
                        if (funcVpShow) {
                            llMainContainer.removeView(vp);
                            funcVpShow = false;
                            isVpShow = false;
                        } else {
                            vp.setAdapter(vpFuncAdapter);
                            funcVpShow = true;
                            emoVpShow = false;
                        }
                    } else {
                        vp.setAdapter(vpFuncAdapter);
                        llMainContainer.addView(vp);
                        emoVpShow = false;
                        funcVpShow = true;
                        isVpShow = true;
                    }
                    return;
                }
                edInput.setText("");
                final ChatMsgBean msgISend = ChatMsgBean.newTextMsg(msg, "");
                msgISend.isSending = true;
                holder.acceptMsg(msgISend,false,true);
                lvMsgList.setSelection(msgListAdapter.getCount());
                ProtoClass.Msg.Builder msgBuilder = ProtoClass.Msg.newBuilder();
                ProtoClass.PersonalMsg.Builder personalBuilder = ProtoClass.PersonalMsg.newBuilder();
                personalBuilder.addRecverID(Integer.parseInt(holder.userAccount))
                        .setSenderID(Integer.parseInt(ImApplication.instance.User_id))
                        .setMsgType(ProtoClass.DataType.TEXT)
                        .setContent(msg)
                        .setMsgId(msgISend.msgId);
                msgBuilder.setPersonalMsg(personalBuilder)
                          .setToken(token)
                          .setMsgType(ProtoClass.MsgType.MsgType_SESSION);

                Tools.startTcpRequest(msgBuilder, new Tools.TcpListener() {
                    @Override
                    public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                        ImApplication.mainActivity.sessionLisener.onProcess(responseMsg);
                        return false;
                    }
                });
            }
        });
        //右下角提示未读消息的控件的点击事件
        btnUnReadTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float curAlpha = btnUnReadTip.getAlpha();
                if (curAlpha != 0) {
                    lvMsgList.setSelection(msgListAdapter.getCount());
                }
            }
        });
        //当holder接收到消息时会触发这个方法
        holder.setOnAcceptMsgListener(new SessionHolder.OnAcceptMsgListener() {
            @Override
            public void onAccept(ChatMsgBean bean) {
                msgListAdapter.notifyDataSetChanged();
                if (shouldSmoothScrollToBottom()) {
                    lvMsgList.smoothScrollToPosition(msgListAdapter.getCount());
                    return;
                }
                showUnReadCountToTip();
            }
        });
        //当消息被阅读会触发这个方法
        holder.addOnMsgReadedListener(new SessionHolder.OnMsgReadedListener() {
            @Override
            public void onMsgReaded(int unReadMsgCount,int count) {
                showUnReadCountToTip();
            }
        });
        refreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                String oldestTime = holder.getOldestMsgSentTime();
                if(TextUtils.isEmpty(oldestTime)) {
                    Log.e("onRefresh","获取出的时间为空");
                    oldestTime = "";
                }
                    int friendID= Integer.parseInt(holder.userAccount);
                    ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                    builder.setFriendID(friendID)
                            .setMsgTime(oldestTime)
                            .setMsgType(ProtoClass.MsgType.VIEW_HISTOTY_MSG);
                    Tools.startTcpRequest(builder, new Tools.TcpListener() {
                        @Override
                        public void onSendFail(Exception e) {
                            Toast.makeText(getBaseContext(),"send failed",Toast.LENGTH_SHORT).show();
                        }
                        //第二个参数是服务器返回的响应消息
                        @Override
                        public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                            viewHistoryListener.onProcess(responseMsg);
                            return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
                        }
                    });



                // refresh complete
                refreshLayout.setRefreshing(false);
            }
        });
    }


    private void handleVoiceMsg() {
        tvVoiceTip.setText("按住 说话");
        rlRootView.removeView(rlVoiceRecordingView);
        levelTest = false;
        final ChatMsgBean voiceMsg = recorder.stop(shouldCancelVoice);
        startFileUploadRequest(voiceMsg);
    }

    @Override
    protected void onStop() {
        holder.setMsgListVisable(false);
        super.onStop();
    }

    @Override
    protected void onStart() {
        holder.setMsgListVisable(true);
        super.onStart();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK&&requestCode!=CODE_SEARCH_MSG_RESULT) {
            return;
        }
        switch (requestCode) {
            case CODE_FILE_SELECT:  //获取文件地址 以及发送
                Uri uri = data.getData();
                String path = FileUtils.getPath(this, uri);
                if (TextUtils.isEmpty(path)) {
                    Utils.showToast2(this, "获取文件路径失败");
                    return;
                }
                final ChatMsgBean fileBean = ChatMsgBean.newFileMsg(path, "", "");
                fileBean.isSending = true;
                startFileUploadRequest(fileBean);
                break;

            case CODE_TAKE_PHOTO:  //保存图片以及发送
                SelectPicDlg selectPicDlg = new SelectPicDlg(ChatActivity.this);
                selectPicDlg.showBiaoQingSize(10 * 1024);
                final File pic = new File(picFilePath);
                selectPicDlg.showYuanTuSize(pic.length());
                selectPicDlg.setOnPositiveListener(new SelectPicDlg.OnPositiveListener() {
                    @Override
                    public void onClick(boolean isBiaoqingChoose) {
                        if (isBiaoqingChoose) {
                            //TODO 生成标清图片
                            Utils.showToast2(ChatActivity.this, "发送了标清图片");
                            return;
                        }
                        ChatMsgBean fileMsg = ChatMsgBean.newFileMsg(picFilePath, "", "");
                        fileMsg.isSending = true;
                        startFileUploadRequest(fileMsg);
                    }
                });
                rlRootView.addView(selectPicDlg);
                break;



        }
        switch (resultCode){
            case CODE_SEARCH_MSG_RESULT:
                String msgId=data.getExtras().getString("msgId");
                String sendTime=data.getExtras().getString("sendTime");
                String textContent=data.getExtras().getString("textContent");
                if((msgId==null)||(sendTime==null)||(textContent==null)){
                    return;
                }
                int position=msgList.size()-1;
                for(int i=0;i<msgList.size();i++){
                    if(msgId.equals(msgList.get(i).msgId)&&(sendTime.equals(msgList.get(i).sentTime)&&
                            (textContent.equals(msgList.get(i).getTextContent())))){
                        position=i;
                        Log.e("textContent",msgList.get(i).getTextContent());
                    };

                }
                lvMsgList.setSelection(position);
                break;
            default:
                break;
        }
    }


    // 把文件上传添加 聊天列表里，然后发送上传请求,上传成功后自动发送会话消息
    public void startFileUploadRequest(ChatMsgBean fileMsg) {
        if (fileMsg == null) {
            Log.e("要上传的信息为 null", "----------");
            return;
        }
        File uploadFile = new File(fileMsg.getFileName());
        if (!uploadFile.exists()) {
            Log.e("要上传的文件不存在", "----------");
            return;
        }
        holder.acceptMsg(fileMsg,false,true);
        fileUploadQueue.add(fileMsg);
        List<String> fingerPrints = new ArrayList<String>();
        fingerPrints.add(fileMsg.getFileFingerPrint());
        ProtoClass.MsgForFileUpload.Builder fileBuilder = ProtoClass.MsgForFileUpload.newBuilder();
        fileBuilder.addAllFileFingerPrint(fingerPrints);
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setToken(token)
                .setFileUploadMsg(fileBuilder.build())
                .setMsgType(ProtoClass.MsgType.MsgType_UPLOAD_FILE);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                /**
                 * 当文件上传完成后要做的事，具体成功还是失败，看onProcess，成功的话则执行回调
                 */

                List<UploadFile.OnAfterUploadListener> listeners = new ArrayList<UploadFile.OnAfterUploadListener>();
                listeners.add(new UploadFile.OnAfterUploadListener() {
                    @Override
                    public void onUpload(ChatMsgBean fileMsg, int index) {
                        ProtoClass.PersonalMsg.Builder personalBuilder = ProtoClass.PersonalMsg.newBuilder();
                        personalBuilder.addRecverID(Integer.parseInt(fileMsg.getParentSessionHolder().userAccount))
                                .setSenderID(Integer.parseInt(ImApplication.User_id))
                                .setMsgType(ProtoClass.DataType.forNumber(fileMsg.getDataType()))
                                .setFileName(new File(fileMsg.getFileName()).getName())
                                .setFileLen(fileMsg.getFileSize())
                                .setMsgId(fileMsg.msgId)
                                .setContent(fileMsg.getFileFingerPrint());
                        final ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                        builder.setToken(ChatActivity.this.token)
                                .setPersonalMsg(personalBuilder)
                                .setMsgType(ProtoClass.MsgType.MsgType_SESSION);

                        final Tools.TcpListener tcpListener = new Tools.TcpListener() {
                            @Override
                            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                                ImApplication.mainActivity.sessionLisener.onProcess(responseMsg);
                                return true;
                            }
                        };
                        Tools.startTcpRequest(builder, tcpListener);
                    }
                });
                new UploadFile(ChatActivity.this).setOnAfterUploadListeners(listeners).onProcess(responseMsg);
                return true;
            }
        });
    }

    //判断是否应该要缓慢滚动到底部
    private boolean shouldSmoothScrollToBottom() {
        return msgListAdapter.getCount() - lvMsgList.getLastVisiblePosition() < 3;
    }

    public Dialog chatMenu;
    boolean levelTest;
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {

            float curAlpha = btnUnReadTip.getAlpha();

            switch (message.what) {
                case MSG_TIP_FADE_IN:
                    if (!isTipFadeIn) {  //fadeIn 动作被取消
                        btnUnReadTip.setAlpha(0f);
                        return;
                    }
                    Log.e(this.toString(), " -- FadeIn :" + curAlpha);
                    if (curAlpha >= 1.0f - TIP_ALPHA_GAP) {
                        btnUnReadTip.setAlpha(1.0f);
                    } else {
                        btnUnReadTip.setAlpha(curAlpha + TIP_ALPHA_GAP);
                        handler.sendEmptyMessageDelayed(MSG_TIP_FADE_IN, TIP_CHANGE_GAP);
                    }
                    break;
                case MSG_TIP_FADE_OUT:
                    if (isTipFadeIn) {  //fadeOut动作被取消
                        btnUnReadTip.setAlpha(1f);
                        return;
                    }
                    Log.e(this.toString(), " -- FadeOut :" + curAlpha);
                    if (curAlpha <= TIP_ALPHA_GAP) {
                        btnUnReadTip.setAlpha(0f);
                    } else {
                        btnUnReadTip.setAlpha(curAlpha - TIP_ALPHA_GAP);
                        handler.sendEmptyMessageDelayed(MSG_TIP_FADE_OUT, TIP_CHANGE_GAP);
                    }
                    break;
                case VOICE_PLAYING: //播放音乐时的动画
                    if ((boolean) curHandleObj.getTag()) {
                        Log.e("start", "----------");
                        setVoiceIconRank(curHandleObj, isLeftObj, curHandleObjRank);
                        curHandleObjRank = (++curHandleObjRank) % 3 + 1;
                        handler.sendEmptyMessageDelayed(VOICE_PLAYING, 190);
                    } else {
                        setVoiceIconRank(curHandleObj, isLeftObj, 1);
                        Log.e("stop", "----------");
                    }
                    break;

                case 88:  //音量等级变化测试
                    rlVoiceRecordingView.setVoiceLevel((int) (Math.random() * 6 + 1));
                    if (levelTest) {
                        handler.sendEmptyMessageDelayed(88, 200);
                    }
                    break;
            }
            super.getMessageName(message);
        }
    };

    private void showUnReadCountToTip() {
        int unReadMsgCount = holder.getUnReadMsgCount();
        if (unReadMsgCount == 0) {
            isTipFadeIn = false; //这个变量主要用于 防止fadeIn和fadeOut同时执行
            handler.sendEmptyMessage(MSG_TIP_FADE_OUT);
            return;
        }
        isTipFadeIn = true;
        btnUnReadTip.setText("" + unReadMsgCount);
        handler.sendEmptyMessage(MSG_TIP_FADE_IN);
    }
    @Override
    protected void onDestroy() {
        ImApplication.instance.getCurSessionHolder().chatActivity = null;
        for(ChatMsgBean msg:msgList){
            msg.recycleRootView();
        }
        super.onDestroy();
    }



    @Override
    public void successGetPerHistoryMsg(LinkedList<ChatMsgBean> historyMsgList) {
        if(historyMsgList==null||historyMsgList.isEmpty()){
            Utils.showToast2(this,"没有更多消息了！");
            return;
        }
        for(ChatMsgBean item:historyMsgList){
            item.msgSentFinished = true;
            holder.acceptMsg(item,true,true);
            item.tryToBeReaded(); //默认所有历史消息在接收到的时候都被阅读了
            item.showMsgSentState("",true);
        }
        msgListAdapter.notifyDataSetChanged();
    }



    @Override
    public void failedGetHistoryMsg(String errMsg) {
         Utils.showToast2(this,errMsg);
    }

    private class ChatMsgAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return msgList.size();
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
            ChatMsgBean bean = msgList.get(position);
            bean.tryToBeReaded();
            if(bean.root==null){
                View root = MsgViewCache.getView(bean);
                bean.root = root;
                if(!bean.isSending){
                    bean.showMsgSentState(bean.msgSentFinished ? "" : "[离线]", true);
                }
            }
            return bean.root;
        }
    }

    private String curVoiceFile;
    private View curHandleObj;
    private int curHandleObjRank;
    private boolean isLeftObj;
    private MediaPlayer player;

    /**
     * @param voiceFile    要求是文件的全路径
     * @param handleTarget
     * @param pIsLeftObj
     */
    public void triggerVoice(final String voiceFile, final View handleTarget, final boolean pIsLeftObj) {
        Log.e("触发路径", voiceFile);
        Log.e("文件大小是：", new File(voiceFile).length() + "");
        if (voiceFile.equals(curVoiceFile)) {
            if (player.isPlaying()) {
                player.pause();
                curHandleObj.setTag(false);   //播放 -》 暂停
            } else {
                player.start();
                curHandleObj.setTag(true);   //暂停 -》 播放
                handler.sendEmptyMessageDelayed(VOICE_PLAYING, 201);
            }
        } else {  //切换
            boolean shouldDelay = false;
            if (player != null) {
                if (player.isPlaying()) {
                    player.pause();
                    curHandleObj.setTag(false);
                }
                player.stop();
                player.release();
                player = null;
                shouldDelay = true;
            }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    curHandleObjRank = 1;
                    curHandleObj = handleTarget;
                    isLeftObj = pIsLeftObj;
                    player = new MediaPlayer();
                    try {
                        File file = new File(voiceFile);
                        if (!file.exists()) {
                            Log.e("文件不存在", file.getAbsolutePath());
                        }
                        player.setDataSource(voiceFile);
                        player.prepare();
                    } catch (Exception e) {
                        Log.e("in setDataSource", e.toString());
                    }

                    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            curHandleObj.setTag(false);
                        }
                    });
                    curVoiceFile = voiceFile;
                    player.setVolume(1f, 1f);
                    player.start();
                    curHandleObj.setTag(true);
                    handler.sendEmptyMessage(VOICE_PLAYING);
                }
            }, shouldDelay ? 201 : 0);
        }

    }

    private void setVoiceIconRank(View target, boolean isLeft, int rank) {
        try {
            ImageView icon = (ImageView) target.findViewById(R.id.iv_voice_icon);
            String idStr = (isLeft ? "voice32_l_" : "voice32_r_") + rank;
            int id = getResources().getIdentifier(idStr, "drawable", getPackageName());
            icon.setBackgroundResource(id);
        } catch (Exception e) {
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chatting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_friend_msg:
                Intent intent = new Intent(this,FriendMsgActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_search_historyMsg:
                Intent inToSearch=new Intent(this,SearchHistoryMsgActivity.class);
                inToSearch.putExtra("isPerMsg",true);
                startActivityForResult(inToSearch,CODE_SEARCH_MSG_RESULT);
                break;
        }
        return true;
    }

}


