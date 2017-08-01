package com.cst14.im.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.UnderlineSpan;
import android.view.View;

import com.cst14.im.R;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.sessionUtils.ChatMsgBean;
import com.cst14.im.utils.sessionUtils.SessionHolder;

/**
 * liwenjun
 * 字符串转换器，字符串操作工具，
 */
public class StringParser {

    // 把含特殊字符[emo00]~[emo40]的字符串，转换成图文混排对象
    public static SpannableStringBuilder convert(Context context, String str, int drawableSize) {
        SpannableStringBuilder resultBuilder = new SpannableStringBuilder("");
        float density = context.getResources().getDisplayMetrics().density;
        int beg = 0, end;
        boolean findBeg = false;
        // 表情转换
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '[') {
                findBeg = true;
                beg = i + 1;
            } else if (str.charAt(i) == ']') {
                findBeg = false;
                end = i;
                String idStr = str.substring(beg, end);
                String emoName = "[" + idStr + "]";
                SpannableStringBuilder builder = new SpannableStringBuilder(emoName);
                try {
                    int id = context.getResources().getIdentifier(idStr, "drawable", context.getPackageName());
                    Drawable drawable = context.getResources().getDrawable(id);
                    if (drawableSize <= 0) {
                        adjustDrawable(drawable, density);
                    } else {
                        drawable.setBounds(0, 0, drawableSize, drawableSize);
                    }
                    ImageSpan span = new ImageSpan(drawable);
                    builder.setSpan(span, 0, emoName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (Exception e) {
                } finally {
                    resultBuilder.append(builder);
                }
            } else {
                if (findBeg) {
                    continue;
                }
                resultBuilder.append(str.charAt(i));
            }
        }
        return resultBuilder;
    }

    // 返回对应字符串的高亮显示对象
    public static SpannableStringBuilder toLinkHighLight(String content) {
        SpannableStringBuilder resultBuilder = new SpannableStringBuilder(content);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.BLUE);
        UnderlineSpan underlineSpan = new UnderlineSpan();
        resultBuilder.setSpan(colorSpan, 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        resultBuilder.setSpan(underlineSpan, 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return resultBuilder;
    }

    // 调整资源的尺寸，适当缩小，最大宽度250dp
    private static void adjustDrawable(Drawable drawable, float density) {
        int height = drawable.getIntrinsicHeight();
        int width = drawable.getIntrinsicWidth();
        if (width > (int) (250.0f * density)) {
            float k = ((float) width) / height;
            height = (int) (250 * density);
            width = (int) (height * k);
        }
        drawable.setBounds(0, 0, width, height);
    }

    // 转换成简要信息显示在会话列表外
    public static String convertToBirfMsg(Context context, ChatMsgBean bean) {
        boolean sendFinish = true;
        if (bean.isMineMsg()) {
            try{
                View pbSending = ((MsgViewCache.RootContainerHolder)bean.root.getTag()).pbSending;
                sendFinish = pbSending.getVisibility() == View.GONE;
            }catch(Exception e){}
        }
        if (bean.getDataType() == ProtoClass.DataType.VOICE_VALUE) {
            return sendFinish ? "[语音]" : "[发送中][语音]";
        } else if (bean.getDataType() == ProtoClass.DataType.FILE_VALUE) {
            return sendFinish ? "[文件]" : "[发送中][文件]";
        }else if(bean.getDataType() == ProtoClass.DataType.VIDEO_VALUE){
            return sendFinish ? "[小视频]" : "[发送中][小视频]";
        }
        String resStr = sendFinish ? "" : "[发送中]";
        String str = bean.getTextContent();
        int beg = 0, end;
        boolean findBeg = false;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '[') {
                findBeg = true;
                beg = i + 1;
            } else if (str.charAt(i) == ']') {
                findBeg = false;
                end = i;
                String idStr = str.substring(beg, end);
                String emoName = "[" + idStr + "]";
                try {
                    int id = context.getResources().getIdentifier(idStr, "drawable", context.getPackageName());
                    context.getResources().getDrawable(id);
                    emoName = "[表情]";
                } catch (Exception e) {
                } finally {
                    resStr = resStr + emoName;
                }
            } else {
                if (findBeg) {
                    continue;
                }
                resStr = resStr + str.charAt(i);
            }
        }
        return resStr;
    }

    /**
     *
     * @param time 2016-5-5 12:12:12 - > 12:12:12
     * @return
     */
    public static String getBrifTime(String time){
        String[] timeArr = time.split(" ");
        if(timeArr.length==2){
            if(timeArr[0].equals(SessionHolder.getCurTime("yyyy-MM-dd"))){
                return timeArr[1]; //今天
            }
            return timeArr[0]; //不是今天
        }
        return time;
    }
}
