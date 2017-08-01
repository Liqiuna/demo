package com.cst14.im.layoutView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.activity.GroupChatActivity;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by qi on 16-8-28.
 */
public class RecordButton extends Button {

    private static final int MIN_RECORD_TIME = 1; // 最短录音时间，单位秒
    private static final int RECORD_OFF = 0; // 不在录音
    private static final int RECORD_ON = 1; // 正在录音

    private static final int HANDEL_Canceled = 1;
    private static final int HANDEL_TOUCHUP = 2;

    private Dialog mRecordDialog;
    private Thread mRecordThread;
    private MediaRecorder mMediaRcoder;
    //private RecordListener listener;
    private float mTime = 0;//用来记录录音的时长

    private int recordState = 0; // 录音状态
    private float recodeTime = 0.0f; // 录音时长，如果录音时间太短则录音失败
    private double voiceValue = 0.0; // 录音的音量值
    private boolean isCanceled = false; // 是否取消录音
    private float downY;
    private String filePath;

    private TextView dialogTextView;
    private ImageView dialogImg;
    private Context mContext;
    private GroupChatActivity groupChatActivity;

    public RecordButton(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public RecordButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        this.setText("按住 说话");
    }

    // 录音时显示Dialog
    private void showVoiceDialog(int flag) {
        if (mRecordDialog == null) {
            mRecordDialog = new Dialog(mContext, R.style.record_style);
            mRecordDialog.setContentView(R.layout.dailog_record);
            dialogImg = (ImageView) mRecordDialog
                    .findViewById(R.id.iv_dailog_record_icon);
            dialogTextView = (TextView) mRecordDialog
                    .findViewById(R.id.tv_dailog_record_text);
        }
        switch (flag) {
            case 1:
                dialogImg.setImageResource(R.drawable.cancel);
                dialogTextView.setText("松开手指\n取消录音");
                this.setText("松开手指 取消录音");
                break;

            default:
                dialogImg.setImageResource(R.drawable.voice_64_0);
                dialogTextView.setText("向上滑动\n取消录音");
                this.setText("松开手指 完成录音");
                break;
        }
        dialogTextView.setTextSize(14);
        mRecordDialog.show();
    }

    // 开启录音计时线程
    private void callRecordTimeThread() {
        mRecordThread = new Thread(recordThread);
        mRecordThread.start();
    }


    // 录音线程
    private Runnable recordThread = new Runnable() {
        @Override
        public void run() {
            recodeTime = 0.0f;
            while (recordState == RECORD_ON) {
                try {
                    Thread.sleep(100);
                    recodeTime += 0.1;
                    if (!isCanceled) {
                        recordHandler.sendEmptyMessage(HANDEL_Canceled);
                    }
                    if (recodeTime >= 60.0) {
                        Log.i("recodeTime > 60", "");
                        recordHandler.sendEmptyMessage(HANDEL_TOUCHUP);
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler recordHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDEL_Canceled:
                    voiceValue = getVoiceLevel(8);
                    setDialogImage();
                    break;

                case HANDEL_TOUCHUP:
                    final long downTime = SystemClock.uptimeMillis();
                    final MotionEvent upEvent = MotionEvent.obtain(
                            downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 10, 10, 0);
                    RecordButton.this.onTouchEvent(upEvent);
                    upEvent.recycle();
                    break;
            }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // 按下按钮
                recordState = recordPrepared();
                if (recordState != RECORD_ON) {
                    Utils.showToast2(mContext, "录音未准备好");
                    mMediaRcoderRelease();
                    break;
                }
                showVoiceDialog(0);
                downY = event.getY();
                callRecordTimeThread();
                break;
            case MotionEvent.ACTION_MOVE: // 滑动手指
                float moveY = event.getY();
                if (downY - moveY > 50) {
                    isCanceled = true;
                    showVoiceDialog(1);
                }
                if (downY - moveY < 20) {
                    isCanceled = false;
                    showVoiceDialog(0);
                }
                break;
            case MotionEvent.ACTION_UP: // 松开手指
                if (recordState == RECORD_ON) {
                    recordState = RECORD_OFF;
                    if (mRecordDialog.isShowing()) {
                        mRecordDialog.dismiss();
                    }
                    mRecordThread.interrupt();
                    voiceValue = 0;
                    mMediaRcoderRelease();
                    //取消了语音
                    if (isCanceled) {
                        break;
                    }
                    if (recodeTime < MIN_RECORD_TIME) {
                        Utils.showToast2(mContext, "录音时间太短");
                        break;
                    }
                    isCanceled = false;
                    this.setText("按住 说话");
                    //完成录音
                    if (GroupChatActivity.instance != null) {
                        Log.i("完成录音", "activity isn't  null");
                       GroupChatActivity.instance.startFileUploadRequest(filePath, ProtoClass.DataType.VOICE,recodeTime);
                    } else {
                        Log.e("完成录音", "activity is null");
                        Utils.showToast2(mContext, "activity is null");
                    }
                }
                break;
        }
        return true;
    }

    // 录音Dialog图片随录音音量大小切换
    private void setDialogImage() {
        if (voiceValue < 1) {
            dialogImg.setImageResource(R.drawable.voice_64_0);
        } else if (voiceValue >= 1 && voiceValue < 2) {
            dialogImg.setImageResource(R.drawable.voice_64_1);
        } else if (voiceValue >= 2 && voiceValue < 3) {
            dialogImg.setImageResource(R.drawable.voice_64_2);
        } else if (voiceValue >= 3 && voiceValue < 4) {
            dialogImg.setImageResource(R.drawable.voice_64_3);
        } else if (voiceValue >= 4 && voiceValue < 5) {
            dialogImg.setImageResource(R.drawable.voice_64_4);
        } else if (voiceValue >= 5 && voiceValue < 6) {
            dialogImg.setImageResource(R.drawable.voice_64_5);
        } else if (voiceValue >= 6 && voiceValue < 7) {
            dialogImg.setImageResource(R.drawable.voice_64_6);
        } else if (voiceValue >= 7 && voiceValue < 8) {
            dialogImg.setImageResource(R.drawable.voice_64_7);
        }
    }

    //录音的准备工作，要准备好录音存取的文件地址，录音器的准备等
    public int recordPrepared() {
        /// /录下的声音所输出的文件名
        String fileName = new DateFormat().format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA)) + ".amr";
        filePath = ImApplication.voiceDir + File.separator + fileName;
        File file = new File(ImApplication.voiceDir + File.separator, fileName);//最终在文件夹mDir下面生成文件fileName
        try {
            /*
             * 下面的代码为初始化录音的这个实例，并做录音准备工作
             */
            mMediaRcoder = new MediaRecorder();
            //设置音频输出到哪个文件中，注意该参数应该是一个完成的路径，最终文件应该是.mar格式的。
            mMediaRcoder.setOutputFile(filePath);
            //设置音频源为我们的麦克风
            mMediaRcoder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置音频格式
            mMediaRcoder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            //设置音频的编码格式为amr
            mMediaRcoder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRcoder.prepare();
            mMediaRcoder.start();
            return RECORD_ON;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return RECORD_OFF;
        } catch (IOException e) {
            e.printStackTrace();
            return RECORD_OFF;
        }

    }

    //通过音频获得声音的级别，转化为1~maxLevel之间
    public int getVoiceLevel(int maxLevel) {
        if (mMediaRcoder != null) {
            try {
                return maxLevel * mMediaRcoder.getMaxAmplitude() / 32768 + 1;
            } catch (IllegalStateException e) {//在这里，我们捕捉一下错误，是为了不让影响程序进行。
                //因为就算音频没法捕捉到，也不是什么大事，只要声音录制到了就可以正常进行。
                //所以在此忽略掉这个错误
            }
        }
        return 1; //没有捕捉到音频，就默认为等级为1，并返回
    }

    //释放资源
    public void mMediaRcoderRelease() {
        if (mMediaRcoder != null) {
            mMediaRcoder.stop();
            mMediaRcoder.release();
            mMediaRcoder = null;
        }
    }
}
