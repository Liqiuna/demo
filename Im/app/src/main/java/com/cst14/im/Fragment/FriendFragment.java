package com.cst14.im.Fragment;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cst14.im.R;

import com.cst14.im.activity.MainActivity;
import com.cst14.im.activity.NewFriendActivity;
import com.cst14.im.adapter.FriendListAdapter;
import com.cst14.im.db.dao.FriendsDao;
import com.cst14.im.layoutView.FriendMenuLayout;
import com.cst14.im.listener.Friend_Listener;

import com.cst14.im.activity.GroupActivity;

import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Utils;
import com.cst14.im.utils.sessionUtils.ChatMsgBean;
import com.cst14.im.utils.sessionUtils.SessionHolder;

import java.util.LinkedList;
import java.util.Map;

import static com.cst14.im.activity.MainActivity.setFriendRequestCount;
import static com.cst14.im.db.dao.FriendsDao.KeepfriendsToDB;
import static com.cst14.im.db.dao.FriendsDao.delGroup;
import static com.cst14.im.db.dao.FriendsDao.loadFriendRequest;
import static com.cst14.im.db.dao.FriendsDao.updateFriendInfo;

/**
 * Created by I_C_U on 2016/8/24.
 */
public class FriendFragment extends Fragment implements Friend_Listener.Friend_View, View.OnClickListener {

    private static Friend_Listener friend_listener;
    private static FriendListAdapter friendListAdapter;
    public ExpandableListView expandablelistview;
    private FriendMenuLayout menu;
    public Context context;
    public static ImApplication app;
    private static int nowGroupPos,nowChildPos;
    private String tem_detFriendID;
    private ImageView img_avatar;//对方头像
    private TextView tv_FrindNick;
    private static TextView tv_red_cycle_in_friend_fragment;//对方昵称
    public FriendsDao friendsDao;//数据库接口0
    private RelativeLayout rlToGroupChat,rl_fragment_new_friend;
    private static final int groupPos=-1;
    private static final int childPos=1;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = ImApplication.instance.getMainActivity();
        friendsDao=new FriendsDao(context);
        View view = View.inflate(context,R.layout.friend_list_fragment,null);
        tv_red_cycle_in_friend_fragment =(TextView)view.findViewById(R.id.tv_red_cycle_in_friend_fragment);
        tv_red_cycle_in_friend_fragment.setAlpha(0f);
        expandablelistview= (ExpandableListView) view.findViewById(R.id.lvContact);
        expandablelistview.setGroupIndicator(null);
        img_avatar = (ImageView) view.findViewById(R.id.image_head);//头像
        tv_FrindNick =(TextView)view.findViewById(R.id.tv_friend_item_name);//名字
        app= ImApplication.instance;
        friend_listener = new Friend_Listener(this);
        friendListAdapter=new FriendListAdapter(context);
        expandablelistview.setAdapter(friendListAdapter);
        Tools.addPresenter(friend_listener); //如果需要接收消息推送的话，就要添加监听
        expandablelistview.setLongClickable(true);

        expandablelistview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                nowGroupPos = (Integer) view.getTag(R.id.friend_Group_continer); //参数值是在setTag时使用的对应资源id号
                nowChildPos = (Integer) view.getTag(R.id.friend_list_continer);
                //长按-- 菜单
                if (nowChildPos != groupPos) {//长按的是好友item
                    showMenu(childPos);
                    return true;
                }
                showMenu(groupPos);//长按的是分组item 父类
                return true;
            }

        });
        expandablelistview.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                FriendInfo friend_item = (FriendInfo) friendListAdapter.getChild(groupPosition, childPosition);
                ImApplication.mainActivity.mSessionsFragment.startSession(friend_item.getUser_id(), true);
                return false;
            }
        });

        expandablelistview.setOverScrollMode(View.OVER_SCROLL_ALWAYS);  //OVER_SCROLL_NEVER

        rlToGroupChat= (RelativeLayout) view.findViewById(R.id.rl_fragment_friend_groupchat);
        rlToGroupChat.setOnClickListener(this);
        rl_fragment_new_friend= (RelativeLayout) view.findViewById(R.id.rl_fragment_new_friend);
        rl_fragment_new_friend.setOnClickListener(this);
        Log.e("friend:", ImApplication.mapGroupFriends.toString());
        if(ImApplication.isFirstLoginInThisPhone){
            Log.e("need to getFriendlists;", "111111");
            getFriendList();
        }else{
            getOfflineMsg();

        }
        loadFriendRequest();
        MainActivity.setFriendRequestCount(ImApplication.newFriendRequestNum);
        setFriendRequestCountInFragment(ImApplication.newFriendRequestNum);
        return view;
    }
    // 设置显示多少个好友申请
    public static void setFriendRequestCountInFragment(int friendRequestCount) {
        if (friendRequestCount <= 0) {
            tv_red_cycle_in_friend_fragment.setAlpha(0f);
            return;
        }
        tv_red_cycle_in_friend_fragment.setText("" + friendRequestCount);
        tv_red_cycle_in_friend_fragment.setAlpha(1f);
    }

    public void showMenu(final int childOrGroup) {
        menu = (FriendMenuLayout) View.inflate(getContext(), R.layout.layout_friend_long_click_menu, null);
        menu.setContactFragment(this,childOrGroup);
        final String  Title,Message;
        if(childOrGroup==groupPos){
            Title="删除该分组";
            Message="删除分组，将该分组中的好友全部移至“我的好友”";
        }else{
            Title="删除好友";
            Message="好友删除，将同时删除与该好友的聊天记录";
        }

        ListView lv_option = (ListView) menu.findViewById(R.id.lv_menu);
        lv_option.setAdapter(menu.getMenuOptionAdapter());
        lv_option.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                SessionHolder holder = sessionHolders.get(curSessionIndex);
                ContentValues values = new ContentValues();
                switch (position) {
                    case 0:
                        new AlertDialog.Builder(getActivity()).
                                setTitle(Title).//标题
                                setMessage(Message).//提示内容 //  IM的 可以在这加上删掉该好友聊天记录！！！
                                setPositiveButton("删除", new DialogInterface.OnClickListener() {//确定
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                if (childOrGroup == groupPos) {
                                    deleteGroup(nowGroupPos);
                                } else {
                                    FriendInfo friend = ImApplication.mapGroupFriends.get(nowGroupPos).get(nowChildPos);
                                    deleteFriend(friend.getUser_id());
                                    tem_detFriendID = friend.getUser_id();
                                }

                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {//取消
                            @Override
                            public void onClick(DialogInterface arg1, int witch) {
                                //no to do
                            }
                        }).show();
                        break;
                    case 1:
                        //加载布局
                        View contentView = LayoutInflater.from(ImApplication.mainActivity)
                                .inflate(R.layout.add_group_or_remark, null);
                        final AlertDialog dialog = new AlertDialog.Builder(ImApplication.mainActivity).setView(contentView).create();
                        final TextView tv_add_or_remark_name = (TextView) contentView.findViewById(R.id.tv_add_or_remark_name);
                        final TextView tv_message_detail = (TextView) contentView.findViewById(R.id.tv_message_detail);
                        final EditText ed_add_or_remark_namel = (EditText) contentView.findViewById(R.id.et_add_or_remark_name);
                        final View btn_add_or_remark_name_cancle = contentView.findViewById(R.id.btn_add_or_remark_name_cancle);
                        final View btn_add_or_remark_name_ok = contentView.findViewById(R.id.btn_add_or_remark_name_ok);
                        if (childOrGroup == groupPos) {
                            tv_add_or_remark_name.setText("修改分组名");
                            tv_message_detail.setText("请输入新的分组名称");
                        } else {
                            tv_add_or_remark_name.setText("修改好友备注");
                            tv_message_detail.setText("请输入新的好友备注");
                        }
                        btn_add_or_remark_name_cancle.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        btn_add_or_remark_name_ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String name = ed_add_or_remark_namel.getText().toString();
                                if (childOrGroup == groupPos) {
                                    reMarkGroup(name);//运行到这里表明是修改分组名
                                    dialog.dismiss();
                                    return;
                                }
                                reMarkFriend(name);//运行到这里表明是修改好友备注
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                        break;

                }
                dismissMenu();
            }
        });
        ImApplication.mainActivity.getRootViewGroup().addView(menu);
    }

    public void dismissMenu() {
        ImApplication.mainActivity.getRootViewGroup().removeView(menu);
    }

    public static void updateUI() {
        friendListAdapter.notifyDataSetChanged();
    }

    //删除分组
    public static void deleteGroup(int GroupID) {
        int id = Integer.parseInt(app.User_id);
        ProtoClass.User user = ProtoClass.User.newBuilder().setUserID(id).build();
        ProtoClass.FriendList friendList = ProtoClass.FriendList.newBuilder().setListNO(GroupID).build();
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setUser(user)
                .addFriendLists(friendList)
                .setMsgType(ProtoClass.MsgType.DET_Friend_GROUP);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Log.e("删除好友分组：", e.toString());
            }

            //第二个参数是服务器返回的响应消息
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                friend_listener.onProcess(responseMsg);
                return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
            }
        });
    }

    //删除好友
    public static void deleteFriend(String friendID){
        //delete session and relatived msgData if exists
        ImApplication.mainActivity.mSessionsFragment.delSessionItemByFriendId(friendID);

        int id = Integer.parseInt(app.User_id);
        int friendIDInt=Integer.parseInt(friendID);
        ProtoClass.User user = ProtoClass.User.newBuilder().setUserID(id).build();
        ProtoClass.User friend= ProtoClass.User.newBuilder().setUserID(friendIDInt).build();
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setUser(user)
                .addFriends(friend)
                .setMsgType(ProtoClass.MsgType.DET_FRIEND);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Log.e("删除好友：", e.toString());
            }

            //第二个参数是服务器返回的响应消息
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                friend_listener.onProcess(responseMsg);
                return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
            }
        });
    }

    //修改好友备注
    public static void reMarkFriend(String name){
        int id = Integer.parseInt(app.User_id);
        ProtoClass.User user = ProtoClass.User.newBuilder().setUserID(id).build();
        FriendInfo old_friend=ImApplication.mapGroupFriends.get(nowGroupPos).get(nowChildPos);
        int friendID=Integer.parseInt(old_friend.getUser_id());//根据位置取出好友ID
        int listNo=old_friend.getListNo();
        String nick=old_friend.getName();
        ProtoClass.User friend= ProtoClass.User.newBuilder().setUserID(friendID).setNickName(nick).setRemark(name).setListNO(listNo).build();
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setUser(user)
                .addFriends(friend)
                .setMsgType(ProtoClass.MsgType.Remark_Friend);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Log.e("修改备注：", e.toString());
            }

            //第二个参数是服务器返回的响应消息
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                friend_listener.onProcess(responseMsg);
                return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
            }
        });
    }
    //修改分组名
    public static void reMarkGroup(String name){
        int id = Integer.parseInt(app.User_id);
        //根据位置取出分组ID
        ProtoClass.User user = ProtoClass.User.newBuilder().setUserID(id).build();
        ProtoClass.FriendList Friendlist= ProtoClass.FriendList.newBuilder().setListNO(nowGroupPos).setListName(name).build();
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setUser(user)
                .addFriendLists(Friendlist)
                .setMsgType(ProtoClass.MsgType.Remark_Friend_GROUP);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Log.e("修改分组名：", e.toString());
            }

            //第二个参数是服务器返回的响应消息
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                friend_listener.onProcess(responseMsg);
                return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
            }
        });
    }
    //获取离线消息
    public void getOfflineMsg(){
        String account = ImApplication.User_id;
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setAccount(account)
                .setMsgType(ProtoClass.MsgType.GET_OFFLINE_MSG);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Toast.makeText(getActivity(), "离线请求发送失败", Toast.LENGTH_SHORT).show();
            }

            //第二个参数是服务器返回的响应消息
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                friend_listener.onProcess(responseMsg);
                return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
            }
        });
    }
    //获取好友列表
    public  void getFriendList(){
        int id = Integer.parseInt(ImApplication.User_id);
        ProtoClass.User user = ProtoClass.User.newBuilder().setUserID(id).build();
        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder().setMsgType(ProtoClass.MsgType.GET_FRIEND);
        builder.setUser(user).build();
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendSuccess(String msgId) {//发送成功
            }

            @Override
            public void onSendFail(Exception e) {//发送失败

            }

            //第二个参数是服务器返回的响应消息
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                friend_listener.onProcess(responseMsg);
                return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
            }
        });
    }


    //-------------------好友分组-------------------
    @Override
    public void reMarkGroupSucess(int listNo,String listName){
        ImApplication.mapFriendGroup.put(listNo,listName);
        friendsDao.updateGroup(listNo, listName);
        updateUI();
    };//重命名分组成功
    @Override
    public void reMarkGroupFailed(String errMsg){
        Toast.makeText(getActivity(), "修改分组名失败！"+errMsg, Toast.LENGTH_SHORT).show();
        nowGroupPos=0;
        nowChildPos=0;
    };//重命名分组失败
    @Override
    public void deltGroupSucess(){
        LinkedList<FriendInfo> friendlist=ImApplication.mapGroupFriends.get(nowGroupPos);
        for(FriendInfo friend_Item:friendlist){
            friend_Item.setListNo(0);
            ImApplication.mapGroupFriends.get(0).add(friend_Item);
            ImApplication.mapFriends.put(friend_Item.getUser_id(), friend_Item);
            updateFriendInfo(friend_Item);
        }
        ImApplication.mapGroupFriends.remove(nowGroupPos);
        ImApplication.mapFriendGroup.remove(nowGroupPos);
        delGroup(nowGroupPos);
        updateUI();
    };//删除好友分组成功
    @Override
    public void deltGroupFailed(String errMsg){
        Toast.makeText(getActivity(), "删除好友分组失败！"+errMsg, Toast.LENGTH_SHORT).show();
        nowGroupPos=0;
        nowChildPos=0;
    };//删除好友分组失败
    @Override
    public void addGroupSucess(int listNo,String listName){
        ImApplication.mapFriendGroup.put(listNo,listName);
        LinkedList<FriendInfo> friendlist= new LinkedList<FriendInfo>();
        ImApplication.mapGroupFriends.put(listNo,friendlist);
        friendsDao.addGroup(listNo, listName);
        updateUI();
    }//增加分组成功
    @Override
    public void addGroupFailed(String errMsg){
        Toast.makeText(getActivity(), "增加分组失败！"+errMsg, Toast.LENGTH_SHORT).show();
        ImApplication.Friend_group_MAX_number--;
    }; //删除好友失败

    //-------------------好友-------------------
    @Override
    public void addFriendResponse(FriendInfo friend){
        if(friend.getIsAgree()==1){//同意好友申请   保存好友
            Toast.makeText(getActivity(), "添加成功,你们可以开始对话啦！", Toast.LENGTH_SHORT).show();
            LinkedList<FriendInfo> friendList=ImApplication.mapGroupFriends.get(0);
            friend.setListNo(0);//默认好友在分组0  “我的好友”
            friend.setMark(friend.getName());
            friendList.add(friend);
            ImApplication.mapGroupFriends.put(0, friendList);
            ImApplication.mapFriends.put(String.valueOf(friend.getUser_id()),friend);
            updateUI();
            friendsDao.addFriend(friend);
            ImApplication.mainActivity.startSession(friend.getUser_id());
            return;
        }
        Toast.makeText(getActivity(), friend.getUser_id()+":拒绝添加你为好友", Toast.LENGTH_SHORT).show();
    };//同意好友申请   保存好友 / 拒绝添加你为好友
    @Override
    public void addFriendRequest(FriendInfo friend){
        friendsDao.addFriendRequest(friend);
        friendsDao.loadFriendRequest();
        setFriendRequestCountInFragment(ImApplication.newFriendRequestNum);
        setFriendRequestCount(ImApplication.newFriendRequestNum);
    };//别人发来的好友请求
    @Override
    public void deltFriendSucess(){
        Toast.makeText(getActivity(), "成功删除好友！", Toast.LENGTH_SHORT).show();
        ImApplication.mapGroupFriends.get(nowGroupPos).remove(nowChildPos);
        if(!tem_detFriendID.equals("")) {
//            ImApplication.mainActivity.mSessionsFragment.delItem(Integer.parseInt(tem_detFriendID));
            ImApplication.mapFriends.remove(tem_detFriendID);
        }
        tem_detFriendID="";
        updateUI();
        friendsDao.delFriend(tem_detFriendID);
    }; //删除好友成功
    @Override
    public void deltFriendFailed(String errMsg){
        Toast.makeText(getActivity(), "删除好友失败！"+errMsg, Toast.LENGTH_SHORT).show();
        tem_detFriendID="";
    }; //删除好友失败

    @Override
    public void  beDeleteFriend(int friendID){
        Toast.makeText(getActivity(), "您的好友"+friendID + "：从好友列表中删除了你！", Toast.LENGTH_SHORT).show();
        String friendIDStr=String.valueOf(friendID);
        FriendInfo friend=ImApplication.mapFriends.get(friendIDStr);
        ImApplication.mapGroupFriends.get(friend.getListNo());
        ImApplication.mapGroupFriends.get(friend.getListNo()).remove(friend);
        updateUI();
//        ImApplication.mainActivity.mSessionsFragment.delItem(friendID);
        ImApplication.mapFriends.remove(friendIDStr);
        friendsDao.delFriend(friendIDStr);
    }//被删除好友   -----对方主动删除好友 ，服务器发来同步消息
    @Override
    public void reMarkFriendSucess(FriendInfo friend){
        ImApplication.mapFriends.get(friend.getUser_id()).setMark(friend.getMark());
        ImApplication.mapGroupFriends.get(friend.getListNo()).get(nowChildPos).setMark(friend.getMark());
        friendsDao.updateFriendInfo(friend);
        updateUI();
    };//备注好友成功
    @Override
    public void reMarkFriendFailed(String errMsg){
        Toast.makeText(getActivity(), "修改好友备注失败！"+errMsg, Toast.LENGTH_SHORT).show();
        nowGroupPos=0;
        nowChildPos=0;
    };//备注好友失败

    public void sunccessGetFriendsInFragment(){
        updateUI();
        getOfflineMsg();
        KeepfriendsToDB();
        loadFriendRequest();
        MainActivity.setFriendRequestCount(ImApplication.newFriendRequestNum);
        setFriendRequestCountInFragment(ImApplication.newFriendRequestNum);
    }//获取好友列表成功

    //-------------------离线消息-------------------
    @Override
    public void sunccessGetOfflineMsg(LinkedList<Map<String,ChatMsgBean>> offlineMsgList) {
        // 显示离线消息
        try {
            for (Map<String, ChatMsgBean> mOffLineMsg : offlineMsgList) {
                for (Map.Entry<String, ChatMsgBean> entry : mOffLineMsg.entrySet()) {
                    SessionHolder holder = ImApplication.mainActivity.mSessionsFragment.startSession(entry.getKey(), false);
                    if (holder.has(entry.getValue().sentTime)) {
                        continue;  // 重复的数据不再添加
                    }
                    holder.acceptMsg(entry.getValue(), false,true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void failedGetOfflineMsg(String errMsg) {
        Log.e("获取离线信息失败", "----------");
        Utils.showToast2(getActivity(), errMsg);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_fragment_friend_groupchat:
                Intent intent1=new Intent(getContext(), GroupActivity.class);
                startActivity(intent1);
                break;
            case R.id.rl_fragment_new_friend:
                Intent intent=new Intent(getContext(), NewFriendActivity.class);
                startActivity(intent);
                break;
        }
    }


}