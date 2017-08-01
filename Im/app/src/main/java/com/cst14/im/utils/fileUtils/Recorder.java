package com.cst14.im.utils.fileUtils;

import android.media.MediaRecorder;
import android.util.Log;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.sessionUtils.ChatMsgBean;

import java.io.File;
import java.util.UUID;

/**
 * Created by MRLWJ on 2016/7/18.
 */
public class Recorder {
    private static final String LOG_TAG = "recoder";
    private String mFileName;
    private long begTime;
    private MediaRecorder mRecorder;
    private boolean isOk;
    public void start() {
        try {
            mFileName = UUID.randomUUID().toString() + ".3gp";
            File directory = new File(ImApplication.voiceDir, mFileName).getParentFile();
            if (!directory.exists() && !directory.mkdirs()) {
                Log.i(LOG_TAG, "Path to file could not be created");
            }
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mRecorder.setOutputFile(ImApplication.voiceDir + File.separator + mFileName);
            mRecorder.prepare();
            isOk = true;
            begTime = System.currentTimeMillis();
            mRecorder.start();
        } catch (Exception e) {}
    }

    public ChatMsgBean stop(boolean isReadyToCancel) {
        try {
            if (!isOk) {
                return null;
            }
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            isOk = false;
            File voiceFile = new File(ImApplication.voiceDir , mFileName);
            if (isReadyToCancel) {
                if (voiceFile.exists()) {
                    voiceFile.delete();
                }
                return null;
            }
            ChatMsgBean voiceMsgBean = ChatMsgBean.newVoiceMsg(voiceFile.getAbsolutePath(), ""+(System.currentTimeMillis() - begTime), "");
            voiceMsgBean.isSending = true;
            return voiceMsgBean;
        } catch (Exception e) {}
        return null;
    }
}
