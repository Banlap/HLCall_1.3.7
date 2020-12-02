package hzhl.net.hlcall.utils;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by elileo on 2019/9/17
 */
public class PhotoUtils {
    public static File createImageFile(String path) throws IOException {
        File storageDir = null;
        if(TextUtils.isEmpty(path) || !(new File(path).isDirectory())){
            storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
        }else {
            storageDir = new File(path);
        }
        FileUtils.ensureDirExists(storageDir.getAbsolutePath());
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }
}
