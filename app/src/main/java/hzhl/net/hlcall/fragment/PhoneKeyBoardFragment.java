package hzhl.net.hlcall.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;

import butterknife.Bind;
import butterknife.OnClick;
import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.MainActivity;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.activity.BaseFragment;
import hzhl.net.hlcall.activity.ContactsAddActivity;
import hzhl.net.hlcall.listener.CallNumChangeListener;
import hzhl.net.hlcall.utils.LinphoneUtils;
import hzhl.net.hlcall.utils.StringUtil;

public class PhoneKeyBoardFragment extends BaseFragment implements View.OnLongClickListener{
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

    private Core core;
    private AudioManager mAudioManager;
    private Vibrator mVibrator;
    private boolean mAudioFocused;
    private PopupWindow popupWindow;
    private View contentView;

    private boolean isClickKeyborad= false;  //是否点击键盘
    CallbackValue mCallbackValue;  //回调boolean返回activity
    private String mIsDeleteFlag = "";

    /*
    *  回调数据 监听
    * */
    private CallNumChangeListener callNumChangeListener;
    public void setCallNumChangeListener(CallNumChangeListener callNumChangeListener){
        this.callNumChangeListener = callNumChangeListener;
    }

    /*
     *  回调数据 接口
     * */
    public interface CallbackValue{
        public void isDeleteNum(boolean isDelete);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbackValue = (CallbackValue) getActivity();
    }

    @Override
    public boolean onLongClick(View v) {
        mTvNumber.setText("");
        return false;
    }


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_phone_key_board;
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


        mTvNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(callNumChangeListener != null){
                    callNumChangeListener.callNumChange(s.toString());
                }
            }
        });

        //得到从Activity传来的数据
        Bundle bundle =this.getArguments();
        String mess = null;
        if(bundle!=null){
            mess = bundle.getString("isDeleteNum");
            delNumberCallback();
        }
    }

    CoreListenerStub coreListenerStub = new CoreListenerStub() {
        @Override
        public void onRegistrationStateChanged(Core core, ProxyConfig proxyConfig, RegistrationState state, String message) {
            super.onRegistrationStateChanged(core, proxyConfig, state, message);
            if (core.getDefaultProxyConfig() == proxyConfig) {
            }
        }

    };

    @OnClick({R.id.iv_del, R.id.ll_0, R.id.ll_1, R.id.ll_2, R.id.ll_3, R.id.ll_4,
            R.id.ll_5, R.id.ll_6, R.id.ll_7, R.id.ll_8, R.id.ll_9, R.id.ll_star, R.id.ll_jing,
            R.id.ll_call_voice, R.id.ll_call_more,R.id.ll_call_sos})
    public void onClickVoid(View view) {
        switch (view.getId()) {
            case R.id.ll_call_voice:
                call();
                break;
            case R.id.iv_del:
                delNumber();
                break;
            case R.id.ll_call_more:
                String number = mTvNumber.getText().toString().trim();
                if (StringUtil.isEmpty(number) && !StringUtil.isNum(number)) {
                    showToast("请输入拨打号码");
                    return;
                }
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
            case R.id.ll_call_sos:
                mTvNumber.setText("B");
                call();
                break;
        }
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
     * 删除号码 回调boolean值
     */
    public void delNumberCallback() {
        boolean isDeleteAll;
        String number = mTvNumber.getText().toString();
        StringBuffer stringBuffer = new StringBuffer(number);
        int length = number.length();
        if (length >= 1) {
            stringBuffer.delete(length - 1, length);
            mTvNumber.setText(stringBuffer.toString());
            isDeleteAll =  false;
        } else {
            isDeleteAll =  true;
        }
        mCallbackValue.isDeleteNum(isDeleteAll);
    }



    public void onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(isClickKeyborad) {
                delNumber();
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
                call();
                break;
            case KeyEvent.KEYCODE_DEL:
                delNumber();
                break;
        }
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

        isClickKeyborad = true;
    }

    public void setCallNumberText(String number){
        mTvNumber.setText(number);
    }

    private void call() {
        String number = mTvNumber.getText().toString().trim();
        if (StringUtil.isEmpty(number) && !StringUtil.isNum(number)) {
            showToast("请输入拨打号码");
            return;
        }
        boolean isCalled = LinphoneUtils.call(getmContext(), number,false);
        if (isCalled) {
            mTvNumber.setText("");
        }
    }

    //回调拨打电话
    public void isCall() {
        String number = mTvNumber.getText().toString().trim();
        if (StringUtil.isEmpty(number) && !StringUtil.isNum(number)) {
            showToast("请输入拨打号码");
            return;
        }
        boolean isCalled = LinphoneUtils.call(getmContext(), number,false);
        if (isCalled) {
            mTvNumber.setText("");
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

        //banlap： 隐藏部分功能
        TextView tvCallSystem = contentView.findViewById(R.id.tv_call_system);
        TextView tvCall = contentView.findViewById(R.id.tv_call);
        tvCall.setVisibility(View.GONE);
        TextView tvCallVideo = contentView.findViewById(R.id.tv_call_video);
        tvCallVideo.setVisibility(View.GONE);
        TextView tvAddContact = contentView.findViewById(R.id.tv_add_contact);
        tvAddContact.setVisibility(View.GONE);
        TextView tvDismiss = contentView.findViewById(R.id.tv_dismiss);
        TextView tv_send_message = contentView.findViewById(R.id.tv_send_message);
        tv_send_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                LinphoneUtils.sendMessage(getmContext(), mTvNumber.getText().toString(),null);
                mTvNumber.setText("");
            }
        });


        tvCallSystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                boolean isCalled = LinphoneUtils.callInSystem(getmContext(), mTvNumber.getText().toString());
                if (isCalled) {
                    mTvNumber.setText("");
                }
            }
        });
        tvCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                LinphoneUtils.call(getmContext(), mTvNumber.getText().toString(), false);
                mTvNumber.setText("");
            }
        });
        tvCallVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                LinphoneUtils.call(getmContext(), mTvNumber.getText().toString(), true);
                mTvNumber.setText("");
            }
        });
        tvAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
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
}
