package com.cst14.im.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cst14.im.R;

public class Utils {
    /**
     * user liwenjun
     * @param context
     * @param text
     * 从上往下滑出一个toast，再上移消失
     */
    public static final int duation = 500;
    public static final int delay = 800;
    public static void showToast2(Context context, String text) {
        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.flags =WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.format = PixelFormat.TRANSLUCENT;
        params.gravity = Gravity.TOP|Gravity.LEFT;

        final View toast2 = View.inflate(context, R.layout.layout_toast2, null);
        TextView tvMsg = (TextView) toast2.findViewById(R.id.tv_toast2_msg);
        tvMsg.setText(text);

        final RelativeLayout root = new RelativeLayout(context);
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.addRule(RelativeLayout.CENTER_HORIZONTAL);
        toast2.setLayoutParams(p);

        root.addView(toast2);
        windowManager.addView(root,params);

        final float translation = 80;
        AnimatorSet show = new AnimatorSet();
        ObjectAnimator translationY = ObjectAnimator.ofFloat(toast2, "translationY", 0, translation);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(toast2, "alpha", 0, 1);
        show.setDuration(duation);
        show.setTarget(toast2);
        show.playTogether(translationY, alpha);
        show.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AnimatorSet hide = new AnimatorSet();
                ObjectAnimator translationY = ObjectAnimator.ofFloat(toast2, "translationY", translation, 0);
                ObjectAnimator alpha = ObjectAnimator.ofFloat(toast2, "alpha", 1, 0);
                hide.setDuration(duation);
                hide.setTarget(toast2);
                hide.playTogether(translationY, alpha);
                hide.setStartDelay(delay);
                hide.start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            windowManager.removeView(root);
                        }catch (Exception e){
                            Log.e("In Toast2",e.toString());
                        }
                    }
                },duation+delay);
            }
        }, duation);
    }

    public static void showToast(Context context, String text) {
        Toast toa = new Toast(context);
        View t = View.inflate(context, R.layout.layout_toast, null);
        TextView tv_toast_msg = (TextView) t.findViewById(R.id.tv_toast_msg);
        tv_toast_msg.setText(text);
        toa.setView(t);
        toa.show();
    }
    //获取视频缩略图
    public static Bitmap getVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime();
        }catch(Exception e) {
            Log.e("错误",e.toString());
        }
        finally {
            try {
                retriever.release();
            }
            catch (Exception e) {
                Log.e("错误",e.toString());
            }
        }
        return bitmap;
    }
}
