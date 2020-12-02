package hzhl.net.hlcall.utils;


import android.content.Context;
import android.os.Environment;

import com.orhanobut.logger.Logger;

import org.linphone.core.Address;
import org.linphone.core.tools.Log;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import hzhl.net.hlcall.R;

public class FileUtils {
    public static String getCallRecordingFilename(Context context, Address address) {
        String fileName = getRecordingsDirectory(context) + "/";

        String name =
                address.getDisplayName() == null ? address.getUsername() : address.getDisplayName();
        fileName += name + "_";

        DateFormat format = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
        fileName += format.format(new Date()) + ".mkv";

        //fileName += format.format(new Date()) + ".wav";

        return fileName;
    }

    public static String getRecordingsDirectory(Context mContext) {
        String recordingsDir =
                Environment.getExternalStorageDirectory()
                        + "/"
                        + mContext.getString(
                        mContext.getResources()
                                .getIdentifier(
                                        "app_name", "string", mContext.getPackageName()))
                        + "/recordings";
        File file = new File(recordingsDir);

      //  Logger.d(recordingsDir+" 是否存在："+file.exists());
        if (!file.isDirectory() || !file.exists()) {
            Logger.d("[File Utils] Directory "
                    + file
                    + " doesn't seem to exists yet, let's create it");
            boolean result = file.mkdirs();
            if (!result) {
                Logger.d("[File Utils] Couldn't create directory " + file.getAbsolutePath());
            }
            MediaScanner mediaScanner=new MediaScanner(mContext);
            mediaScanner.scanFile(file, null);
        }
        return recordingsDir;
    }

    public static String getMimeFromFile(String path) {
        if (isExtensionImage(path)) {
            return "image/" + getExtensionFromFileName(path);
        }
        return "file/" + getExtensionFromFileName(path);
    }

    public static Boolean isExtensionImage(String path) {
        String extension = getExtensionFromFileName(path);
        if (extension != null) extension = extension.toLowerCase();
        return (extension != null && extension.matches("(png|jpg|jpeg|bmp|gif)"));
    }

    public static String getExtensionFromFileName(String fileName) {
        String extension = null;
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }

    public static boolean ensureDirExists(String dirPath) {
        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            return dirFile.mkdirs();
        }
        return true;
    }

    public static String getNameFromFilePath(String filePath) {
        if (filePath == null) return null;

        String name = filePath;
        int i = filePath.lastIndexOf('/');
        if (i > 0) {
            name = filePath.substring(i + 1);
        }
        return name;
    }

    public static String getStorageDirectory(Context mContext) {
        String storageDir =
                Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/"
                        + mContext.getString(R.string.app_name);
        File file = new File(storageDir);
        if (!file.isDirectory() || !file.exists()) {
            Log.w(
                    "[File Utils] Directory "
                            + file
                            + " doesn't seem to exists yet, let's create it");
            boolean result = file.mkdirs();
            if (!result) {
                Log.e(
                        "[File Utils] Couldn't create media directory "
                                + file.getAbsolutePath()
                                + ", using external storage dir instead");
                return Environment.getExternalStorageDirectory().getAbsolutePath();
            }
        }
        return storageDir;
    }

    public static String getImageDirectory(Context mContext,String filename) {
        String recordingsDir =
                Environment.getExternalStorageDirectory()
                        + "/"
                        + mContext.getString(
                        mContext.getResources()
                                .getIdentifier(
                                        "app_name", "string", mContext.getPackageName()))
                        + "/image/";
        /*String recordingsDir =
                Environment.getExternalStorageDirectory()
                        + "/"
                        + "DCIM"
                        + "/Camera/";*/

        File file = new File(recordingsDir);
        if (!file.isDirectory())file.mkdirs();
        return recordingsDir + filename;
    }


}
