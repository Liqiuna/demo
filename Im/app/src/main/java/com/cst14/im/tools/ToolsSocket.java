package com.cst14.im.tools;

import android.os.SystemClock;
import android.util.Log;

import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.protobuf.ProtoClass;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class ToolsSocket extends Socket {
    private boolean isConnecting = false;
    private SocketParams params;
    public static final int LINK_TIMEOUT = 3000;
    public static final int MAX_FRAME_LEN = 10240;
    public static final String SIGN = "142857";
    public static final String FRAME_LEN_FORMAT = "0x12345678";
    int signLen = SIGN.getBytes().length;
    int frameLenAttrLen = FRAME_LEN_FORMAT.getBytes().length;


    public ToolsSocket(SocketParams params) {
        this.params = params;
    }

    /**
     * init socket connect once
     *
     * @return
     */
    private boolean init() {
        Log.e("初始化一次", "------------");
        if (isConnecting) {
            return true;
        }
        try {
            this.connect(new InetSocketAddress(params.getHost(), params.getPort()), LINK_TIMEOUT);
        } catch (Exception e) {
            Log.e("初始化出现异常 -1 ", "-- " + e.toString());
            return false;
        }
        try {
            this.setKeepAlive(true);
        } catch (SocketException e2) {
            Log.e("初始化出现异常 - 2", "-- " + e2.toString());
            return false;
        }
        isConnecting = true;
        return true;
    }

    /**
     * 当初始化成功之后，重启服务器，初始化就会一直失败：
     * java.net.SocketException: java.io.IOException: fcntl failed: EBADF (Bad file number)
     * 得把后台线程全部关闭 才能初始化成功
     */
    public void run() {
        while (!init()) {
            SystemClock.sleep(3000);
            Log.e("初始化间隔睡眠", "------------");
        }
        Log.e("初始化完成", "------------");
        Tools.toolsSocketMap.put(params, this);
        if (onConnectedListener != null) {
            onConnectedListener.onConnected(this);
        }
        while (isConnecting) {
            ProtoClass.Msg msg = readMsg();
            if (msg == null) {
                continue;
            }
            /**
             * if the coming msg lack of the msgUniqueTag means this msg must sent by server automatically
             * so let iPresenter in tcpListenerList handle it
             */
            String msgUniqueTag = msg.getMsgUniqueTag();
            Tools.TcpListener listener = Tools.tcpListenerMap.get(msgUniqueTag);
            if (listener != null) {
                boolean shouldBreak = listener.onResponse(msgUniqueTag, msg);
                Tools.tcpListenerMap.remove(msgUniqueTag);
                if (shouldBreak) {
                    continue;
                }
            }
            for (iPresenter i : Tools.tcpListenerList) {
                i.onProcess(msg);
            }
        }
        Log.e("连接结束", "------------");
        Tools.toolsSocketMap.remove(params);
    }

    // 原来的函数
//    private ProtoClass.Msg readMsg() {
//        InputStream in = null;
//        try {
//            in = this.getInputStream();
//        } catch (Exception e1) {
//        }
//        byte[] bufIn = new byte[MAX_FRAME_LEN];
//        try {
//            int curBytesCount = in.read(bufIn);
//            String str = new String(bufIn, 0, curBytesCount);
//            if (curBytesCount <= 0) {
//                this.isConnecting = false;
//                Log.e("读取出来的数据长度小于0", "-----------");
//                return null;
//            }
//            if (curBytesCount < signLen + frameLenAttrLen) {
//                return null;
//            }
//            String sign = str.substring(0, signLen);
//            if (!sign.equals(SIGN)) {
//                return null;
//            }
//            String dataLenStr = str.substring(signLen + 2, frameLenAttrLen);
//            int dataLen;
//            try{
//                dataLen = Integer.parseInt(dataLenStr, 16);
//            }catch (Exception e){
//                Log.e("长度转换出现异常", e.toString());
//                return null;
//            }
//            String contentStr = str.substring(frameLenAttrLen + signLen);
//            while(contentStr.getBytes().length<dataLen){
//                int n = in.read(bufIn,curBytesCount,dataLen - contentStr.getBytes().length);
//                curBytesCount+=n;
//                contentStr+=new String(bufIn,curBytesCount,n);
//            }
//            try {
//                ProtoClass.Msg msg = ProtoClass.Msg.parseFrom(contentStr.getBytes());
//                return msg;
//            } catch (Exception ex) {
//                Log.e("解包出现异常", ex.toString());
//                return null;
//            }
//        } catch (Exception e) {
//            this.isConnecting = false;
//            Log.e("读取出现异常", " -- " + e.toString());
//        }
//        return null;
//    }
  //  燕娟的函数
    public ProtoClass.Msg readMsg() {
        InputStream in = null;
        try {
            in = this.getInputStream();
        } catch (Exception e1) {
        }
        try {
            byte[] messageHeaderBuf = new byte[signLen + frameLenAttrLen]; //msgSign(string) 6 ;msgLength 10(string)
            int realReadLen = 0;
            while (realReadLen != signLen + frameLenAttrLen) {
                realReadLen += in.read(messageHeaderBuf);
            }

            if (realReadLen < signLen + frameLenAttrLen) {
                return null;
            }
            String sign = new String(messageHeaderBuf, 0, signLen); //取消息头msgSign
            if (!sign.equals(SIGN)) {
                return null;
            }
            String dataLen = new String(messageHeaderBuf, signLen,frameLenAttrLen);
            int size = Integer.parseInt(dataLen.substring(2), signLen + frameLenAttrLen); //取消息长度，过滤掉"0x"
            byte[] messageBodyBuf = new byte[size];
            realReadLen = 0;
            while (realReadLen != size) {
                realReadLen += in.read(messageBodyBuf);
            }

            ProtoClass.Msg msg = ProtoClass.Msg.parseFrom(messageBodyBuf);
            System.out.println("收到的信息为" + msg);
            return msg;
        } catch (Exception e) {
            System.out.println("读取数据失败！");
            e.printStackTrace();
        }
        return null;
    }

    public void sendMsg(ProtoClass.Msg msg) throws Exception {
        OutputStream out = this.getOutputStream();
        byte[] outByte = msg.toByteArray();
        String dataLenStr = Integer.toHexString(outByte.length);
        int strlen = dataLenStr.length();
        for (int i = 0; i < 8 - strlen; i++) {
            dataLenStr = "0" + dataLenStr;
        }
        dataLenStr = "0x" + dataLenStr;
        String responseData = SIGN + dataLenStr;
        out.write(responseData.getBytes());
        out.write(outByte);
    }

    public interface OnConnectedListener {
        void onConnected(ToolsSocket socket);
    }

    public void setOnConnectedListener(OnConnectedListener onConnectedListener) {
        this.onConnectedListener = onConnectedListener;
    }

    private OnConnectedListener onConnectedListener;
}
