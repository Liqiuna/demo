package com.cst14.im.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cst14.im.Fragment.FriendFragment;
import com.cst14.im.Fragment.FriendInfo;
import com.cst14.im.R;
import com.cst14.im.adapter.newFriendAdapter;
import com.cst14.im.db.dao.FriendsDao;
import com.cst14.im.listener.NewFriendListener;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Utils;

import java.util.LinkedList;
import java.util.List;

import static com.cst14.im.Fragment.FriendFragment.setFriendRequestCountInFragment;
import static com.cst14.im.activity.MainActivity.setFriendRequestCount;
import static com.cst14.im.db.dao.FriendsDao.loadFriendRequest;
import static com.cst14.im.db.dao.FriendsDao.updateFriendRequest;

public class NewFriendActivity extends Activity implements NewFriendListener.NewFriendView, View.OnClickListener{
    public static NewFriendListener newFriendListener;
    public newFriendAdapter newFriendAdapter;
    private RelativeLayout rl_return_friend_fragment;//返回界面
    private TextView tv_add_in_new_friend;//跳转到查询界面
    private ListView lv_new_friend;//好友请求列表
    ImageView new_friend_image_head;//头像
    TextView tv_new_friend_item_name;//昵称
    TextView tv_is_agree_in_ragment;// 显示是否已同意文字
    LinearLayout new_friend_continer;
    Button btn_new_friend_agree,btn_new_friend_disagree;  //按钮 同意   拒绝
    private List<FriendInfo> requestFriendList = new LinkedList<>();
    public FriendsDao friendsDao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);
        friendsDao=new FriendsDao(getBaseContext());
        rl_return_friend_fragment= (RelativeLayout) findViewById(R.id.rl_return_friend_fragment);
        tv_add_in_new_friend= (TextView) findViewById(R.id.tv_add_in_new_friend);
        lv_new_friend= (ListView) findViewById(R.id.lv_new_friend);
        new_friend_continer = (LinearLayout)findViewById(R.id.new_friend_continer);
        new_friend_image_head = (ImageView)findViewById(R.id.new_friend_image_head);//头像
        tv_new_friend_item_name = (TextView)findViewById(R.id.tv_item_chat_msg);//昵称
        tv_is_agree_in_ragment =(TextView)findViewById(R.id.tv_is_agree_in_ragment);// 显示是否已同意文字
        btn_new_friend_agree =(Button)findViewById(R.id.btn_new_friend_agree);//同意
        btn_new_friend_disagree =(Button)findViewById(R.id.btn_new_friend_disagree);//拒绝
        requestFriendList=friendsDao.loadFriendRequest();
        newFriendAdapter=new newFriendAdapter(getBaseContext(),requestFriendList);
        lv_new_friend.setAdapter(newFriendAdapter);
        newFriendListener=new NewFriendListener(this);
        Tools.addPresenter(newFriendListener); //如果需要接收消息推送的话，就要添加监听
        tv_add_in_new_friend.setOnClickListener(this);
        rl_return_friend_fragment.setOnClickListener(this);
        FriendFragment.setFriendRequestCountInFragment(ImApplication.newFriendRequestNum);
        MainActivity.setFriendRequestCount(ImApplication.newFriendRequestNum);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_return_friend_fragment:
                finish();
                break;
            case R.id.tv_add_in_new_friend:
                Intent intent=new Intent(NewFriendActivity.this, SearchActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
    public static void response(FriendInfo response){
        boolean isAgree=false;
        if(response.getIsAgree()==1)isAgree=true;
        int curUserId = Integer.parseInt(ImApplication.User_id);
        ProtoClass.User user = ProtoClass.User.newBuilder().setUserID(curUserId).build();
        ProtoClass.User friend= ProtoClass.User.newBuilder().setNickName(response.getName()).setUserID(Integer.parseInt(response.getUser_id())).setIsAgree(isAgree).build();
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setUser(user)
                .addFriends(friend)
                .setMsgType(ProtoClass.MsgType.ADD_FRIEND_Response);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Log.e("发送", "发送失败,请检查你的网络");
            }

            //第二个参数是服务器返回的响应消息
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                newFriendListener.onProcess(responseMsg);
                return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
            }
        });
    }

    public void failedResponse(String errMsg){
        Toast.makeText(NewFriendActivity.this, "拒绝失败"+errMsg, Toast.LENGTH_SHORT).show();
    };
    public void updateUI(){
        requestFriendList.clear();
        requestFriendList=friendsDao.loadFriendRequest();
        newFriendAdapter=new newFriendAdapter(getBaseContext(),requestFriendList);
        lv_new_friend.setAdapter(newFriendAdapter);
        newFriendAdapter.notifyDataSetChanged();
    }
    public void sucessResponse(FriendInfo friend){
        updateFriendRequest(friend);
        updateUI();
        setFriendRequestCount(ImApplication.newFriendRequestNum);
        setFriendRequestCountInFragment(ImApplication.newFriendRequestNum);
        if(friend.getIsAgree()==1){//同意好友申请   保存好友
        Toast.makeText(NewFriendActivity.this, "添加成功,你们现在可以开始对话了", Toast.LENGTH_SHORT).show();//默认好友在分组0  “我的好友”
        LinkedList<FriendInfo> friendList=ImApplication.mapGroupFriends.get(0);
        friend.setListNo(0);
        friend.setMark(friend.getName());
        friendList.add(friend);
        ImApplication.mapGroupFriends.put(0, friendList);
        ImApplication.mapFriends.put(String.valueOf(friend.getUser_id()), friend);
        FriendFragment.updateUI();
        friendsDao.addFriend(friend);
        ImApplication.mainActivity.startSession(friend.getUser_id());
        return;
        }
        Toast.makeText(NewFriendActivity.this, "拒绝成功", Toast.LENGTH_SHORT).show();
    };
}

