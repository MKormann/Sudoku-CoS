package com.licktopia.thechampionofsudoku;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;

/**
 * Created by Matt on 8/17/2017.
 */

public class CustomHorizontalScrollView extends HorizontalScrollView {

    static final int MIN_DISTANCE = 25;
    float x1, x2;

    public CustomHorizontalScrollView(Context context) {
        super(context);
  //      setOnClickListener();
    }

    public CustomHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //setOnClickListener();
    }

    public CustomHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //setOnClickListener();
    }

    public CustomHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
//        setOnClickListener();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        Log.i("Scroller", "Gen X: " + motionEvent.getX());
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        /*
        Log.i("Scroller", MotionEvent.actionToString(motionEvent.getAction()));
        if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
            x1 = motionEvent.getX();
            Log.i("Scroller", "Start X: " + x1);
        }
        else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
            x2 = motionEvent.getX();
            float deltaX = x2 - x1;

            WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
            Display d = wm.getDefaultDisplay();
            int view_width = 2*d.getWidth();
            Log.i("Scroller", "View width:" + view_width);
            Log.i("Scroller", "Abs DeltaX: " + Math.abs(deltaX) + " MIN_DISTANCE: " + MIN_DISTANCE);
            if (Math.abs(deltaX) > MIN_DISTANCE)
            {
                Log.i("Scroller", "Abs is greater than MINDISTANCE");
                // Left to Right swipe action
                if (x2 > x1) {
                    Log.i("Scroller", "L TO R");
                    smoothScrollTo(0, 0);


                }

                // Right to left swipe action
                else
                {
                    Log.i("Scroller", "R TO L");

                    smoothScrollTo(view_width,0);
                }

            }
            else
            {
                Log.i("Scroller", "ScrollX" + getScrollX());
                if(getScrollX()!=0 || getScrollX()!=view_width){
                    if(getScrollX()<view_width/2) {
                        smoothScrollTo(0, 0);
                    }
                    else{
                        smoothScrollTo(view_width, 0);
                    }
                }
            }
            return true;
        }*/
        return false;
    }

    public float getX1() {
        return x1;
    }
}
