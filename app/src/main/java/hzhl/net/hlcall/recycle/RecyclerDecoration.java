package hzhl.net.hlcall.recycle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * Created by elileo on 2017/3/22.
 */
public class RecyclerDecoration extends RecyclerView.ItemDecoration{
    private Drawable mDivider;
    private int orientation;
    private int dividerPadding = 0;

    public static final int VERTICAL = LinearLayoutManager.VERTICAL;
    public static final int HORIZONTAL = LinearLayoutManager.HORIZONTAL;

    public RecyclerDecoration(Context context, int orientation, int divider) {
        this.mDivider = context.getResources().getDrawable(divider);
        this.orientation = orientation;
    }

    public RecyclerDecoration(Context context, int orientation, int divider, int dividerPadding){
        this(context, orientation, divider);
        this.dividerPadding = dividerPadding;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        switch (orientation){
            case VERTICAL :
                drawVerticalLine(c, parent, state);
                break;
            case HORIZONTAL :
                drawHorizontalLine(c, parent, state);
                break;
        }
    }
    private void drawHorizontalLine(Canvas c, RecyclerView parent, RecyclerView.State state){
        int left = parent.getPaddingLeft();
        int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++){
            final View child = parent.getChildAt(i);

            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)child.getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left + dividerPadding, top, right - dividerPadding, bottom);
            mDivider.draw(c);
        }
    }


    private void drawVerticalLine(Canvas c, RecyclerView parent, RecyclerView.State state){
        int top = parent.getPaddingTop();
        int bottom = parent.getHeight() - parent.getPaddingBottom();
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++){
            final View child = parent.getChildAt(i);

            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)child.getLayoutParams();
            final int left = child.getRight() + params.rightMargin;
            final int right = left + mDivider.getIntrinsicWidth();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if(orientation == VERTICAL){
            outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
        }else {
            outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
        }
    }
}
