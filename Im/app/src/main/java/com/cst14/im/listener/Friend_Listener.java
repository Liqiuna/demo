package com.cst14.im.listener;

import android.util.Log;
import android.widget.Toast;

import com.cst14.im.Fragment.FriendFragment;
import com.cst14.im.Fragment.FriendInfo;
import com.cst14.im.activity.LoginActivity;
import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.db.dao.FriendsDao;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.sessionUtils.ChatMsgBean;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by I_C_U on 2016/8/24.
 */
public class Friend_Listener implements iPresenter {
    private  ProtoClass.MsgType [] op = new ProtoClass.MsgType[] {ProtoClass.MsgType.DET_FRIEND,
            ProtoClass.MsgType.GET_OFFLINE_MSG,
            ProtoClass.MsgType.GET_FRIEND,
            ProtoClass.MsgType.ADD_Friend_GROUP,
            ProtoClass.MsgType.DET_Friend_GROUP,
            ProtoClass.MsgType.Remark_Friend,
            ProtoClass.MsgType.Remark_Friend_GROUP,
            ProtoClass.MsgType.ADD_FRIEND_Rquest, ProtoClass.MsgType.ADD_FRIEND_Response
    };
    Friend_View v;
    public ImApplication app;

    public Friend_Listener(Friend_View view){
        this.v= view;
        app= ImApplication.instance;
    }
    public void onProcess(ProtoClass.Msg msg) {
        Log.e("msg", msg.toString());
        if (!Arrays.asList(op).contains(msg.getMsgType())) return;
        switch (msg.getMsgType()) {
            case ADD_Friend_GROUP:{
                final String errMsg = msg.getErrMsg();
                if (msg.getResponseState().equals(ProtoClass.StatusCode.FAILED)) {
                    ImApplication.mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v.addGroupFailed(errMsg);
                        }
                    });
                    return;
                }
                final int listNo=msg.getFriendLists(0).getListNO();final String listName=msg.getFriendLists(0).getListName();
                ImApplication.mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.addGroupSucess(listNo,listName);
                    }
                });
            }break;
            case DET_Friend_GROUP: {
                final String errMsg = msg.getErrMsg();
                if (msg.getResponseState().equals(ProtoClass.StatusCode.FAILED)) {
                    ImApplication.mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v.deltGroupFailed(errMsg);
                        }
                    });
                    return;
                }
                ImApplication.mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.deltGroupSucess();
                    }
                });
            }break;
            case Remark_Friend_GROUP:{
                final String errMsg = msg.getErrMsg();
                if (msg.getResponseState().equals(ProtoClass.StatusCode.FAILED)) {
                    ImApplication.mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v.reMarkGroupFailed(errMsg);
                        }
                    });
                    return;
                }
                final int listNo=msg.getFriendLists(0).getListNO();final String listName=msg.getFriendLists(0).getListName();
                ImApplication.mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.reMarkGroupSucess(listNo,listName);
                    }
                });
            }
            break;
            case ADD_FRIEND_Rquest:{
                if(msg.hasResponseState())return;
                //运行到这里表明是别人发送过来的好友申请
                final FriendInfo requestRriend=new FriendInfo(msg.getUser().getUserID(),msg.getUser().getNickName(),0);//默认未处理
                ImApplication.mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.addFriendRequest(requestRriend);
                    }
                });
            }break;
            case ADD_FRIEND_Response:{
                if(msg.hasResponseState())return;
                //运行到这里表明是别人发送过来的好友同意/拒绝
                int isAgree=1;
                if(msg.getFriends(0).getIsAgree()==false){
                    isAgree=2;
                }
                final FriendInfo requestRriend=new FriendInfo(msg.getFriends(0).getUserID(),msg.getFriends(0).getNickName(),isAgree);
                ImApplication.mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.addFriendResponse(requestRriend);
                    }
                });
            }break;
            case DET_FRIEND: {
                if(!msg.hasResponseState()){//运行到这里面  表明是：被删除好友   -----对方主动删除好友 ，服务器发来同步消息
                    final int friendID=msg.getFriends(0).getUserID();
                    ImApplication.mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v.beDeleteFriend(friendID);
                        }
                    });
                    return;
                }
                final String errMsg = msg.getErrMsg();
                if (msg.getResponseState().equals(ProtoClass.StatusCode.FAILED)) {
                    ImApplication.mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v.deltFriendFailed(errMsg);
                        }
                    });
                    return;
                }
                ImApplication.mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.deltFriendSucess();
                    }
                });
            }break;
            case GET_FRIEND:{
                final String errMsg=msg.getErrMsg();
                if (msg.getResponseState().equals(ProtoClass.StatusCode.FAILED)) {
                    ImApplication.mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("GET_FRIEND failed :", errMsg);
                        }
                    });
                    break;
                }

                List<ProtoClass.FriendList>FriendGroup=msg.getFriendListsList();   //先取出好友分组
                ImApplication.clearFriend();//从服务器获得最新版本的好友信息 直接清除以前的
                for(ProtoClass.FriendList element:FriendGroup){
                    //从服务器获得最新版本的好友信息 直接清除以前的
                    ImApplication.mapFriendGroup.put(element.getListNO(),element.getListName());
                    LinkedList<FriendInfo> friendlist= new LinkedList<FriendInfo>();
                    ImApplication.mapGroupFriends.put(element.getListNO(),friendlist);
                    if(ImApplication.Friend_group_MAX_number<element.getListNO())
                        ImApplication.Friend_group_MAX_number=element.getListNO();//记录好友分组的最大组号
                }

                //获取到的好友加到全局变量 APP。好友map 里面
                List<ProtoClass.User> frendInfoList = msg.getFriendsList();
                for (ProtoClass.User element:frendInfoList){             //FriendInfo的构造函数  取出来并且新建好友信息   加到map里面的 list
                    int ListNo=element.getListNO();//默认在分组0;我的好友
                    if(!element.hasListNO()||!ImApplication.mapFriendGroup.containsKey(ListNo))
                        ListNo=0;
                    FriendInfo friend_item=new FriendInfo(element.getUserID(),element.getNickName(),element.getRemark(),ListNo);
                    ImApplication.mapGroupFriends.get(ListNo).add(friend_item);
                    ImApplication.mapFriends.put(friend_item.getUser_id(), friend_item);
                }
                //获取离线好友添加请求
                List<ProtoClass.User> frendRequestList = msg.getNewFriendRequestList();
                for (ProtoClass.User element:frendRequestList){             //FriendInfo的构造函数  取出来并且新建好友信息   加到map里面的 list
                    FriendInfo friend_item=new FriendInfo(element.getUserID(),element.getNickName(),0);
                    FriendsDao.addFriendRequest(friend_item);
                }
                ImApplication.mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.sunccessGetFriendsInFragment();
                    }
                });
            }break;
            case Remark_Friend:{
                final String errMsg = msg.getErrMsg();
                if (msg.getResponseState().equals(ProtoClass.StatusCode.FAILED)) {
                    ImApplication.mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v.reMarkFriendFailed(errMsg);
                        }
                    });
                    return;
                }
                final FriendInfo friend=new FriendInfo(msg.getFriends(0).getUserID(),msg.getFriends(0).getNickName(),msg.getFriends(0).getRemark(),msg.getFriends(0).getListNO());
                ImApplication.mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.reMarkFriendSucess(friend);
                    }
                });
            }
            break;

            case GET_OFFLINE_MSG: {
                if (msg.getResponseState() == ProtoClass.StatusCode.FAILED) {
                    final String errMsg2 = msg.getErrMsg();
                    ImApplication.mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v.failedGetOfflineMsg(errMsg2);
                        }
                    });return;}
                final LinkedList<Map<String,ChatMsgBean>> offlineMsgList= returnAllOfflineMsg(msg);//离线信息在这里获取
                ImApplication.mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.sunccessGetOfflineMsg(offlineMsgList);
                    }
                });
            }break;
        }


    }


    //避免onProcess太乱，写多了一个returnAllOfflineMsg函数
    public LinkedList<Map<String,ChatMsgBean>> returnAllOfflineMsg(ProtoClass.Msg msg){
        LinkedList<Map<String,ChatMsgBean>> offlineMsgList = new LinkedList<Map<String,ChatMsgBean>> ();//存储所有的离线消息
        if (msg.getOfflineMsgList().isEmpty())return offlineMsgList;
        //因目前系统群聊功能暂未加入 此处仅处理私聊信息
        for (ProtoClass.PersonalMsg personalMsg :msg.getOfflineMsgList()) {
            Map<String,ChatMsgBean> singleMsg= new HashMap<String,ChatMsgBean>();
            final String msgId = personalMsg.getMsgId();
            final String from = personalMsg.getSenderID() + "";
            final ProtoClass.DataType dataType = personalMsg.getMsgType();
            ChatMsgBean bean = null;
            switch (dataType) {
                case TEXT:
                    bean = ChatMsgBean.newTextMsg(personalMsg.getContent(), from);
                    break;
                case VOICE:
                    bean = ChatMsgBean.newVoiceMsg(personalMsg.getFileName(), personalMsg.getFileLen(), from);
                    bean.setFileFingerPrint(personalMsg.getContent());
                    break;
                case FILE:
                    bean = ChatMsgBean.newFileMsg( personalMsg.getFileName(), personalMsg.getFileLen(), from);
                    bean.setFileFingerPrint(personalMsg.getContent());
                    break;
                case VIDEO:
                    bean = ChatMsgBean.newVideoMsg(personalMsg.getFileName(), personalMsg.getFileLen(), from);
                    bean.setFileFingerPrint(personalMsg.getContent());
                    bean.setThumbFingerPrint(personalMsg.getThumbFingerPrint());
                    break;
                default:
                    break;
            }
            if (bean==null)continue;
//            bean.msgId = msgId;
            bean.sentTime = personalMsg.getSendTime();
            singleMsg.put(from, bean);
            offlineMsgList.add(singleMsg);
        }
        Log.e("获取到的离线消息数",offlineMsgList.size()+"");
        return offlineMsgList;
    }




    public interface Friend_View {
        void addFriendResponse(FriendInfo friend);
        void addFriendRequest(FriendInfo friend);
        void deltFriendSucess();  //删除好友成功
        void deltFriendFailed(String errMsg); //删除好友失败
        void beDeleteFriend(int friendID);//被删除好友
        void addGroupSucess(int listNo,String listName);//增加分组成功
        void addGroupFailed(String errMsg); //删除分组失败
        void deltGroupSucess();//删除好友分组成功
        void deltGroupFailed(String errMsg);//删除好友分组失败
        void sunccessGetOfflineMsg(LinkedList<Map<String,ChatMsgBean>> offlineMsgList);
        void failedGetOfflineMsg(String errMsg);
        void sunccessGetFriendsInFragment();
        void reMarkFriendSucess(FriendInfo friend);
        void reMarkFriendFailed(String errMsg);
        void reMarkGroupSucess(int listNo,String listName);
        void reMarkGroupFailed(String errMsg);

    }

}
