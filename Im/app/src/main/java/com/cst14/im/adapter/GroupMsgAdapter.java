package com.cst14.im.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.bean.UserChat;
import com.cst14.im.layoutView.GifView;
import com.cst14.im.utils.ImApplication;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class GroupMsgAdapter extends ArrayAdapter<UserChat> {
    private final static String TAG = "GroupMsgAdapter";
    private Context context;
    private final static int ME = 0;
    private final static int OTHER = 1;

    private final static int[] LAYOUT = {R.layout.item_group_chat_me, R.layout.item_group_chat_other};
    private final static int[] IV_ICON = {R.id.iv_item_group_chat_me_icon, R.id.iv_item_group_chat_other_icon};
    private final static int[] TV_NAME = {R.id.tv_item_group_chat_me_name, R.id.tv_item_group_chat_other_name};
    private final static int[] TV_CONETNET = {R.id.tv_item_group_chat_me_text, R.id.tv_item_group_chat_other_text};
    private final static int[] GIF_VIEW = {R.id.gif_item_group_chat_me, R.id.gif_item_group_chat_other};
    private final static int[] IV_VOICE = {R.id.iv_item_group_chat_me_voice, R.id.iv_item_group_chat_other_voice};
    private final static int[] TV_VOICE_TIME = {R.id.tv_item_group_chat_me_voice_time, R.id.tv_item_group_chat_other_voice_time};
    private final static int[] TV_SEND_TIME = {R.id.tv_item_group_chat_me_send_time, R.id.tv_item_group_chat_other_send_time};

    final static int[] IV_VOICE_STATUS = {R.drawable.voice32_l_1, R.drawable.voice32_l_2, R.drawable.voice32_l_3, R.drawable.voice32_r_1, R.drawable.voice32_r_2, R.drawable.voice32_r_3};
    final static int[] IMAGE_FIRST_VOICE = {R.drawable.voice32_l_3, R.drawable.voice32_r_3};

    public GroupMsgAdapter(Context context, int resource, List<UserChat> objects) {
        super(context, resource, objects);
        this.context = context;
    }


    class ViewHolder {
        ImageView ivIcon;
        TextView tvContent, tvName, tvVoiceTime, tvSendtime;
        GifView gifView;
        ImageView ivVoice;
        ProgressBar pbSendMsg;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i(TAG, "position = " + position);
        View view;
        UserChat userChat = getItem(position);
        ViewHolder viewHolder;
        int who;
        if ((userChat.getName()).equals(ImApplication.User_id)) {
            who = ME;
        } else {
            who = OTHER;
        }
        Log.i("71:" + TAG, userChat.getName() + "  " + userChat.getStrContent() + "   " + who);
        Log.i("71:" + TAG, "msg id " + userChat.getMsgUniqueTag() + "  " + "isSendOK " + userChat.getIsSendOK());

        // if (convertView == null) {
        view = LayoutInflater.from(getContext()).inflate(LAYOUT[who], null);
        viewHolder = new ViewHolder();
        viewHolder.ivIcon = (ImageView) view.findViewById(IV_ICON[who]);
        viewHolder.ivVoice = (ImageView) view.findViewById(IV_VOICE[who]);
        viewHolder.tvName = (TextView) view.findViewById(TV_NAME[who]);
        viewHolder.tvContent = (TextView) view.findViewById(TV_CONETNET[who]);
        viewHolder.tvVoiceTime = (TextView) view.findViewById(TV_VOICE_TIME[who]);
        viewHolder.tvSendtime = (TextView) view.findViewById(TV_SEND_TIME[who]);
        viewHolder.gifView = (GifView) view.findViewById(GIF_VIEW[who]);
        viewHolder.pbSendMsg = (ProgressBar) view.findViewById(R.id.pb_item_group_chat_me);
        view.setTag(viewHolder);
        //} else {
        //  view = convertView;
        // viewHolder = (ViewHolder) view.getTag();
        //}
        String msgType = userChat.getMsgType();
        viewHolder.ivIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.initial_header));
        viewHolder.tvName.setText(userChat.getName());
        invisibleAll(viewHolder);
        if (userChat.getIsSendOK() == 0 && who == 0) {
            viewHolder.pbSendMsg.setVisibility(View.VISIBLE);
        }
        viewHolder.tvSendtime.setVisibility(View.VISIBLE);
        viewHolder.tvSendtime.setText(twoDateDistance(userChat.getMsgSendTime()));
        switch (msgType) {
            case "TEXT":
                viewHolder.tvContent.setVisibility(View.VISIBLE);
                viewHolder.tvContent.setText(userChat.getStrContent());
                break;

            //图片文件分为动态gif和静态
            case "IMAGE":
                String filePath = userChat.getFilePath();
                String extenName = filePath.substring(filePath.lastIndexOf(".") + 1, filePath.length());
                Log.i(TAG, "extenName = " + extenName);
                Log.i(TAG, "filepath = " + filePath);
                //如果是gif
                if (extenName.equals("gif")) {
                    viewHolder.gifView.setVisibility(View.VISIBLE);
                    viewHolder.gifView.setMovieResource(filePath);
                    break;
                }
                //如果是jpg
                String htmlStr = "<img src = \'" + userChat.getFilePath() + "\'/><br>";
                viewHolder.tvContent.setVisibility(View.VISIBLE);

                viewHolder.tvContent.setText(Html.fromHtml(htmlStr, new Html.ImageGetter() {
                    @Override
                    public Drawable getDrawable(String source) {
                        Drawable drawable = null;
                        drawable = Drawable.createFromPath(source); //显示本地图片
                        if (drawable != null) {
                            drawable = zoomDrawable(drawable);
                            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                        }
                        return drawable;
                    }
                }, null));
                break;
            case "VIDEO":
                break;

            case "VOICE":
                viewHolder.ivVoice.setVisibility(View.VISIBLE);
                viewHolder.tvVoiceTime.setVisibility(View.VISIBLE);
                viewHolder.tvContent.setVisibility(View.VISIBLE);
                viewHolder.tvContent.setText("");
                viewHolder.tvContent.setHeight(25);
                int voiceLen = (userChat.getVoiceTime() / 80) * 40 + 25;
                Log.i(TAG, "voiceTime=" + userChat.getVoiceTime() + "\tviewWidth=" + view.getWidth() + "\tvoiceLen=" + voiceLen);
                viewHolder.tvContent.setWidth(voiceLen);
                viewHolder.tvVoiceTime.setText("" + userChat.getVoiceTime() + "''");
                viewHolder.ivVoice.setImageDrawable(context.getResources().getDrawable(IMAGE_FIRST_VOICE[who]));
                break;

            case "FILE":
                break;

        }

        return view;
    }

    private void invisibleAll(ViewHolder viewHolder) {
        viewHolder.gifView.setVisibility(View.INVISIBLE);
        viewHolder.tvContent.setVisibility(View.INVISIBLE);
        viewHolder.ivVoice.setVisibility(View.INVISIBLE);
        viewHolder.tvVoiceTime.setVisibility(View.INVISIBLE);
        viewHolder.tvSendtime.setVisibility(View.INVISIBLE);
        if (viewHolder.pbSendMsg != null) {
            viewHolder.pbSendMsg.setVisibility(View.INVISIBLE);
        }
    }

    // drawable 转换成 bitmap
    private Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();   // 取 drawable 的长宽
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;         // 取 drawable 的颜色格式
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);     // 建立对应 bitmap
        Canvas canvas = new Canvas(bitmap);         // 建立对应 bitmap 的画布
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);      // 把 drawable 内容画到画布中
        return bitmap;
    }

    private Drawable zoomDrawable(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Log.i("width:", width + "heigh" + height);
        int pixelCount = (width * height) >> 20;//看看这张照片有多少百万像素
        System.out.println("=========================pixelcount:" + pixelCount);
        // if (pixelCount > 4) {//如果超过了4百万像素
        int w = 1080, h = w * height / width;
        if (width < height) {
            w = 720;
            h = w * height / width;
        }
        Log.i("width:", w + "heigh" + h);
        Bitmap oldbmp = drawableToBitmap(drawable); // drawable 转换成 bitmap
        Matrix matrix = new Matrix();   // 创建操作图片用的 Matrix 对象
        float scaleWidth = ((float) w / width);   // 计算缩放比例
        float scaleHeight = ((float) h / height);
        matrix.postScale(scaleWidth, scaleHeight);         // 设置缩放比例
        Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true);       // 建立新的 bitmap ，其内容是对原 bitmap 的缩放后的图
        return new BitmapDrawable(newbmp);       // 把 bitmap 转换成 drawable 并返回
      /*  } else {
            Bitmap oldbmp = drawableToBitmap(drawable); // drawable 转换成 bitmap
            Matrix matrix = new Matrix();   // 创建操作图片用的 Matrix 对象
            matrix.postScale(1, 1);         // 设置缩放比例
            Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true);       // 建立新的 bitmap ，其内容是对原 bitmap 的缩放后的图
            return new BitmapDrawable(newbmp);       // 把 bitmap 转换成 drawable 并返回
        }*/

    }


    //局部刷新，语音播放动态图
    public void updateItemView(View view, int who, int status) {
        if (view == null) {
            Log.i(TAG, "veiw is null");
            return;
        }
        //从view中取得holder
        ViewHolder holder = (ViewHolder) view.getTag();
        if (holder == null) {
            Log.i(TAG, "holder is null");
            return;
        }
        status = who * 3 + status;
        holder.ivVoice.setVisibility(View.VISIBLE);
        holder.ivVoice.setImageDrawable(context.getResources().getDrawable(IV_VOICE_STATUS[status]));

    }

    public String twoDateDistance(String sendTime) {
        Date nowDate = Calendar.getInstance().getTime();
        Date startDate=new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
             startDate = df.parse(sendTime);
        } catch (Exception e){
          e.printStackTrace();
            return  "";
        }
        if(startDate == null ||nowDate == null){
            return null;
        }
        long timeLong = nowDate.getTime() - startDate.getTime();
        if (timeLong<60*1000)
            return timeLong/1000 + "秒前";
        else if (timeLong<60*60*1000){
            timeLong = timeLong/1000 /60;
            return timeLong + "分钟前";
        }
        else if (timeLong<60*60*24*1000){
            timeLong = timeLong/60/60/1000;
            return timeLong+"小时前";
        }
        else if (timeLong<60*60*24*1000*7){
            timeLong = timeLong/1000/ 60 / 60 / 24;
            return timeLong + "天前";
        }
        else if (timeLong<60*60*24*1000*7*4){
            timeLong = timeLong/1000/ 60 / 60 / 24/7;
            return timeLong + "周前";
        }
        else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
            return sdf.format(startDate);
        }
    }


}