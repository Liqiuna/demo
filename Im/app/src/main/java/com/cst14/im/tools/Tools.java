package com.cst14.im.tools;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.cst14.im.baseClass.iPresenter;
import com.cst14.im.protobuf.ProtoClass;
import com.yolanda.nohttp.Headers;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.download.DownloadListener;
import com.yolanda.nohttp.download.DownloadRequest;
import com.yolanda.nohttp.rest.Response;
import com.yolanda.nohttp.rest.SimpleResponseListener;
import com.yolanda.nohttp.rest.StringRequest;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Created by MRLWJ on 2016/8/8.
 * <p>
 * 1.支持socket，get，post 请求 和响应回调
 * 2.支持http缓存
 * 3.网络操作运行在线程池中
 * 4.支持多个socket请求
 *
 * TODO 当前socket数量只会一直增加，不会减少
 */
public class Tools {
    private static String LOG_TAG = "Tools";
    private static SharedPreferences sp;
    private static Application app;
    private static SocketParams defaultSocketParams;
    private static ToolsSocket defaultSocket;
    private static ExecutorService threadPool = Executors.newCachedThreadPool();
    public static ConcurrentHashMap<String,TcpListener> tcpListenerMap = new ConcurrentHashMap<String,TcpListener>();
    public static List<iPresenter> tcpListenerList= Collections.synchronizedList(new LinkedList<iPresenter>());
    public static ConcurrentHashMap<SocketParams,ToolsSocket> toolsSocketMap = new ConcurrentHashMap<SocketParams,ToolsSocket>();

    /**
     * LOCAL_DATA_FIRST means it will use the local data if exists , and it will request net data if local data not exists
     * NET_DATA_FIRST means it will always send a request for data event in case of local data exists
     * it currently effective in GET request and download request
     */
    public enum HttpCacheMode{
        LOCAL_DATA_FIRST,NET_DATA_FIRST
    }
    /**
     * if you want to launch Tools debugMode ,you must call setDebugMode(true) before init
     */
    private static boolean debugMode;
    public static void setDebugMode(boolean debugMode){
        Tools.debugMode = debugMode;
    }

    /**
     * init relative params,it must call before any other func
     * this func should only call once in Application lifetime
     * @param app
     * @param defaultSocketParams if you do not use the default connect , it can set null
     *          public static final String host = "192.168.191.1";   wifi
     *          private String host = "172.22.71.144";
     *          private int port = 8080;
     */
    public static void init(Application app, final SocketParams defaultSocketParams) {
        if(Tools.app!=null){
            // init() calls for more than two times
            throw new RuntimeException("init() should only call once");
        }
        NoHttp.initialize(app);
        if(debugMode){
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        SystemClock.sleep(2000);
                        Log.d("size of tcpListenerMap",tcpListenerMap.size()+"");
                        Log.d("size of toolsSocketMap",toolsSocketMap.size()+"");
                    }
                }
            });
        }

        Tools.app = app;
        Tools.defaultSocketParams = defaultSocketParams;
        sp = app.getApplicationContext().getSharedPreferences("tools",0);
        if(defaultSocketParams ==null){
            return;
        }
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Log.e("a new socket in tools", "------------");
                    defaultSocket = new ToolsSocket(defaultSocketParams);
                    defaultSocket.run();
                    try {
                        defaultSocket.close();
                        Log.e("socket关闭成功", "------------");
                    } catch (IOException e) {
                        Log.e("socket关闭失败", "-- " + e.toString());
                    }
                    SystemClock.sleep(1000);
                }
            }
        });
    }

    public static void startHttpRequest(StringRequest request, HttpRequestListener listener, HttpCacheMode cacheMode) {
        if(cacheMode == HttpCacheMode.LOCAL_DATA_FIRST && request.getRequestMethod() == RequestMethod.GET){
            String resp = sp.getString(request.url(),"");
            if(!TextUtils.isEmpty(resp)){
                listener.onSuccee(resp);
                Log.e(LOG_TAG,"复用字符串缓存");
                return;
            }
        }
        NoHttp.newRequestQueue().add(0, request, listener);
    }

    public static void startDownloadRequest(String url, String dir, String fileName,ToolsDownloadListener listener,HttpCacheMode cacheMode) {
        if(cacheMode == HttpCacheMode.LOCAL_DATA_FIRST && new File(dir,fileName).exists()){
            listener.onDownloadFinish(dir,fileName);
            Log.e(LOG_TAG,"复用文件缓存");
            return ;
        }
        DownloadRequest request = NoHttp.createDownloadRequest(url, dir, true);
        listener.setFileName(fileName);
        NoHttp.newDownloadQueue().add(0, request,listener);
    }

    /**
     * send msg by defalutSocket
     * call this func require your xxx.proto file must have a msgId attr
     * @param msgBuilder
     * @param listener
     */
    public static void startTcpRequest(final ProtoClass.Msg.Builder msgBuilder, final TcpListener listener){
       if(defaultSocketParams == null){
           throw new RuntimeException("you can not use default connect in case default params is not set");
       }
        startTcpRequest(msgBuilder,null,listener);
    }

    /**
     * send tcp request in a special connect (not in default connect)
     * it will open a new connect by params if necessary
     * @param msgBuilder
     * @param params
     * @param listener should not be null
     */
    public static void startTcpRequest(final ProtoClass.Msg.Builder msgBuilder, final SocketParams params, final TcpListener listener){
        if(listener==null){
            throw new RuntimeException("tcp listener should not be null");
        }
        String msgUniqueTag = msgBuilder.getMsgUniqueTag();
        if(TextUtils.isEmpty(msgUniqueTag)){  // add a unique tag here
            msgBuilder.setMsgUniqueTag(System.currentTimeMillis()+"");
        }
        tcpListenerMap.put(msgBuilder.getMsgUniqueTag(),listener);
        // get a socket
        ToolsSocket socket = (params==null?defaultSocket:toolsSocketMap.get(params));

        // if the socket is null , it will open a socket first and send msg
        if(socket==null){
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    ToolsSocket newSocket = new ToolsSocket(params);
                    newSocket.setOnConnectedListener(new ToolsSocket.OnConnectedListener() {
                        @Override
                        public void onConnected(ToolsSocket socket) {
                            try{
                                socket.sendMsg(msgBuilder.build());
                                listener.onSendSuccess(msgBuilder.getMsgUniqueTag());
                            }catch (Exception e){
                                listener.onSendFail(e);
                            }
                            socket.setOnConnectedListener(null);
                        }
                    });
                    while (true) {
                        newSocket.run();
                        try {
                            newSocket.close();
                            Log.e("socket关闭成功", "------------");
                        } catch (IOException e) {
                            Log.e("socket关闭失败", "-- " + e.toString());
                        }
                        newSocket = new ToolsSocket(params);
                        SystemClock.sleep(1000);
                    }
                }
            });
            return;
        }
        try{
            socket.sendMsg(msgBuilder.build());
            listener.onSendSuccess(msgBuilder.getMsgUniqueTag());
            Log.e("a",msgBuilder.build().toString());
        }catch (Exception e){
            listener.onSendFail(e);
        }
    }

    public static Application getApplication() {
        return app;
    }

    public static SocketParams getDefaultSocketParams() {
        return defaultSocketParams;
    }

    /**
     * onSucceed() and onFailed() should not be overrided
     * you can just override the abstract method here
     */
    public static abstract class HttpRequestListener extends SimpleResponseListener<String> {
        abstract public void onSuccee(String resp);

        abstract public void onFail(String url, Exception e);

        @Override
        public void onSucceed(int what, Response<String> response) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(response.url(),response.get());
            editor.commit();
            onSuccee(response.get());
        }

        @Override
        public void onFailed(int what, String url, Object tag, Exception exception, int responseCode, long networkMillis) {
            onFail(url, exception);
        }
    }

    /**
     * use this abstract class , you must override onDownloadError() and onDownloadFinish()
     * note: you can override other method casually but not onFinish()
     *       the setFileName() should be call before use ToolsDownloadListener
     */
    public static abstract class ToolsDownloadListener implements DownloadListener {
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        private String fileName;
        @Override
        abstract public void onDownloadError(int what, Exception exception);

        abstract public void onDownloadFinish(String dir,String fileName);
        @Override
        public void onStart(int what, boolean isResume, long rangeSize, Headers responseHeaders, long allCount) {
        }

        @Override
        public void onProgress(int what, int progress, long fileCount) {
        }

        @Override
        public void onFinish(int what, String filePath){
            File file = new File(filePath);
            if(TextUtils.isEmpty(fileName)){
                onDownloadError(0,new Exception("fileName in downloadListener is empty"));
                return;
            }
            boolean ok = renameFile(file.getParent(),file.getName(),fileName);
            if(!ok){
                onDownloadError(0,new Exception("file remane error!"));
            }else{
                onDownloadFinish(file.getParent(),fileName);
            }
        }

        @Override
        public void onCancel(int what) {
        }
    }

    /**
     * the msgId in callBack function below means a unique tag for each msg you sent
     * if the msg you want to sent without the unique tag (msgId) ,Tools will set a System.currentTimeMillis() value on it automatically
     * if return true in onResponse() means the msg will not be handle in the listener you add before
     * return false means it will be range in listenerList
     */
    public static abstract class TcpListener{
        public void onSendSuccess(String msgId){}
        public void onSendFail(Exception e){}
        public boolean onResponse(String msgId,ProtoClass.Msg responseMsg){return false;}
    }
    private static boolean renameFile(String path, String oldname, String newname) {
        if (oldname.equals(newname)) {
            return false;
        }
        File oldfile = new File(path + File.separator + oldname);
        File newfile = new File(path + File.separator + newname);
        if (!oldfile.exists()){
            Log.e("旧文件不存在","----------");
            return false;
        }
        if (newfile.exists()){
            Log.e("新文件已经存在","-------------");
            return true;
        }
        return oldfile.renameTo(newfile);
    }

    public static void addPresenter(iPresenter presenter){
        if(presenter==null){
            throw new RuntimeException("往tools 里添加的 监听不能为空");
        }
        tcpListenerList.add(presenter);
    }
    public static void removePresenter(iPresenter presenter){
        tcpListenerList.remove(presenter);
    }
}
