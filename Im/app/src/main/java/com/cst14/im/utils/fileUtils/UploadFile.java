package com.cst14.im.utils.fileUtils;

import android.text.TextUtils;
import android.util.Log;

import com.cst14.im.activity.ChatActivity;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Utils;
import com.cst14.im.utils.sessionUtils.ChatMsgBean;
import com.yolanda.nohttp.FileBinary;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.StringRequest;
import java.util.ArrayList;
import java.util.List;


public class UploadFile {

    public interface OnAfterUploadListener{
        void onUpload(ChatMsgBean fileMsg, int index);
    }

    public UploadFile setOnAfterUploadListeners(List<OnAfterUploadListener> onAfterUploadListeners) {
        this.onAfterUploadListeners = onAfterUploadListeners;
        return this;
    }

    private  List<OnAfterUploadListener> onAfterUploadListeners;

    public interface  OnAllFinishListener{
        void onAllFinish(ChatMsgBean fileMsg);
    }
    private boolean checkIfAllFinish(List<Boolean> list){
        for(Boolean finish:list){
            if(!finish){
                return false;
            }
        }
        return true;
    }

    public UploadFile setOnAllFinishListener(OnAllFinishListener onAllFinishListener) {
        this.onAllFinishListener = onAllFinishListener;
        return this;
    }

    private OnAllFinishListener onAllFinishListener;

    ChatActivity activity;

    public UploadFile(ChatActivity activity) {
        this.activity = activity;
    }

    public void onProcess(ProtoClass.Msg msg) {
        if (msg.getMsgType() != ProtoClass.MsgType.MsgType_UPLOAD_FILE) {
            return;
        }
        if (msg.getResponseState() != ProtoClass.StatusCode.SUCCESS) {
            final String errMsg = msg.getErrMsg();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.showToast(activity.getBaseContext(), errMsg);
                }
            });
            return;
        }
        if (activity.getFileUploadQueue().size() == 0) {
            return;
        }

        final ChatMsgBean fileMsg = activity.getFileUploadQueue().poll();
        List<Boolean> shouldAvoidUploads_readOnly = msg.getFileUploadMsg().getShouldAvoidUploadList();
        final List<Boolean> shouldAvoidUploads = new ArrayList<>(shouldAvoidUploads_readOnly);
        for(int i = 0;i<shouldAvoidUploads.size();i++){
            if(shouldAvoidUploads.get(i)&&onAfterUploadListeners.get(i)!=null){
                onAfterUploadListeners.get(i).onUpload(fileMsg,i);
                Log.e("极速上传","" +i);
            }
        }
        if(checkIfAllFinish(shouldAvoidUploads)){
            if(onAllFinishListener!=null){
                onAllFinishListener.onAllFinish(fileMsg);
            }
            return;
        }

        List<String> fileTokenForHttpList = msg.getFileUploadMsg().getFileTokenForHttpList();

        for(int i = 0;i<fileTokenForHttpList.size();i++){
            String fileTokenForHttp = fileTokenForHttpList.get(i);
            if(TextUtils.isEmpty(fileTokenForHttp)){
                continue;
            }
            String url = ImApplication.FILE_SERVER_HOST + "/file";
            StringRequest request = new StringRequest(url, RequestMethod.POST);
            request.add("file", new FileBinary(i==0?fileMsg.getFile():fileMsg.getThumbFile()));
            request.add("fileToken", fileTokenForHttp);
            request.add("fingerPrint", i==0?fileMsg.getFileFingerPrint():fileMsg.getThumbFingerPrint());
            // request 中最后一个参数的值总是莫名其妙多出来一个回车，下一行随便加一个参数，多出来的回车就不会影响上面的值了
            request.add("a", "a");
            final int finalI = i;
            Tools.startHttpRequest(request, new Tools.HttpRequestListener() {
                @Override
                public void onSuccee(final String resp) {
                    OnAfterUploadListener listener = onAfterUploadListeners.get(finalI);
                    if(listener!=null){
                       listener.onUpload(fileMsg, finalI);
                    }
                    shouldAvoidUploads.set(finalI,true);
                    if(checkIfAllFinish(shouldAvoidUploads)){
                        if(onAllFinishListener!=null){
                            onAllFinishListener.onAllFinish(fileMsg);
                        }
                        return;
                    }
                }

                @Override
                public void onFail(String url, Exception e) {

                }
            }, Tools.HttpCacheMode.NET_DATA_FIRST);
        }
    }
}
