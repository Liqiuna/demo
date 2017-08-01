package com.cst14.im.utils.groupChatUtils;

import android.text.TextUtils;
import android.util.Log;

import com.cst14.im.activity.GroupChatActivity;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Utils;
import com.cst14.im.utils.sessionUtils.ChatMsgBean;
import com.yolanda.nohttp.FileBinary;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.StringRequest;

import java.io.File;
import java.util.List;

/**
 * Created by qi on 16-9-2.
 */
public class UploadFileGroup {
    private final static String TAG = "GroupChatUploadFile";

    public interface OnAfterUploadListener {
        void onUpload();
    }

    public UploadFileGroup setOnAfterUploadListeners(List<OnAfterUploadListener> onAfterUploadListeners) {
        this.onAfterUploadListeners = onAfterUploadListeners;
        return this;
    }

    private List<OnAfterUploadListener> onAfterUploadListeners;

    public interface OnAllFinishListener {
        void onAllFinish(ChatMsgBean fileMsg);
    }

    private boolean checkIfAllFinish(List<Boolean> list) {
        for (Boolean finish : list) {
            if (!finish) {
                return false;
            }
        }
        return true;
    }

    public UploadFileGroup setOnAllFinishListener(OnAllFinishListener onAllFinishListener) {
        this.onAllFinishListener = onAllFinishListener;
        return this;
    }

    private OnAllFinishListener onAllFinishListener;

    GroupChatActivity activity;
    String filePath; //需要上传的文件绝对路径
    String fingerPrint;

    public UploadFileGroup(GroupChatActivity activity, String filePath) {
        this.activity = activity;
        this.filePath = filePath;
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

        Log.i(TAG, "jinru le");
        Log.e(TAG, msg.toString());

        List<String> fileTokenForHttpList = msg.getFileUploadMsg().getFileTokenForHttpList();

        Log.i(TAG, "fileTokenForHttpList.size()\t" + fileTokenForHttpList.size());
        for (int i = 0; i < fileTokenForHttpList.size(); i++) {
            final int finalI = i;
            String fileTokenForHttp = fileTokenForHttpList.get(i);
            Log.i(TAG, "fileTokenForHttp is "+fileTokenForHttp);
            if (TextUtils.isEmpty(fileTokenForHttp)) {
                Log.i(TAG, "fileTokenForHttp is empty");
                OnAfterUploadListener listener = onAfterUploadListeners.get(finalI);
                if (listener != null) {
                    Log.i(TAG, "onSuccee");
                    listener.onUpload();
                }
            continue;
        }
        String url = ImApplication.FILE_SERVER_HOST + "/file";
        Log.i(TAG, "the url is\t" + url);
        StringRequest request = new StringRequest(url, RequestMethod.POST);
        request.add("file", new FileBinary(new File(filePath)));
        request.add("fileToken", fileTokenForHttp);
        request.add("fingerPrint", activity.getFileMD5(new File(filePath)));
        // request 中最后一个参数的值总是莫名其妙多出来一个回车，下一行随便加一个参数，多出来的回车就不会影响上面的值了
        request.add("a", "a");

        Tools.startHttpRequest(request, new Tools.HttpRequestListener() {
            @Override
            public void onSuccee(final String resp) {
                OnAfterUploadListener listener = onAfterUploadListeners.get(finalI);
                if (listener != null) {
                    listener.onUpload();
                }
            }

            @Override
            public void onFail(String url, Exception e) {
                Log.i(TAG, "onFail");
            }
        }, Tools.HttpCacheMode.NET_DATA_FIRST);


    }

}

    public void onResult(String resp) {

    }

}
