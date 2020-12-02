package hzhl.net.hlcall.utils;

import android.net.Uri;

public interface MediaScannerListener {
    void onMediaScanned(String path, Uri uri);
}
