package com.cst14.im.utils.fileUtils;

import android.content.Context;
import android.util.Log;

import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import java.io.File;

/**
 * Created by MRLWJ on 2016/8/2.
 *
 * 以指定的方式处理文件，如果有必要的话会先下载文件
 */

public class FileOpenHelper {
    private Context context;
    private String mfilePath, fileFingerPrint;

    /**
     * @param context
     * @param filePath        要打开的文件的全路径
     * @param fileFingerPrint for download file
     */
    public FileOpenHelper(Context context, String filePath, String fileFingerPrint, FileTrigger fileTrigger) {
        this.context = context;
        this.mfilePath = filePath;
        this.fileFingerPrint = fileFingerPrint;
        this.fileTrigger = fileTrigger;
    }

    public void exec() {
        final File fileToOpen = new File(mfilePath);
        String url = ImApplication.FILE_SERVER_HOST + "/" + fileFingerPrint;
        Tools.startDownloadRequest(url, fileToOpen.getParent(), fileToOpen.getName(), new Tools.ToolsDownloadListener() {
            @Override
            public void onDownloadError(int what, Exception exception) {
                Log.e("文件下载失败",exception.toString());
            }
            @Override
            public void onDownloadFinish(String dir, String fileName) {
                if (fileTrigger != null) {
                    fileTrigger.trigger(fileToOpen);
                }
            }
        }, Tools.HttpCacheMode.LOCAL_DATA_FIRST);
    }
    public interface FileTrigger {
        void trigger(File file);
    }

    public void setFileTrigger(FileTrigger fileTrigger) {
        this.fileTrigger = fileTrigger;
    }

    private FileTrigger fileTrigger;
}
