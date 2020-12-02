package hzhl.net.hlcall;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.CallLog;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import android.view.WindowManager;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.ChatMessage;
import org.linphone.core.ChatRoom;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.GlobalState;
import org.linphone.core.InfoMessage;
import org.linphone.core.LogCollectionState;
import org.linphone.core.PayloadType;
import org.linphone.core.ProxyConfig;
import org.linphone.core.Reason;
import org.linphone.core.RegistrationState;
import org.linphone.core.VideoActivationPolicy;
import org.linphone.core.tools.H264Helper;
import org.linphone.core.tools.Log;
import org.linphone.mediastream.Version;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import hzhl.net.hlcall.activity.BohaoActivity;
import hzhl.net.hlcall.activity.CallRingActivity;
import hzhl.net.hlcall.activity.NewChatActivity;
import hzhl.net.hlcall.compatibility.Compatibility;
import hzhl.net.hlcall.constant.Constants;
import hzhl.net.hlcall.entity.BlacklistEntity;
import hzhl.net.hlcall.entity.BlacklistEntityDao;
import hzhl.net.hlcall.entity.DaoSession;
import hzhl.net.hlcall.utils.ContactsUtil;
import hzhl.net.hlcall.utils.DataCache;
import hzhl.net.hlcall.utils.MyLog;

import static android.app.Notification.DEFAULT_ALL;

public class LinphoneService extends Service {
    private static final String START_LINPHONE_LOGS = " ==== Device information dump ====";
    // Keep a static reference to the Service so we can access it from anywhere in the app
    private static LinphoneService sInstance;

    private Handler mHandler;
    private Timer mTimer;
    private Core mCore;
    private CoreListenerStub mCoreListener;
    private NotificationManager notificationManager;
    private PendingIntent mNotifContentIntent, mMissedCallsNotifContentIntent, mInComingCallIntent, mOutGoingCallIntent;
    private WindowManager mWindowManager;

    public static final int IC_LEVEL_ORANGE = 0;
    private final static int NOTIF_ID = 133;
    private final static int INCALL_NOTIF_ID = 233;
    private final static int OUTGOING_NOTIF_ID = 333;
    private final static int CUSTOM_NOTIF_ID = 433;
    private final static int MISSED_NOTIF_ID = 533;

    private final static int FOREGROUND_ID = 2343;

    public static boolean isReady() {
        return sInstance != null;
    }

    public static LinphoneService getInstance() {
        if (isReady()) return sInstance;

        throw new RuntimeException("LinphoneService not instantiated yet");
    }


    public static Core getCore() {
        return sInstance.mCore;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Logger.d("onCreate");
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        // The first call to liblinphone SDK MUST BE to a Factory method  liblinphone sdk的第一个调用必须是工厂方法
        // So let's enable the library debug logs & log collection  所以让我们启用库调试日志和日志集合
        String basePath = getFilesDir().getAbsolutePath();
        Factory.instance().setLogCollectionPath(basePath);
        Factory.instance().enableLogCollection(LogCollectionState.Enabled);
        Factory.instance().setDebugMode(true, getString(R.string.app_name));



        // Dump some useful information about the device we're running on 转储一些有关我们正在运行的设备的有用信息
        Log.i(START_LINPHONE_LOGS);
        dumpDeviceInformation();
        dumpInstalledLinphoneInformation();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //  notificationManager.cancel(INCALL_NOTIF_ID); // in case of crash the icon is not removed
        Intent notifIntent = new Intent(this, MainActivity.class);
        notifIntent.putExtra("Notification", true);
        notifIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mNotifContentIntent = PendingIntent.getActivity(this, NOTIF_ID, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent inComingIntent = new Intent(this, CallRingActivity.class);
        inComingIntent.putExtra("InComing", true);
        inComingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mInComingCallIntent = PendingIntent.getActivity(this, INCALL_NOTIF_ID, inComingIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent OutGoingIntent = new Intent(this, BohaoActivity.class);
        inComingIntent.putExtra("OutGoing", true);
        OutGoingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mOutGoingCallIntent = PendingIntent.getActivity(this, OUTGOING_NOTIF_ID, OutGoingIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent missedCallNotifIntent = new Intent(this, MainActivity.class);
        missedCallNotifIntent.putExtra("GoToHistory", true);
        missedCallNotifIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mMissedCallsNotifContentIntent = PendingIntent.getActivity(this, MISSED_NOTIF_ID, missedCallNotifIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mHandler = new Handler();
        // This will be our main Core listener, it will change activities depending on events
        //这将是我们的主要核心侦听器，它将根据事件更改活动
        mCoreListener = new CoreListenerStub() {
            @Override
            public void onCallStateChanged(Core core, Call call, Call.State state, String message) {
                if (core.isInConference())return;
                try {
                    Logger.d("state:" + state + "；Username:" + call.getRemoteAddress().getUsername()
                            + "；message:" + message);
                    // sendNotification(OUTGOING_NOTIF_ID, mOutGoingCallIntent, getString(R.string.outgoing_call) + call.getRemoteAddress().getUsername());
                    if (state == Call.State.IncomingReceived) {
                        Logger.d("收到来电");
                        boolean isPass = true;
                        DaoSession daoSession = App.getDaoInstant();
                        BlacklistEntityDao blacklistEntityDao = daoSession.getBlacklistEntityDao();
                        List<BlacklistEntity> list = blacklistEntityDao.queryBuilder().limit(20).list();
                        for (int i = 0; i < list.size(); i++) {
                            BlacklistEntity entity = list.get(i);
                            if (entity.getUser().equals(call.getRemoteAddress().getUsername())) {
                                MyLog.d(entity.getUser() + "黑名单");
                                isPass = false;
                                entity.setCount(entity.getCount() + 1);
                                blacklistEntityDao.insertOrReplace(entity);
                            }
                        }
                        if (isPass) {
                            Logger.d("通过");
                            Logger.d(core.getCalls().length);
                            boolean isInCall = false;//是否在通话中/拨号/来电中
                            if (core.getCalls().length >= 2) {
                                isInCall = true;
                            }
                            if (!isInCall) {
                                Logger.d("进入来电界面");
                                Intent intent = new Intent().setClass(LinphoneService.this, CallRingActivity.class);
                                // This flag is required to start an Activity from a Service context
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                sendNotification(INCALL_NOTIF_ID, mInComingCallIntent, getString(R.string.incoming_call) + call.getRemoteAddress().getUsername());
                            } else {
                                Logger.d("拒绝");
                                call.decline(Reason.Busy);
                            }
                        } else {
                            Logger.d("拒绝");
                            call.decline(Reason.Declined);

                        }
                    } else if (state == Call.State.Connected) {

                    } else if (state == Call.State.UpdatedByRemote) {
                        // If the correspondent proposes video while audio call
                        boolean remoteVideo = call.getRemoteParams().videoEnabled();
                        boolean localVideo = call.getCurrentParams().videoEnabled();
                        boolean autoAcceptCameraPolicy = core.getVideoActivationPolicy().getAutomaticallyAccept();
                        if (remoteVideo
                                && !localVideo
                                && !autoAcceptCameraPolicy
                                && mCore.getConference() == null) {
                            call.deferUpdate();
                        }
                    }
                    if (state == Call.State.OutgoingInit || state == Call.State.OutgoingProgress || state == Call.State.OutgoingRinging) {
                        sendNotification(OUTGOING_NOTIF_ID, mOutGoingCallIntent, getString(R.string.outgoing_call) + call.getRemoteAddress().getUsername());
                    }
                    if (state == Call.State.Released && call.getCallLog().getStatus() == Call.Status.Missed) {
                        Logger.d("未接来电");
                        int missedCallCount = core.getMissedCallsCount();
                        String body;
                        if (missedCallCount > 1) {
                            body = getString(R.string.missed_calls_notif_body).replace("%i", String.valueOf(missedCallCount));
                        } else {
                            Address address = call.getToAddress();
                            body = getString(R.string.missed_calls_notif_title) + address.getUsername();
                            if (body == null) {
                                body = address.asStringUriOnly();
                            }
                        }
                        sendNotification(MISSED_NOTIF_ID, mMissedCallsNotifContentIntent, body);
                    }
                    if (state == Call.State.End || state == Call.State.Error) {
                        Logger.d(Call.State.End + "--" + Call.State.Error);
                        Logger.d(core.getCallsNb());
                        //   ActivityCollector.finishAll();//关掉来电未关掉的铃声和震动
                        core.getDefaultProxyConfig().refreshRegister();//重新注册，刷新注册通知
                        String fromAddress = call.getCallLog().getFromAddress().getUsername();
                        String duration = String.valueOf(call.getCallLog().getDuration());
                        String type;
                        if (call.getCallLog().getDir() == Call.Dir.Incoming) {
                            type = "1";//呼入

                            if (call.getCallLog().getStatus() == Call.Status.Missed) {
                                type = "3";//未接
                            }
                        } else {
                            type = "2";//呼出,fromAddress为对方号码
                            fromAddress = call.getCallLog().getToAddress().getUsername();
                        }
    /*              Logger.d(call.getDuration());
                    Logger.d(call.getState().name());
                    Logger.d(call.getCallLog().getFromAddress().getUsername());
                    Logger.d(call.getCallLog().getLocalAddress().getUsername());
                    Logger.d(call.getCallLog().getRemoteAddress().getUsername());
                    Logger.d(call.getCallLog().getToAddress().getUsername());*/
                        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(call.getCallLog().getStartDate()));
                        //  Logger.d(date);
                        insertCallLog(fromAddress, duration, type, "1");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    if (call!=null)call.terminate();
                }
            }

            @Override
            public void onRegistrationStateChanged(Core lc, ProxyConfig cfg, RegistrationState state, String message) {

                Logger.d("state:" + state + "；Username:" + cfg.getIdentityAddress().getUsername()
                        + "；message:" + message);
                // Toast.makeText(LinphoneService.this, lc.inCall() ? "正在通话中" : "不在通话", Toast.LENGTH_SHORT).show();
                Logger.d(lc.inCall());//拨号中和来电中，不会进行注册，通话中会注册
                if (!lc.inCall()) {
                    if (displayServiceNotification() && state == RegistrationState.Ok && getCore().getDefaultProxyConfig() != null
                            && getCore().getDefaultProxyConfig().registerEnabled()) {
                        sendNotification(NOTIF_ID, mNotifContentIntent, cfg.getIdentityAddress().getUsername() + getString(R.string.notification_registered));
                    }

                    if (displayServiceNotification() && (state == RegistrationState.Failed || state == RegistrationState.Cleared)
                            && (getCore().getDefaultProxyConfig() == null || getCore().getDefaultProxyConfig().registerEnabled())) {
                        //getCore().getDefaultProxyConfig().registerEnabled() 是否启用 代理配置的注册。
                        sendNotification(NOTIF_ID, mNotifContentIntent, cfg.getIdentityAddress().getUsername() + getString(R.string.notification_register_failure));
                    }

                    if (displayServiceNotification() && state == RegistrationState.None) {
                        sendNotification(NOTIF_ID, mNotifContentIntent, cfg.getIdentityAddress().getUsername() + getString(R.string.notification_started));
                    }
                }
            }

            @Override
            public void onGlobalStateChanged(Core lc, GlobalState state, String message) {
                //  Logger.d(state + message);
                if (state == GlobalState.On && displayServiceNotification()) {
                    //  sendNotification(IC_LEVEL_ORANGE, R.string.notification_started,lc.getDefaultProxyConfig().getIdentityAddress().getUsername());
                }
            }

            @Override
            public void onMessageReceived(Core lc, ChatRoom room, ChatMessage message) {
                super.onMessageReceived(lc, room, message);

                sendMessageNotification(message.getTextContent(),message.getFromAddress().getUsername());
                Address addressToCall = mCore.getDefaultProxyConfig().getIdentityAddress();
                Address address = message.getFromAddress();
                new DataCache(LinphoneService.this).putString(address.getUsername(),address.asStringUriOnly());
                Logger.d("onMessageReceived" + addressToCall.asStringUriOnly());
                Logger.d("onMessageReceived" + message.getLocalAddress().asString());
                Logger.d("onMessageReceived" + message.getFromAddress().asString());
                Logger.d("onMessageReceived" + address.asStringUriOnly());

                EventBus.getDefault().post(Constants.EVENT_UPDATE_UN_READ);

            }

            @Override
            public void onInfoReceived(Core lc, Call call, InfoMessage msg) {
                super.onInfoReceived(lc, call, msg);
                Logger.d(msg.getContent().getName());
            }

        };
        try {
            // Let's copy some RAW resources to the device将一些原始资源复制到设备
            // The default config file must only be installed once (the first time)默认配置文件只能安装一次（第一次）
            copyIfNotExist(R.raw.linphonerc_default, basePath + "/.linphonerc");
            // The factory config is used to override any other setting, let's copy it each time
            // 工厂配置用于覆盖任何其他设置，让我们每次都复制它
            copyFromPackage(R.raw.linphonerc_factory, "linphonerc");
        } catch (IOException ioe) {
            Log.e(ioe);
        }

        // Create the Core and add our listener
        mCore = Factory.instance().createCore(basePath + "/.linphonerc", basePath + "/linphonerc", this);
        // Core is ready to be configured   Core已准备好配置
        configureCore();
        mCore.addListener(mCoreListener);

        //set video setting
        VideoActivationPolicy vap = mCore.getVideoActivationPolicy();
        vap.setAutomaticallyAccept(true);
        vap.setAutomaticallyInitiate(true);
        mCore.setVideoActivationPolicy(vap);

        //设置音频
        for (PayloadType pt : mCore.getAudioPayloadTypes()) {
            MyLog.i("wen",pt.getMimeType());
            if (pt.getMimeType().contains("PCM"))pt.enable(true);
            else pt.enable(false);
        }


        for (String s : mCore.getSoundDevicesList()) {
            MyLog.i("wen",s);
            if (s.contains("ANDROID SND"))mCore.setPlaybackDevice(s);
        }



        //mCore.enableEchoCancellation(true);




    /*  Logger.d(mCore.getDelayedTimeout());
        Logger.d(mCore.getIncTimeout());
        Logger.d(mCore.getNortpTimeout());
        Logger.d(mCore.getInCallTimeout());
        Logger.d(mCore.getSipTransportTimeout());*/
       /* mCore.setNortpTimeout(R.integer.norpt_timeout);//设置Nortp超时,默认30秒
        mCore.setInCallTimeout(R.integer.outgoing_timeout);//设置呼叫超时,默认30秒。
        mCore.setIncTimeout(R.integer.incoming_timeout);//设置来电超时,默认30秒。来电超时要少于呼叫超时，才会触发未接电话*/
        mCore.setNortpTimeout(getResources().getInteger(R.integer.norpt_timeout));//设置Nortp超时,默认30秒
        //    mCore.setInCallTimeout(getResources().getInteger(R.integer.outgoing_timeout));//设置呼叫超时,默认0秒。
        mCore.setIncTimeout(getResources().getInteger(R.integer.incoming_timeout));//设置来电超时,默认30秒。来电超时要少于呼叫超时，才会触发未接电话
        mCore.setRing(null);//设置铃声为空，默认为软件自带铃声
        //    setRingUriname();
        //   mCore.getConfig().setBool("app", "device_ringtone", false);
        // Retrieve methods to publish notification and keep Android
        // from killing us and keep the audio quality high.

    /*    if (displayServiceNotification()) {
                startForegroundCompat(NOTIF_ID, mNotif);
        }*/
        //make sure the application will at least wakes up every 10 mn
        Intent intent = new Intent(this, KeepAliveReceiver.class);
        PendingIntent keepAlivePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = ((AlarmManager) this.getSystemService(Context.ALARM_SERVICE));
        Compatibility.scheduleAlarm(alarmManager, AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 600000, keepAlivePendingIntent);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        // If our Service is already running, no need to continue如果我们的服务已经在运行，则无需继续
        if (sInstance != null) {
            return START_STICKY;
        }
        //  Logger.d("onStartCommand");
        // Our Service has been started, we can keep our reference on it我们的服务已经开始了，我们可以保留参考资料
        // From now one the Launcher will be able to call onServiceReady()从现在开始，启动程序将能够调用onServiceReady（）
        sInstance = this;

        // Core must be started after being created and configured  核心必须在创建和配置之后启动
        mCore.start();
        // We also MUST call the iterate() method of the Core on a regular basis
        //我们还必须定期调用核心的iterate（）方法
        TimerTask lTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.post(() -> {
                            if (mCore != null) {
                                mCore.iterate();

                            }
                        });
            }
        };
        mTimer = new Timer("Linphone scheduler");
        mTimer.schedule(lTask, 0, 20);
        H264Helper.setH264Mode(H264Helper.MODE_AUTO, mCore);
        return START_STICKY;
    }


    private void sendNotification(int ID, PendingIntent pendingIntent, String content) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("hlcall_id", "hlcall",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0L});
            channel.setSound(null, null);
            notificationManager.createNotificationChannel(channel);
        }

        Resources res = getResources();
        Bitmap bmp = BitmapFactory.decodeResource(res, R.mipmap.ic_launcher);
        //channelId要和上面的channel id一样
        Notification notification = new NotificationCompat.Builder(this, "hlcall_id")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(bmp)
                .setContentTitle(getString(R.string.service_name))
                .setContentText(content)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setVibrate(new long[]{0L})
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .build();
        switch (ID) {
            case NOTIF_ID:
            case INCALL_NOTIF_ID:
            case OUTGOING_NOTIF_ID:
                startForeground(ID, notification);
                break;
            case MISSED_NOTIF_ID:
                notificationManager.notify(ID, notification);
        }
    }

    private void sendMessageNotification(String content,String number) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("hlcall_message", "hlcall message",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
            //channel.setVibrationPattern(new long[]{500L});
            notificationManager.createNotificationChannel(channel);
        }
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        //channelId要和上面的channel id一样

        Intent intent = new Intent(this, NewChatActivity.class);
        intent.putExtra(NewChatActivity.REMOTE_SIP_URI,number);
        intent.putExtra(NewChatActivity.REMOTE_DISPLAY_NAME, ContactsUtil.getNameFormNumber(this,number));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIF_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, "hlcall_message")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(bmp)
                .setContentTitle(number)
                .setContentText(content)
                .setAutoCancel(false)
                .setDefaults(DEFAULT_ALL)
                //.setVibrate(new long[]{500L})
                .setContentIntent(pendingIntent)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis())
                .build();
        MyLog.d("wen","send" + Thread.currentThread().getName());
        notificationManager.notify(Integer.parseInt(number), notification);
        //notificationManager.cancel(1001);
    }


    private boolean displayServiceNotification() {
        return mCore.getConfig().getBool("app", "show_service_notification", true);
    }


    /**
     * 插入一条通话记录
     *
     * @param number   通话号码
     * @param duration 通话时长（响铃时长）以秒为单位 1分30秒则输入90
     * @param type     通话类型  1呼入 2呼出 3未接
     * @param isNew    是否已查看    0已看1未看
     */
    private void insertCallLog(String number, String duration, String type, String isNew) {
        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, number);
        values.put(CallLog.Calls.DATE, System.currentTimeMillis());
        values.put(CallLog.Calls.DURATION, duration);
        values.put(CallLog.Calls.TYPE, type);
        values.put(CallLog.Calls.NEW, isNew);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED) {
            getApplicationContext().getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
            EventBus.getDefault().post(Constants.EVENT_UPDATE_CALL_RECORD);
        }
    }


    @Override
    public void onDestroy() {
        mCore.removeListener(mCoreListener);
        if (mTimer != null) {
            mTimer.cancel();
        }
        mCore.stop();
        // A stopped Core can be started again停止的核心可以重新启动
        // To ensure resources are freed, we must ensure it will be garbage collected
        //为了确保资源被释放，我们必须确保它将被垃圾回收
        mCore = null;
        // Don't forget to free the singleton as well
        sInstance = null;
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // For this sample we will kill the Service at the same time we kill the app
        //对于这个示例，我们将在终止应用程序的同时终止服务
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    private void configureCore() {
        // We will create a directory for user signed certificates if needed
        String basePath = getFilesDir().getAbsolutePath();
        String userCerts = basePath + "/user-certs";
        File f = new File(userCerts);
        if (!f.exists()) {
            if (!f.mkdir()) {
                Logger.i(userCerts + " can't be created.");
            }
        }
        mCore.setUserCertificatesPath(userCerts);
    }

    private void dumpDeviceInformation() {
        StringBuilder sb = new StringBuilder();
        sb.append("DEVICE=").append(Build.DEVICE).append("\n");
        sb.append("MODEL=").append(Build.MODEL).append("\n");
        sb.append("MANUFACTURER=").append(Build.MANUFACTURER).append("\n");
        sb.append("SDK=").append(Build.VERSION.SDK_INT).append("\n");
        sb.append("Supported ABIs=");
        for (String abi : Version.getCpuAbis()) {
            sb.append(abi).append(", ");
        }
        sb.append("\n");
        Logger.i(sb.toString());
    }

    private void dumpInstalledLinphoneInformation() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException nnfe) {
            Logger.i(nnfe.toString());
        }

        if (info != null) {
            Logger.i("[Service] Linphone version is ",
                    info.versionName + " (" + info.versionCode + ")");
        } else {
            Logger.i("[Service] Linphone version is unknown");
        }
    }

    private void copyIfNotExist(int ressourceId, String target) throws IOException {
        File lFileToCopy = new File(target);
        if (!lFileToCopy.exists()) {
            copyFromPackage(ressourceId, lFileToCopy.getName());
        }
    }

    private void copyFromPackage(int ressourceId, String target) throws IOException {
        FileOutputStream lOutputStream = openFileOutput(target, 0);
        InputStream lInputStream = getResources().openRawResource(ressourceId);
        int readByte;
        byte[] buff = new byte[8048];
        while ((readByte = lInputStream.read(buff)) != -1) {
            lOutputStream.write(buff, 0, readByte);
        }
        lOutputStream.flush();
        lOutputStream.close();
        lInputStream.close();
    }


}
