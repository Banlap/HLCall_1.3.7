package hzhl.net.hlcall.recording;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import com.orhanobut.logger.Logger;

import org.linphone.core.Core;
import org.linphone.core.Player;
import org.linphone.core.PlayerListener;
import org.linphone.core.tools.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hzhl.net.hlcall.LinphoneService;

public class Recording implements PlayerListener, Comparable<Recording> {
    public static final Pattern RECORD_PATTERN =
            Pattern.compile(".*/(.*)_(\\d{2}-\\d{2}-\\d{4}-\\d{2}-\\d{2}-\\d{2})\\..*");

    private final String mRecordPath;
    private String mName;
    private Date mRecordDate;
    private Player mPlayer;
    private RecordingListener mListener;
    private Runnable mUpdateCurrentPositionTimer;
    private Handler sHandler = new Handler(Looper.getMainLooper());
    private Core core;

    @SuppressLint("SimpleDateFormat")
    public Recording(Context context, String recordPath) {
        this.mRecordPath = recordPath;

        Matcher m = RECORD_PATTERN.matcher(recordPath);
        if (m.matches()) {
            mName = m.group(1);

            try {
                mRecordDate = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss").parse(m.group(2));
            } catch (ParseException e) {
                Log.e(e);
            }
        }

        mUpdateCurrentPositionTimer =
                new Runnable() {
                    @Override
                    public void run() {
                        //  Logger.d(mListener);
                        if (mListener != null)
                            mListener.currentPositionChanged(getCurrentPosition());
                        if (isPlaying())
                            sHandler.postDelayed(mUpdateCurrentPositionTimer, 20);
                    }
                };

        if (LinphoneService.isReady()) {
            core = LinphoneService.getCore();
        }
        if (core != null) {
            mPlayer = core.createLocalPlayer(null, null, null);
            mPlayer.addListener(this);
        }
    }

    public String getRecordPath() {
        return mRecordPath;
    }

    public String getName() {
        return mName;
    }

    public Date getRecordDate() {
        return mRecordDate;
    }

    public boolean isClosed() {
        if (mPlayer != null) {
            return mPlayer.getState() == Player.State.Closed;
        }
        return false;
    }

    public void play() {
        if (mPlayer != null) {
            if (isClosed()) {
                mPlayer.open(mRecordPath);
            }

            mPlayer.start();
            sHandler.post(mUpdateCurrentPositionTimer);
        }
    }

    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.getState() == Player.State.Playing;
        }
        return false;
    }

    public void pause() {
        if (!isClosed() && mPlayer != null) {
            mPlayer.pause();
        }
    }

    public boolean isPaused() {
        if (mPlayer != null) {
            return mPlayer.getState() == Player.State.Paused;
        }
        return false;

    }

    public void seek(int i) {
        if (mPlayer != null && !isClosed()) mPlayer.seek(i);
    }

    public int getCurrentPosition() {
        if (mPlayer != null) {
            if (isClosed()) {
                mPlayer.open(mRecordPath);
                Logger.d(mPlayer.getCurrentPosition());
            }
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (mPlayer != null) {
            if (isClosed()) {
                mPlayer.open(mRecordPath);
            }

            Logger.d(mPlayer.getDuration());
            return mPlayer.getDuration();
        }
        return 0;
    }

    public void close() {
        if (mPlayer != null) {
            mPlayer.removeListener(this);
            mPlayer.close();
        }
    }

    public void setRecordingListener(RecordingListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onEofReached(Player player) {
        Logger.d(mListener);
        if (mListener != null) mListener.endOfRecordReached();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Recording) {
            Recording r = (Recording) o;
            return mRecordPath.equals(r.getRecordPath());
        }
        return false;
    }

    @Override
    public int compareTo(@NonNull Recording o) {
        return -mRecordDate.compareTo(o.getRecordDate());
    }
}
