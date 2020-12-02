package hzhl.net.hlcall.activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import androidx.annotation.Nullable;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import org.linphone.core.Call;
import org.linphone.core.Conference;
import org.linphone.core.Core;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import hzhl.net.hlcall.App;
import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.adapter.MeetingContactsAdapter;
import hzhl.net.hlcall.adapter.MeetingContactsAdapter2;
import hzhl.net.hlcall.entity.ContactsEntity;
import hzhl.net.hlcall.entity.MeetingEntity;

public class MeetingActivity extends BaseActivity implements SensorEventListener {
    private MeetingEntity meetingEntity;
    private Core core;
    private MeetingContactsAdapter2 adapter;
    private AudioManager mAudioManager;
    @Bind(R.id.rec_meeting_contacts)
    RecyclerView rec_meeting_contacts;
    @Bind(R.id.ll_call_voice)
    LinearLayout ll_call_voice;
    @Bind(R.id.ll_call_end)
    LinearLayout ll_call_end;
    @Bind(R.id.ll_del)
    LinearLayout ll_del;
    @Bind(R.id.chronometer)
    Chronometer chronometer;

    @Bind(R.id.iv_speaker)
    ImageView iv_speaker;
    @Bind(R.id.iv_keep)
    ImageView iv_keep;
    @Bind(R.id.iv_del)
    ImageView iv_del;
    @Bind(R.id.iv_mute)
    ImageView iv_mute;
    @Bind(R.id.tv_title)
    TextView tv_title;
    private Sensor mSensor;
    private PowerManager.WakeLock mWakeLock;
    private SensorManager sensorManager;
    private PowerManager mPowerManager;
    private String name = "";
    private boolean mAudioFocused = false;
    private boolean isNew = false;

    //操作条监听
    private View.OnClickListener operateListener = v -> {
        if (v.isSelected()) {
            v.setSelected(false);
        }
        else v.setSelected(true);
        switch (v.getId()){
            case R.id.iv_speaker:
                toggleSpeaker();
                break;
            case R.id.iv_keep:
                pause(v.isSelected());
                break;
            case R.id.iv_del:
                if (adapter.getMode() == MeetingContactsAdapter.DEL_MODE)adapter.setMode(0);
                else adapter.setMode(MeetingContactsAdapter.DEL_MODE);
                break;
            case R.id.iv_mute:
                if (adapter.getMode() == MeetingContactsAdapter.MUTE_MODE)adapter.setMode(0);
                else adapter.setMode(MeetingContactsAdapter.MUTE_MODE);
                break;
        }
    };

    private boolean isStart = false;


    @Override
    protected int getLayoutResID() {
        return R.layout.activity_meeting;
    }

    @Override
    protected String getTopTitle() {
        return null;
    }

    @Override
    protected void init() {
        setRightIv(R.drawable.icon_tianjia);
        iv_speaker.setOnClickListener(operateListener);
        iv_del.setOnClickListener(operateListener);
        iv_keep.setOnClickListener(operateListener);
        iv_mute.setOnClickListener(operateListener);

        initSensor();


        Drawable drawable = getResources().getDrawable(R.drawable.icon_bianjiaaa);
        tv_title.post(()-> {

            //设置图片的上下左右的位置，也就是宽高
            drawable.setBounds(0, 0, tv_title.getHeight()/2, tv_title.getHeight()/2);
            //设置textView上下左右的图片
            tv_title.setCompoundDrawables(null,null,drawable,null);
        });

        tv_title.setOnClickListener(v -> {
            final EditText inputServer = new EditText(this);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("输入新名称")
                    .setView(inputServer)
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
            builder.setPositiveButton("确定", (dialog, which) -> {
                String text = inputServer.getText().toString();
                tv_title.setText(text);
                meetingEntity.setName(text);
                App.getDaoInstant().getMeetingEntityDao().update(meetingEntity);
            });

            builder.show();
        });



        meetingEntity = (MeetingEntity) getIntent().getSerializableExtra("data");
        List<ContactsEntity> contacts = meetingEntity.getContacts();
        setTopTitle(meetingEntity.getName());
        if (LinphoneService.isReady())core = LinphoneService.getCore();
        if (core == null ||core.getDefaultProxyConfig() == null) {
            showToast("没登录(选择)主用户");
            finish();
            return;
        }else {
            name = core.getDefaultProxyConfig().getIdentityAddress().getUsername();
        }
        rec_meeting_contacts.setLayoutManager(new GridLayoutManager(this,3));
        adapter = new MeetingContactsAdapter2(this,meetingEntity);
        //禁用item复用
        adapter.setHasStableIds(true);
        rec_meeting_contacts.setAdapter(adapter);
        adapter.setOnItemClickListener(new MeetingContactsAdapter2.OnRecyclerViewItemClickListener(){
            @Override
            public void onCallClick(ContactsEntity entity) {
                super.onCallClick(entity);
                start();
            }

            @Override
            public void onAddClick(ContactsEntity entity) {
                super.onAddClick(entity);
                onClickRightIv(null);
            }

            @Override
            public void onEnd() {
                end();
                terminate();
            }
        });


        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setSpeakerphoneOn(false);//默认关闭扬声器

        if (getIntent().getBooleanExtra("is_new",false)){
            ll_call_voice.performClick();
        }

    }

    @OnClick({R.id.ll_call_voice,R.id.ll_call_end,R.id.ll_del})
    @Override
    public void onClick(View v) {
        if (core == null)return;
        super.onClick(v);
        switch (v.getId()){
            case R.id.ll_call_voice:
                start();
                //core.leaveConference();
                adapter.initCall();

                /*CallParams params = core.createCallParams(null);
                CallManager.getInstance().mBandwidthManager.updateWithProfileSettings(params);
                params.enableVideo(false);
                core.inviteWithParams("8005",params);*/
                break;
            case R.id.ll_call_end:
                terminate();
                end();
                break;
            case R.id.ll_del:
                App.getDaoInstant().getMeetingEntityDao().delete(meetingEntity);
                terminate();
                finish();

                break;
        }


    }

    private void end() {
        setLeftBackVisible();
        ll_call_end.setVisibility(View.GONE);
        ll_call_voice.setVisibility(View.VISIBLE);
        ll_del.setVisibility(View.VISIBLE);
        chronometer.stop();
        isStart = false;
    }

    private void start() {
        if (core == null ||core.getDefaultProxyConfig() == null) {
            showToast("没登录(选择)主用户");
            return;
        }else if (!core.isNetworkReachable()){
            showToast("网络连接失败");
            return;
        }
        requestAudioFocus(AudioManager.STREAM_VOICE_CALL);
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        mAudioManager.setSpeakerphoneOn(false);
        //core.enableEchoCancellation(false);

        setLeftBackGone();
        updateCallTime();
        ll_call_voice.setVisibility(View.GONE);
        ll_del.setVisibility(View.GONE);
        ll_call_end.setVisibility(View.VISIBLE);
        isStart = true;
    }






    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isStart)return false;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onClickRightIv(View view) {
        if (view != null)super.onClickRightIv(view);
        Intent intent = new Intent(this, MeetingAddActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("meeting_id",meetingEntity.getId());
        bundle.putSerializable("name",name);
        intent.putExtras(bundle);
        startActivityForResult(intent,1001);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1001){
            meetingEntity = App.getDaoInstant().getMeetingEntityDao().load(meetingEntity.getId());
            adapter.upDateCall(isStart,meetingEntity);
        }
    }

    private void toggleSpeaker() {
        if (mAudioManager.isSpeakerphoneOn()) {
            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            mAudioManager.setSpeakerphoneOn(false);
        } else {
            //mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            mAudioManager.setSpeakerphoneOn(true);
        }

        iv_speaker.setSelected(mAudioManager.isSpeakerphoneOn());
    }

    public void updateCallTime() {
        // Be a good citizen.  Make sure UI changes fire on the UI thread.
        runOnUiThread(() -> {
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
        });
    }

    public void terminate(){

        if (core == null)return;
        Conference conference = core.getConference();
        if (conference == null)return;
        for (Call c:core.getCalls()
        ) {
            conference.removeParticipant(c.getRemoteAddress());
            c.terminate();
        }
        core.terminateConference();
        core.leaveConference();
        chronometer.stop();
    }
    private void pause(boolean isPause){
        if (core == null)return;

        for (Call c:core.getCalls()) {
            if (isPause)c.pause();
            else c.resume();
        }

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
    protected void onResume() {
        super.onResume();
        //注册传感器,先判断有没有传感器
        if (mSensor != null)
            sensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        //传感器取消监听
        if (mSensor!=null)
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter!=null)adapter.removeCoreListener();
        if (core != null)terminate();

        //传感器取消监听
        sensorManager.unregisterListener(this);
        //释放息屏
        if (mWakeLock.isHeld())
            mWakeLock.release();
        mWakeLock = null;
        mPowerManager = null;

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

}
