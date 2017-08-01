package com.cst14.im.layoutView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Utils;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by MRLWJ on 2016/8/6.
 */
public class VideoCaptureLayout extends RelativeLayout implements SurfaceHolder.Callback {
    private View root;
    private Context context;
    private SurfaceView sv_view;
    private Camera mCamera;
    private boolean isRecording;
    private MediaRecorder mediaRecorder;
    private TextView tvBeginCapture;
    public static final int LINE_CHANGE_SPACING = 5;
    public static final int LINE_CHANGE_TIME_GAP = 100;
    public static final int CODE_BEGIN_CHANGE = 0;
    public static final int CODE_END_CHANGE = 1;
    private DynaicLine dlTimeLine;
    private TextView tvTipUp;
    private TextView tvTipDown;
    private View preLayout;  //相机开始预览之前的准备界面
    private int density;
    private int displayHeight, displayWidth;

    public VideoCaptureLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        density = (int) context.getResources().getDisplayMetrics().density;
        root = View.inflate(context, R.layout.layout_video_capture, null);
        addView(root);
        preLayout = findViewById(R.id.rl_vdo_pre);
        sv_view = (SurfaceView) findViewById(R.id.sv_view);
        sv_view.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 声明Surface不维护自己的缓冲区，针对Android3.0以下设备支持
        sv_view.getHolder().addCallback(this);
        tvBeginCapture = (TextView) findViewById(R.id.tv_video_begin);
        tvBeginCapture.setOnTouchListener(new OnTouchListener() {
            int startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = (int) event.getRawY();
                        isCancelVideo = false;
                        dlTimeLine.reset();
                        handler.sendEmptyMessage(CODE_BEGIN_CHANGE);
                        tvTipDown.setVisibility(View.VISIBLE);
                        start();
                        break;
                    case MotionEvent.ACTION_UP:
                        handler.sendEmptyMessage(CODE_END_CHANGE);
                        tvTipDown.setVisibility(View.GONE);
                        tvTipUp.setVisibility(View.GONE);
                        stop();
                        if (!isCancelVideo && onVideoCaptureFinishListener != null) {
                            if (videoFile == null) {  //video应该被舍弃
                                onVideoCaptureFinishListener.onFinish(null, null);
                            } else {
                                Bitmap thumb = Utils.getVideoThumbnail(videoFile.getAbsolutePath());
                                onDestroy();
                                onVideoCaptureFinishListener.onFinish(thumb, videoFile);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // 这里不知道为什么用GestureDector 判断触摸事件，onScroll不执行，而语音那边却可以
                        int dis = startY - (int) event.getRawY();
                        if (dis > 70) {
                            dlTimeLine.updateColor(Color.RED);
                            tvTipDown.setVisibility(View.GONE);
                            tvTipUp.setVisibility(View.VISIBLE);
                            isCancelVideo = true;
                        } else {
                            dlTimeLine.updateColor(Color.GREEN);
                            tvTipDown.setVisibility(View.VISIBLE);
                            tvTipUp.setVisibility(View.GONE);
                            isCancelVideo = false;
                        }
                        break;
                }
                return true;
            }
        });

        dlTimeLine = (DynaicLine) findViewById(R.id.dl_time_line);
        tvTipDown = (TextView) findViewById(R.id.tv_video_tip_down);
        tvTipUp = (TextView) findViewById(R.id.tv_video_tip_up);
    }

    private boolean isCancelVideo;
    private File videoFile;

    private void start() {
        try {
            String videoName = new DateFormat().format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA)) + ".mp4";
            videoFile = new File(ImApplication.videoDir, videoName);
            File dir = videoFile.getParentFile();
            if (!dir.exists()) dir.mkdirs();
            if (videoFile.exists()) {
                videoFile.delete();
            }
            mediaRecorder = new MediaRecorder();
            mediaRecorder.reset();
            mCamera.setDisplayOrientation(90);
            mCamera.unlock();
            mediaRecorder.setCamera(mCamera);
            mediaRecorder.setOrientationHint(90);
            // 设置音频录入源
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // 设置视频图像的录入源
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            // 设置录入媒体的输出格式
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setVideoSize(200 * density, 230 * density);
            // 设置音频的编码格式
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            // 设置视频的编码格式
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
            // 设置视频的采样率，每秒4帧
            mediaRecorder.setVideoFrameRate(4);
            // 设置录制视频文件的输出路径
            mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
            // 设置捕获视频图像的预览界面
            mediaRecorder.setPreviewDisplay(sv_view.getHolder().getSurface());

            mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {

                @Override
                public void onError(MediaRecorder mr, int what, int extra) {
                    try {
                        // 发生错误，停止录制
                        mediaRecorder.stop();
                        mediaRecorder.release();
                        mediaRecorder = null;
                        isRecording = false;
                    } catch (Exception e) {
                        Log.e("错误", e.toString());
                    }
                    Log.e("错误", "OnErrorListener");
                }
            });
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
        } catch (Exception e) {
            Log.e("错误 in start", e.toString());
        }
    }

    private void stop() {
        try {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
        } catch (Exception e) {
            Log.e("错误", e.toString());
        }
        if (videoFile != null && videoFile.exists() && videoFile.length() > 0) {
            return;
        }
        videoFile = null; //运行到这里说明 file 无效
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CODE_BEGIN_CHANGE:
                    int lineWidth = dlTimeLine.getLineWidth();
                    lineWidth -= LINE_CHANGE_SPACING;
                    lineWidth = lineWidth >= 0 ? lineWidth : 0;
                    dlTimeLine.setLineWidth(lineWidth);
                    if (lineWidth > 0) {
                        handler.sendEmptyMessageDelayed(CODE_BEGIN_CHANGE, LINE_CHANGE_TIME_GAP);
                    }
                    break;
                case CODE_END_CHANGE:
                    dlTimeLine.setLineWidth(0);
                    break;
            }
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startPreView();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    // 录像完成时的回调定义
    public interface OnVideoCaptureFinishListener {
        void onFinish(Bitmap thumb, File videoFile);
    }

    public void setOnVideoCaptureFinishListener(OnVideoCaptureFinishListener onVideoCaptureFinishListener) {
        this.onVideoCaptureFinishListener = onVideoCaptureFinishListener;
    }

    private OnVideoCaptureFinishListener onVideoCaptureFinishListener;

    public void onDestroy() {
        try {
            if (isRecording) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            }
            if (null != mCamera) {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            Log.e("错误", e.toString());
        }
    }

    public void startPreView() {
        try {
            mCamera = Camera.open();
            mCamera.setPreviewDisplay(sv_view.getHolder());
            mCamera.setDisplayOrientation(90);
            preLayout.setVisibility(View.INVISIBLE);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.e("错误", e.toString());
        }
    }
}
