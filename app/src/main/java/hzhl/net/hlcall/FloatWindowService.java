package hzhl.net.hlcall;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.util.List;

import hzhl.net.hlcall.entity.SettingEntity;
import hzhl.net.hlcall.entity.SettingEntityDao;
import hzhl.net.hlcall.utils.MyLog;
import hzhl.net.hlcall.utils.ScreenUtils;

public class FloatWindowService extends Service {
    /*   private WindowManager mWindowManager;
       private WindowManager.LayoutParams wmParams;
       private LayoutInflater inflater;
       private View mFloatingLayout;*/
    public static boolean isStarted = false;
    private WindowManager mWindowManage;
    private View mFloatWindow;
    private WindowManager.LayoutParams mFloatWindowParams;
    private boolean mFloatWindowAdd = false;
    private float mXInView;
    private float mYInView;
    private int mScreenWidthHalf;
    private ValueAnimator mAnimator;
    private float mXDown;
    private float mYDown;
    private float mXMove;
    private float mYMove;
    private int mPxDp25;

    private SettingEntityDao settingEntityDao;
    private SettingEntity settingEntity;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d("onCreate");
        isStarted = true;
        settingEntityDao = App.getDaoInstant().getSettingEntityDao();
        settingEntity = settingEntityDao.load(1L);
        initWindow();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d("onStartCommand");
        //showFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d("onDestroy");
        if (mWindowManage != null) {
            mWindowManage.removeView(mFloatWindow);
            isStarted = false;
        }
    }

    /**
     * 设置悬浮框基本参数（位置、宽高等）
     */
    private void initWindow() {
        if (mWindowManage == null || mFloatWindow == null) {
            mPxDp25 = ScreenUtils.dpToPxInt(this, 25);
            mScreenWidthHalf = ScreenUtils.GetScreenWidthPx(this) / 2;
            mWindowManage = (WindowManager) getSystemService(Application.WINDOW_SERVICE);
            mFloatWindowParams = new WindowManager.LayoutParams();
            mFloatWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            if (Build.VERSION.SDK_INT >= 26) {
                mFloatWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                mFloatWindowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }
            mFloatWindowParams.format = PixelFormat.RGBA_8888;
            mFloatWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            mFloatWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
            Logger.d(settingEntity.getFloatWindowParamsX()+"/"+settingEntity.getFloatWindowParamsY());
            if (settingEntity != null) {
                  /*  Logger.d("getFloatWindowParamsX:"+settingEntity.getFloatWindowParamsX());
                    Logger.d("getFloatWindowParamsY:"+settingEntity.getFloatWindowParamsY());*/
                    mFloatWindowParams.x = settingEntity.getFloatWindowParamsX();
                    mFloatWindowParams.y = settingEntity.getFloatWindowParamsY();

            } else {
                mFloatWindowParams.x = mScreenWidthHalf * 2;
                mFloatWindowParams.y = mScreenWidthHalf;
            }
            mFloatWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            mFloatWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            mFloatWindow = LayoutInflater.from(this).inflate(R.layout.avchat_float_window, null);
            //
            showFloatingWindow();
            //
            TextView tvPrompt = (TextView) mFloatWindow.findViewById(R.id.tv_prompt);
          /*      mFloatWindow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, AVChatActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                });*/
            mFloatWindow.setOnTouchListener(new FloatingListener());
        }
    }

    class FloatingListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mXInView = event.getX();
                    mYInView = event.getY();
                    mXDown = event.getRawX();
                    mYDown = event.getRawY();
                    mXMove = event.getRawX();
                    mYMove = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    mFloatWindowParams.x = (int) (event.getRawX() - mXInView);
                    mFloatWindowParams.y = (int) (event.getRawY() - mPxDp25 - mYInView);
                    if (isStarted) {
                        mWindowManage.updateViewLayout(mFloatWindow, mFloatWindowParams);
                    }
                    mXMove = event.getRawX();
                    mYMove = event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    int start = mFloatWindowParams.x;
                    int end = start < mScreenWidthHalf ? 0 : mScreenWidthHalf * 2;
                    //  MyLog.d("mScreenWidthHalf=" + mScreenWidthHalf);
                    mAnimator = ValueAnimator.ofInt(start, end);
                    mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int animatedValue = (int) animation.getAnimatedValue();
                            // MyLog.d("onAnimationUpdate: animatedValue=" + animatedValue);
                            mFloatWindowParams.x = animatedValue;
                            //防止服务onDestroy后， mWindowManage.removeView(mFloatWindow)后，mFloatWindow不在mWindowManage里
                            if (isStarted) {
                                mWindowManage.updateViewLayout(mFloatWindow, mFloatWindowParams);
                            }
                        }
                    });
                    mAnimator.start();
                    if (Math.abs(mXDown - mXMove) < 10 && Math.abs(mYDown - mYMove) < 10) {
                        Logger.d("跳转");
                        if (settingEntity == null) {
                            settingEntity = new SettingEntity();
                        }
                        Logger.d(mFloatWindowParams.x+"/"+mFloatWindowParams.y);
                        settingEntity.setFloatWindowParamsX(mFloatWindowParams.x);
                        settingEntity.setFloatWindowParamsY(mFloatWindowParams.y);
                        settingEntityDao.insertOrReplace(settingEntity);
                        //if (App.sContext.getActivityCount() <=0) {
                            moveToFront();
                                                                                                                                                                                                                                                                      mFloatWindow.setVisibility(View.GONE);
                            /*
                            Intent intent = new Intent(FloatWindowService.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);*/
                        //}
                 /*       Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                        try {
                            pendingIntent.send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }*/
                    }
                    break;
            }
            return true;

        }
    }


    private void showFloatingWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//判断系统版本
            if (Settings.canDrawOverlays(this)) {
                mFloatWindow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MyLog.d("调转1");
                        if (App.sContext.getActivityCount() <=0) {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            //startActivity(intent);
                        }
                    }
                });
                mScreenWidthHalf = ScreenUtils.GetScreenWidthPx(this) / 2;
                mWindowManage.addView(mFloatWindow, mFloatWindowParams);
            }
        } else {
            mFloatWindow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (App.sContext.getActivityCount() <=0) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        //startActivity(intent);
                    }
                }
            });
            mScreenWidthHalf = ScreenUtils.GetScreenWidthPx(this) / 2;
            mWindowManage.addView(mFloatWindow, mFloatWindowParams);
        }
    }

    //切回前台
    @TargetApi(11)
    protected void moveToFront() {
        // honeycomb
        boolean hsFront = false;
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> recentTasks = manager.getRunningTasks(100);
        for (int i = 0; i < recentTasks.size(); i++){
            Log.e("xk", "  "+recentTasks.get(i).baseActivity.toShortString() + "   ID: "+recentTasks.get(i).id+"");
            Log.e("xk","@@@@  "+recentTasks.get(i).topActivity.toShortString());
            // bring to front
            if (recentTasks.get(i).topActivity.getPackageName().contains(getPackageName())) {
                manager.moveTaskToFront(recentTasks.get(i).id, ActivityManager.MOVE_TASK_WITH_HOME);
                if (App.sContext.getActivityCount()>0)hsFront = true;
            }
        }
        if (!hsFront){
            Intent intent = new Intent(FloatWindowService.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
    }

}
