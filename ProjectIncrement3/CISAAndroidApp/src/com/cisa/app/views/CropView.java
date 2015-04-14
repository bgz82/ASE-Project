package com.cisa.app.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.cisa.app.R;

public class CropView extends View {

    public static final int DRAG = 0;
    public static final int LEFT = 1;
    public static final int TOP = 2;
    public static final int RIGHT = 3;
    public static final int BOTTOM = 4;


    public static final int MODE_RECT = 5;
    public static final int MODE_CIRCLE = 6;

    private int currentDirection = -1;
    private int mode = MODE_RECT;

    private Paint mBitmapPaint;
    private Paint mPaint;
    private boolean drawFlag = false;
    private int mHeight;
    private int mWidth;
    private Bitmap hor_adjust;
    private Bitmap ver_adjust;
    private RectF mRect;
    private Path pt;
    private float prevX;
    private float prevY;

    public CropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBitmapPaint = new Paint();
        mPaint = new Paint();		
        mBitmapPaint.setColor(Color.GRAY);
		mPaint.setColor(Color.parseColor("#90cccccc"));
       //mPaint.setColor(Color.LTGRAY);

        mRect = new RectF(30f, 50f, 200f, 250f);

        pt = new Path();
        pt.addOval(mRect, Direction.CCW);

        hor_adjust = BitmapFactory.decodeResource(getResources(),
                R.drawable.resize_horizontal);
        ver_adjust = BitmapFactory.decodeResource(getResources(),
                R.drawable.resize_vertical);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        super.onSizeChanged(w, h, oldw, oldh);

        this.mHeight = h;
        this.mWidth = w;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        if (drawFlag) {

            // assume current clip is full canvas
            // put a hole in the current clip
            if (mode == MODE_RECT) {
                canvas.clipRect(mRect, Region.Op.DIFFERENCE);
            } else if (mode == MODE_CIRCLE) {
                pt.reset();
                pt.addOval(mRect, Direction.CCW);
                canvas.clipPath(pt, Region.Op.DIFFERENCE);
            }
            // fill with semi-transparent red
            canvas.drawARGB(50, 255, 0, 0);
            // restore full canvas clip for any subsequent operations
            canvas.clipRect(
                    new Rect(0, 0, canvas.getWidth(), canvas.getHeight()),
                    Region.Op.REPLACE);

            canvas.drawBitmap(
                    hor_adjust,
                    mRect.left - hor_adjust.getWidth() / 2,
                    mRect.top + mRect.height() / 2 - hor_adjust.getHeight() / 2,
                    mBitmapPaint);
            canvas.drawBitmap(
                    hor_adjust,
                    mRect.right - hor_adjust.getWidth() / 2,
                    mRect.top + mRect.height() / 2 - hor_adjust.getHeight() / 2,
                    mBitmapPaint);

            canvas.drawBitmap(ver_adjust, mRect.left + mRect.width() / 2
                            - ver_adjust.getWidth() / 2,
                    mRect.top - ver_adjust.getHeight() / 2, mBitmapPaint);
            canvas.drawBitmap(ver_adjust, mRect.left + mRect.width() / 2
                            - ver_adjust.getWidth() / 2,
                    mRect.bottom - ver_adjust.getHeight() / 2, mBitmapPaint);

        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int eventaction = event.getAction();
        float x = event.getX();
        float y = event.getY();


        switch (eventaction) {

            case MotionEvent.ACTION_DOWN:
                RectF leftReft = new RectF(
                        mRect.left - hor_adjust.getWidth() / 2,
                        mRect.top + mRect.height() / 2 - hor_adjust.getHeight() / 2,
                        mRect.left + hor_adjust.getWidth() / 2, mRect.top
                        + mRect.height() / 2 + hor_adjust.getHeight() / 2);

                RectF topRect = new RectF(mRect.left + mRect.width() / 2
                        - ver_adjust.getWidth() / 2, mRect.top
                        - ver_adjust.getHeight() / 2, mRect.left + mRect.width()
                        / 2 + ver_adjust.getWidth() / 2, mRect.top
                        + ver_adjust.getHeight() / 2);

                RectF rightRect = new RectF(
                        mRect.right - hor_adjust.getWidth() / 2, mRect.top
                        + mRect.height() / 2 - hor_adjust.getHeight() / 2,
                        mRect.right + hor_adjust.getWidth() / 2, mRect.top
                        + mRect.height() / 2 + hor_adjust.getHeight() / 2);

                RectF bottomRect = new RectF(mRect.left + mRect.width() / 2
                        - ver_adjust.getWidth() / 2, mRect.bottom
                        - ver_adjust.getHeight() / 2, mRect.left + mRect.width()
                        / 2 + ver_adjust.getWidth() / 2, mRect.bottom
                        + ver_adjust.getHeight() / 2);

                if (leftReft.contains(x, y)) {
                    currentDirection = LEFT;
                } else if (topRect.contains(x, y)) {
                    currentDirection = TOP;
                } else if (rightRect.contains(x, y)) {
                    currentDirection = RIGHT;
                } else if (bottomRect.contains(x, y)) {
                    currentDirection = BOTTOM;
                } else {
                    currentDirection = DRAG;
                    prevX = x;
                    prevY = y;
                }
                break;
            case MotionEvent.ACTION_MOVE:

                if (currentDirection == LEFT) {

                    if (x < mRect.right && x >= 0) {
                        mRect.left = x;
                    }

                } else if (currentDirection == RIGHT) {

                    if (x > mRect.left && x <= mWidth) {
                        mRect.right = x;
                    }
                } else if (currentDirection == TOP) {
                    if (y < mRect.bottom && y >= 0) {

                        mRect.top = y;
                    }

                } else if (currentDirection == BOTTOM) {

                    if (y > mRect.top && y <= mHeight) {
                        mRect.bottom = y;
                    }
                } else if (currentDirection == DRAG) {

                    if(mRect.left < x && mRect.right > x && mRect.top < y && mRect.bottom > y ) {
                        //Toast.makeText(getContext(), mRect.left+ ", " + x + ", " + a, Toast.LENGTH_SHORT).show();
                        mRect.left = mRect.left + (x-prevX);
                        mRect.right = mRect.right + (x - prevX);
                        mRect.top = mRect.top + y - prevY;
                        mRect.bottom = mRect.bottom + y - prevY;

                        prevX = x;
                        prevY = y;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                currentDirection = -1;
                break;

        }
        invalidate();
        return true;
    }

    public void showCropView() {

        mRect = new RectF(mWidth / 4, mHeight / 4, 3 * mWidth / 4,
                3 * mHeight / 4);
        drawFlag = true;
        invalidate();
    }

    public void clearCropView() {

        drawFlag = false;
        // mRect = new RectF(mWidth/4,mHeight/4,3*mWidth/4,3*mHeight/4);
        invalidate();
    }

    public void setMode(int mode) {

        this.mode = mode;

        invalidate();
    }

    public int getMode() {
        return mode;
    }

    public RectF getRect() {
        return mRect;
    }

}
