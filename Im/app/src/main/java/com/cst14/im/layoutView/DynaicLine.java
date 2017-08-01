package com.cst14.im.layoutView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by MRLWJ on 2016/8/6.
 * 控件高度 暂时要求固定为3dp
 */
public class DynaicLine extends View {
    private Paint paint;
    private int screenWidth;

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
        invalidate();
    }

    public int getLineWidth() {
        return lineWidth;
    }
    public void reset(){
        setLineWidth(screenWidth);
        paint.setColor(Color.GREEN);
    }

    private int lineWidth;
    public DynaicLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();
    }

    public void updateColor(int color) {
        paint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float left = (screenWidth-lineWidth)/2.0f;
        float top = 0;
        float right = left+lineWidth;
        float bottom = 3;
        RectF oval3 = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(oval3, 3, 3, paint);
    }
}
