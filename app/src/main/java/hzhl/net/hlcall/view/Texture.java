package hzhl.net.hlcall.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

public class Texture extends TextureView {
    int mRatioWidth = 0;
    int mRatioHeight = 0;

    public Texture(Context context) {
        super(context);
    }

    public Texture(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Texture(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width > height) {    //注意这里骚操作，替换"小于号"为"大于号"
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }

    }

    public void setRatio(int mRatioWidth,int mRatioHeight) {
        this.mRatioWidth = mRatioWidth;
        this.mRatioHeight = mRatioHeight;
        postInvalidate();
        getSurfaceTexture().setDefaultBufferSize(mRatioWidth,mRatioHeight);
    }




}
