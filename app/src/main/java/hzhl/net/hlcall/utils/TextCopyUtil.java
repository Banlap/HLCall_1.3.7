package hzhl.net.hlcall.utils;

import android.content.ClipData;
import android.content.Context;
import android.os.Build;
import android.text.ClipboardManager;

public class TextCopyUtil {

    /**
       * 实现文本复制功能
       * @param content
       * @return true--复制成功
       */
     @SuppressWarnings("deprecation")
     public static boolean copy(String content, Context context) {
      // 得到剪贴板管理器
      try {
       String n1 = content.trim();
       int sdk = Build.VERSION.SDK_INT;
       if (sdk < Build.VERSION_CODES.HONEYCOMB) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(n1);
       } else {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip_data = ClipData.newPlainText(
                                  "TSMS", n1);
        clipboard.setPrimaryClip(clip_data);
       }
       return true;
      } catch (Exception e) {
       e.printStackTrace();
      }
      return false;
     }

     /**
      * 实现粘贴功能
      * @param context
      * @return null--粘贴失败
      */
     public static String paste(Context context) {
      // 得到剪贴板管理器
      try {
       String n1 = null;
       int sdk = android.os.Build.VERSION.SDK_INT;
       if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        n1 = clipboard.getText().toString();
       }else{
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        n1 = (String) clipboard.getPrimaryClip().getItemAt(0) .getText();
       }
       return n1;
      } catch (Exception e) {
       e.printStackTrace();
      }
      return null;
     }

}
