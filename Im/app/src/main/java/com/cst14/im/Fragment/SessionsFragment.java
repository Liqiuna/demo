package com.cst14.im.Fragment;

/**
 * Created by MRLWJ on 2016/6/28.
 */

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.activity.ChatActivity;
import com.cst14.im.db.dao.PersonalMsgDao;
import com.cst14.im.layoutView.MenuLayout;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.StringParser;
import com.cst14.im.utils.Utils;
import com.cst14.im.utils.sessionUtils.ChatMsgBean;
import com.cst14.im.utils.sessionUtils.SessionHolder;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class SessionsFragment extends Fragment {

    private ListView lvSessionList;
    private int curSessionIndex;
    private SessionListAdapter sessionAdapter;
    private HashMap<String, SessionHolder> sessionMap;
    private String sessionsFilePath;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionsFilePath = ImApplication.mainActivity.getFilesDir() + File.separator + ImApplication.User_id + "-sessions.ser";
        loadAllSessions();
        loadSessionToMap();
    }

    private void loadSessionToMap() {
        sessionMap = new HashMap<String, SessionHolder>();
        for (SessionHolder holder : sessionHolders) {
            sessionMap.put(holder.userAccount, holder);
        }
        ImApplication.instance.setSessionHolderMap(sessionMap);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        lvSessionList = (ListView) view.findViewById(R.id.lv_test);
        sessionAdapter = new SessionListAdapter();
        lvSessionList.setAdapter(sessionAdapter);
        lvSessionList.setLongClickable(true);
        lvSessionList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                curSessionIndex = position;
                showMenu();
                return true;
            }
        });
        lvSessionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SessionHolder holder = sessionHolders.get(position);
                startSession(holder.userAccount, true);
            }
        });
        lvSessionList.setOverScrollMode(View.OVER_SCROLL_NEVER);
        return view;
    }

    public SessionHolder startSession(String account, boolean showActivity) {
        SessionHolder holder = sessionMap.get(account);
        if (holder == null) {
            holder = new SessionHolder();
            holder.userAccount = account;
            holder.lastBrifMsgTime = holder.getCurTime("yyyy-MM-dd HH:mm:ss");
            initSessionHolder(holder);
            sessionHolders.add(holder);
            sessionMap.put(account, holder);
            sessionAdapter.notifyDataSetChanged();
            PersonalMsgDao.addHolder(holder);
        }
        if (showActivity) {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            ImApplication.instance.setCurSessionList(holder.msgList);
            ImApplication.instance.setCurSessionHolder(holder);
            startActivity(intent);
        }
        sessionMap.put(account, holder);
        return holder;
    }

    private MenuLayout menu;

    public void showMenu() {
        menu = (MenuLayout) View.inflate(getContext(), R.layout.layout_session_long_click_menu, null);
        menu.setContactFragment(this);
        if (sessionHolders.get(curSessionIndex).isFixTop()) {
            menu.getOptionArr()[0] = "取消置顶";
        } else {
            menu.getOptionArr()[0] = "消息置顶";
        }
        if (sessionHolders.get(curSessionIndex).isNoTip()) {
            menu.getOptionArr()[3] = "恢复提醒";
        } else {
            menu.getOptionArr()[3] = "消息免打扰";
        }
        ListView lv_option = (ListView) menu.findViewById(R.id.lv_menu);
        lv_option.setAdapter(menu.getMenuOptionAdapter());
        lv_option.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SessionHolder holder = sessionHolders.get(curSessionIndex);
                ContentValues values = new ContentValues();
                switch (position) {
                    case 0:
                        if (holder.isFixTop()) {
                            holder.contentView.setBackgroundResource(R.drawable.session_def_selector);
                            holder.setFixTop(false);
                        } else {
                            holder.contentView.setBackgroundResource(R.drawable.session_top_selector);
                            holder.setFixTop(true);
                            sessionToTop(holder);
                        }
                        values.put("fix_top", holder.isFixTop());
                        PersonalMsgDao.db.update("session_holders",
                                values,
                                "my_id=? and friend_id=?",
                                new String[]{ImApplication.User_id, holder.userAccount});
                        break;
                    case 1:
                        delItem(curSessionIndex);
                        break;
                    case 2:
                        holder.setUnReadTag();
                        break;
                    case 3:
                        holder.setNoTip(!holder.isNoTip());
                        ImApplication.mainActivity.setTotalUnReadMsgCount(calcTotalUnReadedMsgCount());
                        holder.updateRedCycle();
                        values.put("no_tip", holder.isNoTip());
                        PersonalMsgDao.db.update("session_holders",
                                values,
                                "my_id=? and friend_id=?",
                                new String[]{ImApplication.User_id, holder.userAccount});
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

    /**
     * delete session item by its index in list
     * @param index
     */
    public void delItem(int index) {
        delSessionItemByFriendId(sessionHolders.get(index).userAccount);
    }

    public void delSessionItemByFriendId(String friendId){
        boolean ok = PersonalMsgDao.delHolder(friendId);
        if (!ok) {
            Log.e("delete session","failed to delete session item in sqlite");
        }
        sessionMap.remove(friendId);
        for(SessionHolder holder:sessionHolders){
            if(friendId.equals(holder.userAccount)){
                sessionHolders.remove(holder);
                break;
            }
        }
        sessionAdapter.notifyDataSetChanged();
        ImApplication.mainActivity.setTotalUnReadMsgCount(calcTotalUnReadedMsgCount());
    }

    private LinkedList<SessionHolder> sessionHolders;

    public void saveAllSessions() {
        for (SessionHolder holder : sessionHolders) {
            PersonalMsgDao.upDateHolder(holder);
        }
    }

    public void loadAllSessions() {
        sessionHolders = PersonalMsgDao.getAllHolder();
        for (SessionHolder holder : sessionHolders) {
            initSessionHolder(holder);
        }
    }

    private void initSessionHolder(final SessionHolder holder) {
        View v = View.inflate(getContext(), R.layout.item_session, null);
        holder.lastChatMsg = (TextView) v.findViewById(R.id.tv_item_chat_msg);
        if (!TextUtils.isEmpty(holder.lastBrifMsg)) {
            holder.lastChatMsg.setText(holder.lastBrifMsg);
        }
        holder.lastMsgTime = (TextView) v.findViewById(R.id.tv_item_chat_time);
        if (!TextUtils.isEmpty(holder.lastBrifMsgTime)) {
            holder.lastMsgTime.setText(StringParser.getBrifTime(holder.lastBrifMsgTime));
        }
        holder.head = (ImageView) v.findViewById(R.id.iv_item_head);
        holder.ivNoTip = (ImageView) v.findViewById(R.id.iv_no_tip);
        holder.nickName = (TextView) v.findViewById(R.id.tv_item_nick_name);
        FriendInfo friendInfo = ImApplication.mapFriends.get(holder.userAccount);
        holder.nickNameStr = friendInfo==null?"null":friendInfo.getMark();
        if (!TextUtils.isEmpty(holder.nickNameStr)) {
            holder.nickName.setText(holder.nickNameStr);
        }
        holder.tvRedCycle = (TextView) v.findViewById(R.id.tv_red_cycle_in_session);
        holder.tvRedCycle.setAlpha(0f);
        holder.ivNoTipSmallRedCycle = (ImageView) v.findViewById(R.id.iv_small_red_cycle_in_item);
        holder.ivNoTipSmallRedCycle.setImageAlpha(0);
        holder.contentView = v;
        holder.sessionsFragment = this;
        holder.mapToSendingMsgBean = new HashMap<String, ChatMsgBean>();
        if (holder.isFixTop()) {
            v.setBackgroundResource(R.drawable.session_top_selector);
        } else {
            v.setBackgroundResource(R.drawable.session_def_selector);
        }
        v.setTag(holder);
        holder.initListeners();
        holder.setOnAcceptMsgListener(new SessionHolder.OnAcceptMsgListener() {
            @Override
            public void onAccept(ChatMsgBean bean) {
                holder.updateLastMsg();
                holder.updateRedCycle();
                if (!holder.isNoTip()) {
                    ImApplication.mainActivity.setTotalUnReadMsgCount(calcTotalUnReadedMsgCount());
                    sessionToTop(holder);
                }
                saveAllSessions();
            }
        });
        holder.addOnMsgReadedListener(new SessionHolder.OnMsgReadedListener() {
            @Override
            public void onMsgReaded(int unReadMsgCount, int count) {
                holder.updateRedCycle();
                ImApplication.mainActivity.setTotalUnReadMsgCount(calcTotalUnReadedMsgCount());
                ContentValues values = new ContentValues();
                if (count == 0 && unReadMsgCount == 0) { //标记为未读
                    values.put("unread_msg_count", 1);
                } else {
                    values.put("unread_msg_count", holder.unReadMsgCount);
                }
                PersonalMsgDao.db.update("session_holders",
                        values,
                        "my_id=? and friend_id=?",
                        new String[]{ImApplication.User_id, holder.userAccount});
            }
        });
        for (ChatMsgBean bean : holder.msgList) {
            bean.setParentSessionHolder(holder);
            String tip = bean.msgSentFinished ? "" : "[离线]";
            bean.showMsgSentState(tip, true);
        }
        holder.updateRedCycle();
        ImApplication.mainActivity.setTotalUnReadMsgCount(calcTotalUnReadedMsgCount());
        holder.setNoTip(holder.isNoTip());
    }

    private void sessionToTop(SessionHolder curSession) {
        try {
            sessionHolders.remove(curSession);
            int insertIndex;
            for (insertIndex = 0; insertIndex < sessionHolders.size(); insertIndex++) {
                SessionHolder h = sessionHolders.get(insertIndex);
                if (!h.isFixTop()) {
                    break;
                }
            }
            sessionHolders.add(insertIndex, curSession);
            sessionAdapter.notifyDataSetChanged();
        } catch (Exception e) {
        }
    }

    private int calcTotalUnReadedMsgCount() {
        int totalUnReadMsgCount = 0;
        for (SessionHolder h : sessionHolders) {
            if (h.isNoTip()) {
                continue;
            }
            totalUnReadMsgCount += h.getUnReadMsgCount();
        }
        return totalUnReadMsgCount;
    }

    private class SessionListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return sessionHolders.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SessionHolder holder = sessionHolders.get(position);
            holder.updateRedCycle();
            return holder.contentView;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    }
}