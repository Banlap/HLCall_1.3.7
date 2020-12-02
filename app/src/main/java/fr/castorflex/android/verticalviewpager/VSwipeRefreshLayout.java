package fr.castorflex.android.verticalviewpager;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import hzhl.net.hlcall.R;


/**
 * Created by elileo on 2017/3/23.
 */
public class VSwipeRefreshLayout extends SwipeRefreshLayout {
    private float startY;
    private float startX;
    private boolean mIsVpDrag;
    private final int mTouchSlop;

    private int backgroundColor ;
    private int progressColor;

    public VSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VSwipeRefreshLayout);
        backgroundColor = a.getColor(R.styleable.VSwipeRefreshLayout_progressBackgroundColor, Color.parseColor("#ffc424"));
        progressColor = a.getColor(R.styleable.VSwipeRefreshLayout_progressColor, Color.parseColor("#368CEF"));
        a.recycle();
//        setProgressBackgroundColorSchemeColor(backgroundColor);
        setColorSchemeColors(progressColor);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                startY = ev.getRawY();
                startX = ev.getRawX();
                mIsVpDrag = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if(mIsVpDrag) {
                    return false;
                }

                float endY = ev.getRawY();
                float endX = ev.getRawX();
                float distanceX = Math.abs(endX - startX);
                float distanceY = Math.abs(endY - startY);
                // 如果X轴位移大于Y轴位移，那么将事件交给viewPager处理。
                if(distanceX > mTouchSlop && distanceX > 0.5 * distanceY) {
                    mIsVpDrag = true;
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsVpDrag = false;
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }
}
