package com.cst14.im.activity;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import com.cst14.im.R;
import com.cst14.im.layoutView.VideoCaptureLayout;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.fileUtils.FileUtils;
import com.cst14.im.utils.fileUtils.UploadFile;
import com.cst14.im.utils.sessionUtils.ChatMsgBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoActivity extends Activity {
    private VideoCaptureLayout layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        layout = (VideoCaptureLayout) findViewById(R.id.vl_video_layout);
        final ChatActivity chatActivity = ImApplication.instance.getCurSessionHolder().chatActivity;
        layout.setOnVideoCaptureFinishListener(new VideoCaptureLayout.OnVideoCaptureFinishListener() {
            @Override
            public void onFinish(Bitmap thumb, File videoFile) {
                if(thumb==null&&videoFile==null){
                    Log.e("两个参数都null","----------");
                    finish();
                    return;
                }
                File targetThumb = new File(ImApplication.cacheDir, FileUtils.getFileNameNoEx(videoFile.getName())+"vdo_thumb.png");
                boolean ok = FileUtils.saveBitmap(thumb,targetThumb);
                if(!ok){
                    Log.e("缩略图保存失败","----------");
                    return;
                }
                Log.e("222",""+targetThumb.getAbsolutePath());
                Log.e("发送上传文件请求","----------");

                ChatMsgBean videoFileMsg = ChatMsgBean.newVideoMsg(videoFile.getAbsolutePath(),"","");
                videoFileMsg.isSending = true;
                chatActivity.getFileUploadQueue().add(videoFileMsg);
                ImApplication.instance.getCurSessionHolder().acceptMsg(videoFileMsg,false,true);
                List<String> fingerPrints = new ArrayList<String>();
                fingerPrints.add(videoFileMsg.getFileFingerPrint());
                fingerPrints.add(videoFileMsg.getThumbFingerPrint());

                ProtoClass.MsgForFileUpload.Builder fileBuilder = ProtoClass.MsgForFileUpload.newBuilder();
                fileBuilder.addAllFileFingerPrint(fingerPrints);
                ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                builder.setToken(chatActivity.token)
                        .setFileUploadMsg(fileBuilder.build())
                        .setMsgType(ProtoClass.MsgType.MsgType_UPLOAD_FILE);

                Tools.startTcpRequest(builder, new Tools.TcpListener() {
                    @Override
                    public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                        List<UploadFile.OnAfterUploadListener>listeners = new ArrayList<UploadFile.OnAfterUploadListener>();
                        UploadFile.OnAfterUploadListener listener = new UploadFile.OnAfterUploadListener() {
                            @Override
                            public void onUpload(ChatMsgBean fileMsg, int index) {
                                Log.e("第"+index+"个文件上传完成","-------");
                            }
                        };
                        // 两个文件上传完，都执行以上的方法
                        listeners.add(listener);
                        listeners.add(listener);

                        UploadFile.OnAllFinishListener allFinishListener = new UploadFile.OnAllFinishListener() {
                            @Override
                            public void onAllFinish(ChatMsgBean fileMsg) {
                                // TODO  发送会话消息
                                String fName = new File(fileMsg.getFileName()).getName();
                                ProtoClass.PersonalMsg.Builder personalMsg = ProtoClass.PersonalMsg.newBuilder();
                                personalMsg.addRecverID(Integer.parseInt(fileMsg.getParentSessionHolder().userAccount))
                                        .setSenderID(Integer.parseInt(ImApplication.User_id))
                                        .setMsgType(ProtoClass.DataType.VIDEO)
                                        .setFileName(fName)
                                        .setFileLen(fileMsg.getFileSize())
                                        .setMsgId(fileMsg.msgId)
                                        .setContent(fileMsg.getFileFingerPrint())
                                        .setThumbFingerPrint(fileMsg.getThumbFingerPrint());
                                final ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
                                builder.setToken(chatActivity.token)
                                        .setPersonalMsg(personalMsg.build())
                                        .setMsgType(ProtoClass.MsgType.MsgType_SESSION);
                                final Tools.TcpListener tcpListener = new Tools.TcpListener() {
                                    @Override
                                    public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                                        ImApplication.mainActivity.sessionLisener.onProcess(responseMsg);
                                        return true;
                                    }
                                };
                                Tools.startTcpRequest(builder,tcpListener);
                            }
                        };

                        new UploadFile(chatActivity)
                                .setOnAfterUploadListeners(listeners)
                                .setOnAllFinishListener(allFinishListener)
                                .onProcess(responseMsg);
                        return true;
                    }
                });

               finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        layout.onDestroy();
        super.onBackPressed();
        overridePendingTransition(0, R.anim.fade_out);
    }
}