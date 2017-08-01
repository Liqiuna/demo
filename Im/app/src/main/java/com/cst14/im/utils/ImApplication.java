package com.cst14.im.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.cst14.im.Fragment.FriendInfo;
import com.cst14.im.activity.MainActivity;
import com.cst14.im.bean.User;
import com.cst14.im.listener.Friend_Listener;
import com.cst14.im.tools.SocketParams;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.sessionUtils.ChatMsgBean;
import com.cst14.im.utils.sessionUtils.SessionHolder;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by MRLWJ on 2016/8/22.
 */
public class ImApplication extends Application {
    public static String getLoginToken() {
        return loginToken;
    }
    private Context context;
    public static LinkedList<Map<String,ChatMsgBean>> mOffLineMsgList;
    public static HashMap<Integer, LinkedList<FriendInfo>> mapGroupFriends = new HashMap<Integer, LinkedList<FriendInfo>>();//存放好友 : 组号-   好友链表
    public static HashMap<Integer, String> mapFriendGroup = new HashMap<Integer, String>();//存放好友分组  分组号-分组名
    public static HashMap<String, FriendInfo> mapFriends = new HashMap<String, FriendInfo>();//存放好友 : ID-好友 的map  这个现在不用了
    public static int Friend_group_MAX_number=1;//好友分组的最大组号
    public static boolean isFirstLoginInThisPhone =false;//标记用户是否第一次登陆  无用户记录
    public static int newFriendRequestNum=0;
    public static void clearFriend(){
        mapFriendGroup.clear();//从服务器获得最新版本的好友信息 直接清除以前的
        mapGroupFriends.clear();
        mapFriends.clear();
    }

    public static void setLoginToken(String loginToken) {
        ImApplication.loginToken = loginToken;
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("login_token", loginToken);
        editor.commit();
    }
    public static float density; //屏幕密度
    public static String User_id = "";
    private static String loginToken;
    public static ImApplication instance;  // 应用的实例
    private static User curUser;
    private static int mRoleID;
    public static final int SUPER_MANAGER = 1;
    public static final int NORMAL_MANAGER = 2;
    public static final int CUSTOMER_SERVICE = 3;
    public static final int NORMAL_USER = 4;

    public static SharedPreferences getSp() {
        return sp;
    }

    public static MainActivity mainActivity;

    private static SharedPreferences sp;

//    public static final String HOST = "119.29.209.92";//云服务器
    public static final String HOST = "192.168.0.116";//自行添加119.29.209.92
    public static final int PORT = 8080;
    public static final String FILE_SERVER_HOST = "http://" + HOST + ":8888";
    public static final String GAME_SERVER_HOST = "http://" + HOST + ":9090";
    @Override
    public void onCreate() {
        super.onCreate();
        Tools.init(this, new SocketParams(HOST, PORT));
        sp = getSharedPreferences("globalSpData", 0);
        loginToken = sp.getString("login_token", "");
        instance = this;
        context=getApplicationContext();

        boolean sdCardExists = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);
        if (!sdCardExists) {
            Log.e("sd卡不存在", "--------");
            dataPath = getFilesDir().toString();
        } else {
            dataPath = Environment.getExternalStorageDirectory().toString() + File.separator + getPackageName();
        }
        photoDir = dataPath + File.separator + "photo";
        cacheDir = dataPath + File.separator + "cache";
        fileDir = dataPath + File.separator + "file";
        videoDir = dataPath + File.separator + "video";
        iconDir = dataPath + File.separator + "icon";
        voiceDir = dataPath + File.separator + "voice";

        String[] dirArr = {photoDir, cacheDir, fileDir, videoDir, iconDir, voiceDir};
        for (String dir : dirArr) {
            File file = new File(dir);
            if (!file.exists()) {
                file.mkdirs();
            }
        }

    }

    public HashMap<String, SessionHolder> getSessionHolderMap() {
        return sessionHolderMap;
    }

    public void setSessionHolderMap(HashMap<String, SessionHolder> sessionHolderMap) {
        this.sessionHolderMap = sessionHolderMap;
    }

    public String getCurAccount() {
        return curAccount;
    }

    public void setCurAccount(String curAccount) {
        this.curAccount = curAccount;
    }

    private String curAccount;

    private HashMap<String, SessionHolder> sessionHolderMap;

    public SessionHolder getCurSessionHolder() {
        return curSessionHolder;
    }

    public void setCurSessionHolder(SessionHolder curSessionHolder) {
        this.curSessionHolder = curSessionHolder;
    }

    private SessionHolder curSessionHolder;

    public LinkedList<ChatMsgBean> getCurSessionList() {
        return curSessionList;
    }

    public void setCurSessionList(LinkedList<ChatMsgBean> curSessionList) {
        this.curSessionList = curSessionList;
    }

    private LinkedList<ChatMsgBean> curSessionList;

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    private static String dataPath;
    public static String photoDir, cacheDir, fileDir, videoDir, iconDir, voiceDir;

    public Context getContext() {
        return context;
    }

    public User getCurUser() {
        return curUser;
    }

    public void setCurUser(User curUser) {
        this.curUser = curUser;
    }

    public static void setMyRoleID(int mRoleID){
        ImApplication.mRoleID = mRoleID;
    }

    public static int getMyRoleID(){
        return ImApplication.mRoleID;
    }
}
