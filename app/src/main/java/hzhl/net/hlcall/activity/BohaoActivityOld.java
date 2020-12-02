package hzhl.net.hlcall.activity;

import android.Manifest;
import android.app.AlertDialog;
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
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import androidx.core.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.PayloadType;
import org.linphone.core.Reason;
import org.linphone.core.tools.Log;
import org.linphone.mediastream.video.capture.CaptureTextureView;

import butterknife.Bind;
import butterknife.OnClick;
import hzhl.net.hlcall.ActivityCollector;
import hzhl.net.hlcall.CallManager;
import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.MainActivity;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.utils.ContactsUtil;
import hzhl.net.hlcall.utils.FileUtils;
import hzhl.net.hlcall.utils.LinphoneUtils;
import hzhl.net.hlcall.utils.MyLog;
import hzhl.net.hlcall.utils.StringUtil;

import static android.media.AudioManager.STREAM_VOICE_CALL;
import static hzhl.net.hlcall.activity.RecordsActivity.WRITE_EXTERNAL_STORAGE_RESULT;

public class BohaoActivityOld extends BaseActivity implements SensorEventListener {
    @Bind(R.id.tv_call_number)
    TextView mTvCallNumber;
    @Bind(R.id.tv_state)
    TextView mTvState;
    @Bind(R.id.tv_mianti)
    TextView mTvMianti;
    @Bind(R.id.tv_mute)
    TextView mTvMute;
    @Bind(R.id.chronometer)
    Chronometer mChronometer;
    @Bind(R.id.tv_describe)
    TextView tvDescribe;
    @Bind(R.id.ll_other)
    LinearLayout llOther;
    @Bind(R.id.ll_number)
    LinearLayout llNumber;
    @Bind(R.id.tv_hide)
    TextView tvHide;
    @Bind(R.id.tv_to)
    TextView tvTo;
    @Bind(R.id.iv_del)
    ImageView ivDel;
    @Bind(R.id.tv_record)
    TextView tvRecord;

    @Bind(R.id.tv_video)
    TextView tvVideo;
    @Bind(R.id.iv_switch_camera)
    ImageView ivSwitchCamera;
    @Bind(R.id.iv_close_bohao)
    ImageView iv_close_bohao;
    @Bind(R.id.local_preview_texture)
    CaptureTextureView mLocalPreview;
    @Bind(R.id.remote_video_texture)
    TextureView mRemoteVideo;

    @Bind(R.id.call_menu_layout)
    LinearLayout mMenuLayout;
    @Bind(R.id.rl_bohao_top)
    RelativeLayout rl_bohao_top;


    private static final int CAMERA_TO_TOGGLE_VIDEO = 121;


    private String number;
    private Handler handler = new Handler();
    private Call mCall;
    private CoreListenerStub mListener;
    private Core core;
    private AudioManager mAudioManager;
    private boolean mAudioFocused;//是否获取音频焦点

    //默认清晰度
    /*private int videoWith = 600;
    private int videoHeight = 840;*/
    private int videoWith = 600;
    private int videoHeight = 840;
    private int lastClickTime = 0;
    boolean isRequestVideoCall;
    private AlertDialog.Builder normalDialog;//对话框,防止多次弹出
    boolean mIsVideoCallModel = false;
    boolean mIsConnected = false;
    private OrientationEventListener mOrientationListener;
    private int mainOrientation = 0;

    //传感器
    private Sensor mSensor;
    private PowerManager.WakeLock mWakeLock;
    private SensorManager sensorManager;
    private PowerManager mPowerManager;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_bohao_new;
    }

    @Override
    protected String getTopTitle() {
        return null;
    }

    @Override
    protected void init() {
        ActivityCollector.addActivity(this);
        setCommonTitleBarGone();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        //获取壁纸图片
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        getWindow().setBackgroundDrawable(wallpaperDrawable);

        initSensor();
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        /*mOrientationListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {

            @Override
            public void onOrientationChanged(int orientation) {
                MyLog.d("wen",
                        "Orientation changed to " + orientation);

                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                    return;  //手机平放时，检测不到有效的角度
                }
                //只检测是否有四个角度的改变
                if (orientation > 350 || orientation < 10) { //0度
                    if (mainOrientation !=0) {
                        mainOrientation = 0;

                    }
                } else if (orientation > 80 && orientation < 100) { //90度
                    mainOrientation = 90;
                } else if (orientation > 170 && orientation < 190) { //180度
                    mainOrientation = 180;
                } else if (orientation > 260 && orientation < 280) { //270度
                    mainOrientation = 270;
                } else {
                    return;
                }
            }
        };

        if (mOrientationListener.canDetectOrientation()) {
            MyLog.d("wen", "Can detect orientation");
            mOrientationListener.enable();
        } else {
            MyLog.d("wen", "Cannot detect orientation");
            mOrientationListener.disable();
        }*/




        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setSpeakerphoneOn(false);//默认关闭扬声器
        mListener = new CoreListenerStub() {
            @Override
            public void onDtmfReceived(Core lc, Call call, int dtmf) {
                super.onDtmfReceived(lc, call, dtmf);
                MyLog.d("wen "+ (char)dtmf);
                lc.playDtmf((char) dtmf,500);
            }

            @Override
            public void onCallStateChanged(
                    Core core, Call call, Call.State state, String message) {

            /*Logger.d("state:" + state +"  getCurrentParams: "
                    + call.getCurrentParams().videoEnabled()
                    + "  getRemoteParams:"+call.getRemoteParams().videoEnabled()
                    + "  message:"+ message
            );  */

                if (state == Call.State.End || state == Call.State.Error) {
                    // Convert Core message for internalization
                    if (call.getErrorInfo().getReason() == Reason.Declined) {
                        //没有通话时,显示状态
                        if (core.getCallsNb() == 0) {
                            updateStatus(getString(R.string.error_call_declined));
                        }
                        // showToast(getString(R.string.error_call_declined));
                    } else if (call.getErrorInfo().getReason() == Reason.NotFound) {
                        if (core.getCallsNb() == 0) {
                            updateStatus(getString(R.string.error_user_not_found));
                        }
                        // showToast(getString(R.string.error_user_not_found));
                    } else if (call.getErrorInfo().getReason() == Reason.NotAcceptable) {
                        if (core.getCallsNb() == 0) {
                            updateStatus(getString(R.string.error_incompatible_media));
                        }
                        //  showToast(getString(R.string.error_incompatible_media));
                    } else if (call.getErrorInfo().getReason() == Reason.Busy) {
                        if (core.getCallsNb() == 0) {
                            updateStatus(getString(R.string.error_user_busy));
                        }
                        //   showToast(getString(R.string.error_user_busy));
                    } else {
                        if (core.getCallsNb() == 0) {
                            updateStatus("已挂机");
                        }
                    }
                    /*else if (message != null) {
                        showToast(getString(R.string.error_unknown) + " - " + message);
                    }*/
                    if (core.getCallsNb() == 0) {
                        if (mAudioFocused) {
                            int res = mAudioManager.abandonAudioFocus(null);
                            Log.d("[Audio Manager] Audio focus released a bit later: "
                                    + (res
                                    == AudioManager
                                    .AUDIOFOCUS_REQUEST_GRANTED
                                    ? "Granted"
                                    : "Denied"));
                            mAudioFocused = false;
                        }
                        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                        if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                            mAudioManager.setMode(AudioManager.MODE_NORMAL);
                            //  Logger.d("[Audio Manager] 所有呼叫终止，返回听筒");
                            mAudioManager.setSpeakerphoneOn(false);
                        }

                        if (mIsConnected)finishDelay();
                    }
                } else if (state == Call.State.Connected) {
                    Logger.d("connected");
                    if (core.getCallsNb() == 1) {
                        // It is for incoming calls, because outgoing calls enter
                        // MODE_IN_COMMUNICATION immediately when they start.
                        // However, incoming call first use the MODE_RINGING to play the
                        // local ring.
                        if (call.getDir() == Call.Dir.Outgoing) {
                            mIsConnected = true;
                            updateCallTime();
                            updateStatus("通话中");
                        }

                        /*在接通状态下，必须查看来电的远程参数，确定其带有视频通话的参数才打开视频通话，否则只进行语音通话*/
                        CallParams remoteParams = call.getRemoteParams();
                        android.util.Log.d("wen", "remoteParams: " +remoteParams.videoEnabled());

                            if (!remoteParams.videoEnabled()){
                                mIsVideoCallModel = false;
                                isRequestVideoCall = false;
                            }
                            else if (isRequestVideoCall) {
                            mIsVideoCallModel = true;
                            isRequestVideoCall = false;
                        }
                            updateInterfaceDependingOnVideo();

                    }
                }else if(state == Call.State.StreamsRunning){
                    /*CallParams params = call.getRemoteParams();
                    if (params.videoEnabled() && isRequestVideoCall){
                        mIsVideoCallModel = true;
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
                        //mCall.deferUpdate();
                        MyLog.i("getRemoteParams: " + params.videoEnabled()
                        +"  isRequestVideoCall :"+isRequestVideoCall
                        +"  mIsVideoCallModel :"+mIsVideoCallModel
                        );
                        boolean videoEnabled = params.videoEnabled();
                        if (!videoEnabled){
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
                if (state == Call.State.OutgoingInit) {
                    // Enter the MODE_IN_COMMUNICATION mode as soon as possible, so that
                    // ringback is heard normally in earpiece or bluetooth receiver.
                    mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    requestAudioFocus(STREAM_VOICE_CALL);
                }

              /*  if (core.getCallsNb() == 0) {
                    finishDelay();
                }*/
            }
        };
        if (LinphoneService.isReady()) {
            core = LinphoneService.getCore();
        }
        if (core != null) {
            core.addListener(mListener);
        }
        initiateCall();

        setButtonEnable(false);
    }

    //TODO 视频通话默认true
    private void acceptCallVideo(boolean accept) {
        CallManager.getInstance().acceptCallVideo(accept);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void requestAudioFocus(int stream) {
        if (!mAudioFocused) {
            int res = mAudioManager.requestAudioFocus(
                    null, stream, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
            Log.d("[Audio Manager] Audio focus requested: "
                    + (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
                    ? "Granted同意"
                    : "Denied拒绝"));
            if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) mAudioFocused = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //保持常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //注册传感器,先判断有没有传感器
        if (mSensor != null)
            sensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        if (LinphoneService.isReady()) {
            core = LinphoneService.getCore();
        }
        if (core != null) {
            core.setNativePreviewWindowId(mLocalPreview);
            core.setNativeVideoWindowId(mRemoteVideo);
            core.addListener(mListener);
            //wenyeyang
            setStatusBarFullTransparent();
            //带宽
            /*core.setExpectedBandwidth(2000);
            core.enableAdaptiveRateControl(false);*/
            //core.getPreferredVideoDefinition().setHeight(videoHeight);
            //core.getPreferredVideoDefinition().setWidth(videoWith);
            Matrix matrix = new Matrix();
            //视频缩放
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            float dx = 1;
            float dy = 1;
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
            findViewById(R.id.ll_call_bohao).setOnClickListener(v -> {
               switchMenuPanel();
            });
            //帧率
            setVideoPreset("high-fps");
            //编码
            populateVideoCodecs();

        }
        mCall = null;
        // Only one call ringing at a time is allowed
        if (core != null) {
            for (Call call : core.getCalls()) {
                Call.State cstate = call.getState();
                Logger.d(cstate.name());
                if (Call.State.OutgoingInit == cstate
                        || Call.State.OutgoingProgress == cstate
                        || Call.State.OutgoingRinging == cstate
                        || Call.State.OutgoingEarlyMedia == cstate
                        || Call.State.Connected == call.getState()
                        || Call.State.StreamsRunning == call.getState()) {
                    mCall = call;
                    break;
                }
            }
        }
        if (mCall == null) {
            Logger.e("Couldn't find outgoing call");
            finishDelay();
            // jump(MainActivity.class);
            return;
        }
        if (Call.State.OutgoingInit == mCall.getState()
                || Call.State.OutgoingProgress == mCall.getState()
                || Call.State.OutgoingRinging == mCall.getState()
                || Call.State.OutgoingEarlyMedia == mCall.getState()) {

            Address address = mCall.getRemoteAddress();
            Logger.d("拨号：" + address.getUsername() + "@" + address.getDomain());
            updateStatus("拨号中...");
        }
    }

    @Override
    protected void onPause() {
        if (LinphoneService.isReady()) {
            core = LinphoneService.getCore();
        }
        if (core != null) {
            core.setNativeVideoWindowId(null);
            core.setNativePreviewWindowId(null);
            core.removeListener(mListener);
        }
        super.onPause();

        //传感器取消监听
        if (mSensor!=null)
            sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // showToast("拨号关闭");
        if (mCall != null) {
            mCall.terminate();
        }
        if (core!=null)core.removeListener(mListener);
        ActivityCollector.removeActivity(this);
        mCall = null;
        mListener = null;
        mChronometer.stop();
        mAudioManager.abandonAudioFocus(null);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @OnClick({R.id.iv_close_bohao, R.id.tv_mute, R.id.tv_keyboard, R.id.tv_mianti, R.id.tv_record
            , R.id.ll_call_bohao, R.id.rl_bohao_top, R.id.tv_hide, R.id.tv_to, R.id.tv_transfer, R.id.iv_del
            , R.id.ll_0, R.id.ll_1, R.id.ll_2, R.id.ll_3, R.id.ll_4
            , R.id.ll_5, R.id.ll_6, R.id.ll_7, R.id.ll_8, R.id.ll_9, R.id.ll_star, R.id.ll_jing,
            R.id.iv_switch_camera, R.id.tv_video, R.id.tv_more})
    public void onClickVoid(View view) {
        switch (view.getId()) {
            case R.id.iv_close_bohao:
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
                tvHide.setText("返回");
                llNumber.setVisibility(View.VISIBLE);
                llOther.setVisibility(View.GONE);
                tvHide.setVisibility(View.VISIBLE);
                tvTo.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_keyboard:
                tvHide.setText("隐藏");
                llNumber.setVisibility(View.VISIBLE);
                llOther.setVisibility(View.GONE);
                tvHide.setVisibility(View.VISIBLE);
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
            case R.id.iv_switch_camera:
                CallManager.getInstance().switchCamera();
                break;
            case R.id.tv_video:
                if(!mIsConnected){
                    return;
                }
                if (checkAndRequestPermission(Manifest.permission.CAMERA, CAMERA_TO_TOGGLE_VIDEO)) {
                    LinphoneUtils.reloadVideoDevices();
                    toggleVideo();
                }
                break;
            case R.id.tv_more:
                switchMenuPanel();
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
            try {
                Call call = core.getCurrentCall();
                if (call == null) return;
                if (mIsVideoCallModel) {
                    CallManager.getInstance().removeVideo();
                    //ivSwitchCamera.setVisibility(View.GONE);
                    //mMenuLayout.setVisibility(View.VISIBLE);
                    mIsVideoCallModel = false;
                    tvVideo.setSelected(false);
                    isRequestVideoCall = false;
                } else if (!isRequestVideoCall){
                    CallManager.getInstance().addVideo();
                    //ivSwitchCamera.setVisibility(View.VISIBLE);
                    //mMenuLayout.setVisibility(View.GONE);
                    //mIsVideoCallModel = true;
                    tvVideo.setSelected(true);
                    isRequestVideoCall = true;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            updateInterfaceDependingOnVideo();
        }


    }

    /**
     * 打电话
     */
    public void initiateCall() {
        Intent intent = getIntent();
        number = intent.getStringExtra("number");
        isRequestVideoCall = intent.getBooleanExtra("isVideoCall", false);
        if (number == null) {
            showToast("号码为空");
            return;
        }
        mTvCallNumber.setText(number);
        if (!LinphoneService.isReady()) {
            showToast("电话服务未准备好");
            return;
        }
        core = LinphoneService.getCore();
        if (core != null) {
            if (core.getDefaultProxyConfig() == null) {
                showToast("没登录(选择)主用户");
                return;
            }
            if (core.getDefaultProxyConfig().getIdentityAddress().getUsername().equals(number)) {
                showToast("不能给自己打电话");
                return;
            }
        }
        if (core != null) {
            Address addressToCall = core.getDefaultProxyConfig().normalizeSipUri(number);
           /* Logger.d(addressToCall.getDomain() + "user:" + addressToCall.getUsername()
                    + ";port:" + addressToCall.getPort());*/

            mTvCallNumber.setText(ContactsUtil.getNameFormNumber(this,addressToCall.getUsername()));
            CallParams params = core.createCallParams(null);
            CallManager.getInstance().mBandwidthManager.updateWithProfileSettings(params);
            if(isRequestVideoCall){
                params.enableVideo(true);
                core.enableVideoCapture(true);
                core.enableVideoDisplay(true);
                //wenyeyang
                params.enableLowBandwidth(false);
                //params.setAudioBandwidthLimit(1000);
                core.setUploadBandwidth(600);
                core.setDownloadBandwidth(600);
                LinphoneUtils.reloadVideoDevices();
            }else{
                params.enableVideo(false);
            }
            if (addressToCall != null) {
                String recordFile = FileUtils.getCallRecordingFilename(this, addressToCall);
                params.setRecordFile(recordFile);
                core.inviteAddressWithParams(addressToCall, params).deferUpdate();
            }


        }
    }

    private void updateInterfaceDependingOnVideo() {
        Call call = core.getCurrentCall();
        if (call == null) {
            showVideoControls(false);
            return;
        }
        //boolean videoEnabled = call.getCurrentParams().videoEnabled();
        showVideoControls(mIsVideoCallModel);
    }

    private void showVideoControls(boolean videoEnabled) {
        mRemoteVideo.setVisibility(videoEnabled ? View.VISIBLE : View.GONE);
        mLocalPreview.setVisibility(videoEnabled ? View.VISIBLE : View.GONE);
        ivSwitchCamera.setVisibility(videoEnabled ? View.VISIBLE : View.INVISIBLE);
        tvVideo.setSelected(isRequestVideoCall);
        setVideoEnabled(mIsVideoCallModel);
    }

    private void transfer() {
        if (mCall != null) {
            String number = tvDescribe.getText().toString().trim();
            if (StringUtil.isEmpty(number)) {
                showToast("请输入转移号码");
                return;
            }
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
     * 终止通话
     */
    private void terminate() {
        if (mCall != null) {
            updateStatus("挂断");
            jump(MainActivity.class);
            mCall.terminate();
            finish();
        }

    }

    private void toggleSpeaker() {
        if (mAudioManager.isSpeakerphoneOn()) {
            mAudioManager.setMode(AudioManager.MODE_IN_CALL);
            mAudioManager.setSpeakerphoneOn(false);
        } else {
            //mAudioManager.setMode(AudioManager.MODE_NORMAL);
            mAudioManager.setSpeakerphoneOn(true);
        }
        mTvMianti.setSelected(mAudioManager.isSpeakerphoneOn());
    }

    public void updateStatus(final String status) {
        // Be a good citizen.  Make sure UI changes fire on the UI thread.
        handler.post(new Runnable() {
            public void run() {
                mTvState.setText(status);
            }
        });
    }

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
                    iv_close_bohao.setVisibility(View.INVISIBLE);
                }

                if (mCall!=null){

                }



            });

            setButtonEnable(true);

        });
    }

    public void setButtonEnable(Boolean f){
        findViewById(R.id.tv_mute).setEnabled(f);
        tvRecord.setEnabled(f);
        tvVideo.setEnabled(f);
        findViewById(R.id.tv_mianti).setEnabled(f);
        findViewById(R.id.tv_transfer).setEnabled(f);

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
        }
    }

    private void playDtmf(String s) {
        if (core != null) {
            char c = s.subSequence(0, 1).charAt(0);
            core.playDtmf(c, 500);
            core.getCurrentCall().sendDtmf(c);
        }
    }


    private void finishDelay() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Logger.d("延迟关闭");
                finish();
              //  jump(MainActivity.class);
            }
        }, 1500);
        //}, 200);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Logger.d("BohaoActivity返回");
            Intent home = new Intent(Intent.ACTION_MAIN);
            //  home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void switchMenuPanel(){
        lastClickTime  = 0;
        if(mIsVideoCallModel){
            int visibility = llOther.getVisibility();
            llOther.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
            visibility = iv_close_bohao.getVisibility();
            iv_close_bohao.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);

        }else {
            llOther.setVisibility(View.VISIBLE);
            iv_close_bohao.setVisibility(View.VISIBLE);
        }
    }

    private void setVideoEnabled(Boolean mIsVideo){
        runOnUiThread(() -> {
                if (mIsVideo != null && mIsVideo) {
                    llOther.setVisibility(View.GONE);
                    //mIsVideoCallModel = true;
                    tvVideo.setSelected(true);
                    rl_bohao_top.setGravity(Gravity.LEFT);
                    //LinphoneUtils.reloadVideoDevices();
                    //CallManager.getInstance().acceptCallVideo(true);
                } else {
                    //CallManager.getInstance().acceptCallVideo(false);
                    llOther.setVisibility(View.VISIBLE);
                    iv_close_bohao.setVisibility(View.VISIBLE);
                    //mIsVideoCallModel = false;
                    tvVideo.setSelected(false);
                    rl_bohao_top.setGravity(Gravity.CENTER);
                }
        });
    }

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
        normalDialog =
                new AlertDialog.Builder(this);
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
    //编码,只使用H264
    private void populateVideoCodecs() {
        if (core != null) {
            for (PayloadType pt : core.getVideoPayloadTypes()) {
                MyLog.i("wen",pt.getMimeType());
                if (pt.getMimeType().contains("H264"))pt.enable(true);
                else pt.enable(false);
            }
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

}
