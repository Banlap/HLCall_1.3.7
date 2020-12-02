package hzhl.net.hlcall.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;

import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

public class LoadingView {

    private static ZLoadingDialog dialog;

    private static void show(Context context){
        dismiss();
        Activity activity = (Activity)context;
        if (activity.isDestroyed())return;
        dialog = new ZLoadingDialog(context);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK)//颜色
                .setHintText("发送中")
                .setCanceledOnTouchOutside(false)
              //  .setHintTextSize(16) // 设置字体大小 dp
                .setHintTextColor(Color.BLACK)  // 设置字体颜色
                .setDurationTime(1) // 设置动画时间百分比 - 0.5倍
                .setDialogBackgroundColor(Color.WHITE) // 设置背景色，默认白色
                .show();
    }

    private static void dismiss(){
        if (dialog == null)return;
        dialog.dismiss();
        dialog = null;
    }


    public static void show(Context context,boolean isShow){
        if (isShow) show(context);
        else dismiss();
    }

}
