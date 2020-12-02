package hzhl.net.hlcall.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.tencent.bugly.crashreport.CrashReport;

import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.PayloadType;
import org.linphone.core.Reason;
import org.linphone.core.tools.Log;
import org.linphone.mediastream.video.capture.CaptureTextureView;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hzhl.net.hlcall.ActivityCollector;
import hzhl.net.hlcall.App;
import hzhl.net.hlcall.CallManager;
import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.entity.SettingEntity;
import hzhl.net.hlcall.entity.SettingEntityDao;
import hzhl.net.hlcall.utils.ContactsUtil;
import hzhl.net.hlcall.utils.FileUtils;
import hzhl.net.hlcall.utils.HomeListen;
import hzhl.net.hlcall.utils.LinphoneUtils;
import hzhl.net.hlcall.utils.MyLog;
import hzhl.net.hlcall.utils.SizeUtils;
import hzhl.net.hlcall.utils.StringUtil;

import static android.media.AudioManager.MODE_RINGTONE;
import static android.media.AudioManager.STREAM_RING;
import static android.media.AudioManager.STREAM_VOICE_CALL;
import static hzhl.net.hlcall.activity.RecordsActivity.WRITE_EXTERNAL_STORAGE_RESULT;

public class CallRingActivity extends BaseActivity implements SensorEventListener {
    @Bind(R.id.rl_call_ring)
    ConstraintLayout mRlRing;
    @Bind(R.id.ll_call_connected)
    LinearLayout mLlCallConnected;
    @Bind(R.id.tv_incoming_number)
    TextView mTvIncomingNumber;
    @Bind(R.id.tv_state)
    TextView mTvState;
    @Bind(R.id.tv_mianti)
    ImageView mTvMianti;
    @Bind(R.id.tv_mute)
    TextView mTvMute;
    @Bind(R.id.tv_describe)
    TextView tvDescribe;
    @Bind(R.id.chronometer)
    Chronometer mChronometer;
    @Bind(R.id.ll_other)
    LinearLayout llOther;
    @Bind(R.id.ll_number)
    LinearLayout llNumber;
    @Bind(R.id.iv_call_answer)
    ImageView ivCallAnswer;
    @Bind(R.id.iv_call_answer_video)
    ImageView iv_call_answer_video;
    @Bind(R.id.iv_close_ring)
    ImageView ivCloseRing;
    @Bind(R.id.iv_close_connect)
    ImageView iv_close_connect;
    @Bind(R.id.tv_to)
    ImageView tvTo;
    @Bind(R.id.iv_del)
    ImageView ivDel;
    @Bind(R.id.iv_up)
    ImageView ivUp;
    @Bind(R.id.tv_move_up)
    TextView tvMoveUp;
    @Bind(R.id.tv_record)
    TextView tvRecord;
    @Bind(R.id.tv_stop)
    TextView tv_stop;


    @Bind(R.id.tv_video)
    TextView tvVideo;
    @Bind(R.id.iv_switch_camera_ring)
    ImageView ivSwitchCamera;
    @Bind(R.id.local_preview_texture)
    CaptureTextureView mLocalPreview;
    @Bind(R.id.remote_video_texture)
    TextureView mRemoteVideo;
    @Bind(R.id.fl_local_preview_texture)
    FrameLayout fl_local_preview_texture;
    //Texture mRemoteVideo;

    //wenyeyang
    @Bind(R.id.rl_ring_top)
    RelativeLayout rl_ring_top;
    //
    @Bind(R.id.call_menu_layout)
    LinearLayout mMenuLayout;
    @Bind(R.id.rl_bottom_call)
    RelativeLayout mBottomCallLayout;

    private Handler handler = new Handler();
    private boolean isScreenLocked = false;//是否已接电话
    private Vibrator mVibrator;
    private Call mCall;
    private Call targetCall;
    private Core core;
    private CoreListenerStub mListener;
    private Address addressIncoming;
    private boolean mAlreadyAcceptedOrDeniedCall;//准备好接电话或者不在电话中
    private AudioManager mAudioManager;
    private MediaPlayer mRingerPlayer;
    private boolean mAudioFocused;
    private final static int INCALL_NOTIF_ID = 2;
    private float lastX;
    private float lastY;

    private boolean isClickKeyborad= false;  //接通时是否点击键盘

    float viewY = 0;//记录触碰开始viewY坐标（父布局为原点）
    private AnimatorSet animatorSetIvUp;//向上指示箭头 组合动画
    //wenyeyang
    ArrayMap<Integer,ObjectAnimator> upMap = new ArrayMap<>();
    ArrayMap<Integer,ObjectAnimator> downMap = new ArrayMap<>();
    private int lastClickTime = 0;
    private boolean isStopped = false;
    public static final int MOVE_DISTANCE = 300;//接听、拒绝电话滑动距离

    private static final int CAMERA_TO_TOGGLE_VIDEO = 121;
    boolean mIsVideoCallModel = false;
    private AlertDialog.Builder normalDialog;//对话框,防止多次弹出
    boolean isRequestVideoCall = false;
    boolean isAnswer = false;
    //默认清晰度
    /*private int videoWith = 600;
    private int videoHeight = 840;*/
    private int videoWith = 650;
    private int videoHeight = 840;
    private long llOtherLastShowTime = 0;

    private Timer timer = new Timer("Timer");
    private TimerTask timerTask= new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(() -> {
                if(mIsVideoCallModel){
                    llOther.setVisibility(View.GONE);
                }
            });
        }
    };


    private Runnable runnableJump = new Runnable() {
        @Override
        public void run() {
            isStopped = false;
/*            upThrowAnswer(ivCallAnswer);
            upThrowDecline();*/
            /*upThrowAnswer(ivCallAnswer);
            upThrowDecline(ivCloseRing);
            freeFallAnswer(iv_call_answer_video);
            ivUp();*/
        }
    };
    //距离传感器
    private Sensor mSensor;
    private PowerManager.WakeLock mWakeLock;
    private SensorManager sensorManager;
    private PowerManager mPowerManager;


    //挂断键广播监听
    private HomeListen mHomelisten = null;


    @Override
    protected int getLayoutResID() {
        return R.layout.activity_call_ring;
    }

    @Override
    protected String getTopTitle() {
        return null;
    }

    @Override
    protected void init() {
      /*  isScreenLocked = ScreenUtils.isScreenLocked(this);
        if (isScreenLocked) {
            ivCloseRing.setOnTouchListener(onCloseRingTouchListener);
            ivCallAnswer.setOnTouchListener(onCallAnswerTouchListener);
        }*/

        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        //获取壁纸图片
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        getWindow().setBackgroundDrawable(wallpaperDrawable);

        //ivCloseRing.setOnTouchListener(onCloseRingTouchListener);
        //ivCallAnswer.setOnTouchListener(onCallAnswerTouchListener);
        //iv_call_answer_video.setOnTouchListener(onCallAnswerTouchListener);

        //挂断键广播监听
        initHomeListen();
    }

    View.OnTouchListener onCloseRingTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastY = event.getRawY();//触碰控件View的那一点的Y坐标(屏幕坐标)
                    viewY = v.getY();//记录控件View的Y坐标(父布局为原点)
                    Logger.d(viewY);
                    stopLoading();
                    break;
                case MotionEvent.ACTION_MOVE:
                    //禁止向下滑动，显示提示TextView;
                    if ((event.getRawY() - lastY) >= 0) {
                        tvMoveUp.setVisibility(View.VISIBLE);
                    } else {
                        v.setY(v.getY() + event.getRawY() - lastY);//改变View位置
                        lastY = event.getRawY();//view位置改变，触碰点屏幕Y坐标重新赋值
                        //view移动超过250
                        if ((viewY - v.getY()) > MOVE_DISTANCE) {
                            v.setVisibility(View.GONE);
                            decline();
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    // 手指抬起，view移动少于250
                    if ((viewY - v.getY()) <= MOVE_DISTANCE) {
                        //回到原位
                        v.setY(viewY);
                        startJump();
                    }
                    tvMoveUp.setVisibility(View.GONE);
                    break;
            }
            return false;
        }
    };

    View.OnTouchListener onCallAnswerTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastY = event.getRawY();
                    viewY = v.getY();
                    stopLoading();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if ((event.getRawY() - lastY) >= 0) {
                        tvMoveUp.setVisibility(View.VISIBLE);
                    } else {
                        v.setY(v.getY() + event.getRawY() - lastY);
                        lastY = event.getRawY();
                        if ((viewY - v.getY()) > MOVE_DISTANCE) {
                            v.setVisibility(View.GONE);
                            if (v.getId() == R.id.iv_call_answer_video)answer(true);
                            if (v.getId() == R.id.iv_call_answer)answer(false);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    //  Logger.d(v.getY());
                    // 手指抬起
                    if ((viewY - v.getY()) <= MOVE_DISTANCE) {
                        v.setY(viewY);
                        startJump();
                    }
                    tvMoveUp.setVisibility(View.GONE);
                    break;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setCommonTitleBarGone();
        ButterKnife.bind(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        NotificationManager mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNM.cancel(INCALL_NOTIF_ID);

        initSensor();


        if (LinphoneService.isReady()) {
            core = LinphoneService.getCore();
        }
        lookupCurrentCall();

        mListener = new CoreListenerStub() {
            @Override
            public void onCallStateChanged(
                    Core core, Call call, Call.State state, String message) {
                /*Logger.d("state:" + state +"  getCurrentParams: "
                        + call.getCurrentParams().videoEnabled()
                        + "  getRemoteParams:"+call.getRemoteParams().videoEnabled()
                        + "  message:"+ message
                );
                */

                call.getCurrentParams().videoEnabled();
                call.getRemoteParams().videoEnabled();
                if (call == mCall) {
                    if (state == Call.State.Connected) {
                        if (core.getCallsNb() == 1) {
                            if (call.getDir() == Call.Dir.Incoming) {
                                //mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                                // mAudioManager.abandonAudioFocus(null);
                                //requestAudioFocus(STREAM_VOICE_CALL);
                                updateCallTime();
                                updateStatus("通话中");
                            }


                            CallParams remoteParams = call.getRemoteParams();
                            if (remoteParams!=null) {
                                if (remoteParams.videoEnabled() && isRequestVideoCall) {
                                    mIsVideoCallModel = true;
                                    isRequestVideoCall = false;
                                } else {
                                    isRequestVideoCall = false;
                                    mIsVideoCallModel = false;
                                }
                                //mCall.acceptUpdate(remoteParams);
                                updateInterfaceDependingOnVideo();
                            }
                        }

                    } else if (state == Call.State.End || state == Call.State.Error) {
                        Logger.d("end");
                        if (core.getCallsNb() == 0) {

                            updateStatus("已挂机");

                            if (mAudioFocused) {
                                int res = mAudioManager.abandonAudioFocus(null);
                                Log.d(
                                        "[Audio Manager] Audio focus released a bit later: "
                                                + (res
                                                == AudioManager
                                                .AUDIOFOCUS_REQUEST_GRANTED
                                                ? "Granted"
                                                : "Denied"));
                                mAudioFocused = false;
                            }
                            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                            if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                                //  Logger.d("[Audio Manager] ---AndroidAudioManager: back to MODE_NORMAL");
                                mAudioManager.setMode(AudioManager.MODE_NORMAL);
                                // Logger.d("[Audio Manager] 所有呼叫终止，返回听筒");
                                mAudioManager.setSpeakerphoneOn(false);
                            }
                        }
                        finish();
                    }else if(state == Call.State.StreamsRunning){
                        /*CallParams params = call.getRemoteParams();
                        if (params.videoEnabled() && isRequestVideoCall){
                                mIsVideoCallModel = true;
                                isRequestVideoCall = false;
                        }
                        updateInterfaceDependingOnVideo();*/
                    }else if (state == Call.State.UpdatedByRemote) {
                        // If the correspondent asks for video while in audio call
                        /*boolean videoEnabled = core.videoEnabled();
                        if (!videoEnabled) {
                            // Video is disabled globally, don't even ask user
                            CallManager.getInstance().acceptCallVideo(false);
                            return;
                        }*/

                        //wenyeyang



                        CallParams params = call.getRemoteParams();

                        if (params!=null) {
                            /*MyLog.d("state:" + state +"  isRequestVideoCall: "
                                    + isRequestVideoCall
                                    + "  getRemoteParams:"+params.videoEnabled()
                                    + "  mIsVideoCallModel:"+ mIsVideoCallModel);*/
                            boolean v = params.videoEnabled();
                            if (!v){
                                isRequestVideoCall = false;
                                mIsVideoCallModel = false;
                                CallManager.getInstance().acceptCallVideo(false);
                            }
                            else if (isRequestVideoCall){
                                mIsVideoCallModel = true;
                                isRequestVideoCall = false;
                                CallManager.getInstance().acceptCallVideo(true);
                            }
                            else if (!mIsVideoCallModel)showDellDialog();
                        }
                        updateInterfaceDependingOnVideo();

                    }
                }
                if (call == targetCall) {
                    if (state == Call.State.Connected) {
                        Logger.d(core.getCallsNb());
                        if (call.getDir() == Call.Dir.Outgoing) {
                            // mCall.transferToAnother(targetCall);
                        }



                    }
                    if (state == Call.State.OutgoingRinging) {
                        // mCall.transferToAnother(targetCall);
                    }
                }

                if (core.getCallsNb() == 0) {
                    finish();
                    //(MainActivity.class);
                }
            }

            @Override
            public void onTransferStateChanged(Core lc, Call transfered, Call.State newCallState) {
                super.onTransferStateChanged(lc, transfered, newCallState);
                Logger.d(transfered.getRemoteAddress().getUsername());
                Logger.d(transfered.getDiversionAddress().getUsername());
                Logger.d(transfered.getToAddress().getUsername());
                Logger.d(newCallState);
            }

            @Override
            public void onDtmfReceived(Core lc, Call call, int dtmf) {
                super.onDtmfReceived(lc, call, dtmf);
                MyLog.d("wen "+ (char)dtmf);
                lc.playDtmf((char) dtmf,500);
            }
        };


    }

    //监听home键，但未屏蔽
    private void initHomeListen() {
        mHomelisten = new HomeListen(this);
        mHomelisten.setOnHomeBtnPressListener(new HomeListen.OnHomeBtnPressLitener() {
            @Override
            public void onHomeBtnPress() {
                System.out.println("来电时挂断电话：");
                decline();
            }
            @Override
            public void onHomeBtnLongPress() {
                System.out.println("长按");
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        //保持常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mHomelisten.start();

        if (mSensor != null)
            sensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);


        if (LinphoneService.isReady()) {
            core = LinphoneService.getCore();
        }
        if (core != null) {
            core.setNativeVideoWindowId(mRemoteVideo);
            core.setNativePreviewWindowId(mLocalPreview);
            core.addListener(mListener);

            //wenyeyang
            setStatusBarFullTransparent();
            //core.getPreferredVideoDefinition().setHeight(videoHeight);
            //core.getPreferredVideoDefinition().setWidth(videoWith);
            //带宽
            /*core.setExpectedBandwidth(2000);
            core.enableAdaptiveRateControl(false);*/
            //视频缩放
            Matrix matrix = new Matrix();
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            float dx = 1;
            float dy = 1;
            //metrics.heightPixels = 2160;
            MyLog.d("Scale:matrix" + "x:" + metrics.widthPixels+"\n:"+metrics.heightPixels);
            if (metrics.heightPixels/(double)metrics.widthPixels > videoHeight/(double)videoWith){
                dx = metrics.widthPixels/(float)videoWith;
                dy = metrics.heightPixels/(dx*videoHeight);
                matrix.setScale(1f,dy,metrics.widthPixels/2,metrics.heightPixels/2);
                MyLog.d("Scale:y" + dy);
            }else {
                dy = metrics.heightPixels/(float)videoHeight;
                dx = metrics.widthPixels/(dy*videoWith);
                matrix.setScale(dx,1f,metrics.widthPixels/2,metrics.heightPixels/2);
                MyLog.d("Scale:x" + dx);

            }
            mRemoteVideo.setTransform(matrix);


            //帧率
            //setVideoPreset("high-fps");
            //编码
            populateVideoCodecs();

        }
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAlreadyAcceptedOrDeniedCall = true;
        mCall = null;
        // Only one call ringing at a time is allowed一次只能打一个电话
        lookupCurrentCall();
        if (mCall == null) {
            // The incoming call no longer exists.
            Logger.d("Couldn't find incoming call");
            finish();
            //jump(MainActivity.class);
            return;
        }
        if (Call.State.IncomingReceived == mCall.getState()
                || Call.State.IncomingEarlyMedia == mCall.getState()) {
            /*if (isScreenLocked) {
                startJump();
            }*/
            startJump();
            Logger.d(core.inCall());
            startRinging();
            addressIncoming = mCall.getRemoteAddress();
            SettingEntityDao settingEntityDao = App.getDaoInstant().getSettingEntityDao();
            SettingEntity settingEntity = settingEntityDao.load(1L);

            Logger.d("来电：" + addressIncoming.getUsername() + "@" + addressIncoming.getDomain());
            updateNumber(ContactsUtil.getNameFormNumber(this,addressIncoming.getUsername()));
            updateStatus("来电...");
            mRlRing.setVisibility(View.VISIBLE);

            //自动接听 判断
            if (settingEntity != null) {
                boolean isAutoAnswerCall = settingEntity.getIsAutoAnswerCall();
                if (isAutoAnswerCall) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // For this sample we will automatically answer incoming calls对于此示例，我们将自动应答传入的呼叫
                            toggleSpeaker();
                            answer(settingEntity.getIsAutoAnswerCallVideo());

                        }
                    }, 3000);
                }
            }

            if (!mCall.getRemoteParams().videoEnabled()){
                iv_call_answer_video.setVisibility(View.GONE);
            }else {
                iv_call_answer_video.setVisibility(View.VISIBLE);


            }


        }

    }

    private void startJump() {
        handler.post(runnableJump);
    }

    private void startRinging() {
        //mAudioManager.setSpeakerphoneOn(false);
        mAudioManager.setMode(MODE_RINGTONE);
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        // 等待3秒，震动3秒，从第0个索引开始，一直循环
        if (mVibrator != null) {
            mVibrator.vibrate(new long[]{1500, 1500}, 0);
        }
        if (mRingerPlayer == null) {
            mAudioManager.setSpeakerphoneOn(true);
            requestAudioFocus(STREAM_RING);
            mRingerPlayer = new MediaPlayer();
            mRingerPlayer.setAudioStreamType(STREAM_RING);
            if (core != null) {
                //取得配置文件的铃声，没有设置取系统默认铃声
                String ringtone = core.getConfig()
                        .getString("app", "ringtone", Settings.System.DEFAULT_RINGTONE_URI.toString());
                if (ringtone == null || ringtone.isEmpty())
                    ringtone = Settings.System.DEFAULT_RINGTONE_URI.toString();
                try {
                    if (ringtone.startsWith("content://")) {
                        mRingerPlayer.setDataSource(this, Uri.parse(ringtone));
                    } else {
                        FileInputStream fis = new FileInputStream(ringtone);
                            mRingerPlayer.setDataSource(fis.getFD());
                        fis.close();
                    }
                } catch (IOException e) {
                    CrashReport.postCatchedException(e);
                    Log.e(e, "[Audio Manager] Cannot set ringtone");
                }
            }
            mRingerPlayer.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                mp.start();
            });
            mRingerPlayer.prepareAsync();

        } else {
            Log.w("[Audio Manager] Already ringing");
        }
    }

    private void requestAudioFocus(int stream) {
        if (!mAudioFocused) {
            int res = mAudioManager.requestAudioFocus(
                    null, stream,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
                    //AudioManager.AUDIOFOCUS_GAIN);
            Logger.d(
                    "[Audio Manager] Audio focus requested: "
                            + (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
                            ? "Granted"
                            : "Denied"));
            if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) mAudioFocused = true;
        }
    }

    @Override
    protected void onPause() {
        Logger.d("onPause");
        if (LinphoneService.isReady()) {
            core = LinphoneService.getCore();
        }
        if (core != null) {
            core.removeListener(mListener);
            core.setNativeVideoWindowId(null);
            core.setNativePreviewWindowId(null);
        }
        super.onPause();

        if (mSensor!=null)
            sensorManager.unregisterListener(this);

        mHomelisten.stop();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d("onDestroy");
        //showToast("听电话关闭");
        if (mCall != null) {
            mCall.terminate();
        }
        if (core!=null)core.removeListener(mListener);
        ActivityCollector.removeActivity(this);
        mCall = null;
        mListener = null;
        stopRinging();
        stopLoading();
        mChronometer.stop();
        mAudioManager.abandonAudioFocus(null);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void lookupCurrentCall() {
        if (core != null) {
            for (Call call : core.getCalls()) {
                Logger.d(call.getState());
                if (Call.State.IncomingReceived == call.getState()
                        || Call.State.IncomingEarlyMedia == call.getState()
                        || Call.State.Connected == call.getState()
                        || Call.State.StreamsRunning == call.getState()) {
                    mCall = call;
                    break;
                }
            }
        }
    }

    @OnClick({R.id.iv_close_ring, R.id.iv_close_connect, R.id.tv_mute, R.id.tv_record
            , R.id.tv_mianti, R.id.iv_call_answer, R.id.tv_transfer, R.id.tv_keyboard, R.id.tv_to, R.id.iv_del
            , R.id.rl_ring_top, R.id.ll_0, R.id.ll_1, R.id.ll_2, R.id.ll_3, R.id.ll_4, R.id.tv_stop
            , R.id.ll_5, R.id.ll_6, R.id.ll_7, R.id.ll_8, R.id.ll_9, R.id.ll_star, R.id.ll_jing,
            R.id.iv_switch_camera_ring, R.id.tv_video, R.id.tv_more,R.id.iv_call_answer_video})
    public void onClickVoid(View view) {
        switch (view.getId()) {
            case R.id.iv_call_answer:
                answer(false);
                break;
            case R.id.iv_call_answer_video:
                answer(true);
                break;
            case R.id.iv_close_ring:
                decline();
                break;
            case R.id.iv_close_connect:
                terminate();
                break;
            case R.id.tv_mute:
                if (mCall != null) {
                    if (view.isSelected()) {
                        mCall.setMicrophoneMuted(false);
                        mTvMute.setSelected(false);
                    } else {
                        mCall.setMicrophoneMuted(true);
                        mTvMute.setSelected(true);
                    }
                }
                break;
            case R.id.tv_mianti:
                toggleSpeaker();
                break;
            case R.id.tv_transfer:
                //llNumber.setVisibility(View.VISIBLE);
                //llOther.setVisibility(View.GONE);
                //iv_close_connect.setVisibility(View.INVISIBLE);
                //tvTo.setVisibility(View.VISIBLE);
                transfer();
                break;
            case R.id.tv_keyboard:
                //banlap: bug fixes:  修复通话中打开键盘 --2020.10.13
                llNumber.setVisibility(View.VISIBLE);
                llOther.setVisibility(View.GONE);
                iv_close_connect.setVisibility(View.VISIBLE);
                //banlap: bug fixes:  --end
                //banlap: bug: 判断通话中是否点击键盘
                if(!isClickKeyborad) {
                    isClickKeyborad = true;
                }
                break;
            case R.id.tv_hide:
                if (view.getVisibility() == View.VISIBLE) {
                    view.setVisibility(View.GONE);
                    llNumber.setVisibility(View.GONE);
                    llOther.setVisibility(View.VISIBLE);
                    tvTo.setVisibility(View.GONE);
                }
                break;
            case R.id.tv_to:
                transfer();
                break;
            case R.id.tv_record:
                if (isOpenPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    toggleRecording();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                            , WRITE_EXTERNAL_STORAGE_RESULT);
                }


                break;
            case R.id.iv_del:
                delNumber();
                break;
            case R.id.ll_0:
                addNumber("0");
                break;
            case R.id.ll_1:
                addNumber("1");
                break;
            case R.id.ll_2:
                addNumber("2");
                break;
            case R.id.ll_3:
                addNumber("3");
                break;
            case R.id.ll_4:
                addNumber("4");
                break;
            case R.id.ll_5:
                addNumber("5");
                break;
            case R.id.ll_6:
                addNumber("6");
                break;
            case R.id.ll_7:
                addNumber("7");
                break;
            case R.id.ll_8:
                addNumber("8");
                break;
            case R.id.ll_9:
                addNumber("9");
                break;
            case R.id.ll_star:
                addNumber("*");
                break;
            case R.id.ll_jing:
                addNumber("#");
                break;
            case R.id.iv_switch_camera_ring:
                CallManager.getInstance().switchCamera();
                break;
            case R.id.tv_video:
                if (checkAndRequestPermission(Manifest.permission.CAMERA, CAMERA_TO_TOGGLE_VIDEO)) {
                    //LinphoneUtils.reloadVideoDevices();
                    toggleVideo();
                    MyLog.d("Permission:" + true);
                }else MyLog.d("Permission:" + false);
                break;
            case R.id.tv_more:
                switchMenuPanel();
                //banlap: bug: 判断通话时是否点击键盘
                if(isClickKeyborad) {
                    isClickKeyborad = false;
                }
                break;
            case R.id.tv_stop:
                if (mCall!=null){
                    boolean isPause = view.isSelected();
                    if (isPause)mCall.resume();
                    else mCall.pause();
                    view.setSelected(!isPause);
                }
                break;
        }
    }

    private boolean checkAndRequestPermission(String permission, int result) {
        if (!checkPermission(permission)) {
            ActivityCompat.requestPermissions(this, new String[] {permission}, result);
            return false;
        }
        return true;
    }

    private boolean checkPermission(String permission) {
        int granted = getPackageManager().checkPermission(permission, getPackageName());
        return granted == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Permission not granted, won't change anything
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) return;

        switch (requestCode) {
            case CAMERA_TO_TOGGLE_VIDEO:
                LinphoneUtils.reloadVideoDevices();
                toggleVideo();
                break;
            case  WRITE_EXTERNAL_STORAGE_RESULT: {
                if (!verifyPermissions(grantResults)) {
                    //还有权限未申请
                    showMissingPermissionDialog();
                    // if (isNeedCheck) {checkPermissions(needPermissions);} 防止去检测权限，不停的弹框
                    //isNeedCheck = false;
                } else {
                    toggleRecording();
                }
            }
            break;
        }
    }

    private void toggleVideo() {
        if(core != null){
            Call call = core.getCurrentCall();
            if (call == null) return;
            if (mIsVideoCallModel) {
                CallManager.getInstance().removeVideo();
                //ivSwitchCamera.setVisibility(View.GONE);
                //mMenuLayout.setVisibility(View.VISIBLE);
                //llOther.setVisibility(View.VISIBLE);
                mIsVideoCallModel = false;
                tvVideo.setSelected(false);
                isRequestVideoCall = false;
            } else if (!isRequestVideoCall){
                CallManager.getInstance().addVideo();
                //ivSwitchCamera.setVisibility(View.VISIBLE);
                //mMenuLayout.setVisibility(View.GONE);
                //llOther.setVisibility(View.GONE);
                //mIsVideoCallModel = false;
                tvVideo.setSelected(true);
                isRequestVideoCall = true;
            }

            updateInterfaceDependingOnVideo();
            //setVideoEnabled(mIsVideoCallModel);
        }
    }

    private void updateInterfaceDependingOnVideo() {
        Call call = core.getCurrentCall();
        if (call == null) {
            showVideoControls(false);
            return;
        }
        showVideoControls(mIsVideoCallModel);
    }

    private void showVideoControls(boolean videoEnabled) {
        mRemoteVideo.setVisibility(videoEnabled ? View.VISIBLE : View.GONE);
        fl_local_preview_texture.setVisibility(videoEnabled ? View.VISIBLE : View.GONE);
        ivSwitchCamera.setVisibility(videoEnabled ? View.VISIBLE : View.INVISIBLE);
        tvVideo.setSelected(isRequestVideoCall);
        if (core!=null)core.setNativePreviewWindowId(videoEnabled ? mLocalPreview : null);
        setVideoEnabled(videoEnabled);
    }

    private void transfer() {
        if (mCall != null) {
            String number = tvDescribe.getText().toString().trim();
            if (StringUtil.isEmpty(number)) {
                showToast("请输入转移号码");
                return;
            }
            Address addressToCall = core.getDefaultProxyConfig().normalizeSipUri("8040");
            mCall.transfer(number);
        }
    }

    private void toggleRecording() {
        if (core != null) {
            Call call = core.getCurrentCall();
            if (call == null) return;

            if (call.isRecording()) {
                call.stopRecording();
                Logger.d("停止录音");
            } else {
                Logger.d("开启录音");
                call.startRecording();
            }
            tvRecord.setSelected(call.isRecording());
        }
    }
    /**
     * 接听 上抛
     */
    public void upThrowAnswer(View v) {
        ObjectAnimator animatorUpThrowAnswer = upMap.get(v.getId());
        if (animatorUpThrowAnswer == null) {
            animatorUpThrowAnswer = ObjectAnimator.ofFloat(v, "translationY"
                    , 0, -SizeUtils.dip2px(this, 50f));
            //objectAnimatorList.add(animatorUpThrowAnswer);
            upMap.put(v.getId(),animatorUpThrowAnswer);
            animatorUpThrowAnswer.setDuration(500);
            animatorUpThrowAnswer.setInterpolator(new DecelerateInterpolator(1.2f));//设置动画速度模型
            animatorUpThrowAnswer.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (!isStopped) {
                        freeFallAnswer(v);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        }
        animatorUpThrowAnswer.start();
    }

    /**
     * 接听下落
     */
    public void freeFallAnswer(View v) {
        ObjectAnimator animatorFreeFallAnswer = downMap.get(v.getId());
        if (animatorFreeFallAnswer == null) {
            animatorFreeFallAnswer = ObjectAnimator.ofFloat(v, "translationY"
                    , -SizeUtils.dip2px(this, 50f), 0);
            //objectAnimatorList.add(animatorFreeFallAnswer);
            downMap.put(v.getId(),animatorFreeFallAnswer);
            animatorFreeFallAnswer.setDuration(500);
            animatorFreeFallAnswer.setInterpolator(new AccelerateInterpolator(1.2f));//设置动画速度模型
            animatorFreeFallAnswer.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (!isStopped) {
                        upThrowAnswer(v);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        }
        animatorFreeFallAnswer.start();
    }

    /**
     * 拒绝 上抛
     */
    public void upThrowDecline(View v) {
        ObjectAnimator animatorUpThrowDecline = upMap.get(v.getId());
        if (animatorUpThrowDecline == null) {
            animatorUpThrowDecline = ObjectAnimator.ofFloat(v, "translationY"
                    , 0, -SizeUtils.dip2px(this, 20f));
            //objectAnimatorList.add(animatorUpThrowDecline);
            upMap.put(v.getId(),animatorUpThrowDecline);
            animatorUpThrowDecline.setDuration(750);
            animatorUpThrowDecline.setInterpolator(new DecelerateInterpolator(1.2f));//设置动画速度模型

            animatorUpThrowDecline.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (!isStopped) {
                        freeFallDecline(v);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        }
        animatorUpThrowDecline.start();
    }

    /**
     * 拒绝下落
     */
    public void freeFallDecline(View v) {
        ObjectAnimator animatorFreeFallDecline = downMap.get(v.getId());
        if (animatorFreeFallDecline == null) {
            animatorFreeFallDecline = ObjectAnimator.ofFloat(v, "translationY"
                    , -SizeUtils.dip2px(this, 20f), 0);
            //objectAnimatorList.add(animatorFreeFallDecline);
            downMap.put(v.getId(),animatorFreeFallDecline);
            animatorFreeFallDecline.setDuration(750);
            animatorFreeFallDecline.setInterpolator(new AccelerateInterpolator(1.2f));//设置动画速度模型
            animatorFreeFallDecline.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (!isStopped) {
                        upThrowDecline(v);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        }
        animatorFreeFallDecline.start();
    }

    /**
     * 向上指示
     */
    public void ivUp() {
        if (animatorSetIvUp == null) {
            ivUp.setVisibility(View.VISIBLE);
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(ivUp, "translationY"
                    , 0, -SizeUtils.dip2px(this, 30f));
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(ivUp, "alpha", 1f, 0f);
            animatorSetIvUp = new AnimatorSet();
            animatorSetIvUp.playTogether(objectAnimator, alphaAnimator);
            animatorSetIvUp.setDuration(1000);
            animatorSetIvUp.setInterpolator(new AccelerateInterpolator(1.2f));//设置动画速度模型
            animatorSetIvUp.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (!isStopped) {
                        ivUp();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        }
        animatorSetIvUp.start();
    }

    private void stopLoading() {
        isStopped = true;
        for (ObjectAnimator o :upMap.values()) {
            if (o.isRunning())o.cancel();
            o.removeAllListeners();
        }
        for (ObjectAnimator o :downMap.values()) {
            if (o.isRunning())o.cancel();
            o.removeAllListeners();
        }

       /* for (int i = 0; i < objectAnimatorList.size(); i++) {
            if (objectAnimatorList.get(i) != null) {
                if (objectAnimatorList.get(i).isRunning()) {
                    objectAnimatorList.get(i).cancel();
                }
                objectAnimatorList.get(i).removeAllListeners();
            }
        }*/
        if (animatorSetIvUp != null) {
            if (animatorSetIvUp.isRunning()) {
                animatorSetIvUp.cancel();
            }
            animatorSetIvUp.removeAllListeners();
            for (Animator animator : animatorSetIvUp.getChildAnimations()) {
                animator.removeAllListeners();
            }
            animatorSetIvUp = null;
        }
        handler.removeCallbacks(runnableJump);
    }

    /*private void toggleSpeaker() {
        //int volume =  mAudioManager.getStreamVolume(STREAM_VOICE_CALL);
        //mAudioManager.setStreamVolume(STREAM_VOICE_CALL,0,AudioManager.FLAG_SHOW_UI);
        //
        //requestAudioFocus(STREAM_VOICE_CALL);
        if (mAudioManager.isSpeakerphoneOn()) {
            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            mAudioManager.setSpeakerphoneOn(false);


        } else {
            //mAudioManager.setMode(AudioManager.MODE_NORMAL);
            mAudioManager.setSpeakerphoneOn(true);
        }

        //mAudioManager.setStreamVolume(STREAM_VOICE_CALL,volume,AudioManager.FLAG_SHOW_UI);


        mTvMianti.setSelected(mAudioManager.isSpeakerphoneOn());
    }*/


    /**
     * 部分手机打不开扬声器,这里使用反射开启
     */
    private boolean isSpeakerOn = false;
    private Method setForceUse;
    private void toggleSpeaker() {
        //int volume =  mAudioManager.getStreamVolume(STREAM_VOICE_CALL);
        //mAudioManager.setStreamVolume(STREAM_VOICE_CALL,0,AudioManager.FLAG_SHOW_UI);
        //
        //requestAudioFocus(STREAM_VOICE_CALL);
        /*if (mAudioManager.isSpeakerphoneOn()) {
            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            mAudioManager.setSpeakerphoneOn(false);
            *//*mAudioManager.setStreamVolume(STREAM_VOICE_CALL,
                    mAudioManager.getStreamVolume(STREAM_VOICE_CALL),
                    STREAM_VOICE_CALL);*//*
        } else {
            //mAudioManager.setMode(AudioManager.MODE_NORMAL);
            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            mAudioManager.setBluetoothScoOn(false);
            mAudioManager.setSpeakerphoneOn(true);

            *//*mAudioManager.setStreamVolume(STREAM_VOICE_CALL,
                    mAudioManager.getStreamVolume(STREAM_VOICE_CALL),
                    STREAM_VOICE_CALL);
            *//*
        }*/
        try {
            if (setForceUse == null) {
                Class audioSystemClass = Class.forName("android.media.AudioSystem");
                setForceUse = audioSystemClass.getMethod("setForceUse", int.class, int.class);
            }
            // First 1 == FOR_MEDIA, second 1 == FORCE_SPEAKER. To go back to the default
            // behavior, use FORCE_NONE (0).
            //MyLog.d("wen" + new Gson().toJson(audioSystemClass));
            if (!isSpeakerOn){
                setForceUse.invoke(null, 0, 1);
                isSpeakerOn = true;
            }else {
                setForceUse.invoke(null, 0, 0);
                isSpeakerOn = false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        //mAudioManager.setStreamVolume(STREAM_VOICE_CALL,volume,AudioManager.FLAG_SHOW_UI);

        mTvMianti.setSelected(isSpeakerOn);
    }


    /**
     * 添加号码
     */
    public void addNumber(String s) {
        playDtmf(s);
        String number = tvDescribe.getText().toString();
        StringBuffer stringBuffer = new StringBuffer(number);
        stringBuffer.append(s);
        tvDescribe.setText(stringBuffer.toString());
        ivDel.setVisibility(View.VISIBLE);
    }

    /**
     * 删除号码
     */
    public void delNumber() {
        String number = tvDescribe.getText().toString();
        StringBuffer stringBuffer = new StringBuffer(number);
        int length = number.length();
        if (length >= 1) {
            stringBuffer.delete(length - 1, length);
            tvDescribe.setText(stringBuffer.toString());
        }
        if (tvDescribe.getText().toString().trim().length() == 0) {
            ivDel.setVisibility(View.GONE);
            isClickKeyborad = false;
            switchMenuPanel();
        }
    }

    private void playDtmf(String s) {
        if (core != null) {
            requestAudioFocus(STREAM_RING);
            char c = s.subSequence(0, 1).charAt(0);
            core.playDtmf(c, 500);
            if (!tv_stop.isSelected())core.getCurrentCall().sendDtmf(c);
        }
    }


    public void updateDesri(final String status) {
        // Be a good citizen.  Make sure UI changes fire on the UI thread.
        handler.post(new Runnable() {
            public void run() {
                tvDescribe.setText(status);
            }
        });
    }

    public void updateStatus(final String status) {
        // Be a good citizen.  Make sure UI changes fire on the UI thread.
        handler.post(new Runnable() {
            public void run() {
                mTvState.setText(status);
            }
        });
    }

    public void updateNumber(final String number) {
        // Be a good citizen.  Make sure UI changes fire on the UI thread.
        handler.post(new Runnable() {
            public void run() {
                mTvIncomingNumber.setText(number);
            }
        });
    }

    /**
     * 接通时调用
     */
    public void updateCallTime() {

        // Be a good citizen.  Make sure UI changes fire on the UI thread.
        handler.post(() -> {
            mChronometer.setBase(SystemClock.elapsedRealtime());
            mChronometer.start();
            mChronometer.setVisibility(View.VISIBLE);
            mChronometer.setOnChronometerTickListener(chronometer -> {
                lastClickTime++;
                MyLog.d("lastClickTime"+lastClickTime);
                if (lastClickTime>3 && mIsVideoCallModel){
                    llOther.setVisibility(View.GONE);
                    iv_close_connect.setVisibility(View.INVISIBLE);
                }
                //
                if (lastClickTime>10 && lastClickTime%2==0){
                    //if (mCall!=null)mCall.update(null);
                }
            });

            mRemoteVideo.setOnClickListener(v -> {
                switchMenuPanel();
            });
        });
    }

    /**
     * 拒绝通话，繁忙
     */
    private void decline() {
        if (mCall != null) {
            updateStatus("用户繁忙");
            mCall.decline(Reason.Declined);
            finish();
            //jump(MainActivity.class);
        }
    }

    /**
     * 终止通话
     */
    private void terminate() {
        if (mCall != null) {
            updateStatus("挂断");
            mCall.terminate();
            finish();
            //jump(MainActivity.class);
        }
    }

    //banlap： 电话接听 核心代码
    private void answer(boolean isVideo) {
        stopRinging();
        stopLoading();
        if (isAnswer)return;
        isAnswer = true;
        requestAudioFocus(STREAM_VOICE_CALL);
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        mAudioManager.setSpeakerphoneOn(false);

        //banlap: bug fixes: 修复接通电话后双方无声音情况 --2020.10.20
        //强制初始化音频AudioTrack, 每秒8K个点，双声道，一个采样点16比特-2个字节, PCM流
        int minBufSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufSize, AudioTrack.MODE_STREAM);
        audioTrack.play();
        //banlap: bug fixes:  --end

        isRequestVideoCall = isVideo;
        mIsVideoCallModel = isVideo;
        requestAudioFocus(STREAM_VOICE_CALL);
        if (core != null && mCall != null) {
            //抖动缓冲区大小
            core.setAudioJittcomp(1);
            MyLog.i("wen",core.getAudioJittcomp()+"");

            CallParams params = core.createCallParams(mCall);
            if (params != null) {
                if(isVideo){
                    params.enableVideo(true);
                    core.enableVideoDisplay(true);
                    core.enableVideoCapture(true);
                    //wenyeyang
                    //默认摄像头
                    params.enableLowBandwidth(false);
                    //params.setAudioBandwidthLimit(1000);
                    //core.setUploadBandwidth(600);
                    //core.setDownloadBandwidth(600);
                    MyLog.d("wen" + core.getVideoDisplayFilter());
                    MyLog.d("wen" + Arrays.toString(core.getVideoPayloadTypes()));

                    LinphoneUtils.reloadVideoDevices();
                    //CallManager.getInstance().resetCameraFromPreferences();
                    //banlap：视频自动开启免提
                    toggleSpeaker();
                }else{
                    params.enableVideo(false);

                }
//                params.enableVideo(false);
                params.setRecordFile(
                        FileUtils.getCallRecordingFilename(this, mCall.getRemoteAddress()));
                //wenyeyang
                //mCall.deferUpdate();
                //
                mCall.acceptWithParams(params);
                Logger.d("acceptWithParams");




                mRlRing.setVisibility(View.GONE);
                mLlCallConnected.setVisibility(View.VISIBLE);
                mBottomCallLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void stopRinging() {
        if (mRingerPlayer != null) {
            mRingerPlayer.stop();
            mRingerPlayer.release();
            mRingerPlayer = null;
        }
        if (mVibrator != null) {
            mVibrator.cancel();
        }
    }



    //重写onKeyDown
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isClickKeyborad) {
                delNumber();
                return true;
            } else {
                Logger.d("CallRingActivity返回");
                Intent home = new Intent(Intent.ACTION_MAIN);
                //  home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                home.addCategory(Intent.CATEGORY_HOME);
                startActivity(home);
                return true;
            }
        }
        switch (keyCode){
            case KeyEvent.KEYCODE_0:
                addNumber("0");
                break;
            case KeyEvent.KEYCODE_1:
                addNumber("1");
                break;
            case KeyEvent.KEYCODE_2:
                addNumber("2");
                break;
            case KeyEvent.KEYCODE_3:
                addNumber("3");
                break;
            case KeyEvent.KEYCODE_4:
                addNumber("4");
                break;
            case KeyEvent.KEYCODE_5:
                addNumber("5");
                break;
            case KeyEvent.KEYCODE_6:
                addNumber("6");
                break;
            case KeyEvent.KEYCODE_7:
                addNumber("7");
                break;
            case KeyEvent.KEYCODE_8:
                addNumber("8");
                break;
            case KeyEvent.KEYCODE_9:
                addNumber("9");
                break;
            case KeyEvent.KEYCODE_STAR:
                addNumber("*");
                break;
            case KeyEvent.KEYCODE_POUND:
                addNumber("#");
                break;
            case KeyEvent.KEYCODE_CALL:
                answer(true);
                return true;
            case KeyEvent.KEYCODE_ENDCALL:
                decline();
                break;
            case KeyEvent.KEYCODE_DEL:
                delNumber();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void switchMenuPanel(){
        lastClickTime = 0;
        if(mIsVideoCallModel){
            //int visibility = mMenuLayout.getVisibility();
            //mMenuLayout.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
            int visibility = llOther.getVisibility();
            llOther.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
            visibility = iv_close_connect.getVisibility();
            iv_close_connect.setVisibility(visibility == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
        }else {
            llOther.setVisibility(View.VISIBLE);
            iv_close_connect.setVisibility(View.VISIBLE);
        }

        llNumber.setVisibility(View.GONE);
        tvTo.setVisibility(View.GONE);
    }


    private void setVideoEnabled(Boolean mIsVideo){
        if (mIsVideo != null && mIsVideo) {
            LinphoneUtils.reloadVideoDevices();
            //CallManager.getInstance().acceptCallVideo(true);
            llOther.setVisibility(View.GONE);
            //mIsVideoCallModel = true;
            tvVideo.setSelected(true);
            rl_ring_top.setGravity(Gravity.LEFT|Gravity.BOTTOM);
        } else {
            //CallManager.getInstance().acceptCallVideo(false);
            llOther.setVisibility(View.VISIBLE);
            //mIsVideoCallModel = false;
            iv_close_connect.setVisibility(View.VISIBLE);
            tvVideo.setSelected(false);
            rl_ring_top.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);
        }
    }


    /**
     * 全透状态栏
     */
    protected void setStatusBarFullTransparent() {
        if (Build.VERSION.SDK_INT >= 21) {//21表示5.0
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= 19) {//19表示4.4
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //虚拟键盘也透明
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }
    /**
     * 对话框
     */
    private void showDellDialog() {
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        if (normalDialog!=null)return;
        normalDialog = new AlertDialog.Builder(this);
        //   normalDialog.setIcon(R.drawable.icon_dialog);
        normalDialog.setTitle("对方请求视频通话");
        normalDialog.setPositiveButton("确定",
                (dialog, which) -> {
                    //...To-do
                    /*mIsVideoCallModel = true;
                    CallManager.getInstance().acceptCallVideo(true);
                    updateInterfaceDependingOnVideo();
                    normalDialog =null;
                    isRequestVideoCall = false;

                    callParams.enableVideo(true);
                    mCall.update(callParams);*/
                    acceptVideo(true);
                });
        normalDialog.setNegativeButton("拒绝",
                (dialog, which) -> {
                    //...To-do
                    /*mIsVideoCallModel = false;
                    CallManager.getInstance().acceptCallVideo(false);
                    updateInterfaceDependingOnVideo();
                    normalDialog =null;
                    isRequestVideoCall = false;

                    callParams.enableVideo(false);
                    mCall.update(callParams);*/
                    acceptVideo(false);
                });

        normalDialog.setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK
                    && event.getAction() == KeyEvent.ACTION_UP) {
                //重写退出键
                acceptVideo(false);
            }
            return false;
        });
        //点击外部区域不关闭dialog写法
        normalDialog.setCancelable(false);

        // 显示
        normalDialog.show();
    }

    //编码,只使用H264
    private void populateVideoCodecs() {
        if (core != null) {
            for (PayloadType pt : core.getVideoPayloadTypes()) {
                MyLog.i("wen",pt.getMimeType());
                /*if (pt.getMimeType().contains("H264"))pt.enable(true);
                else pt.enable(false);*/

                int bitrate = core.getConfig().getInt("video", "codec_bitrate_limit", 600);
                if (pt.isVbr()) pt.setNormalBitrate(bitrate);
            }

            /*for (PayloadType pt : core.getAudioPayloadTypes()) {
                MyLog.i("wen",pt.getMimeType());
                if (pt.getMimeType().contains("PCM"))pt.enable(true);
                else pt.enable(true);
            }*/
        }
    }
    //帧率
    public void setVideoPreset(String preset) {
        if (core == null) return;
        if (preset.equals("default")) preset = null;
        core.setVideoPreset(preset);
        preset = core.getVideoPreset();
        if (!preset.equals("custom")) {
            core.setPreferredFramerate(0);
        }
    }

    private void acceptVideo(boolean b){
        if (core==null || mCall==null)return;
        CallParams callParams = core.createCallParams(mCall);
        mIsVideoCallModel = b;
        CallManager.getInstance().acceptCallVideo(b);
        updateInterfaceDependingOnVideo();
        isRequestVideoCall = false;
        normalDialog =null;

        callParams.enableVideo(b);
        mCall.update(callParams);
    }

    //传感器

    private void initSensor(){
        //息屏设置
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWakeLock = mPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                    getClass().getName());
        }
        //
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] == 0.0) {
            //贴近手机
            /*//设置免提
            audioManager.setSpeakerphoneOn(false);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);*/
            //关闭屏幕
            if (!mWakeLock.isHeld())
                mWakeLock.acquire(10*60*1000L /*10 minutes*/);

        } else {
            /*//离开手机
            audioManager.setMode(AudioManager.MODE_NORMAL);
            //设置免提
            audioManager.setSpeakerphoneOn(true);*/

            //唤醒设备
            if (mWakeLock.isHeld())
                mWakeLock.release();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
