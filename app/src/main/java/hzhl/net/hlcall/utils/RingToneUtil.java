package hzhl.net.hlcall.utils;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import hzhl.net.hlcall.entity.RingToneEntity;

public class RingToneUtil {
    //RingtoneManager.TYPE_NOTIFICATION;   通知声音

//RingtoneManager.TYPE_ALARM;  警告

//RingtoneManager.TYPE_RINGTONE; 铃声

    /**
     * 获取的是铃声的Uri
     *
     * @param ctx
     * @param type
     * @return
     */
    public static Uri getDefaultRingtoneUri(Context ctx, int type) {

        return RingtoneManager.getActualDefaultRingtoneUri(ctx, type);

    }

    /**
     * 获取的是铃声相应的Ringtone
     *
     * @param ctx
     * @param type
     */
    public Ringtone getDefaultRingtone(Context ctx, int type) {

        return RingtoneManager.getRingtone(ctx,
                RingtoneManager.getActualDefaultRingtoneUri(ctx, type));

    }

    /**
     * 播放铃声
     *
     * @param ctx
     * @param type
     */

    public static void PlayRingTone(Context ctx, int type) {
        MediaPlayer mMediaPlayer = MediaPlayer.create(ctx,
                getDefaultRingtoneUri(ctx, type));
        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();
    }


    public static List<RingToneEntity> getRingtoneList(Context context, int type) {

        List<RingToneEntity> ringToneEntityList = new ArrayList<>();

        RingtoneManager manager = new RingtoneManager(context);

        manager.setType(type);

        Cursor cursor = manager.getCursor();

        int count = cursor.getCount();

        for (int i = 0; i < count; i++) {
            RingToneEntity ringToneEntity = new RingToneEntity();
            ringToneEntity.setRingtone(manager.getRingtone(i));
            ringToneEntity.setRingtoneUri(manager.getRingtoneUri(i).toString());
            ringToneEntityList.add(ringToneEntity);
        }
        return ringToneEntityList;

    }


    public static Ringtone getRingtone(Context context, int type, int pos) {

        RingtoneManager manager = new RingtoneManager(context);

        manager.setType(type);

        return manager.getRingtone(pos);

    }


    public static List<String> getRingtoneTitleList(Context context, int type) {

        List<String> resArr = new ArrayList<String>();

        RingtoneManager manager = new RingtoneManager(context);

        manager.setType(type);

        Cursor cursor = manager.getCursor();

        if (cursor.moveToFirst()) {

            do {

                resArr.add(cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX));

            } while (cursor.moveToNext());

        }

        return resArr;

    }


    public static String getRingtoneUriPath(Context context, int type, int pos, String def) {

        RingtoneManager manager = new RingtoneManager(context);

        manager.setType(type);

        Uri uri = manager.getRingtoneUri(pos);

        return uri == null ? def : uri.toString();

    }


    /**
     * 通过路径返回铃声
     *
     * @param context * @param type
     *                * @param uriPath
     *                * @return
     */
    public static Ringtone getRingtoneByUriPath(Context context, int type, String uriPath) {

        RingtoneManager manager = new RingtoneManager(context);

        manager.setType(type);

        Uri uri = Uri.parse(uriPath);

        return manager.getRingtone(context, uri);

    }
}
