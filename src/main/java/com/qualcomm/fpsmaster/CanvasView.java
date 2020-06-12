package com.qualcomm.fpsmaster;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.RequiresApi;

public class CanvasView extends View {
    private final Paint mGreenPaint;
    private float mFps;
    private long mFrameCount;
    private long mLastDrawNano;
    private long mLastFpsUpdate;
    private static final long BALL_VELOCITY = 420;
    private static final float FPS_UPDATE_THRESHOLD = 20.0f;
    Context context;

    public CanvasView(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;
        this.mGreenPaint = new Paint(1);
        this.mGreenPaint.setColor(-16711936);
        this.mGreenPaint.setAlpha(2);
        this.mGreenPaint.setStyle(Paint.Style.FILL);
        this.mLastDrawNano = 0;
        this.mFps = 0.0f;
        this.mLastFpsUpdate = 0;
        this.mFrameCount = 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT);
        drawBall(canvas);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void drawBall(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        long nanoTime = System.nanoTime();
        long j = nanoTime - this.mLastDrawNano;
        this.mLastDrawNano = nanoTime;
        this.mFrameCount++;
        float f = j < 1000000000 ? 1.0E9f / ((float) j) : 0.0f;
        long j2 = nanoTime - this.mLastFpsUpdate;
        if (Math.abs(this.mFps - f) > FPS_UPDATE_THRESHOLD) {
            this.mFps = f;
            this.mLastFpsUpdate = nanoTime;
            this.mFrameCount = 0;
        } else if (j2 > 1000000000) {
            this.mFps = ((((float) this.mFrameCount) * 1.0f) * 1.0E9f) / ((float) j2);
            this.mLastFpsUpdate = nanoTime;
            this.mFrameCount = 0;
        }
        long j3 = (nanoTime * BALL_VELOCITY) / 1000000000;
        long j4 = (long) (width - 50);
        long j5 = (long) (height - 50);
        long j6 = j3 % j4;
        long j7 = j3 % j5;
        float f2 = ((j3 / j4) & 1) == 0 ? (float) (j4 - j6) : (float) j6;
        float f3 = f2 + 50.0f;
        float f4 = ((j3 / j5) & 1) == 0 ? (float) (j5 - j7) : (float) j7;
        float f5 = f4 + 50.0f;
        canvas.drawOval(f2, j5+50, f3, j5, this.mGreenPaint);
        invalidate();
    }
}
