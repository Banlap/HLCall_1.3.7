package hzhl.net.hlcall.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import hzhl.net.hlcall.R;
import hzhl.net.hlcall.utils.MyLog;
import hzhl.net.hlcall.utils.ScreenUtils;


/**
 * @version V1.0
 * @Title: SideBar
 * @Description: 列表侧边滑动栏
 */
public class SideBar extends View {
    private int firstHeight; // 第一次进入时view的高度
    // 触摸事件
    private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
    // 26个字母
    public static String[] b;
    // 26个字母
    public static String[] b1 = {"A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#"};
    // 13个字母
    public static String[] b2 = {"A", "B", "F", "I",
            "J", "O", "P", "T", "V",
            "Y", "#"};
    private int choose = -1;// 选中
    private Paint paint = new Paint();

    private TextView mTextDialog;

    public void setTextView(TextView mTextDialog) {
        this.mTextDialog = mTextDialog;
    }


    public SideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SideBar(Context context) {
        super(context);
    }

    /**
     * 重写这个方法
     */
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 获取焦点改变背景颜色.
        int height = getHeight();// 获取对应高度
        int width = getWidth(); // 获取对应宽度
        if (firstHeight == 0) {
            firstHeight = height;
        }
        if (height < firstHeight - 80) {
            b = b2;// 软键盘已经弹出,只展示一半的侧边字母
        } else {
            b = b1;// 软键盘未经弹出
        }
        int singleHeight = height / b.length;// 获取每一个字母的高度
        //MyLog.d("drow");
        for (int i = 0; i < b.length; i++) {
//			paint.setColor(Color.rgb(33, 65, 98));
            // paint.setColor(Color.WHITE);
            paint.setColor(Color.parseColor("#2b2b2b"));
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setAntiAlias(true);
            paint.setTextSize(ScreenUtils.dpToPxInt(getContext(),16));
            // 选中的状态
            if (i == choose) {
                paint.setColor(Color.parseColor("#3294fa"));
              //  MyLog.d("11111");
                paint.setFakeBoldText(true);
            } else {
               // MyLog.d("22222");
            }
            // x坐标等于中间-字符串宽度的一半.
            float xPos = width / 2 - paint.measureText(b[i]) / 2;
            float yPos = singleHeight * i + singleHeight;
            canvas.drawText(b[i], xPos, yPos, paint);
            paint.reset();// 重置画笔
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();// 点击y坐标
        final int oldChoose = choose;
        final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
        final int c = (int) (y / getHeight() * b.length);// 点击y坐标所占总高度的比例*b数组的长度就等于点击b中的个数.

        switch (action) {
            case MotionEvent.ACTION_UP:
                setBackgroundResource(R.color.transparent);
                //  choose = -1;//
                MyLog.d("重置选中状态");
                invalidate();
                if (mTextDialog != null) {
                    mTextDialog.setVisibility(View.INVISIBLE);
                }
                break;

            default:
                setBackgroundResource(R.color.transparent);
                if (oldChoose != c) {
                    if (c >= 0 && c < b.length) {
                        if (listener != null) {
                            listener.onTouchingLetterChanged(b[c]);
                        }
                        if (mTextDialog != null) {
                            mTextDialog.setText(b[c]);
                            mTextDialog.setVisibility(View.VISIBLE);
                        }
                        choose = c;
                        invalidate();
                        MyLog.d("点击事件");
                    }
                }

                break;
        }
        return true;
    }

    /**
     * 向外公开的方法
     *
     * @param onTouchingLetterChangedListener
     */
    public void setOnTouchingLetterChangedListener(
            OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }

    public void setNext(String s) {
        int index = -1;
        for (int i = 0; i < b1.length; i++) {
            if (b1[i].equals(s)) {
                if (i + 1 < b1.length) {
                    index = i+1;
                }
                break;
            }
        }
    }

    /**
     * 接口
     *
     * @author coder
     */
    public interface OnTouchingLetterChangedListener {
        public void onTouchingLetterChanged(String s);
    }

}