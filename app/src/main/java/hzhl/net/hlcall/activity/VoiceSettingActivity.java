package hzhl.net.hlcall.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;

import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import org.linphone.core.Config;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.EcCalibratorStatus;
import org.linphone.core.PayloadType;
import org.linphone.mediastream.Log;

import butterknife.Bind;
import butterknife.OnClick;
import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.entity.SettingEntity;
import hzhl.net.hlcall.entity.SettingEntityDao;

import static android.media.AudioManager.STREAM_VOICE_CALL;

public class VoiceSettingActivity extends BaseActivity {
    @Bind(R.id.iv_echo_choose)
    ImageView ivEchoChoose;
    @Bind(R.id.iv_echo_adaptive_rate_control_choose)
    ImageView ivEchoARControlChoose;
    @Bind(R.id.tv_echo_jiaozheng_state)
    TextView tvEchoJiaoZhengState;
    @Bind(R.id.tv_echo_test_state)
    TextView tvEchoTestState;
    @Bind(R.id.tv_bitrate)
    TextView tvBitrate;
    private Core core;
    private SettingEntityDao settingEntityDao;
    private SettingEntity settingEntity;
    private boolean echoTesterIsRunning;
    private AudioManager mAudioManager;
    private boolean mAudioFocused;
    private Handler handler = new Handler();
    private int yourChoice;
    private String bitrateString;
    private static final int RECORD_AUDIO_RESULE = 113;
    private static final int RECORD_AUDIO_RESULE_2 = 114;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_voice_setting;
    }

    @Override
    protected String getTopTitle() {
        return null;
    }

    @Override
    protected void init() {
        setLeftTv("音频");
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (LinphoneService.isReady()) {
            core = LinphoneService.getCore();
        }
        if (core != null) {
            core.addListener(coreListenerStub);
            //设置setNormalBitrate 默认36
            int bitrate = core.getConfig().getInt("audio", "codec_bitrate_limit", 36);
            bitrateString = String.valueOf(bitrate);
            tvBitrate.setText(bitrateString);
            for (final PayloadType pt : core.getAudioPayloadTypes()) {
                if (pt.isVbr()) {
                    pt.setNormalBitrate(bitrate);
                }
            }
            ivEchoChoose.setSelected(core.echoCancellationEnabled());
            ivEchoARControlChoose.setSelected(core.adaptiveRateControlEnabled());
        }
    }

    CoreListenerStub coreListenerStub = new CoreListenerStub() {
        @Override
        public void onEcCalibrationResult(Core lc, EcCalibratorStatus status, int delayMs) {
            super.onEcCalibrationResult(lc, status, delayMs);
            if (mAudioManager != null) {
                mAudioManager.setSpeakerphoneOn(false);//关闭扬声
                mAudioManager.setMode(AudioManager.MODE_NORMAL);
                switch (status) {
                    case DoneNoEcho:
                        updateEchoJiaoZhengState("没有回音" + " 回音延迟:" + delayMs + "ms");
                        switchEchoCancellation(false);
                        break;
                    case Failed:
                        updateEchoJiaoZhengState("校正失败" );
                        switchEchoCancellation(true);
                        break;
                    case Done:
                        updateEchoJiaoZhengState("校正完成" + " 回音延迟:" + delayMs + "ms");
                        switchEchoCancellation(true);
                        break;
                }
            }
        }

        @Override
        public void onEcCalibrationAudioInit(Core lc) {
            super.onEcCalibrationAudioInit(lc);
            updateEchoJiaoZhengState("校正中...");
        }

        @Override
        public void onEcCalibrationAudioUninit(Core lc) {
            super.onEcCalibrationAudioUninit(lc);
        }
    };

    /**
     * 回音消除开关
     */
    private void switchEchoCancellation(boolean b) {
        if (core != null) {
            ivEchoChoose.setSelected(b);
            core.enableEchoCancellation(b);//回音消除
        }
    }

    /**
     * 自适应速率控制开关
     */
    private void switchEchoAdaptiveRateControl(boolean b) {
        if (core != null) {
            ivEchoARControlChoose.setSelected(b);
            core.enableAdaptiveRateControl(b);//自适应速率控制
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @OnClick({R.id.iv_echo_choose, R.id.iv_echo_adaptive_rate_control_choose, R.id.ll_jiaozheng
            , R.id.ll_echo_test, R.id.ll_bitrate})
    public void onClickVoid(View view) {
        switch (view.getId()) {
            case R.id.iv_echo_choose:
                if (core != null) {
                    if (view.isSelected()) {
                        switchEchoCancellation(false);
                    } else {
                        switchEchoCancellation(true);
                    }
                }
                break;
            case R.id.iv_echo_adaptive_rate_control_choose:
                if (core != null) {
                    if (view.isSelected()) {
                        switchEchoAdaptiveRateControl(false);
                    } else {
                        switchEchoAdaptiveRateControl(true);
                    }
                }
                break;
            case R.id.ll_jiaozheng:
                echoJiaoZheng();
                break;
            case R.id.ll_echo_test:
                echoTest();
                break;
            case R.id.ll_bitrate:
                showBitrateDialog();
                break;
        }
    }


    private void echoJiaoZheng() {
        if (isOpenPermission(Manifest.permission.RECORD_AUDIO)) {
            if (echoTesterIsRunning) {
                stopEchoTester();
            }
            if (core != null && mAudioManager != null) {
                mAudioManager.setSpeakerphoneOn(true);//开启扬声
                setAudioManagerInCallMode();
                Log.i("Set audio mode on 'Voice Communication'");
                requestAudioFocus(STREAM_VOICE_CALL);
                int oldVolume = mAudioManager.getStreamVolume(STREAM_VOICE_CALL);
                int maxVolume = mAudioManager.getStreamMaxVolume(STREAM_VOICE_CALL);
                mAudioManager.setStreamVolume(STREAM_VOICE_CALL, maxVolume, 0);
                core.startEchoCancellerCalibration();
                mAudioManager.setStreamVolume(STREAM_VOICE_CALL, oldVolume, 0);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}
                    , RECORD_AUDIO_RESULE);
        }
    }


    private void echoTest() {
        if (isOpenPermission(Manifest.permission.RECORD_AUDIO)) {
            if (echoTesterIsRunning) {
                stopEchoTester();
            } else {
                startEchoTester();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}
                    , RECORD_AUDIO_RESULE_2);
        }
    }

    public void startEchoTester() {
        if (core != null && mAudioManager != null) {
            mAudioManager.setSpeakerphoneOn(true);//开启扬声
            setAudioManagerInCallMode();
            Log.i("Set audio mode on 'Voice Communication'");
            requestAudioFocus(STREAM_VOICE_CALL);
            int maxVolume = mAudioManager.getStreamMaxVolume(STREAM_VOICE_CALL);
            int sampleRate = 44100;
            mAudioManager.setStreamVolume(STREAM_VOICE_CALL, maxVolume, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                String sampleRateProperty = mAudioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
                sampleRate = Integer.parseInt(sampleRateProperty);
            }
            core.startEchoTester(sampleRate);
            echoTesterIsRunning = true;
            updateEchoTestState("回音测试中...");
        }
    }

    public void stopEchoTester() {
        if (core != null && mAudioManager != null) {
            updateEchoTestState("测试已停止");
            int oldVolume = mAudioManager.getStreamVolume(STREAM_VOICE_CALL);
            echoTesterIsRunning = false;
            core.stopEchoTester();
            mAudioManager.setSpeakerphoneOn(false);//关闭扬声
            mAudioManager.setStreamVolume(STREAM_VOICE_CALL, oldVolume, 0);
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
            Log.i("Set audio mode on 'Normal'");
        }
    }

    public void setAudioManagerInCallMode() {
        if (mAudioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
            Log.w("[AudioManager] already in MODE_IN_COMMUNICATION, skipping...");
            return;
        }
        Log.d("[AudioManager] Mode: MODE_IN_COMMUNICATION");
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    private void requestAudioFocus(int stream) {
        if (!mAudioFocused) {
            int res = mAudioManager.requestAudioFocus(null, stream, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            Log.d("Audio focus requested: " + (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED ? "Granted" : "Denied"));
            if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) mAudioFocused = true;
        }
    }

    public void updateEchoTestState(final String state) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                tvEchoTestState.setText(state);
            }
        });

    }

    public void updateEchoJiaoZhengState(final String state) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                tvEchoJiaoZhengState.setText(state);
            }
        });

    }

    private void showBitrateDialog() {
        yourChoice = -1;
        final String items[] = {"10", "15", "20", "36", "64", "128"};
        int item = 3;
        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(bitrateString)) {
                item = i;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("编解码的比特率限制(kbits/s)");
        builder.setSingleChoiceItems(items, item,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        yourChoice = which;
                    }
                });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (core != null && yourChoice != -1) {
                    tvBitrate.setText(items[yourChoice]);
                    int bitrate = Integer.valueOf(items[yourChoice]);
                    core.getConfig().setInt("audio", "codec_bitrate_limit", bitrate);
                    for (final PayloadType pt : core.getAudioPayloadTypes()) {
                        if (pt.isVbr()) {
                            pt.setNormalBitrate(bitrate);
                        }
                    }
                }
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


    public Config getConfig() {
        if (core != null) {
            return core.getConfig();
        }
        return null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (echoTesterIsRunning) {
            stopEchoTester();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] paramArrayOfInt) {
        if (requestCode == RECORD_AUDIO_RESULE) {
            if (!verifyPermissions(paramArrayOfInt)) {
                //还有权限未申请
                showMissingPermissionDialog();
                // if (isNeedCheck) {checkPermissions(needPermissions);} 防止去检测权限，不停的弹框
                //isNeedCheck = false;
            } else {
                echoJiaoZheng();
            }
        } else {
            if (requestCode == RECORD_AUDIO_RESULE_2) {
                if (!verifyPermissions(paramArrayOfInt)) {
                    //还有权限未申请
                    showMissingPermissionDialog();
                    // if (isNeedCheck) {checkPermissions(needPermissions);} 防止去检测权限，不停的弹框
                    //isNeedCheck = false;
                } else {
                    echoTest();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (core!=null)core.removeListener(coreListenerStub);
    }

    //重写onKeyDown
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        android.util.Log.d(getClass().getName(), "onKeyDown:keyCode === " + keyCode);
        /** banlap：bug：实体键 拨号键和 挂机键不操作 */
        if (keyCode == KeyEvent.KEYCODE_CALL) {
            Logger.d("MainActivity不操作");
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_ENDCALL) {
            Logger.d("MainActivity不操作");
            return true;
        }
        /** banlap：bug：实体键 拨号键和 挂机键不操作  --end*/
        return super.onKeyDown(keyCode, event);
    }
}
