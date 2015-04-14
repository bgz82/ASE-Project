package com.cisa.app.views;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.cisa.app.utils.Utils;

public class SelectionPreview extends View {

    private Paint mBitmapPaint;
    private Paint mPaint;
    private int mHeight;
    private int mWidth;
    private ArrayList<RectF> multiRect;
    private ArrayList<Integer> shapes;
    private ArrayList<Integer> damages;
    private RectF mRect;
    boolean drawNow = false;
    private int shape = 0;
    private boolean independent = false;

    public SelectionPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setColor(Color.parseColor("#90cccccc"));
       // mPaint.setColor(Color.LTGRAY);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mHeight = h;
        this.mWidth = w;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (drawNow) {
            if (!independent ) {
                for (int i = 0; i < multiRect.size(); i++) {
                    mPaint = getPaint(Integer.parseInt(damages.get(i) + ""));
                    if (Integer.parseInt(shapes.get(i) + "") == Utils.RECT)
                        canvas.drawRect(multiRect.get(i), mPaint);
                    else if (Integer.parseInt(shapes.get(i) + "") == Utils.OVAL)
                        canvas.drawOval(multiRect.get(i), mPaint);
                }
            } else {
                if(shape == Utils.RECT) {
                    canvas.drawRect(mRect, mPaint);
                } else {
                    canvas.drawOval(mRect, mPaint);
                }
            }
        }
        // canvas.drawRect(10, 10, 100, 100, mPaint);
    }

    public Paint getPaint(int damage) {
        Paint paint = new Paint();
        int color = Color.BLACK;
        paint.setStyle(Paint.Style.STROKE);
        switch (damage) {
            case 0:
                color = Color.RED;
                break;
            case 1:
                color = Color.rgb(202, 113, 58);
                break;
            case 2:
                color = Color.rgb(216, 177, 50);
                break;
            case 3:
                color = Color.rgb(203, 229, 41);
                break;
            case 4:
                color = Color.GREEN;
                break;
        }
        paint.setColor(color);

        return paint;
    }

    public void showSelection(RectF rect, int shape, boolean all) {
        mRect = new RectF(rect.left, rect.top, rect.right, rect.bottom);
        this.shape = shape;
        drawNow = true;
        independent = true;
        invalidate();
    }

    public void drawAll(ArrayList<RectF> rect, ArrayList<Integer> shape,
                        ArrayList<Integer> damage) {
        this.multiRect = rect;
        this.shapes = shape;
        this.damages = damage;
        drawNow = true;
        independent = false;
        invalidate();
    }

    public void clearSelection() {
        // this.setVisibility(View.INVISIBLE);
        drawNow = false;
        invalidate();
    }

}
