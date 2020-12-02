package hzhl.net.hlcall.utils;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import androidx.core.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.linphone.core.Core;
import org.linphone.core.tools.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import hzhl.net.hlcall.CallManager;
import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.activity.BohaoActivity;
import hzhl.net.hlcall.activity.ContactsAddActivity;
import hzhl.net.hlcall.activity.NewChatActivity;

/**
 * Helpers.
 */
public final class LinphoneUtils {
    public static final int CALL_PHONE_RESULT = 118;

    public static boolean isHighBandwidthConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null
                && info.isConnected()
                && isConnectionFast(info.getType(), info.getSubtype()));
    }

    private static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return false;
            }
        }
        // in doubt, assume connection is good.
        return true;
    }

    @SuppressLint("MissingPermission")
    public static boolean call(Context context, String s, boolean isVideoCall) {
        boolean isMobile = false;//是否手机/固话号码
        if (StringUtil.isEmpty(s) && !StringUtil.isNum(s)) {
            Toast.makeText(context, "请输入拨打号码", Toast.LENGTH_SHORT).show();
            return false;
        }
    /*    SettingEntityDao settingEntityDao = App.getDaoInstant().getSettingEntityDao();
        SettingEntity settingEntity = settingEntityDao.load(1L);
        if (settingEntity != null) {
            //是否开启了呼叫前确认
            if (settingEntity.getIsBoHaoType()) {
                isSipBohaoType = settingEntity.getBoHaoType() == 0;
            } else {
                isSipBohaoType = true;
            }
        }*/
        // isMobile = StringUtil.isPhoneNumberValid(StringUtil.delBlankString(number));
        String number = StringUtil.delBlankString(s);
        Intent intent = new Intent(context, BohaoActivity.class);
        intent.putExtra("number", number);
        intent.putExtra("isVideoCall", isVideoCall);
        context.startActivity(intent);
        return true;

    }
    public static boolean sendMessage(Context context, String number,String name) {
        if (LinphoneService.getCore()==null)return false;
        if (LinphoneService.getCore().getDefaultProxyConfig()==null) {
            Toast.makeText(context, "未登录", Toast.LENGTH_SHORT).show();
            return false;
        }

        Intent intent = new Intent(context, NewChatActivity.class);
        if (name==null || name.isEmpty())
            intent.putExtra(NewChatActivity.REMOTE_DISPLAY_NAME, ContactsUtil.getNameFormNumber(context,number));
        else intent.putExtra(NewChatActivity.REMOTE_DISPLAY_NAME,name);
        intent.putExtra(NewChatActivity.REMOTE_SIP_URI,number);
        Logger.d("name = "+name);
        Logger.d("number = " + number);
        context.startActivity(intent);
        return true;

    }


    @SuppressLint("MissingPermission")
    public static boolean callInSystem(Context context, String s) {
        boolean isMobile = false;//是否手机/固话号码
        if (StringUtil.isEmpty(s) && !StringUtil.isNum(s)) {
            Toast.makeText(context, "请输入拨打号码", Toast.LENGTH_SHORT).show();
            return false;
        }
        String number = StringUtil.delBlankString(s);
        if (isOpenPermission(context, Manifest.permission.CALL_PHONE)) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            context.startActivity(intent);
  /*          Intent intent = new Intent();
            intent.setAction("android.intent.action.DIAL");
            intent.setData(Uri.parse("tel:" + number));
            context.startActivity(intent);*/
            return true;
        } else {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}
                    , CALL_PHONE_RESULT);
        }
        return false;
    }


    public static boolean isOpenPermission(Context context, String permission) {
        int checkPermission = context.getPackageManager().checkPermission(permission, context.getPackageName());
        if (checkPermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static PopupWindow setCallPopupWindow(Context context, String number, boolean isShowAddContact) {
        //加载弹出框的布局
        View contentView = LayoutInflater.from(context).inflate(
                R.layout.popup_call_more_type, null);
        PopupWindow popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);// 取得焦点
        //注意  要是点击外部空白处弹框消息  那么必须给弹框设置一个背景色  不然是不起作用的
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        //点击外部消失
        popupWindow.setOutsideTouchable(true);
        //设置可以点击
        popupWindow.setTouchable(true);
        //进入退出的动画，指定刚才定义的style
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);

        TextView tvCallSystem = contentView.findViewById(R.id.tv_call_system);
        TextView tvCall = contentView.findViewById(R.id.tv_call);
        TextView tvCallVideo = contentView.findViewById(R.id.tv_call_video);
        TextView tvAddContact = contentView.findViewById(R.id.tv_add_contact);
        TextView tvDismiss = contentView.findViewById(R.id.tv_dismiss);
        View line = contentView.findViewById(R.id.view);
        line.setVisibility(isShowAddContact ? View.VISIBLE : View.GONE);
        tvAddContact.setVisibility(isShowAddContact ? View.VISIBLE : View.GONE);
        tvCallSystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isCalled = LinphoneUtils.callInSystem(context, number);

            }
        });
        tvCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinphoneUtils.call(context, number, false);
            }
        });
        tvCallVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinphoneUtils.call(context, number, true);
            }
        });
        tvAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ContactsAddActivity.class);
                intent.putExtra("number", number);
                context.startActivity(intent);
            }
        });
        tvDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        return popupWindow;
    }


    public static String timestampToHumanDate(Context context, long timestamp, int format) {
        return timestampToHumanDate(context, timestamp, context.getString(format));
    }

    public static String timestampToHumanDate(Context context, long timestamp, String format) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timestamp * 1000); // Core returns timestamps in seconds...

            SimpleDateFormat dateFormat;
            if (isToday(cal)) {
                dateFormat = new SimpleDateFormat(context.getResources().getString(R.string.today_date_format), Locale.getDefault());
            } else if(isYear(cal)) {
                dateFormat = new SimpleDateFormat(format, Locale.getDefault());
            }else{
                dateFormat = new SimpleDateFormat(context.getResources().getString(R.string.year_date_format), Locale.getDefault());
            }

            return dateFormat.format(cal.getTime());
        } catch (NumberFormatException nfe) {
            return String.valueOf(timestamp);
        }
    }

    private static boolean isYear(Calendar cal){
        return isSameYear(cal, Calendar.getInstance());
    }

    private static boolean isSameYear(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            return false;
        }

        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
                && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR));
    }

    private static boolean isToday(Calendar cal) {
        return isSameDay(cal, Calendar.getInstance());
    }

    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            return false;
        }

        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
                && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    public static void reloadVideoDevices() {
        Core core = LinphoneService.getCore();
        if (core == null) return;

        Log.i("[Utils] Reloading camera devices");
        core.reloadVideoDevices();
        CallManager.getInstance().resetCameraFromPreferences();
  //      CallManager.getInstance().resetCameraFromPreferences();
    }
}
