package com.cst14.im.utils.groupChatUtils;

import android.util.Log;

import com.cst14.im.activity.GroupChatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by qi on 16-9-2.
 */
public class DownloadFile {
    final static String TAG = "DownloadFile";
    GroupChatActivity activity;

    public DownloadFile(GroupChatActivity activity) {
        this.activity = activity;
    }

    public DownloadFile(){}

    public void Download(String urlStr,String filePath) {
        Log.i(TAG,"begin download file\nthe urlStr is "+urlStr+"\tthe filePath is\t"+filePath);
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            URL url=new URL(urlStr);
            InputStream is = url.openStream();
            OutputStream os = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len = 0;
            //从输入六中读取数据,读到缓冲区中
            while((len = is.read(buffer)) > 0) {
                os.write(buffer,0,len);
            }
            //关闭输入输出流
            is.close();
            os.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
