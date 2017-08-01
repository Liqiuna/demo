package com.cst14.im.utils.sessionUtils;

import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.cst14.im.Dialog.PersonalMsgReadingDlg;
import com.cst14.im.R;
import com.cst14.im.db.dao.PersonalMsgDao;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ChatMenuCreator;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.MsgViewCache;
import com.cst14.im.utils.StringParser;
import com.cst14.im.utils.Utils;
import com.cst14.im.utils.fileUtils.FileOpenHelper;
import com.cst14.im.utils.fileUtils.FileUtils;
import com.yolanda.nohttp.rest.StringRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.Serializable;

/**
 * 消息类，操作消息对应的数据以及控件
 * Created by MRLWJ on 2016/7/3.
 */
public class ChatMsgBean{
    public boolean isSending;
    private boolean isMineMsg;   // 代表是否是我的消息
    private String textContent;  //文本内容
    public boolean isReaded;
    public boolean showVoiceUnRead; //代表语音消息是否播放过
    private int dataType;
    public String msgId;        // 消息在当前客户端的唯一标识
    public boolean msgSentFinished;  //消息是否发送完成
    public String sentTime;  //发送时间
    public String readTime; //阅读时间
    public String fileDownState;  //文件当前下载状态
    public String from;
    public String getFileFingerPrint() {
        return fileFingerPrint;
    }
    public View root;
    public void setFileFingerPrint(String fileFingerPrint) {
        this.fileFingerPrint = fileFingerPrint;
    }

    private String fileFingerPrint; //文件md5值

    public String getThumbFingerPrint() {
        return thumbFingerPrint;
    }

    public void setThumbFingerPrint(String thumbFingerPrint) {
        this.thumbFingerPrint = thumbFingerPrint;
    }

    private String thumbFingerPrint;  //缩略图文件md5值
    private transient SessionHolder parentSessionHolder; // 当前消息所在的holder

    public File getFile() {
        return file;
    }

    public File getThumbFile() {
        return thumbFile;
    }

    /**
     * 文件初始化，根据文件名生成文件对象，以及对应的md5值（指纹）
     * 必须要设置消息类型之后调用
     */
    private void fileInit() {
        if(!isMineMsg){
            return;
        }
        if(dataType==ProtoClass.DataType.TEXT_VALUE){
            throw new RuntimeException("文本类型的消息不能调用fileInit（）");
        }
        if (!TextUtils.isEmpty(fileName)) {
            file = new File(fileName);
            if(dataType == ProtoClass.DataType.FILE_VALUE && file.exists()){
                try{
                    fileSize = file.length()+"";
                }catch (Exception e){
                    Utils.showToast2(ImApplication.mainActivity,"获取文件长度失败："+e.toString());
                }
            }
        }

        if (!TextUtils.isEmpty(thumbFileName)) {
            thumbFile = new File(thumbFileName);
        }
        if (TextUtils.isEmpty(fileFingerPrint)) {
            fileFingerPrint = FileUtils.getFileMD5(file);
        }
        if (TextUtils.isEmpty(thumbFingerPrint)) {
            thumbFingerPrint = FileUtils.getFileMD5(thumbFile);
        }
    }

    private transient File file, thumbFile;   //文件对象
    /**
     * 展示消息的发送状态,调用之前确定好msgSentFinished的值
     */
    public void showMsgSentState(String tip, boolean hideProgressBar) {
        try {
            MsgViewCache.RootContainerHolder holder = (MsgViewCache.RootContainerHolder) root.getTag();
            if (hideProgressBar) {
                if (holder.pbSending != null) {
                    holder.pbSending.setVisibility(View.GONE);
                }
            }
            holder.tvShowOffline.setText(tip);
        } catch (Exception e) {
        }
        if(parentSessionHolder!=null){
            parentSessionHolder.updateLastMsg();  //更新界面上的红色圆圈（显示未读消息的数量）
            parentSessionHolder.sessionsFragment.saveAllSessions();
        }
    }

    private String fileName;
    public String thumbFileName; //缩略图文件名称
    private String fileSize;

    public String getFileName() {
        return fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setParentSessionHolder(SessionHolder parentSessionHolder) {
        if (parentSessionHolder == null) {
            throw new RuntimeException("parentSessionHolder 不能设置为空");
        }
        this.parentSessionHolder = parentSessionHolder;
    }

    public SessionHolder getParentSessionHolder() {
        return parentSessionHolder;
    }

    /**
     * 尝试去阅读消息
     * 当消息已经被阅读过了，或者父容器处于不可见状态，则阅读失败
     *
     * @return 返回消息是否阅读成功
     */
    public boolean tryToBeReaded() {
        if(parentSessionHolder==null){
            throw new RuntimeException("必须先调用 setParentSessionHolder");
        }
        if (isReaded || !parentSessionHolder.isMsgListVisable()) {
            return false;
        }
        final ProtoClass.PersonalMsg.Builder personalMsgBuilder = ProtoClass.PersonalMsg.newBuilder();
        personalMsgBuilder.setSenderID(Integer.parseInt(parentSessionHolder.userAccount))
                .addRecverID(Integer.parseInt(ImApplication.instance.User_id))
                .setMsgType(ProtoClass.DataType.FEED_BACK_ONE_READED)
                .setSendTime(sentTime);
        final ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        builder.setMsgType(ProtoClass.MsgType.MsgType_SESSION)
                .setPersonalMsg(personalMsgBuilder)
                .setToken(ImApplication.getLoginToken());
        Tools.startTcpRequest(builder, new Tools.TcpListener() {});
        parentSessionHolder.msgReaded(1);
        return isReaded = true;
    }

    /**
     * 基础构造函数，抽出出其他所有消息对象的默认数据初始化动作
     * 当前时间生成消息在当前客户端的唯一标识
     * 如果是我发出的消息，则默认消息已经被阅读
     *
     * @param from 为空代表是我发出的消息
     */
    private ChatMsgBean(String from) {
        msgId = System.currentTimeMillis() + "";
        isMineMsg = TextUtils.isEmpty(from)||ImApplication.User_id.equals(from);
        isReaded = isMineMsg;
        this.from = from;
    }

    /**
     * 文本消息构造函数
     * @param textContent 文本内容
     * @return
     */
    public static ChatMsgBean newTextMsg(String textContent, String from){
        ChatMsgBean textMsg = new ChatMsgBean(from);
        textMsg.dataType = ProtoClass.DataType.TEXT.getNumber();
        textMsg.textContent = textContent;
        return textMsg;
    }

    /**
     * 文件消息构造函数
     * @param absFileName 文件名全路径，当为false，时只要求是文件名
     * @param size 文件长度，idMineMsg为true时，会被忽略
     * @return
     */
    public static ChatMsgBean newFileMsg(String absFileName, String size, String from){
        ChatMsgBean fileMsg = new ChatMsgBean(from);
        fileMsg.dataType = ProtoClass.DataType.FILE.getNumber();
        fileMsg.fileName = TextUtils.isEmpty(new File(absFileName).getParent()) ? ImApplication.fileDir+"/"+absFileName:absFileName;
        fileMsg.fileSize = TextUtils.isEmpty(size) ? "0" : size;
        fileMsg.fileInit();
        return fileMsg;
    }

    /**
     * 语音消息构造函数
     * @param absFileName 文件名，当为不是我的消息时，时只要求是文件名
     * @return
     */
    public static ChatMsgBean newVoiceMsg(String absFileName,String len,String from){
        ChatMsgBean voiceMsg = new ChatMsgBean(from);
        voiceMsg.fileName = TextUtils.isEmpty(new File(absFileName).getParent()) ? ImApplication.voiceDir+"/"+absFileName:absFileName;
        voiceMsg.dataType = ProtoClass.DataType.VOICE.getNumber();
        voiceMsg.showVoiceUnRead = !voiceMsg.isMineMsg;
        voiceMsg.fileSize = TextUtils.isEmpty(len) ? "0" : len;
        voiceMsg.fileInit();
        return voiceMsg;
    }

    /**
     *  视频消息构造函数
     * 缩略图名称默认是文件名（不包括后缀）+ vdo_thumb.png
     * @param absFileName
     * @param len
     * @return
     */
    public static ChatMsgBean newVideoMsg(String absFileName,String len,String from){
        ChatMsgBean videoMsg = new ChatMsgBean(from);
        videoMsg.fileName = TextUtils.isEmpty(new File(absFileName).getParent()) ? ImApplication.videoDir+"/"+absFileName:absFileName;
        videoMsg.dataType = ProtoClass.DataType.VIDEO_VALUE;
        videoMsg.showVoiceUnRead = !videoMsg.isMineMsg;
        videoMsg.fileSize = TextUtils.isEmpty(len) ? "0" : len;
        videoMsg.thumbFileName = ImApplication.cacheDir + "/"+FileUtils.getFileNameNoEx(new File(videoMsg.fileName).getName()) + "vdo_thumb.png";
        videoMsg.fileInit();
        return videoMsg;
    }


    public boolean isMineMsg() {
        return isMineMsg;
    }

    public String getTextContent() {
        return textContent;
    }

    public int getDataType() {
        return dataType;
    }

    public void recycleRootView(){
        if(root == null){
            return;
        }
        MsgViewCache.recycleRootView(this);
    }
}
