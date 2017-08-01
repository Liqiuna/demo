package com.cst14.im.layoutView;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.activity.ChatActivity;


/**
 * Created by MRLWJ on 2016/7/18.
 */
public class VoiceRecordingLayout extends RelativeLayout {
    private ChatActivity activity;

    public void setIvVoiceRecordingIcon(ImageView ivVoiceRecordingIcon) {
        this.ivVoiceRecordingIcon = ivVoiceRecordingIcon;
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        tvVoiceRecordingMsg.setText("");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private ImageView ivVoiceRecordingIcon;
    private TextView tvVoiceRecordingMsg;
    public VoiceRecordingLayout(Context context) {
        super(context);
        initView(context);
        activity = (ChatActivity) context;
    }

    private void initView(Context context){
        LinearLayout.LayoutParams paramsMatchParent = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        View root =  View.inflate(context, R.layout.layout_voice_recording, null);
        root.setLayoutParams(paramsMatchParent);
        addView(root);
        ivVoiceRecordingIcon = (ImageView) findViewById(R.id.iv_voice_recording_icon);
        tvVoiceRecordingMsg = (TextView) findViewById(R.id.tv_voice_recording_msg);
    }
    public void setVoiceLevel(int level){
        String idStr = "voice_64_"+level;
        int id = activity.getResources().getIdentifier(idStr, "drawable", activity.getPackageName());
        ivVoiceRecordingIcon.setBackgroundResource(id);
    }
    public void setVoiceRecordingMsg(String msg){
        tvVoiceRecordingMsg.setText(msg);
    }
}
