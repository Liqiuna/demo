package com.cst14.im.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cst14.im.R;
import com.cst14.im.bean.AnnounceBean;
import com.cst14.im.bean.LinkedArrayMap;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;

/**
 * Created by Administrator on 2016/8/28 0028.
 */
public class GroupAnnounceActivity extends Activity implements AdapterView.OnItemClickListener{

    private int groupID;
    private Context ctx;
    private ImApplication app;
    private ListView lv_announce;
    private ImageButton ib_announceEdit;

    private LinkedArrayMap<Integer, AnnounceBean> announceList;
    private AnnounceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_announce);

        ctx = this;
        app = (ImApplication) getApplication();
        groupID = getIntent().getIntExtra("groupID", -1);
        initView();
        getAnnounce();
    }

    private void initView() {
        announceList = new LinkedArrayMap<Integer, AnnounceBean>();
        lv_announce = (ListView) findViewById(R.id.lv_announce);
        ib_announceEdit = (ImageButton) findViewById(R.id.btn_announceCreate);
        ib_announceEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, AnnounceCreateActivity.class);
                intent.putExtra("groupID", groupID);
                startActivityForResult(intent, 1);
            }
        });
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        adapter = new AnnounceAdapter();
        lv_announce.setAdapter(adapter);
        lv_announce.setOnItemClickListener(this);
    }

    private void getAnnounce() {
        String name = ImApplication.instance.getCurAccount();
        System.out.println("name:"+name);

        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setAccount(ImApplication.User_id);
        builder.setMsgType(ProtoClass.MsgType.GET_ANNOUNCE);
        builder.setGroupID(groupID);
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                if (ProtoClass.MsgType.GET_ANNOUNCE != responseMsg.getMsgType()) return false;
                if (ProtoClass.StatusCode.SUCCESS   != responseMsg.getResponseState()) {
                    Toast.makeText(ctx, responseMsg.getErrMsg(), Toast.LENGTH_SHORT).show();
                    return true;
                }

                for (ProtoClass.GroupAnnounce announce : responseMsg.getAnnounceList()) {
                    AnnounceBean bean = new AnnounceBean(announce);
                    announceList.addFirst(bean.getAnnounceID(), bean);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
                return true;
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, AnnounceInfoActivity.class);
        intent.putExtra("announceBean", announceList.get(position));
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data == null) return;

        if (resultCode == AnnounceCreateActivity.CREATE_ANNOUNCE_SUCCESS) {
            AnnounceBean bean = (AnnounceBean) data.getExtras().get("announceBean");
            if (bean == null) {
                return;
            }
            announceList.addFirst(bean.getAnnounceID(), bean);
            adapter.notifyDataSetChanged();
        }
        if (resultCode == AnnounceInfoActivity.DELETE_ANNOUNCE_SUCCESS) {
            Integer announceID = data.getIntExtra("announceID", -1);
            announceList.remove(announceID);
            adapter.notifyDataSetChanged();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private class AnnounceAdapter extends BaseAdapter {

        private LayoutInflater inflater = LayoutInflater.from(ctx);

        public AnnounceAdapter() {}
        @Override
        public int getCount() {
            return announceList.size();
        }
        @Override
        public AnnounceBean getItem(int position) {
            return announceList.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View view, ViewGroup parent) {

            ViewHolder holder;
            if (view == null) {
                view = inflater.inflate(R.layout.item_announce, null);
                holder = new ViewHolder();
                holder.tv_title = (TextView) view.findViewById(R.id.tv_title);
                holder.tv_sender = (TextView) view.findViewById(R.id.tv_sender);
                holder.tv_time = (TextView) view.findViewById(R.id.tv_time);
                holder.tv_content = (TextView) view.findViewById(R.id.tv_name);
                view.setTag(holder);
            }
            holder = (ViewHolder) view.getTag();
            holder.tv_title.setText(getItem(position).getTitle());
            holder.tv_sender.setText(getItem(position).getSender());
            holder.tv_time.setText(getItem(position).getSendTime());
            holder.tv_content.setText(getItem(position).getContent());

            return view;
        }

        private class ViewHolder {
            TextView tv_title;
            TextView tv_sender;
            TextView tv_time;
            TextView tv_content;
        }
    }
}
