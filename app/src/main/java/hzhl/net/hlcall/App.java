package hzhl.net.hlcall;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.os.Bundle;
import android.os.Handler;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import android.util.Log;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.tencent.bugly.Bugly;

import hzhl.net.hlcall.entity.DaoMaster;
import hzhl.net.hlcall.entity.DaoSession;
import hzhl.net.hlcall.entity.SettingEntity;
import hzhl.net.hlcall.entity.SettingEntityDao;

/**
 * Created by guang on 2018/7/17.
 */

public class App extends Application{
    private static final String TAG = "App";
    public static App sContext;
    private static SipManager manager;
    private static SipProfile sipProfile;//拨号sipProfile
    private static DaoSession daoSession;
    public static Handler gMainHandler;
    private int mActivityCount = 0;
    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        setupDatabase();


        //CrashHandler crashHandler = CrashHandler.getInstance();
        //crashHandler.init(getApplicationContext());
        //bugly初始化
        //CrashReport.initCrashReport(getApplicationContext(), "168cbdebdf", true);
        Bugly.init(getApplicationContext(), "168cbdebdf", false);
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
                //     .methodCount(2)         // (Optional) How many method line to show. Default 2
                //     .methodOffset(3)        // (Optional) Hides internal method calls up to offset. Default 5
                //     .logStrategy(customLog) // (Optional) Changes the log strategy to print out. Default LogCat
                .tag("MyTag")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));

        gMainHandler = new Handler();
        //文件方式保存LOG在内存卡
         Logger.addLogAdapter(new DiskLogAdapter());

         //注册监听
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                Log.d(TAG,"onActivityCreated");
            }

            @Override
            public void onActivityStarted(Activity activity) {
                Log.d(TAG,"onActivityStarted");
                mActivityCount++;
                openMinWindow(false);
            }

            @Override
            public void onActivityResumed(Activity activity) {
                Log.d(TAG,"onActivityResumed");
            }

            @Override
            public void onActivityPaused(Activity activity) {
                Log.d(TAG,"onActivityPaused");
            }

            @Override
            public void onActivityStopped(Activity activity) {
                Log.d(TAG,"onActivityStopped");
                mActivityCount--;
                //wenyeyang
                if (getSettingEntity() != null) {
                    if (getSettingEntity().getIsFloat()) {
                        if (mActivityCount <= 0) {
                            openMinWindow(true);
                        }
                    }
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                Log.d(TAG,"onActivitySaveInstanceState");
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                Log.d(TAG,"onActivityDestroyed");
            }
        });


        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        SDKInitializer.initialize(this);
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);

    }

    private void setupDatabase() {
        //创建数据库shop.db"
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "Call_2.db", null);
        //获取可写数据库
        SQLiteDatabase db = helper.getWritableDatabase();
        //获取数据库对象
        DaoMaster daoMaster = new DaoMaster(db);
        //获取Dao对象管理者
        daoSession = daoMaster.newSession();
    }

    public static SipManager getSipManager() {
        return manager;
    }

    public static DaoSession getDaoInstant() {
        return daoSession;
    }

    public static void setSipProfile(SipProfile sipProfile) {
        App.sipProfile = sipProfile;
    }

    public static SipProfile getSipProfile() {
        return sipProfile;
    }


    public static void runAsync(Runnable r) {
        gMainHandler.post(r);
    }

    public static void runAsyncDelay(Runnable r, long delayMillis) {
        gMainHandler.postDelayed(r, delayMillis);
    }

    //获取前台Activity数量
    public int getActivityCount( ) {
        return mActivityCount;
    }

    //开启悬浮
    public void openMinWindow(boolean open) {
        Intent serviceIntent = new Intent(sContext, FloatWindowService.class);
        if (!FloatWindowService.isStarted && open) {
            startService(serviceIntent);
            //moveTaskToBack(true);
        }else if (!open){
            stopService(serviceIntent);
        }
    }

    private SettingEntity getSettingEntity() {
        SettingEntityDao settingEntityDao = App.getDaoInstant().getSettingEntityDao();
        SettingEntity settingEntity = settingEntityDao.load(1L);
        return settingEntity;
    }

}
