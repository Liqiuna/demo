package com.cst14.im.utils.pictureUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by zxm on 2016/8/28.
 */
public class httprequestPresenter {
    public static String  uploadBitmap(final String uploadUrl, final String filePath) {
        File file = new File(Environment.getExternalStorageDirectory(),filePath.substring(filePath.lastIndexOf("/") + 1));
        final String mfilePath=file.getAbsolutePath();
        String imageUrl=uploadUrl+"/"+filePath.substring(filePath.lastIndexOf("/") + 1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    String nextLine = "\r\n";
                    String dividerStart = "--";
                    String boundary = "******";
                    URL url = new URL(uploadUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setChunkedStreamingMode(1024 * 256);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("Charset", "UTF-8");
                    connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                    dos.writeBytes(dividerStart + boundary + nextLine);
                    //设置与上传文件相关的信息

                    dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\""
                            + filePath.substring(filePath.lastIndexOf("/") + 1) + "\"" + nextLine);
                    dos.writeBytes(nextLine);
                    FileInputStream fis = new FileInputStream(mfilePath);
                    byte[] buffer = new byte[1024 * 32];
                    int count;


                    while ((count = fis.read(buffer)) != -1) {
                        dos.write(buffer, 0, count);

                    }
                    fis.close();
                    dos.writeBytes(nextLine);
                    dos.writeBytes(dividerStart + boundary + dividerStart + nextLine);
                    dos.flush();
                    // 读取服务器返回结果
                    InputStream is = connection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, "utf-8");
                    BufferedReader br = new BufferedReader(isr);
                    String result = br.readLine();
                    if(result!=null){
                     Log.d("tag: result",result);}
                    dos.close();
                    is.close();

                    connection.disconnect();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }  }).start();
        return imageUrl;

    }


}

