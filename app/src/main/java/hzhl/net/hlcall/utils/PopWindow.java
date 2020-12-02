package hzhl.net.hlcall.utils;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import hzhl.net.hlcall.R;

public class PopWindow implements PopupWindow.OnDismissListener {


    private static PopupWindow popupWindow;
    private Activity activity;
    private OnCreate onCreate;
    private OnDismiss onDismiss;
    // 私有化构造方法，变成单例模式
    private PopWindow() {

    }


    public static PopWindow init(Activity activity){
        PopWindow popWindow = new PopWindow();
        popWindow.activity = activity;
        return popWindow;
    }

    public PopWindow inflaterLayout(int layoutId,int width, int height){
        //ActionBar.LayoutParams.WRAP_CONTENT,
                //ActionBar.LayoutParams.WRAP_CONTENT
        View vPopupWindow = activity.getLayoutInflater().inflate(layoutId, null, false);//引入弹窗布局
        popupWindow = new PopupWindow(vPopupWindow, width, height, true);
        popupWindow.setAnimationStyle(R.style.PopupWindowAnimation);
        return this;
    }


    public PopWindow addBackground() {
        // 设置背景颜色变暗
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = 0.7f;//调节透明度
        activity.getWindow().setAttributes(lp);
        //dismiss时恢复原样
        return this;
    }

    public PopWindow create(OnCreate onCreateView){
        onCreateView.onCreate(popupWindow.getContentView());
        return this;
    }

    @Override
    public void onDismiss() {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = 1f;
        activity.getWindow().setAttributes(lp);
        if (onDismiss!=null)onDismiss.onDismiss();
        activity = null;
        popupWindow = null;
        onCreate = null;
        onDismiss = null;
    }

    public PopWindow onDismiss(OnDismiss onDismiss){
        this.onDismiss = onDismiss;
        return this;
    }

    public void show(View parent, int gravity, int x, int y){
        popupWindow.showAtLocation(parent, gravity,0,0);
        popupWindow.setOnDismissListener(this);
    }

    public void dismiss(){
        if (popupWindow!=null)popupWindow.dismiss();
    }

    public interface OnCreate{
        void onCreate(View v);
    }

    public  interface OnDismiss{
        void onDismiss();
    }

}