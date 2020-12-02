package hzhl.net.hlcall.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;

import org.linphone.mediastream.Log;

import java.io.ByteArrayOutputStream;

public class BitmapToByteUtil {

    public static byte[] editContactPicture(Context context, String filePath) {
        Bitmap image = BitmapFactory.decodeFile(filePath);
        Bitmap scaledPhoto;
        int size = getThumbnailSize(context);
        if (size > 0) {
            scaledPhoto = Bitmap.createScaledBitmap(image, size, size, false);
        } else {
            scaledPhoto = Bitmap.createBitmap(image);
        }
        image.recycle();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scaledPhoto.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] photoToAdd = stream.toByteArray();
        return photoToAdd;
    }

    public static int getThumbnailSize(Context context) {
        int value = -1;
        Cursor c = context.getContentResolver().query(ContactsContract.DisplayPhoto.CONTENT_MAX_DIMENSIONS_URI, new String[]{ContactsContract.DisplayPhoto.THUMBNAIL_MAX_DIM}, null, null, null);
        try {
            if (c != null) {
                c.moveToFirst();
                value = c.getInt(0);
                c.close();
            }
        } catch (Exception e) {
            Log.e(e);
        }
        return value;
    }

    public static Bitmap Bytes2Bimap(byte[] b) {
        if (b != null && b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }
}
