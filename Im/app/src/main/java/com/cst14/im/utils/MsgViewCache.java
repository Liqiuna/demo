package com.cst14.im.utils;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.cst14.im.Dialog.PersonalMsgReadingDlg;
import com.cst14.im.R;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.fileUtils.FileOpenHelper;
import com.cst14.im.utils.fileUtils.FileUtils;
import com.cst14.im.utils.sessionUtils.ChatMsgBean;
import com.yolanda.nohttp.rest.StringRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by MRLWJ on 2016/9/15.
 * 提供视图和复用视图
 */
public class MsgViewCache {
    private static Queue<View> lMsgTextViews,lMsgFileViews,lMsgVideoViews,lMsgVoiceViews;
    private static Queue<View> rMsgTextViews,rMsgFileViews,rMsgVideoViews,rMsgVoiceViews;
    static {
        lMsgTextViews = new LinkedList<View>();
        rMsgTextViews = new LinkedList<View>();
        lMsgFileViews = new LinkedList<View>();
        rMsgFileViews = new LinkedList<View>();
        lMsgVideoViews = new LinkedList<View>();
        rMsgVideoViews = new LinkedList<View>();
        lMsgVoiceViews = new LinkedList<View>();
        rMsgVoiceViews = new LinkedList<View>();
    }
    public static View getView(ChatMsgBean msg){
        switch (msg.getDataType()){
            case ProtoClass.DataType.TEXT_VALUE:
                return getMsgTextView(msg);
            case ProtoClass.DataType.FILE_VALUE:
                return getMsgFileView(msg);
            case ProtoClass.DataType.VOICE_VALUE:
                return getMsgVoiceView(msg);
            case ProtoClass.DataType.VIDEO_VALUE:
                return getMsgVideoView(msg);
            default:
                throw new RuntimeException("未知消息类型");
        }
    }

    /**
     * 根據不同的msg類型，保存到不同緩存隊列中
     * @param msg
     */
    public static void recycleRootView(ChatMsgBean msg){
        View root = msg.root;
        msg.root = null;
        RootContainerHolder holder = (RootContainerHolder) root.getTag();
        if(msg.isMineMsg()){ //只有是我的消息才有以下两个控件
            holder.pbSending.setVisibility(View.VISIBLE);
            holder.tvShowOffline.setText("");
        }
        //以上兩行把控件還原囘最開始的狀態
        switch (msg.getDataType()){
            case ProtoClass.DataType.TEXT_VALUE:
                 if(msg.isMineMsg()){
                     rMsgTextViews.add(root);
                 }else{
                     lMsgTextViews.add(root);
                 }
                break;
            case ProtoClass.DataType.FILE_VALUE:
                boolean isPicFile = msg.getFileName().endsWith(".jpg") || msg.getFileName().endsWith(".png");
                if(isPicFile&&holder.ivPic!=null){
                    holder.ivPic.setImageBitmap(null);  //釋放圖片的空間
                }
                if(msg.isMineMsg()){
                    rMsgFileViews.add(root);
                }else{
                    lMsgFileViews.add(root);
                }
                break;
            case ProtoClass.DataType.VOICE_VALUE:
                if(msg.isMineMsg()){
                    rMsgVoiceViews.add(root);
                }else{
                    lMsgVoiceViews.add(root);
                }
                break;
            case ProtoClass.DataType.VIDEO_VALUE:
                if(msg.isMineMsg()){
                    rMsgVideoViews.add(root);
                }else{
                    lMsgVideoViews.add(root);
                }
                break;
        }
    }
    private static View getMsgTextView(ChatMsgBean msg) {
        View root;
        if(msg.isMineMsg()){
            root = rMsgTextViews.poll();
        }else{
            root = lMsgTextViews.poll();
        }
        if(root == null){
            root = newRootContainer(msg.isMineMsg(), msg.getDataType());
        }
        RootContainerHolder holder = (RootContainerHolder) root.getTag();
        String textContent = msg.getTextContent();
        if (!TextUtils.isEmpty(textContent) && textContent.matches("^(https?://).*")) {
            holder.tvTextContent.setText(StringParser.toLinkHighLight(textContent));
            Tools.startHttpRequest(new StringRequest(textContent), new GetWebPageSummaryHelper(holder, msg), Tools.HttpCacheMode.LOCAL_DATA_FIRST);
            root.setOnLongClickListener(new LinkActionAfterLongClickListener(msg));
            return root;
        }
        holder.tvTextContent.setText(StringParser.convert(ImApplication.instance, textContent, 0));
        root.setOnLongClickListener(new TextActionAfterLongClickListener(msg));
        return root;
    }

    private static View getMsgFileView(ChatMsgBean msg) {
        View root ;
        if(msg.isMineMsg()){
            root = rMsgFileViews.poll();
        }else{
            root = lMsgFileViews.poll();
        }
        boolean isPicFile = msg.getFileName().endsWith(".jpg") || msg.getFileName().endsWith(".png");
        if(root == null){
            root = newRootContainer(msg.isMineMsg(),msg.getDataType());
        }
        RootContainerHolder holder = (RootContainerHolder) root.getTag();
        if (isPicFile) {
            new PictureOpenHelper(ImApplication.mainActivity, msg.getFileName(), msg.getFileFingerPrint(),root).exec();
            if(holder.fileShow!=null){
                holder.fileShow.setVisibility(View.GONE);
            }
        }else{
            initGeneralFileView(root,msg);
            holder.tvFileName.setText(new File(msg.getFileName()).getName());
            holder.tvFileSize.setText(Formatter.formatFileSize(ImApplication.instance, Long.parseLong(msg.getFileSize())));
            updateFileDownloadState(msg);
            if(holder.ivPic!=null){
                holder.ivPic.setVisibility(View.GONE);
            }
        }
        root.setOnClickListener(new ClickToOpenFileListener(msg));
        root.setOnLongClickListener(new FileActionAfterLongClickListener(msg));
        return root;
    }

    private static View getMsgVideoView(ChatMsgBean msg) {
        View root;
        if(msg.isMineMsg()){
            root = rMsgVideoViews.poll();
        }else{
            root = lMsgVideoViews.poll();
        }
        if(root == null){
            root = newRootContainer(msg.isMineMsg(),msg.getDataType());
        }
        RootContainerHolder holder = (RootContainerHolder) root.getTag();
        final ViewGroup content = (ViewGroup) View.inflate(ImApplication.mainActivity, R.layout.layout_vdo_content, null);
        final ImageView vdoIcon = (ImageView) content.findViewById(R.id.iv_vdo_icon);
        final ImageView videoCapture = (ImageView) content.findViewById(R.id.iv_vdo_thumb);
        holder.vdoView = (VideoView) content.findViewById(R.id.vdo_view);
        holder.vdoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                vdoIcon.setBackgroundResource(R.drawable.vdo_icon64);
                videoCapture.setVisibility(View.VISIBLE);
                vdoIcon.setVisibility(View.VISIBLE);
            }
        });
        // 首先下载缩略图文件，然后设置上去
        new FileOpenHelper(ImApplication.mainActivity, msg.thumbFileName, msg.getFileFingerPrint(), new FileOpenHelper.FileTrigger() {
            @Override
            public void trigger(File file) {
                Bitmap thumb = BitmapFactory.decodeFile(file.getAbsolutePath());
                videoCapture.setImageBitmap(thumb);
            }
        }).exec();
        holder.innerViewContent.addView(content);
        root.setOnClickListener(new VideoActionAfterClickListener(vdoIcon, videoCapture, msg, holder));
        root.setOnLongClickListener(new VideoActionAfterLongClickListener(msg));
        return root;
    }

    private static View getMsgVoiceView(ChatMsgBean msg) {
        View root;
        if(msg.isMineMsg()){
            root = rMsgVoiceViews.poll();
        }else{
            root = lMsgVoiceViews.poll();
        }
        if(root == null){
            root = newRootContainer(msg.isMineMsg(),msg.getDataType());
        }
        RootContainerHolder holder = (RootContainerHolder) root.getTag();
        holder.ivVoiceIcon.setVisibility(View.VISIBLE);
        holder.tvVoiceLen.setVisibility(View.VISIBLE);
        holder.tvVoiceLen.setText(Long.parseLong(msg.getFileSize()) / 1000.0 + "'");
        holder.tvTextContent.setText("          ");
        if (msg.showVoiceUnRead) {
            holder.ivVoiceUnReadRedCycle.setVisibility(View.VISIBLE);
        }
        root.setOnClickListener(new VoiceActionAfterClickListener(msg,holder,root));
        root.setOnLongClickListener(new VoiceActionAfterLongClickListener(msg));
        return root;
    }

    /**
     * 形成联系：root->holder
     * @return
     */
    static int count = 0;
    private static View newRootContainer(boolean isMineMsg,int dataType){
        Log.e("newRootContainer","newRootContainer執行 * "+ ++count);
        View container;
        if (isMineMsg) {
            container =  View.inflate(ImApplication.mainActivity, R.layout.item_chat_me, null);
        }else{
            container =  View.inflate(ImApplication.mainActivity, R.layout.item_chat_other, null);
        }
        RootContainerHolder holder = new RootContainerHolder();
        holder.tvShowOffline = (TextView) container.findViewById(R.id.tv_offline_tip);
        holder.pbSending = container.findViewById(R.id.pb_msg_sending);
        holder.ivVoiceIcon = (ImageView) container.findViewById(R.id.iv_voice_icon);
        holder.tvVoiceLen = (TextView) container.findViewById(R.id.tv_voice_len);
        holder.tvTextContent = (TextView) container.findViewById(R.id.tv_chat_msg);
        holder.ivVoiceUnReadRedCycle = (ImageView) container.findViewById(R.id.iv_voice_red_cycle);
        holder.outerViewContainer = (ViewGroup) container.findViewById(R.id.up_container);
        holder.innerViewContent = (RelativeLayout) container.findViewById(R.id.rl_container);
        container.setTag(holder);
        return container;
    }
    public static class RootContainerHolder {
        //---general
        public TextView tvShowOffline;
        public TextView tvVoiceLen;
        public View pbSending;
        public ImageView ivVoiceIcon;
        public TextView tvTextContent;
        public ImageView ivVoiceUnReadRedCycle;
        public RelativeLayout innerViewContent;
        public ViewGroup outerViewContainer;

        //--file
        public TextView tvFileName;
        public TextView tvShowFileDownState;
        public TextView tvFileSize;
        public View fileShow;

        //--text
        public View vWebPageSummary;

        //video
        public VideoView vdoView;

        //pic
        public ImageView ivPic;
    }

    /**
     * 用于显示图片消息的内部类
     * 在文件下载完成之前，会显示一张默认图片，图片下载完成会被替换上去
     */
    private static class PictureOpenHelper extends FileOpenHelper {
        public PictureOpenHelper(final Context context, String filePath, String fileFingerPrint,View root) {
            super(context, filePath, fileFingerPrint, null);
            final RootContainerHolder holder = (RootContainerHolder) root.getTag();
            if(holder.ivPic==null){
                holder.ivPic = new ImageView(context);
                holder.innerViewContent.addView(holder.ivPic);
            }
            holder.ivPic.setVisibility(View.VISIBLE);
            holder.ivPic.setBackgroundResource(R.drawable.load_pic_failed64);
            FileOpenHelper.FileTrigger trigger = new FileTrigger() {
                @Override
                public void trigger(final File file) {
                    holder.ivPic.setImageBitmap(FileUtils.LoadImage(file.getAbsolutePath(), context));
                }
            };
            super.setFileTrigger(trigger);
        }
    }

    /**
     * 把显示普通文件（非图片）的控件初始化
     */
    private static void initGeneralFileView(View root,ChatMsgBean msg) {
        RootContainerHolder holder = (RootContainerHolder) root.getTag();
        if(holder.fileShow!=null){
            holder.fileShow.setVisibility(View.VISIBLE);
            return;
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) (ImApplication.density * 200), RelativeLayout.LayoutParams.WRAP_CONTENT);
        if (msg.isMineMsg()) {
            holder.fileShow = View.inflate(ImApplication.mainActivity, R.layout.layout_show_file_r, null);
            holder.fileShow.setLayoutParams(params);
        } else {
            holder.fileShow = View.inflate(ImApplication.mainActivity, R.layout.layout_show_file_l, null);
            holder.fileShow.setLayoutParams(params);
        }
        holder.tvFileName = (TextView) holder.fileShow.findViewById(R.id.tv_show_file_name);
        holder.tvShowFileDownState = (TextView) holder.fileShow.findViewById(R.id.tv_show_file_isdownload);
        holder.tvFileSize = (TextView) holder.fileShow.findViewById(R.id.tv_show_file_len);
        holder.innerViewContent.addView(holder.fileShow);
    }
    /**
     * 更新文件的下载状态
     * TODO 其他地方根据 fileDownState 把值显示到holder的控件上
     */
    private static void updateFileDownloadState(ChatMsgBean msg) {
        File file = new File(msg.getFileName());
        if (file.exists()) {
            if (msg.isMineMsg()) {
                msg.fileDownState = !TextUtils.isEmpty( msg.fileDownState) ?  msg.fileDownState : "已上传";
            } else {
                msg.fileDownState = "已下载";
            }
            return;
        }
    }

    /**
     * 用于点击打开文件的帮助类，有必要的话会去下载文件
     */
    private static class ClickToOpenFileListener implements View.OnClickListener {

        private ChatMsgBean mMsg;
        public ClickToOpenFileListener(ChatMsgBean msg) {
            mMsg = msg;
        }

        @Override
        public void onClick(View v) {
            new FileOpenHelper(ImApplication.mainActivity, mMsg.getFileName(), mMsg.getFileFingerPrint(), new FileOpenHelper.FileTrigger() {
                @Override
                public void trigger(File file) {
                    try {
                        Intent fileOpenInten = FileUtils.openFile(file.getAbsolutePath());
                        ImApplication.mainActivity.startActivity(fileOpenInten);
                        updateFileDownloadState(mMsg);
                    } catch (Exception e) {
                        Utils.showToast(ImApplication.mainActivity, e.toString());
                    }
                }
            }).exec();
        }
    }

    /**
     * 用于获取链接网址的内容（图片和标题）的帮助类
     */
    private static class GetWebPageSummaryHelper extends Tools.HttpRequestListener {
        private ChatMsgBean mMsg;
        private RootContainerHolder mholder;
        public GetWebPageSummaryHelper(RootContainerHolder holder,ChatMsgBean msg){
            mholder = holder;
            mMsg = msg;
        }
        @Override
        public void onSuccee(String resp) {
            if (mholder.vWebPageSummary == null) {
                mholder.vWebPageSummary = View.inflate(ImApplication.mainActivity, R.layout.layout_webpage_summary, null);
                mholder.outerViewContainer.addView(mholder.vWebPageSummary);
            }
            Document doc = Jsoup.parse(resp);
            final String title = doc.title();
            ImApplication.mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView tvTitle = (TextView) mholder.vWebPageSummary.findViewById(R.id.tv_wp_title);
                    tvTitle.setText(title);
                }
            });
            String textContent = mMsg.getTextContent();
            String iconUrl = textContent.endsWith("/") ? textContent + "favicon.ico" : textContent + "/favicon.ico";
            Tools.startDownloadRequest(iconUrl, ImApplication.iconDir, iconUrl.hashCode() + ".ico", new Tools.ToolsDownloadListener() {
                @Override
                public void onDownloadError(int what, Exception exception) {
                    Log.e("下载失败", exception.toString());
                }

                @Override
                public void onDownloadFinish(final String dir, final String fileName) {
                    ImApplication.mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final Bitmap icon = BitmapFactory.decodeFile(dir + File.separator + fileName);
                            ImageView ivIcon = (ImageView) mholder.vWebPageSummary.findViewById(R.id.iv_wp_icon);
                            ivIcon.setImageBitmap(icon);
                        }
                    });
                }
            }, Tools.HttpCacheMode.LOCAL_DATA_FIRST);
        }

        @Override
        public void onFail(String url, Exception e) {
            Log.e("访问网页失败", e.toString());
        }
    }

    /**
     * 当链接被长按时触发，触发以下动作，以下生成菜单，只有两个选项
     */
    private static class LinkActionAfterLongClickListener implements View.OnLongClickListener {
        private ChatMsgBean mMsg;
        public LinkActionAfterLongClickListener(ChatMsgBean msg){
            mMsg = msg;
        }
        @Override
        public boolean onLongClick(View v) {
            ChatMenuCreator chatMenuCreator = new ChatMenuCreator();
            chatMenuCreator.addAction(new ChatMenuCreator.MenuItemEntry("复制链接地址", new Runnable() {
                @Override
                public void run() {
                    Utils.showToast(ImApplication.mainActivity, "地址已复制");
                    ImApplication.instance.getCurSessionHolder().chatActivity.cbm.setPrimaryClip(ClipData.newPlainText("label", mMsg.getTextContent()));
                }
            }));
            chatMenuCreator.addAction(new ChatMenuCreator.MenuItemEntry("在浏览器中打开", new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri content_url = Uri.parse(mMsg.getTextContent());
                    intent.setData(content_url);
                    ImApplication.mainActivity.startActivity(intent);
                }
            }));
            ImApplication.instance.getCurSessionHolder().chatActivity.chatMenu = chatMenuCreator.build().show();
            return false;
        }
    }

    /**
     * 当文本被长按时，触发以下动作
     */
    private static class TextActionAfterLongClickListener implements View.OnLongClickListener {
        private ChatMsgBean mMsg;
        public TextActionAfterLongClickListener(ChatMsgBean msg){
            mMsg = msg;
        }
        @Override
        public boolean onLongClick(View v) {
            ChatMenuCreator chatMenuCreator = new ChatMenuCreator();
            chatMenuCreator.addAction(new ChatMenuCreator.MenuItemEntry("复制", new Runnable() {
                @Override
                public void run() {
                    Utils.showToast(ImApplication.mainActivity, "消息已复制");
                    ImApplication.instance.getCurSessionHolder().chatActivity.cbm.setPrimaryClip(ClipData.newPlainText("label", mMsg.getTextContent()));
                }
            }));
            chatMenuCreator.addAction(new ChatMenuCreator.MenuItemEntry("详情", new Runnable() {
                @Override
                public void run() {
                    if(!mMsg.isMineMsg()) return;
                    ImApplication.instance.getCurSessionHolder().chatActivity.chatMenu.dismiss();
                    AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(ImApplication.instance.getCurSessionHolder().chatActivity);
                    dlgBuilder.setView(new PersonalMsgReadingDlg(mMsg));
                    dlgBuilder.show();
                }
            }));
            ImApplication.instance.getCurSessionHolder().chatActivity.chatMenu = chatMenuCreator.build().show();
            return false;
        }
    }

    /**
     * 文件消息长按之后触发相应动作
     */
    private static class FileActionAfterLongClickListener implements View.OnLongClickListener{
        private ChatMsgBean mMsg;
        public FileActionAfterLongClickListener(ChatMsgBean msg){
            mMsg = msg;
        }
        @Override
        public boolean onLongClick(View v) {
            ChatMenuCreator chatMenuCreator = new ChatMenuCreator();
            chatMenuCreator.addAction(new ChatMenuCreator.MenuItemEntry("详情", new Runnable() {
                @Override
                public void run() {
                    if(!mMsg.isMineMsg()) return;
                    ImApplication.instance.getCurSessionHolder().chatActivity.chatMenu.dismiss();
                    AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(ImApplication.instance.getCurSessionHolder().chatActivity);
                    dlgBuilder.setView(new PersonalMsgReadingDlg(mMsg));
                    dlgBuilder.show();
                }
            }));
            ImApplication.instance.getCurSessionHolder().chatActivity.chatMenu = chatMenuCreator.build().show();
            return false;
        }
    }

    private static class VoiceActionAfterLongClickListener implements View.OnLongClickListener{
        private ChatMsgBean mMsg;
        public VoiceActionAfterLongClickListener(ChatMsgBean msg){
            mMsg = msg;
        }
        @Override
        public boolean onLongClick(View v) {
            ChatMenuCreator chatMenuCreator = new ChatMenuCreator();
            chatMenuCreator.addAction(new ChatMenuCreator.MenuItemEntry("详情", new Runnable() {
                @Override
                public void run() {
                    if(!mMsg.isMineMsg()) return;
                    ImApplication.instance.getCurSessionHolder().chatActivity.chatMenu.dismiss();
                    AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(ImApplication.instance.getCurSessionHolder().chatActivity);
                    dlgBuilder.setView(new PersonalMsgReadingDlg(mMsg));
                    dlgBuilder.show();
                }
            }));
            ImApplication.instance.getCurSessionHolder().chatActivity.chatMenu = chatMenuCreator.build().show();
            return false;
        }
    }

    private static class VideoActionAfterLongClickListener implements View.OnLongClickListener{
        private ChatMsgBean mMsg;
        public VideoActionAfterLongClickListener(ChatMsgBean msg){
            mMsg = msg;
        }
        @Override
        public boolean onLongClick(View v) {
            ChatMenuCreator chatMenuCreator = new ChatMenuCreator();
            chatMenuCreator.addAction(new ChatMenuCreator.MenuItemEntry("详情", new Runnable() {
                @Override
                public void run() {
                    if(!mMsg.isMineMsg()) return;
                    ImApplication.instance.getCurSessionHolder().chatActivity.chatMenu.dismiss();
                    AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(ImApplication.instance.getCurSessionHolder().chatActivity);
                    dlgBuilder.setView(new PersonalMsgReadingDlg(mMsg));
                    dlgBuilder.show();
                }
            }));
            ImApplication.instance.getCurSessionHolder().chatActivity.chatMenu = chatMenuCreator.build().show();
            return false;
        }
    }
    /**
     * 当语音被点击时，触发以下动作
     */
    private static class VoiceActionAfterClickListener implements View.OnClickListener {
        private ChatMsgBean mMsg;
        private RootContainerHolder mHolder;
        private View mRoot;
        public VoiceActionAfterClickListener(ChatMsgBean msg,RootContainerHolder holder,View root){
            mMsg = msg;
            mRoot = root;
            mHolder = holder;
        }
        @Override
        public void onClick(View v) {
            String voiceFilepath = ImApplication.voiceDir + File.separator + mMsg.getFileName();
            new FileOpenHelper(ImApplication.mainActivity, voiceFilepath, mMsg.getFileFingerPrint(), new FileOpenHelper.FileTrigger() {
                @Override
                public void trigger(File file) {
                    try {
                        ImApplication.instance.getCurSessionHolder().chatActivity.triggerVoice(file.getAbsolutePath(), mRoot, mMsg.isMineMsg());
                        if (!mMsg.isMineMsg()) {
                            mHolder.ivVoiceUnReadRedCycle.setVisibility(View.GONE);
                            mMsg.showVoiceUnRead = false;
                            ImApplication.instance.getCurSessionHolder().sessionsFragment.saveAllSessions();
                        }
                    } catch (Exception e) {
                        Utils.showToast(ImApplication.mainActivity, e.toString());
                    }
                }
            }).exec();
        }
    }

    /**
     * 当小视频被点击时，触发以下动作
     * mVideoTriggerIcon : 视频播放图标
     * mVideoCapture ： 被捕获的一帧视频画面
     */
    private static class VideoActionAfterClickListener implements View.OnClickListener {
        private ImageView mVideoTriggerIcon, mVideoCapture;
        private ChatMsgBean mMsg;
        private RootContainerHolder mHolder;
        public VideoActionAfterClickListener(ImageView videoTriggerIcon, ImageView videoCapture,ChatMsgBean msg,RootContainerHolder holder) {
            mVideoTriggerIcon = videoTriggerIcon;
            mVideoCapture = videoCapture;
            mHolder = holder;
            mMsg = msg;
        }

        @Override
        public void onClick(View v) {
            if (mHolder.vdoView.isPlaying()) {
                return;
            }
            mVideoTriggerIcon.setBackgroundResource(R.drawable.vdo_loading64);
            final RotateAnimation rotateAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(800);
            rotateAnimation.setRepeatCount(Integer.MAX_VALUE);
            rotateAnimation.setRepeatMode(Animation.RESTART);
            mVideoTriggerIcon.startAnimation(rotateAnimation); // 图片在下面开始下载完成之前会一直旋转
            new FileOpenHelper(ImApplication.mainActivity, mMsg.getFileName(), mMsg.getFileFingerPrint(), new FileOpenHelper.FileTrigger() {
                @Override
                public void trigger(final File file) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mVideoTriggerIcon.clearAnimation();
                            mVideoTriggerIcon.setVisibility(View.GONE);
                            Uri uri = Uri.parse(file.getAbsolutePath());
                            mHolder.vdoView.setVideoURI(uri);
                            mHolder.vdoView.start();
                            mVideoCapture.setVisibility(View.GONE);
                            mHolder.vdoView.requestFocus();
                        }
                    }, 3000);
                }
            }).exec();
        }
    }

}
