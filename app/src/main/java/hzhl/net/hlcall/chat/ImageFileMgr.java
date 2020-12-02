package hzhl.net.hlcall.chat;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hzhl.net.hlcall.App;

/**
 * create by elileo on 2019/9/17
 */
public class ImageFileMgr {
    private static final String TAG = ImageFileMgr.class.getSimpleName();
    private static ImageFileMgr sInstance;
    private final ContentResolver mContentResolver;

    public static ImageFileMgr getInstance(){
        if(sInstance == null){
            synchronized (ImageFileMgr.class){
                if(sInstance == null){
                    sInstance = new ImageFileMgr(App.sContext);
                }
            }
        }
        return sInstance;
    }

    private ImageFileMgr(Context context){
        mContentResolver = context.getApplicationContext().getContentResolver();
    }


    public List<String> getSystemPhotoList() {
        List<String> result = new ArrayList<String>();
        Cursor cursor = null;
        try {
            String[] projection =new String[] { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.TITLE, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.SIZE };
            cursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                    new String[]{"image/jpeg", "image/png"}, null);
        } catch (SQLiteException e) {
        }
        // 没有图片
        if (cursor == null || cursor.getCount() <= 0) {
            return null;
        }
        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            // 文件地址
            String path = cursor.getString(index);
            File file = new File(path);
            if (file.exists()) {
                result.add(path);
            }
        }
        cursor.close();
        return result;
    }
}
