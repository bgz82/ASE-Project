package com.cisa.app.views;

import java.util.ArrayList;

import com.cisa.app.R;
import com.cisa.app.utils.EdgeIcon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class GestureView extends View {

    Point point1, point3;
    Point point2, point4;

    /**
     * point1 and point 3 are of same group and same as point 2 and point4
     */
    int groupId = -1;
    private ArrayList<EdgeIcon> icons = new ArrayList<EdgeIcon>();
    private int iconID = 0;
    Paint paint;
    Canvas canvas;

    public GestureView(Context context) {
        super(context);
        paint = new Paint();
        paint.setColor(Color.CYAN);
        setFocusable(true); // necessary for getting the touch events
        canvas = new Canvas();
        // setting the start point
        point1 = new Point();
        point1.x = 50;
        point1.y = 20;

        point2 = new Point();
        point2.x = 150;
        point2.y = 20;

        point3 = new Point();
        point3.x = 150;
        point3.y = 120;

        point4 = new Point();
        point4.x = 50;
        point4.y = 120;

        // declare each icon with the EdgeIcon class
        icons.add(new EdgeIcon(context, R.drawable.grsture_icon, point1));
        icons.add(new EdgeIcon(context, R.drawable.grsture_icon, point2));
        icons.add(new EdgeIcon(context, R.drawable.grsture_icon, point3));
        icons.add(new EdgeIcon(context, R.drawable.grsture_icon, point4));
    }

    public GestureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public GestureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        setFocusable(true); // necessary for getting the touch events
        canvas = new Canvas();
 
        point1 = new Point();
        point1.x = 50;
        point1.y = 20;

        point2 = new Point();
        point2.x = 150;
        point2.y = 20;

        point3 = new Point();
        point3.x = 150;
        point3.y = 120;

        point4 = new Point();
        point4.x = 50;
        point4.y = 120;

        icons.add(new EdgeIcon(context, R.drawable.grsture_icon, point1));
        icons.add(new EdgeIcon(context, R.drawable.grsture_icon, point2));
        icons.add(new EdgeIcon(context, R.drawable.grsture_icon, point3));
        icons.add(new EdgeIcon(context, R.drawable.grsture_icon, point4));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        // canvas.drawColor(0xFFCCCCCC); //if you want another background color

        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.parseColor("#55000000"));
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeJoin(Paint.Join.ROUND);
        // mPaint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(5);

        canvas.drawPaint(paint);
        paint.setColor(Color.parseColor("#55FFFFFF"));

            canvas.drawRect(point2.x + icons.get(1).getWidthOfBall() / 2,
                    point4.y + icons.get(3).getWidthOfBall() / 2, point4.x
                            + icons.get(3).getWidthOfBall() / 2, point2.y
                            + icons.get(1).getWidthOfBall() / 2, paint);
        BitmapDrawable mBitmap;
        mBitmap = new BitmapDrawable();

        // draw the balls on the canvas
        for (EdgeIcon ball : icons) {
            canvas.drawBitmap(ball.getBitmap(), ball.getX(), ball.getY(), new Paint());
        }
    }

    // events when touching the screen
    public boolean onTouchEvent(MotionEvent event) {
        int eventaction = event.getAction();

        int X = (int) event.getX();
        int Y = (int) event.getY();

        switch (eventaction) {

        case MotionEvent.ACTION_DOWN: // touch down so check if the finger is on
                                        // a ball
            iconID = -1;
            groupId = -1;
            for (EdgeIcon ball : icons) {
                // check if inside the bounds of the ball (circle)
                // get the center for the ball
               // Utils.logd("Id : " + ball.getID());
                //Utils.logd("getX : " + ball.getX() + " getY() : " + ball.getY());
                int centerX = ball.getX() + ball.getWidthOfBall();
                int centerY = ball.getY() + ball.getHeightOfBall();
                paint.setColor(Color.CYAN);
                // calculate the radius from the touch to the center of the ball
                double radCircle = Math
                        .sqrt((double) (((centerX - X) * (centerX - X)) + (centerY - Y)
                                * (centerY - Y)));

//                Utils.logd("X : " + X + " Y : " + Y + " centerX : " + centerX
  //                      + " CenterY : " + centerY + " radCircle : " + radCircle);

                if (radCircle < ball.getWidthOfBall()) {

                    iconID = ball.getID();
               //     Utils.logd("Selected ball : " + iconID);
                    if (iconID == 1 || iconID == 3) {
                        groupId = 2;
                        canvas.drawRect(point1.x, point3.y, point3.x, point1.y,
                                paint);
                    } else {
                        groupId = 1;
                        canvas.drawRect(point2.x, point4.y, point4.x, point2.y,
                                paint);
                    }
                    invalidate();
                    break;
                }
                invalidate();
            }

            break;

        case MotionEvent.ACTION_MOVE: // touch drag with the ball
            // move the balls the same as the finger
            if (iconID > -1) {
             //   Utils.logd("Moving Ball : " + iconID);

                icons.get(iconID).setX(X);
                icons.get(iconID).setY(Y);

                paint.setColor(Color.CYAN);

                if (groupId == 1) {
                    icons.get(1).setX(icons.get(0).getX());
                    icons.get(1).setY(icons.get(2).getY());
                    icons.get(3).setX(icons.get(2).getX());
                    icons.get(3).setY(icons.get(0).getY());
                    canvas.drawRect(point1.x, point3.y, point3.x, point1.y,
                            paint);
                } else {
                    icons.get(0).setX(icons.get(1).getX());
                    icons.get(0).setY(icons.get(3).getY());
                    icons.get(2).setX(icons.get(3).getX());
                    icons.get(2).setY(icons.get(1).getY());
                    canvas.drawRect(point2.x, point4.y, point4.x, point2.y,
                            paint);
                }
                invalidate();
            }
            break;

        case MotionEvent.ACTION_UP:
            // touch drop - just do things here after dropping

            break;
        }
        // redraw the canvas
        invalidate();
        return true;

    }

    public void shade_region_between_points() {
        canvas.drawRect(point1.x, point3.y, point3.x, point1.y, paint);
    }
}