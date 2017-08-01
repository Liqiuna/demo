package com.cst14.im.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.widget.PullRefreshLayout;
import com.cst14.im.R;
import com.cst14.im.adapter.GroupMsgAdapter;
import com.cst14.im.bean.GroupChatHolder;
import com.cst14.im.bean.UserChat;
import com.cst14.im.bean.UserGroup;
import com.cst14.im.db.dao.GroupMsgDao;
import com.cst14.im.listener.GroupChatListener;
import com.cst14.im.listener.ViewHistoryListener;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.PathUtils;
import com.cst14.im.utils.Utils;
import com.cst14.im.utils.groupChatUtils.RecoderPlayer;
import com.cst14.im.utils.groupChatUtils.UploadFileGroup;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by qi on 16-8-27.
 */
public class GroupChatActivity extends Activity implements GroupChatListener.iGroupChatView, ViewHistoryListener.GroupHistoryMsgView {
    private EditText etInput;
    private Button btnSend;
    private GridView funcView;
    private ImageButton imgbtnVoice;
    private Button btnVoice;
    private Button btnGroupData;
    private Button btnBack;
    private ImageButton imgbtnFace;
    private Button btnSearchMsg;
    private TextView tvGroupName;
    private ImApplication app;
    public static GroupChatActivity instance = null;
    private String token;
    private GroupMsgDao groupMsgDao;
    private RecodeThread recodeThread = null;
    private ExecutorService msgIsReadThread;

    private ListView lvMsgList;
    public static LinkedList<UserChat> userChatList;
    private GroupMsgAdapter adapter;
    private GroupChatHolder groupChatHolder;
    private int groupID;
    private GroupChatListener groupChatListener;
    private String phoPath;
    private String groupName;
    private PullRefreshLayout refreshLayout;
    private ViewHistoryListener viewHistoryListener;
    private boolean inputIsNull = true;
    private int[] functionImage = {R.drawable.f01, R.drawable.f02, R.drawable.f04};
    private SimpleAdapter simpleAdapter;
    private  boolean isHistoryMsg=false;
    private String[] text = {FUNC_CAMERA, FUNC_PHOTO, FUNC_FILE};
    private static final String FUNC_CAMERA = "拍照";
    private static final String FUNC_PHOTO = "图片";
    private static final String FUNC_FILE = "文件";
    private static final String FUNC_MAP_IMAGE = "image";
    private static final String FUNC_MAP_TEXT = "text";

    private static final int REQUEST_CAREMA = 0;
    private static final int REQUEST_PHOTO = 1;
    private static final int REQUEST_FILE = 2;
    private static final int SEARCH_MSG_RESULT = 4;
    private static final int FLUSH_MSG = 1;
    private static final int FLUSH_VOICE_MSG = 2;
    private static final int FLUSH_READ_STATUS=3;

    private static final String TAG = "GroupChatActivity";

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FLUSH_MSG:
                    adapter.notifyDataSetChanged();
                    lvMsgList.setSelection(lvMsgList.getCount() - 1);
                    break;
                case FLUSH_VOICE_MSG:
                    adapter.notifyDataSetChanged();
                    break;
                case FLUSH_READ_STATUS:
                   Bundle bundle= msg.getData();
                    String readStatus = bundle.getString("readStatus");
                    final LinearLayout linearLayoutMain = new LinearLayout(GroupChatActivity.this);//自定义一个布局文件
                    Point point = new Point();
                    getWindowManager().getDefaultDisplay().getSize(point);
                    linearLayoutMain.setLayoutParams(new ViewGroup.LayoutParams(
                            (int) (point.x * 0.5), (int) (point.y * 0.5)));
                    ListView listView = new ListView(GroupChatActivity.this);//this为获取当前的上下文
                    listView.setFadingEdgeLength(0);
                    readStatus = readStatus.replaceAll("=", "\t\t\t\t\t\t\t\t");
                    String[] strs = readStatus.split(",");
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            GroupChatActivity.this,
                            android.R.layout.simple_expandable_list_item_1,
                            strs);
                    listView.setAdapter(adapter);
                    linearLayoutMain.addView(listView);//往这个布局中加入listview

                    final AlertDialog dialog = new AlertDialog.Builder(GroupChatActivity.this)
                            .setTitle("\t\t名字\t\t\t\t\t\t\t\t\t\t阅读时间").setView(linearLayoutMain)//在这里把写好的这个listview的布局加载dialog中
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // TODO Auto-generated method stub
                                    dialog.cancel();

                                }
                            }).create();
                    dialog.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击
                    dialog.show();

                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        //解决软键盘弹出背景变形
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setVariables();
        bindView();
        viewAction();
    }

    @Override
    public void startSession(final ProtoClass.Msg msg, String filePath) {
        if (msg.getGroupMsg().getGroupID() != groupID) {
            return;
        }
        isHistoryMsg=false;
        userChatList.add(groupChatHolder.protoToUserchat(msg.toBuilder(), null));
        GroupMsgDao.addMsg(msg.toBuilder(), filePath, 1);
        handler.sendEmptyMessage(FLUSH_MSG);
    }

    @Override
    public void msgFeedBack(final ProtoClass.Msg msg) {
        if (msg.getGroupMsg().getGroupID() != groupID) {
            return;
        }
        for (int i = 0; i < userChatList.size(); i++) {
            UserChat userChat = userChatList.get(i);
            Log.i(TAG,"进入了msgfeedback");
            if (userChat.getName().equals(app.User_id) && userChat.getMsgUniqueTag().equals(msg.getMsgUniqueTag())) {
                userChatList.get(i).setIsSendOK(1);
                handler.sendEmptyMessage(FLUSH_MSG);
                return;
            }
        }
    }

    @Override
    public void msgFeedBackRead(final ProtoClass.Msg msg) {
        if (msg.getGroupMsg().getGroupID() != groupID) {
            return;
        }
        Log.i(TAG,"进入了msgfeedback   READ");
        for (int i = 0; i < userChatList.size(); i++) {
            UserChat userChat = userChatList.get(i);
            if (userChat.getName().equals(app.User_id) && userChat.getMsgUniqueTag().equals(msg.getMsgUniqueTag())) {
                userChatList.get(i).setIsRead(1);
                handler.sendEmptyMessage(FLUSH_MSG);
                return;
            }
        }
    }

    //设置全局变量
    private void setVariables() {
        groupID = getIntent().getIntExtra("groupID", -1);
        groupName = getIntent().getStringExtra("groupName");
        Log.i(TAG, "groupName =" + groupName);
        instance = this;
        token = ImApplication.instance.getLoginToken();
        msgIsReadThread = Executors.newSingleThreadExecutor();
        refreshLayout = (PullRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        refreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_CIRCLES);
        viewHistoryListener=new ViewHistoryListener(this);
        Tools.addPresenter(viewHistoryListener);
    }


    private void bindView() {
        etInput = (EditText) findViewById(R.id.et_group_caht_input);
        btnSend = (Button) findViewById(R.id.btn_group_chat_sendMsg);
        funcView = (GridView) findViewById(R.id.gv_group_chat_function);
        imgbtnVoice = (ImageButton) findViewById(R.id.imgbtn_group_chat_voice);
        btnVoice = (Button) findViewById(R.id.btn_group_caht_voice);
        btnGroupData = (Button) findViewById(R.id.imgbtn_group_chat_annu);
        btnBack = (Button) findViewById(R.id.btn_group_chat_back);
        imgbtnFace = (ImageButton) findViewById(R.id.imgbtn_group_chat_face);
        btnSearchMsg = (Button) findViewById(R.id.btn_group_caht_search);
        tvGroupName = (TextView) findViewById(R.id.tv_group_chat_name);
        app = ImApplication.instance;

        tvGroupName.setText(groupName);

        ArrayList<HashMap<String, Object>> imagelist = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < 3; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(FUNC_MAP_IMAGE, functionImage[i]);
            map.put(FUNC_MAP_TEXT, text[i]);
            imagelist.add(map);
        }
        simpleAdapter = new SimpleAdapter(this, imagelist,
                R.layout.item_group_chat_func, new String[]{FUNC_MAP_IMAGE, FUNC_MAP_TEXT}, new int[]{
                R.id.iv_group_chat_item_image, R.id.tv_group_chat_item_text});
        funcView.setAdapter(simpleAdapter);
        setGridViewHeight(0);

        groupChatListener = new GroupChatListener(this);
        Tools.addPresenter(groupChatListener);
        userChatList = new LinkedList<>();
        groupChatHolder = new GroupChatHolder();
        userChatList = GroupMsgDao.getMsgList(groupID);
        lvMsgList = (ListView) findViewById(R.id.lv_group_caht_content);
        adapter = new GroupMsgAdapter(this, R.layout.item_group_chat, this.userChatList);
        lvMsgList.setAdapter(adapter);
        lvMsgList.setSelection(adapter.getCount() - 1);
    }

    private void viewAction() {
        //输入框状态监听
        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                btnSend.setText("");
                btnSend.setBackgroundResource(R.drawable.add64);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    btnSend.setText("发送");
                    btnSend.setBackgroundResource(R.drawable.login_btn_selector);
                    isHistoryMsg=false;
                    inputIsNull = false;
                } else {
                    inputIsNull = true;
                }
            }
        });
        //查找聊天记录监听
        btnSearchMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inToSearch = new Intent(GroupChatActivity.this, SearchHistoryMsgActivity.class);
                inToSearch.putExtra("groupID", String.valueOf(groupID));
                inToSearch.putExtra("isPerMsg", false);
                startActivityForResult(inToSearch, SEARCH_MSG_RESULT);
            }
        });
       //下拉加载更多历史消息
        refreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String oldestTime="";
                oldestTime= GroupMsgDao.getGroupMsgOldestTime(groupID);
                ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                builder.setGroupID(groupID)
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

                refreshLayout.setRefreshing(false);
            }
        });


        //发送按钮事件监听
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputIsNull == true) {
                    setGridViewHeight(180);
                } else {
                    String strMsg = etInput.getText().toString();
                    etInput.setText("");
                    ProtoClass.Msg.Builder msgBuilder = ProtoClass.Msg.newBuilder();
                    ProtoClass.GroupMsg.Builder groupMsgBuilder = ProtoClass.GroupMsg.newBuilder();
                    groupMsgBuilder.setGroupID(groupID)
                            .setSenderName(app.User_id)
                            .setText(strMsg)
                            .setDataType(ProtoClass.DataType.TEXT);
                    msgBuilder.setGroupMsg(groupMsgBuilder)
                            .setAccount(app.User_id)
                            .setGroupID(groupID)
                            .setMsgType(ProtoClass.MsgType.GROUP_CHAT);
                    msgBuilder.setMsgUniqueTag(System.currentTimeMillis() + "");
                    Tools.startTcpRequest(msgBuilder, new Tools.TcpListener() {
                        @Override
                        public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                            //groupChatListener.onProcess(responseMsg);
                            Log.i(TAG, "resoponse msg is " + responseMsg.toString());
                            return false;
                        }
                    });
                    userChatList.add(groupChatHolder.protoToUserchat(msgBuilder, null));
                    GroupMsgDao.addMsg(msgBuilder, null, 0);
                    handler.sendEmptyMessage(FLUSH_MSG);
                }
            }
        });

        //文件功能选择
        funcView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GridView gridView = (GridView) parent;
                HashMap<String, Object> map = (HashMap<String, Object>) gridView.getItemAtPosition(position);
                String textStr = map.get("text").toString();
                switch (textStr) {
                    case FUNC_CAMERA:
                        String phoName = new DateFormat().format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
                        phoPath = ImApplication.photoDir + File.separator + phoName;
                        Uri uri = Uri.fromFile(new File(phoPath));
                        Log.i(TAG, "--camera: photo path is" + phoPath);
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        startActivityForResult(intent, REQUEST_CAREMA);
                        startFileUploadRequest(phoPath, ProtoClass.DataType.IMAGE, 0.0f);
                        break;
                    case FUNC_PHOTO:
                        intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        startActivityForResult(intent, REQUEST_PHOTO);
                        break;
                    case FUNC_FILE:
                        intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("*/*");
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(intent, REQUEST_FILE);
                        break;
                    default:
                        break;
                }
            }

        });

        //录音按键
        imgbtnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etInput.getVisibility() == View.VISIBLE) {
                    etInput.setVisibility(View.INVISIBLE);
                    btnVoice.setVisibility(View.VISIBLE);
                } else {
                    etInput.setVisibility(View.VISIBLE);
                    btnVoice.setVisibility(View.INVISIBLE);
                }
            }
        });


        //进入群资料
        btnGroupData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent intent = new Intent(GroupChatActivity.this, GroupMoreFuncActivity.class);
                intent.putExtra("groupID", groupID);
                startActivity(intent);*/
                List<UserGroup> userGroupList = new ArrayList<>();
                userGroupList.addAll(ImApplication.instance.getCurUser().getGroups());
                for (int i = 0; i < userGroupList.size(); i++) {
                    if (userGroupList.get(i).getID() == groupID) {
                        Intent intent = new Intent(GroupChatActivity.this, GroupInfoActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("usergroup", userGroupList.get(i));
                        bundle.putInt("origin", GroupInfoActivity.From_GROUP_ACTIVITY);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                }
            }
        });

        //返回群列表
        final Intent intent = new Intent(this, GroupActivity.class);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
                finish();
            }
        });

        //listview点击事件
        lvMsgList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                UserChat userChat = (UserChat) listView.getItemAtPosition(position);
                String msgType = userChat.getMsgType();
                int voiceTime = userChat.getVoiceTime();
                int who;
                if (userChat.getName().equals(ImApplication.User_id)) {
                    who = 0;
                } else {
                    who = 1;
                }
                //adapter.test(view);
                Log.i(TAG, "userChat is " + userChat.getFilePath() + "====" + userChat.getName());
                switch (msgType) {
                    case "VOICE":
                        RecoderPlayer.release();
                        if (recodeThread != null) {
                            recodeThread.interrupt();
                            recodeThread.resetUI();
                        }
                        recodeThread = new RecodeThread(view, who, voiceTime);
                        recodeThread.start();
                        RecoderPlayer.playSound(userChat.getFilePath(), GroupChatActivity.this);
                        // adapter.updateItemView(view,who,2);
                        break;
                    case "IMAGE":
                        break;

                    case "VIDEO":

                        break;

                    default:

                        break;
                }
            }
        });

        //listview 长按事件
        lvMsgList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                Log.i(TAG, "position=" + position);
                // Utils.showToast2(GroupChatActivity.this, "你长按了第" + position + "项");
                PopupMenu popupMenu = new PopupMenu(GroupChatActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.menu_group_msg_opt, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.del:
                                GroupMsgDao.deleteMsg(userChatList.get(position), groupID);
                                userChatList.remove(position);
                                handler.sendEmptyMessage(FLUSH_MSG);
                                break;
                            case R.id.see_read_status:
                                //从服务器获取数据
                                UserChat userChat = userChatList.get(position);
                                ProtoClass.Msg.Builder msgBuilder = ProtoClass.Msg.newBuilder();
                                ProtoClass.GroupMsg.Builder groupMsgBuilder = ProtoClass.GroupMsg.newBuilder();
                                groupMsgBuilder.setGroupID(groupID)
                                        .setSenderName(userChat.getName())
                                        .setMsgTime(userChat.getMsgSendTime());
                                msgBuilder.setGroupMsg(groupMsgBuilder)
                                        .setMsgType(ProtoClass.MsgType.SEE_GROUP_MSG_READ_STATUS);
                                msgBuilder.setMsgUniqueTag(System.currentTimeMillis() + "");

                                Tools.startTcpRequest(msgBuilder, new Tools.TcpListener() {
                                    @Override
                                    public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                                        Log.i(TAG, "resoponse msg is " + responseMsg.toString());
                                        String readStatus = responseMsg.getGroupMsg().getText();
                                        Log.e("restatus1 is ", readStatus);
                                        Bundle bundle = new Bundle();
                                        bundle.putString("readStatus",readStatus);
                                        Message message = new Message();
                                        message.what=FLUSH_READ_STATUS;
                                        message.setData(bundle);
                                        handler.sendMessage(message);
                                        return true;
                                    }
                                });
                                break;
                        }
                        return true;
                    }
                });
                Log.e(TAG, "2    befor loop");
                popupMenu.show();
                return false;
            }
        });

        //listview 滚动监听
        //判断用户是否阅读信息
        lvMsgList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                Log.e(TAG, "int i=" + i);
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                // Log.e(TAG,"int i="+i+"\ti1="+i1+"\ti2="+i2);
                final int bengin = i, end = i1;
                if (userChatList.size() == 0||isHistoryMsg) {
                    return;
                }
                UserChat userChat = userChatList.get(i);
                msgIsReadThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        optMsgIsRead(bengin, end);
                    }
                });

            }
        });


        imgbtnFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < userChatList.size(); i++) {
                    Log.i("" + i, userChatList.get(i).toString());
                }
            }
        });


    }

    //判断消息是否阅读
    private void optMsgIsRead(int bengin, int end) {
        for (int i = bengin; i < end; i++) {
            UserChat userChat = userChatList.get(i);
            if (userChat.getIsRead() == 0) {
                ProtoClass.Msg.Builder msgBuilder = ProtoClass.Msg.newBuilder();
                ProtoClass.GroupMsg.Builder groupMsgBuilder = ProtoClass.GroupMsg.newBuilder();
                groupMsgBuilder.setGroupID(groupID)
                        .setSenderName(userChat.getName())
                        .setMsgTime(userChat.getMsgSendTime())
                        .setDataType(ProtoClass.DataType.FEED_BACK_ONE_READED);
                msgBuilder.setGroupMsg(groupMsgBuilder)
                        .setAccount(app.User_id)
                        .setMsgUniqueTag(userChat.getMsgUniqueTag())
                        .setMsgType(ProtoClass.MsgType.GROUP_CHAT);
                Tools.startTcpRequest(msgBuilder, new Tools.TcpListener() {
                    @Override
                    public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                        //groupChatListener.onProcess(responseMsg);
                        Log.i(TAG, "FEAD BACK MSG IS " + responseMsg.toString());
                        return false;
                    }
                });
            }
        }
    }



    // 播放语音线程
    private class RecodeThread extends Thread {
        public View view = null;
        public int position, who, voiceTime;

        public RecodeThread(View view, int who, int voiceTime) {
            //this.position=position;
            //  this.context=context;
            this.view = view;
            this.who = who;
            this.voiceTime = voiceTime;
        }

        /*  Handler handler1 = new Handler(
                  Looper.getMainLooper()) {*/
        Handler handler1 = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 0:
                        adapter.updateItemView(view, who, message.arg1);
                        break;
                    case 1:
                        adapter.updateItemView(view, who, 2);
                        break;
                }

            }
        };

        @Override
        public void run() {
            Log.i(TAG, "" + voiceTime);
            for (int i = 0; i <= ((int) voiceTime / 0.4); i++) {
                try {
                    Log.i(TAG, "status=" + i);
                    final int I = i;
                    Message message = new Message();
                    message.what = 0;
                    message.arg1 = i % 3;
                    handler1.sendMessage(message);
                    Log.i(TAG, "I%3=" + I % 3);
                    sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            handler.sendEmptyMessage(1);
            recodeThread = null;
        }

        public void resetUI() {
            handler1.sendEmptyMessage(1);
            recodeThread = null;
            RecoderPlayer.release();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ProtoClass.DataType dataType = ProtoClass.DataType.FILE;
        if (data == null) {
            return;
        }
        switch (resultCode) {
            case SEARCH_MSG_RESULT: //获取查询到的历史消息并且定位到该位置
                 String sendTime=data.getExtras().getString("sendTime");
                String textContent = data.getExtras().getString("textContent");
                if (textContent == null||sendTime==null) {
                    return;
                }
                int position = userChatList.size() - 1;
                for (int i = 0; i < userChatList.size(); i++) {
                    if ((sendTime.equals(userChatList.get(i).getMsgSendTime()))
                            &&(textContent.equals(userChatList.get(i).getStrContent()))) {
                        position = i;
                    }
                    ;
                }
                lvMsgList.setSelection(position);
                break;
            default:
                break;
        }
        switch (requestCode) {
            case REQUEST_CAREMA:
                return;
            case REQUEST_FILE:
                dataType = ProtoClass.DataType.IMAGE;
                break;
            case REQUEST_PHOTO:
                dataType = ProtoClass.DataType.IMAGE;
                break;

            default:
                break;
        }
        Uri uri = data.getData();
        if (uri == null) {
            return;
        }
        String path = PathUtils.getPath(this, uri);

        Log.i(TAG, "path = " + path);
        if (TextUtils.isEmpty(path)) {
            Utils.showToast2(this, "获取文件路径失败");
            return;
        }
        startFileUploadRequest(path, dataType, 0.0f);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (funcView.getHeight() != 0) {
                setGridViewHeight(0);
                return super.dispatchTouchEvent(ev);
            }
            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    public void setGridViewHeight(int height) {
        ViewGroup.LayoutParams lp = funcView.getLayoutParams();
        lp.height = height;
        funcView.setLayoutParams(lp);
    }


    // 把文件上传添加 聊天列表里，然后发送上传请求,上传成功后自动发送会话消息
    public void startFileUploadRequest(final String filePath, final ProtoClass.DataType dataType, final float voiceTime) {
        final String fileMD5 = getFileMD5(new File(filePath));
        List<String> fingerPrints = new ArrayList<String>();
        fingerPrints.add(fileMD5);
        ProtoClass.MsgForFileUpload.Builder fileBuilder = ProtoClass.MsgForFileUpload.newBuilder();
        fileBuilder.addAllFileFingerPrint(fingerPrints);
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setToken(token)
                .setFileUploadMsg(fileBuilder.build())
                .setMsgType(ProtoClass.MsgType.MsgType_UPLOAD_FILE);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                //***********
                List<UploadFileGroup.OnAfterUploadListener> listeners = new ArrayList<UploadFileGroup.OnAfterUploadListener>();
                listeners.add(new UploadFileGroup.OnAfterUploadListener() {
                    @Override
                    public void onUpload() {
                        Log.i(TAG, "发送消息通知群里人");
                        ProtoClass.FileInfo.Builder fileInfo = ProtoClass.FileInfo.newBuilder();
                        fileInfo.setPath(fileMD5)
                                .setName(filePath.substring(filePath.lastIndexOf("/") + 1));
                        ProtoClass.GroupMsg.Builder groupMsgBuilder = ProtoClass.GroupMsg.newBuilder();
                        groupMsgBuilder.setGroupID(groupID)
                                .setSenderName(ImApplication.User_id)
                                .setDataType(dataType)
                                .setFileInfo(fileInfo)
                                .setVoiceTime((int) voiceTime);

                        ProtoClass.Msg.Builder msgBuilder = ProtoClass.Msg.newBuilder();
                        msgBuilder.setToken(GroupChatActivity.this.token)
                                .setAccount(ImApplication.User_id)
                                .setGroupMsg(groupMsgBuilder)
                                .setMsgType(ProtoClass.MsgType.GROUP_CHAT)
                                .setMsgUniqueTag(System.currentTimeMillis() + "");

                        Tools.startTcpRequest(msgBuilder, new Tools.TcpListener() {
                            @Override
                            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                                //groupChatListener.onProcess(responseMsg);
                                Log.i(TAG, "resoponse msg is " + responseMsg.toString());
                                return false;
                            }
                        });
                        userChatList.add(groupChatHolder.protoToUserchat(msgBuilder, filePath));
                        GroupMsgDao.addMsg(msgBuilder, filePath, 0);
                        handler.sendEmptyMessage(FLUSH_MSG);
                    }
                });

                new UploadFileGroup(GroupChatActivity.this, filePath).setOnAfterUploadListeners((listeners)).onProcess(responseMsg);
                return true;
            }
        });

    }

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    public void setVoiceMsg(int voiceTime, String filePath) {
        Log.i(TAG, "setVoiceMsg");
        // userChatList = groupChatHolder.setVoiceMsg(voiceTime,filePath,groupID);
        //lvMsgList.setSelection(adapter.getCount() - 1);
    }

    @Override
    public void successGetGroupHistoryMsg(LinkedList<UserChat> groupHistoryMsgList) {
        if(groupHistoryMsgList==null||groupHistoryMsgList.isEmpty()){
            Utils.showToast2(this,"没有更多消息了！");
            return;
        }
        isHistoryMsg=true;
        userChatList.addAll(0,groupHistoryMsgList);
        adapter.notifyDataSetChanged();
        for(int i=0;i<groupHistoryMsgList.size();i++) {
            ProtoClass.GroupMsg.Builder groupMsgBuilder = ProtoClass.GroupMsg.newBuilder();
            switch (groupHistoryMsgList.get(i).getMsgType()){
                case "TEXT":
                    groupMsgBuilder.setDataType(ProtoClass.DataType.TEXT);
                    break;
                case "IMAGE":
                    groupMsgBuilder.setDataType(ProtoClass.DataType.IMAGE);
                    break;
                case "VIDEO":
                    groupMsgBuilder.setDataType(ProtoClass.DataType.VIDEO);
                    break;
                case "VOICE":
                    groupMsgBuilder.setDataType(ProtoClass.DataType.VOICE);
                    break;
                case "FILE":
                    groupMsgBuilder.setDataType(ProtoClass.DataType.FILE);
                    break;
                case "FEED_BACK_SEND_OK":
                    groupMsgBuilder.setDataType(ProtoClass.DataType.FEED_BACK_SEND_OK);
                    break;
                case "FEED_BACK_RECEIVED":
                    groupMsgBuilder.setDataType(ProtoClass.DataType.FEED_BACK_RECEIVED);
                    break;
                case "FEED_BACK_ONE_READED":
                    groupMsgBuilder.setDataType(ProtoClass.DataType.FEED_BACK_ONE_READED);
                    break;

            }
            ProtoClass.FileInfo.Builder fileInfoBuilder = ProtoClass.FileInfo.newBuilder();
            fileInfoBuilder.setPath(groupHistoryMsgList.get(i).getFilePath());
            fileInfoBuilder.setName(groupHistoryMsgList.get(i).getName());

            groupMsgBuilder.setGroupID(groupID)
                    .setMsgTime(groupHistoryMsgList.get(i).getMsgSendTime())
                    .setSenderName(groupHistoryMsgList.get(i).getName())
                   .setText(groupHistoryMsgList.get(i).getStrContent())
                   .setVoiceTime(groupHistoryMsgList.get(i).getVoiceTime())
                   .setFileInfo(fileInfoBuilder);

            fileInfoBuilder.setSize(Integer.parseInt(groupHistoryMsgList.get(i).getFileSize()));
            ProtoClass.Msg.Builder msgBuilder = ProtoClass.Msg.newBuilder();
            msgBuilder.setToken(GroupChatActivity.this.token)
                    .setAccount(ImApplication.User_id)
                    .setGroupMsg(groupMsgBuilder)
                    .setMsgType(ProtoClass.MsgType.VIEW_HISTOTY_MSG)
                    .setMsgUniqueTag(System.currentTimeMillis() + "");
            int who=0;
            if(groupHistoryMsgList.get(i).getName().equals(ImApplication.User_id)){
                who=1;
            }else{
                who=0;
            }
            GroupMsgDao.addMsg(msgBuilder,groupHistoryMsgList.get(i).getFilePath(),who);
        }

}
}