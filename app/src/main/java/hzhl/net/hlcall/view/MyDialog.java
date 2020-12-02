package hzhl.net.hlcall.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import hzhl.net.hlcall.utils.SizeUtils;

/**
 * Created by user on 2017/3/5.
 */

public class MyDialog extends Dialog {
    private Context context;
    private int height, width;
    private boolean cancelTouchout;
    private View view;

    private MyDialog(Builder builder) {
        super(builder.context);
        context = builder.context;
        height = builder.height;
        width = builder.width;
        cancelTouchout = builder.cancelTouchout;
        view = builder.view;
    }

    private MyDialog(Builder builder, int resStyle) {
        super(builder.context, resStyle);
        context = builder.context;
        height = builder.height;
        width = builder.width;
        cancelTouchout = builder.cancelTouchout;
        view = builder.view;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  View view = View.inflate(context, R.layout.item_dialog_duihuan, null);
        setContentView(view);
        setCanceledOnTouchOutside(cancelTouchout);

     /*  Window window = getWindow();
        WindowManager windowManager = window.getWindowManager();
        Display display = windowManager.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = (int) (display.getWidth() * 0.6); // 宽度设置为屏幕的0.6
        lp.height = SizeUtils.dip2px(context, 250);
        window.setAttributes(lp);*/

        Window win = getWindow();
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.width = width;
        win.setAttributes(lp);


    }

    public static final class Builder {

        private Context context;
        private int height, width;
        private boolean cancelTouchout;
        private View view;
        private int resStyle = -1;


        public Builder(Context context) {
            this.context = context;
        }

        public Builder view(int resView) {
            view = LayoutInflater.from(context).inflate(resView, null);
            return this;
        }


        public Builder heightpx(int val) {
            height = val;
            return this;
        }

        public Builder widthpx(int val) {
            width = val;
            return this;
        }

        public Builder heightdp(int val) {
            height = SizeUtils.dip2px(context, val);
            return this;
        }

        public Builder widthdp(int val) {
            width = SizeUtils.dip2px(context, val);
            return this;
        }

        public Builder heightDimenRes(int dimenRes) {
            height = context.getResources().getDimensionPixelOffset(dimenRes);
            return this;
        }

        public Builder widthDimenRes(int dimenRes) {
            width = context.getResources().getDimensionPixelOffset(dimenRes);
            return this;
        }

        /**
         * dialog样式
         *
         * @param resStyle
         * @return
         */
        public Builder style(int resStyle) {
            this.resStyle = resStyle;
            return this;
        }

        /**
         * 是否可以点击外面取消dialog
         *
         * @param val
         * @return
         */
        public Builder cancelTouchout(boolean val) {
            cancelTouchout = val;
            return this;
        }

        /**
         * 增加view
         *
         * @param viewRes
         * @param listener
         * @return
         */
        public Builder addViewOnclick(int viewRes, View.OnClickListener listener) {
            view.findViewById(viewRes).setOnClickListener(listener);
            return this;
        }


        public MyDialog build() {
            if (resStyle != -1) {
                return new MyDialog(this, resStyle);
            } else {
                return new MyDialog(this);
            }
        }
    }

}
