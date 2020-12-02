package hzhl.net.hlcall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

import hzhl.net.hlcall.constant.Constants;
import hzhl.net.hlcall.utils.MyLog;

public class HomeWatcherReceiver extends BroadcastReceiver {
        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
    private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        MyLog.d("onReceive: action: " + action);
        if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            MyLog.d("reason: " + reason);

            if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                //发送通知，启动Service
                EventBus.getDefault().post(Constants.EVENT_FloatWindowService);
            } else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                // 长按Home键 或者 activity切换键
            } else if (SYSTEM_DIALOG_REASON_LOCK.equals(reason)) {
                // 锁屏
            } else if (SYSTEM_DIALOG_REASON_ASSIST.equals(reason)) {
                // samsung 长按Home键
            }

        }
    }
}
