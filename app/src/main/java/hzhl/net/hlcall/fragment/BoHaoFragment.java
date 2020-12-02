package hzhl.net.hlcall.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import androidx.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;

import butterknife.Bind;
import butterknife.OnClick;
import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.activity.BaseFragment;
import hzhl.net.hlcall.activity.ContactsAddActivity;
import hzhl.net.hlcall.activity.SipSettingActivity;
import hzhl.net.hlcall.utils.LinphoneUtils;
import hzhl.net.hlcall.utils.StringUtil;

/**
 * Created by guang on 2019/7/29.
 */

public class BoHaoFragment extends BaseFragment implements View.OnLongClickListener {
    @Bind(R.id.tv_number)
    TextView mTvNumber;
    @Bind(R.id.iv_del)
    ImageView mIvDel;
    @Bind(R.id.ll_0)
    LinearLayout m0;
    @Bind(R.id.ll_1)
    LinearLayout m1;
    @Bind(R.id.ll_2)
    LinearLayout m2;
    @Bind(R.id.ll_3)
    LinearLayout m3;
    @Bind(R.id.ll_4)
    LinearLayout m4;
    @Bind(R.id.ll_5)
    LinearLayout m5;
    @Bind(R.id.ll_6)
    LinearLayout m6;
    @Bind(R.id.ll_7)
    LinearLayout m7;
    @Bind(R.id.ll_8)
    LinearLayout m8;
    @Bind(R.id.ll_9)
    LinearLayout m9;
    @Bind(R.id.ll_star)
    LinearLayout mStar;
    @Bind(R.id.ll_jing)
    LinearLayout mJing;
    @Bind(R.id.tv_online)
    TextView mTvOnline;

    private boolean isSipBohaoType = true;//是否sip方式拨号
    private Core core;

    private AudioManager mAudioManager;
    private Vibrator mVibrator;
    private boolean mAudioFocused;
    private PopupWindow popupWindow;
    private View contentView;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_bohao;
    }

    @Override
    protected void initView() {
        if (LinphoneService.isReady()) {
            core = LinphoneService.getCore();
            if (core != null) {
                core.addListener(coreListenerStub);
            }
        }

        mAudioManager = (AudioManager) getmContext().getSystemService(Context.AUDIO_SERVICE);
        mVibrator = (Vibrator) getmContext().getSystemService(Context.VIBRATOR_SERVICE);
        mIvDel.setOnLongClickListener(this);
        showPopwindow();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateLoginState();
    }

    public void updateLoginState() {
        if (LinphoneService.isReady()) {
            ProxyConfig proxyConfig = LinphoneService.getCore().getDefaultProxyConfig();
            if (proxyConfig == null) {
                mTvOnline.setText("未选择");
            } else {
                mTvOnline.setText(proxyConfig.getIdentityAddress().getUsername() + getStatus(proxyConfig.getState()));
                mTvOnline.setSelected(proxyConfig.getState() == RegistrationState.Ok);
            }
        }
    }

    private String getStatus(RegistrationState state) {
        if (state == RegistrationState.Ok) {
            return "已登录";
        } else if (state == RegistrationState.Progress) {
            return "登录中";
        } else if (state == RegistrationState.Failed) {
            return "登录失败";
        } else {
            return "未登录";
        }
    }

    CoreListenerStub coreListenerStub = new CoreListenerStub() {
        @Override
        public void onRegistrationStateChanged(Core core, ProxyConfig proxyConfig, RegistrationState state, String message) {
            super.onRegistrationStateChanged(core, proxyConfig, state, message);
            if (core.getDefaultProxyConfig() == proxyConfig) {
                mTvOnline.setText(proxyConfig.getIdentityAddress().getUsername() + getStatus(proxyConfig.getState()));
                mTvOnline.setSelected(state == RegistrationState.Ok);
            }
        }

    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @OnClick({R.id.tv_online, R.id.iv_del, R.id.ll_0, R.id.ll_1, R.id.ll_2, R.id.ll_3, R.id.ll_4,
            R.id.ll_5, R.id.ll_6, R.id.ll_7, R.id.ll_8, R.id.ll_9, R.id.ll_star, R.id.ll_jing,
            R.id.ll_call_voice, R.id.iv_more})
    public void onClickVoid(View view) {
        switch (view.getId()) {
            case R.id.ll_call_voice:
                call();
                break;
            case R.id.tv_online:
                startActivity(new Intent(getmContext(), SipSettingActivity.class));
                break;
            case R.id.iv_del:
                delNumber();
                break;
            case R.id.iv_more:
                popupWindow.showAtLocation(contentView, Gravity.BOTTOM, 0, 0);
                WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                lp.alpha = 0.5f;
                getActivity().getWindow().setAttributes(lp);
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
        }
    }

    @Override
    public boolean onLongClick(View view) {
        mTvNumber.setText("");
        return false;
    }

    private void call() {
        String number = mTvNumber.getText().toString().trim();
        if (StringUtil.isEmpty(number) && !StringUtil.isNum(number)) {
            showToast("请输入拨打号码");
            return;
        }
        boolean isCalled = LinphoneUtils.call(getmContext(), number, false);
        if (isCalled) {
            mTvNumber.setText("");
        }
    }


    private void playDtmf(String s) {
        if (core != null) {
            boolean mDTMFToneEnabled = Settings.System.getInt(getmContext().getContentResolver(),
                    Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;
            int ringerMode = mAudioManager.getRingerMode();
            if (mDTMFToneEnabled) {
                if ((ringerMode == AudioManager.RINGER_MODE_SILENT) || (ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {
                    // 静音或者震动时不发出按键声音
                    // 等待3秒，震动3秒，从第0个索引开始，一直循环
                    if (mVibrator != null) {
                        mVibrator.vibrate(100);
                    }
                    return;
                }
                mAudioManager.setSpeakerphoneOn(true);
               /* mAudioManager.setMode(MODE_RINGTONE);
                requestAudioFocus(STREAM_RING);*/
                char c = s.subSequence(0, 1).charAt(0);
                core.playDtmf(c, 100);
            }
        }
    }

    private void requestAudioFocus(int stream) {
        if (!mAudioFocused) {
            int res = mAudioManager.requestAudioFocus(
                    null, stream, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
         /*   Logger.d(
                    "[Audio Manager] Audio focus requested: "
                            + (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
                            ? "Granted"
                            : "Denied"));*/
            if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) mAudioFocused = true;
        }
    }

    @Override
    public void onDestroy() {
        if (core != null) {
            core.removeListener(coreListenerStub);
        }
        core = null;
        if (mVibrator != null) {
            mVibrator.cancel();
        }
        super.onDestroy();
    }

    /**
     * 添加号码
     */
    public void addNumber(String s) {
        playDtmf(s);
        String number = mTvNumber.getText().toString();
        StringBuffer stringBuffer = new StringBuffer(number);
        stringBuffer.append(s);
        mTvNumber.setText(stringBuffer.toString());
    }

    /**
     * 删除号码
     */
    public void delNumber() {
        String number = mTvNumber.getText().toString();
        StringBuffer stringBuffer = new StringBuffer(number);
        int length = number.length();
        if (length >= 1) {
            stringBuffer.delete(length - 1, length);
            mTvNumber.setText(stringBuffer.toString());
        }
    }

    /**
     * 显示popupWindow
     */
    private void showPopwindow() {
        //加载弹出框的布局
        contentView = LayoutInflater.from(getmContext()).inflate(
                R.layout.popup_call_more_type, null);
        popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);// 取得焦点
        //注意  要是点击外部空白处弹框消息  那么必须给弹框设置一个背景色  不然是不起作用的
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        //点击外部消失
        popupWindow.setOutsideTouchable(true);
        //设置可以点击
        popupWindow.setTouchable(true);
        //进入退出的动画，指定刚才定义的style
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);

        // 按下android回退物理键 PopipWindow消失解决
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                        lp.alpha = 1f;
                        getActivity().getWindow().setAttributes(lp);
                    }
                }, 300);
            }
        });

        TextView tvCallSystem = contentView.findViewById(R.id.tv_call_system);
        TextView tvCall = contentView.findViewById(R.id.tv_call);
        TextView tvCallVideo = contentView.findViewById(R.id.tv_call_video);
        TextView tvAddContact = contentView.findViewById(R.id.tv_add_contact);
        TextView tvDismiss = contentView.findViewById(R.id.tv_dismiss);
        tvCallSystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isCalled = LinphoneUtils.callInSystem(getmContext(), mTvNumber.getText().toString());
                if (isCalled) {
                    mTvNumber.setText("");
                }
            }
        });
        tvCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinphoneUtils.call(getmContext(), mTvNumber.getText().toString(), false);
                mTvNumber.setText("");
            }
        });
        tvCallVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        tvAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getmContext(), ContactsAddActivity.class);
                intent.putExtra("number", mTvNumber.getText().toString());
                startActivity(intent);
            }
        });
        tvDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
    }


}
