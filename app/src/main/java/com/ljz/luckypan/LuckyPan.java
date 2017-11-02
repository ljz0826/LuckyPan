package com.ljz.luckypan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by lenovo on 2017/11/2.
 */

public class LuckyPan extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    private Thread t;
    private boolean isRunning;
    private Bitmap mBgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg2);
    private String[] mStrs = new String[]{"单反相机", "IPAD", "恭喜发财", "IPHONE", "服装一套", "恭喜发财"};
    private int[] mImgs = new int[]{R.drawable.danfan, R.drawable.ipad, R.drawable.f015, R.drawable.iphone, R.drawable.meizi, R.drawable.f015};
    private int[] mColor = new int[]{0xFFFFC300, 0XFFF17E01, 0xFFFFC300, 0XFFF17E01, 0xFFFFC300, 0XFFF17E01};
    private int mItemCount = 6;
    private RectF mRange = new RectF();
    private int mRadius;
    private Paint mArcPaint;
    private Paint mTextPaint;
    private double mSpeed = 0;
    private volatile float mStartAngle = 0; //保证线程间的可见性
    private boolean isShouldEnd;
    private int mCenter;
    private int mPadding;
    private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, getResources().getDisplayMetrics());
    Bitmap[] mImgsBitmap;

    public LuckyPan(Context context) {
        this(context, null);
    }

    public LuckyPan(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LuckyPan(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mHolder = getHolder(); //不仅获取canvas还管理surfaceview生命周期
        mHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setKeepScreenOn(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setDither(true);

        mTextPaint = new Paint();
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);

        mRange = new RectF(mPadding, mPadding, mPadding + mRadius, mPadding + mRadius);
        mImgsBitmap = new Bitmap[mItemCount];

        for (int i = 0; i < mImgsBitmap.length; i++) {
            mImgsBitmap[i] = BitmapFactory.decodeResource(getResources(), mImgs[i]);
        }

        isRunning = true;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        isRunning = false;
    }

    @Override
    public void run() {
        while (isRunning) {
            long start = System.currentTimeMillis();
            draw();
            long end = System.currentTimeMillis();
            if (end - start < 50) {
                try {
                    Thread.sleep(50 - (end - start));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void draw() {
        try {
            mCanvas = mHolder.lockCanvas();
            if (mCanvas != null) {
                //drawsomething
                drawBg();
                float tmpAngle = mStartAngle;
                float sweepAngle = 360 / mItemCount;
                for (int i = 0; i < mItemCount; i++) {
                    mArcPaint.setColor(mColor[i]);
                    mCanvas.drawArc(mRange, tmpAngle, sweepAngle, true, mArcPaint);//useCenter 从终点画
                    drawText(tmpAngle, sweepAngle, mStrs[i]);
                    drawIcon(tmpAngle, mImgsBitmap[i]);
                    tmpAngle += sweepAngle;
                }
                mStartAngle += mSpeed;
                if (isShouldEnd) {
                    mSpeed -= 1;
                }
                if (mSpeed <= 0) {
                    mSpeed = 0;
                    isShouldEnd = false;
                }
            }
        } catch (Exception e) {

        } finally {
            if (mCanvas != null) {
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    private void drawIcon(float tmpAngle, Bitmap bitmap) {
        int imgWidth = mRadius / 8;
        float angle = (float) ((tmpAngle + 360 / mItemCount / 2) * Math.PI / 180);
        int x = (int) (mCenter + mRadius / 2 / 2 * Math.cos(angle));
        int y = (int) (mCenter + mRadius / 2 / 2 * Math.sin(angle));
        Rect rect = new Rect(x - imgWidth / 2, y - imgWidth / 2, x + imgWidth / 2, y + imgWidth / 2);
        mCanvas.drawBitmap(bitmap, null, rect, null);
    }

    private void drawText(float tmpAngle, float sweepAngle, String mStr) {
        Path path = new Path();
        path.addArc(mRange, tmpAngle, sweepAngle);
        float textWidth = mTextPaint.measureText(mStr);
        int hOffset = (int) (mRadius * Math.PI / mItemCount / 2 - textWidth / 2);
        int vOffset = mRadius / 12;
        mCanvas.drawTextOnPath(mStr, path, hOffset, vOffset, mTextPaint);
    }

    private void drawBg() {
        mCanvas.drawColor(0xffffffff);
        mCanvas.drawBitmap(mBgBitmap, null, new Rect(mPadding / 2, mPadding / 2, getMeasuredWidth() - mPadding / 2, getMeasuredWidth() - mPadding / 2), null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = Math.min(getMeasuredHeight(), getMeasuredWidth());
        mPadding = getPaddingLeft();
        mRadius = width - mPadding * 2;
        mCenter = width / 2;
        setMeasuredDimension(width, width);
    }

    public void luckyStart(int index) {
        float angle = 360 / mItemCount;

        float from = 270 - (index + 1) * angle;
        float end = from + angle;

        float targetFrom = 4 * 360 + from;
        float targetEnd = 4 * 360 + end;

        float v1 = (float) ((-1 + Math.sqrt(1 + 8 * targetFrom)) / 2);
        float v2 = (float) ((-1 + Math.sqrt(1 + 8 * targetEnd)) / 2);
        mSpeed = v1+ Math.random() * (v2 - v1);

        isShouldEnd = false;
    }

    public void luckyEnd() {
        Log.i("zhou","luckend");
        mStartAngle = 0;
        isShouldEnd = true;
    }

    public boolean isStart() {
        return mSpeed != 0;
    }

    public boolean isShouldEnd() {
        return isShouldEnd;
    }

}

