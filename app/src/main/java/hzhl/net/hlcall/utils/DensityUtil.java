package hzhl.net.hlcall.utils;


import hzhl.net.hlcall.App;

public class DensityUtil {

    public static int dip2px(float dpValue) {
        final float scale = App.sContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);  
    }  
  
    public static int px2dip(float pxValue) {
        final float scale = App.sContext.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);  
    }

    public static int px2sp(float pxValue) {
        final float fontScale = App.sContext.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static int sp2px(float spValue) {
        final float fontScale = App.sContext.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
