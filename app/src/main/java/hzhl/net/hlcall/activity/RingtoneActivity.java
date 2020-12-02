package hzhl.net.hlcall.activity;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import org.linphone.core.Core;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.adapter.RingtoneListAdapter;
import hzhl.net.hlcall.entity.RingToneEntity;
import hzhl.net.hlcall.utils.RingToneUtil;

import static android.media.AudioManager.STREAM_MUSIC;

public class RingtoneActivity extends BaseActivity implements RingtoneListAdapter.OnRecyclerViewItemClickListener {
    @Bind(R.id.recy_ringtone)
    RecyclerView mRecyclerView;
    @Bind(R.id.iv_choose_default)
    ImageView ivChooseDefault;
    @Bind(R.id.tv_default)
    TextView tvDefault;
    @Bind(R.id.ll_default)
    LinearLayout llDefault;
    private Core core;
    private MediaPlayer mRingerPlayer;
    private AudioManager mAudioManager;
    private boolean mAudioFocused;

    private RingtoneListAdapter adapter;
    private List<RingToneEntity> ringtoneList = new ArrayList<>();

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_ringtone;
    }

    @Override
    protected String getTopTitle() {
        return null;
    }

    @Override
    protected void init() {
        setLeftTv("铃声");
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        ringtoneList = RingToneUtil.getRingtoneList(this, RingtoneManager.TYPE_RINGTONE);
        adapter = new RingtoneListAdapter(this, ringtoneList);
        adapter.setOnItemClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.shape_line));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setAdapter(adapter);

        if (LinphoneService.isReady()) {
            core = LinphoneService.getCore();
            String ringtone = core.getConfig().getString("app", "ringtone", Settings.System.DEFAULT_RINGTONE_URI.toString());
            if (ringtone.equals(Settings.System.DEFAULT_RINGTONE_URI.toString())) {
                chooseDefaultRingtone();
            }
            for (int i = 0; i < ringtoneList.size(); i++) {
                if (ringtone.equals(ringtoneList.get(i).getRingtoneUri())) {
                    chooseRingtone(i);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void OnItemClick(int position) {
        chooseRingtone(position);
        if (mRingerPlayer != null ) {
            stopRinging();
        }
        startRinging(ringtoneList.get(position).getRingtoneUri());
    }

    private void chooseRingtone(int position) {
        if (core != null) {
            //设置配置文件的铃声
            core.getConfig().setString("app", "ringtone", ringtoneList.get(position).getRingtoneUri());
        }
        for (int i = 0; i < ringtoneList.size(); i++) {
            ringtoneList.get(i).setShow(false);
        }
        ringtoneList.get(position).setShow(true);
        adapter.setData(ringtoneList);
        ivChooseDefault.setVisibility(View.INVISIBLE);
        tvDefault.setSelected(false);
    }

    private void chooseDefaultRingtone() {
        if (core != null) {
            //设置配置文件的铃声
            core.getConfig().setString("app", "ringtone", Settings.System.DEFAULT_RINGTONE_URI.toString());
        }
        ivChooseDefault.setVisibility(View.VISIBLE);
        tvDefault.setSelected(true);
        for (int i = 0; i < ringtoneList.size(); i++) {
            ringtoneList.get(i).setShow(false);
        }
        adapter.setData(ringtoneList);
    }

    @OnClick({R.id.ll_default})
    public void onClickVoid(View view) {
        switch (view.getId()) {
            case R.id.ll_default:
                if (!view.isSelected()) {
                    chooseDefaultRingtone();
                    if (mRingerPlayer != null && mRingerPlayer.isPlaying()) {
                        stopRinging();
                    }
                    startRinging(Settings.System.DEFAULT_RINGTONE_URI.toString());
                }
                break;
        }
    }

    private void startRinging(String ringtoneStr) {
        // mAudioManager.setSpeakerphoneOn(true);
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        if (mRingerPlayer == null) {
            requestAudioFocus(STREAM_MUSIC);
            mRingerPlayer = new MediaPlayer();
            mRingerPlayer.setAudioStreamType(STREAM_MUSIC);
            try {
                if (ringtoneStr.startsWith("content://")) {
                    mRingerPlayer.setDataSource(this, Uri.parse(ringtoneStr));
                } else {
                    FileInputStream fis = new FileInputStream(ringtoneStr);
                    mRingerPlayer.setDataSource(fis.getFD());
                    fis.close();
                }
            } catch (IOException e) {
                org.linphone.core.tools.Log.e(e, "[Audio Manager] Cannot set ringtone");
            }
            try {
                mRingerPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mRingerPlayer.setLooping(false);
            mRingerPlayer.start();
        } else {
            org.linphone.core.tools.Log.w("[Audio Manager] Already ringing");
        }
    }

    private void requestAudioFocus(int stream) {
        if (!mAudioFocused) {
            int res = mAudioManager.requestAudioFocus(
                    null, stream, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
            if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) mAudioFocused = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRinging();
    }

    private void  stopRinging() {
        if (mRingerPlayer != null) {
            mRingerPlayer.stop();
            mRingerPlayer.release();
            mRingerPlayer = null;
        }
    }

    //重写onKeyDown
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(getClass().getName(), "onKeyDown:keyCode === " + keyCode);
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
