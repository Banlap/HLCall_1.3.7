package hzhl.net.hlcall.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import com.orhanobut.logger.Logger;

import org.linphone.core.tools.Log;

import java.io.File;

public class MediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {
    private final MediaScannerConnection mMediaConnection;
    private boolean mIsConnected;
    private File mFileWaitingForScan;
    private MediaScannerListener mListener;

    public MediaScanner(Context context) {
        mIsConnected = false;
        mMediaConnection = new MediaScannerConnection(context, this);
        mMediaConnection.connect();
        mFileWaitingForScan = null;
    }

    @Override
    public void onMediaScannerConnected() {
        mIsConnected = true;
        Logger.d("[MediaScanner] Connected");
        if (mFileWaitingForScan != null) {
            scanFile(mFileWaitingForScan, null);
            mFileWaitingForScan = null;
        }
    }

    public void scanFile(File file, MediaScannerListener listener) {
        scanFile(file, FileUtils.getMimeFromFile(file.getAbsolutePath()), listener);
    }

    private void scanFile(File file, String mime, MediaScannerListener listener) {
        mListener = listener;

        if (!mIsConnected) {
            Logger.d("[MediaScanner] Not connected yet...");
            mFileWaitingForScan = file;
            return;
        }

        Logger.d("[MediaScanner] Scanning file " + file.getAbsolutePath() + " with MIME " + mime);
        mMediaConnection.scanFile(file.getAbsolutePath(), mime);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        Logger.d("[MediaScanner] Scan completed : " + path + " => " + uri);
        if (mListener != null) {
            mListener.onMediaScanned(path, uri);
        }
    }

    public void destroy() {
        Logger.d("[MediaScanner] Disconnecting");
        mMediaConnection.disconnect();
        mIsConnected = false;
    }
}
