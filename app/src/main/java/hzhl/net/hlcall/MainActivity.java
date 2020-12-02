package hzhl.net.hlcall;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.tencent.bugly.beta.Beta;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.linphone.core.ChatRoom;
import org.linphone.core.Core;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import hzhl.net.hlcall.activity.BaseActivity;
import hzhl.net.hlcall.constant.Constants;
import hzhl.net.hlcall.entity.ContactsListEntity;
import hzhl.net.hlcall.entity.ContactsListEntityEvent;
import hzhl.net.hlcall.entity.SettingEntity;
import hzhl.net.hlcall.entity.SettingEntityDao;
import hzhl.net.hlcall.fragment.CallRecordFragment;
import hzhl.net.hlcall.fragment.ContactsListFragment;
import hzhl.net.hlcall.fragment.MessageFragment;
import hzhl.net.hlcall.fragment.MovingFragment;
import hzhl.net.hlcall.fragment.PeiZhiFragment;
import hzhl.net.hlcall.fragment.PhoneKeyBoardFragment;
import hzhl.net.hlcall.utils.ContactsUtil;
import hzhl.net.hlcall.utils.HomeListen;
import hzhl.net.hlcall.utils.MyLog;

public class MainActivity extends BaseActivity implements CallRecordFragment.OnModifyCallRecordListener, PhoneKeyBoardFragment.CallbackValue {
    @Bind(R.id.tv_tongXunLu)
    TextView mTvTongXunLu;
    @Bind(R.id.tv_meeting)
    TextView mTvMeeting;
    @Bind(R.id.tv_boHao)
    TextView mTvBoHao;
    @Bind(R.id.text_unread)
    TextView text_unread;
    @Bind(R.id.tv_message)
    TextView mTvMessage;
    @Bind(R.id.tv_peiZhi)
    TextView mTvPeiZhi;
    @Bind(R.id.ll_bottom)
    LinearLayout llBottom;
    private List<Fragment> listFragment = new ArrayList<>();
    private List<TextView> listTv = new ArrayList<>();
    private FragmentManager mFragmentManager;
    private Fragment mCurrentFragment;
    private ContactsListFragment contactsListFragment;
    private ArrayList<ContactsListEntity> contactsList = new ArrayList<>();
    private static final int READ_CALL_LOG_RESULT = 111;
    private static final int READ_CONTACTS_RESULE = 112;
    private static final int READ_CONTACTS_RESULE2 = 563;
    private static final int CONTACTS_MSG = 2313;

    public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;//自定义的标识

    //挂断键广播监听
    private HomeListen mHomelisten = null;
    private boolean isClickKeyboard = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == CONTACTS_MSG) {
                //更新联系人数据
                EventBus.getDefault().postSticky(new ContactsListEntityEvent(contactsList));
            }
        }
    };

    int unRaed = 0;
    private boolean mIsDeleteAllNum = true;

    //    private MyService.MyBinder myBinder;//我定义的中间人对象
    //  private MyConn conn;
    private Intent serviceIntent;
    private static HomeWatcherReceiver mHomeKeyReceiver = null;


    @Override
    public void modifyCallRecord(String type) {
        if ("modify".equals(type)) {
            llBottom.setVisibility(View.GONE);
        } else {
            llBottom.setVisibility(View.VISIBLE);
        }
    }

    //监听home键，但未屏蔽
  /*  @Override
    protected void onUserLeaveHint() {
        System.out.println("按下1：");
        super.onUserLeaveHint();
    }*/

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //enableHomeKeyDispatched(false);
        this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);
        initHomeListen();
        initMethod();

    }

    private void initMethod() {
      /*  try{
            Class audioSystemClass = Class.forName("android.view.WindowManager");

        } catch(Exception e) {

        }*/

    }

    //监听home键，但未屏蔽
    private void initHomeListen() {
        mHomelisten = new HomeListen(this);
        mHomelisten.setOnHomeBtnPressListener(new HomeListen.OnHomeBtnPressLitener() {
            @Override
            public void onHomeBtnPress() {
                System.out.println("按下2：");
            }
            @Override
            public void onHomeBtnLongPress() {
                System.out.println("长按");
            }
        });
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_main;
    }

    @Override
    protected String getTopTitle() {
        return null;
    }

    @Override
    protected void init() {
        setCommonTitleBarGone();
//        BoHaoFragment boHaoFragment = new BoHaoFragment();

        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();

        //banlap： 设置CallRecordFragment的TAG值
        CallRecordFragment callRecordFragment = (CallRecordFragment) mFragmentManager.findFragmentByTag("callRecordFrag");
        callRecordFragment = new CallRecordFragment();
        callRecordFragment.setModifyCallRecordListener(this);

        listFragment.add(callRecordFragment);
        //listFragment.add(new MeetingFragment());
        listFragment.add(new MovingFragment());
        listFragment.add(new ContactsListFragment());
        listFragment.add(new MessageFragment());
        listFragment.add(new PeiZhiFragment());
        listTv.add(mTvBoHao);
        listTv.add(mTvMeeting);
        listTv.add(mTvTongXunLu);
        listTv.add(mTvMessage);
        listTv.add(mTvPeiZhi);
        mTvBoHao.setSelected(true);


        transaction.replace(R.id.ll_content, listFragment.get(0),"callRecordFrag").commit();
        mCurrentFragment = listFragment.get(0);
        //
        mFragmentManager.executePendingTransactions();

        EventBus.getDefault().register(this);
        //  getContactsData();



       //wenyeyang
        Beta.checkUpgrade(false,false);
        upDateUnRead();
    }

    private void getContactsData() {
        if (isOpenPermission(Manifest.permission.READ_CONTACTS)) {
            queryContacts();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}
                    , READ_CONTACTS_RESULE2);
        }
    }

    private void queryContacts() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //需要在子线程中处理的逻辑
                contactsList = ContactsUtil.queryContacts(MainActivity.this);
                //处理完成后给handler发送消息  
                Message msg = new Message();
                msg.what = CONTACTS_MSG;
                handler.sendMessage(msg);
            }
        }).start();
    }

    private SettingEntity getSettingEntity() {
        SettingEntityDao settingEntityDao = App.getDaoInstant().getSettingEntityDao();
        SettingEntity settingEntity = settingEntityDao.load(1L);
        return settingEntity;
    }


    @OnClick({R.id.tv_boHao, R.id.tv_meeting, R.id.tv_tongXunLu, R.id.tv_message, R.id.tv_peiZhi})
    public void onClickVoid(View view) {
        switch (view.getId()) {
            case R.id.tv_boHao:
                if (isOpenPermission(Manifest.permission.READ_CALL_LOG)) {
                    boolean isRepeatClick = switchFragment(mCurrentFragment, listFragment.get(0));
                    resteState(0);
                    if(mCurrentFragment instanceof CallRecordFragment){
                        ((CallRecordFragment) mCurrentFragment).switchPhoneKeyBoard(isRepeatClick);
                    }
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG}
                            , READ_CALL_LOG_RESULT);
                }
                break;
            case R.id.tv_meeting:
                switchFragment(mCurrentFragment, listFragment.get(1));
                resteState(1);
                break;
            case R.id.tv_tongXunLu:
                if (isOpenPermission(Manifest.permission.READ_CONTACTS)) {
                    switchFragment(mCurrentFragment, listFragment.get(2));
                    resteState(2);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}
                            , READ_CONTACTS_RESULE);
                }
                break;
            case R.id.tv_message:
                switchFragment(mCurrentFragment, listFragment.get(3));
                resteState(3);
                break;
            case R.id.tv_peiZhi:
                switchFragment(mCurrentFragment, listFragment.get(4));
                resteState(4);
                break;
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        if (serviceIntent != null) {
            stopService(serviceIntent);
        }
    }

    //订阅EventBus事件，开启悬浮窗
    @Subscribe
    public void onEvent(String event) {
        if (event.equals(Constants.EVENT_FloatWindowService)) {
            if (getSettingEntity() != null) {
                if (getSettingEntity().getIsFloat()) {
                    //openMinWindow();
                    finish();
                }
            }
        } else if (event.equals(Constants.EVENT_UPDATE_CONTACTS)) {
            Logger.d("gengxinmian");
            // getContactsData();
        }
        else if (event.equals(Constants.EVENT_UPDATE_UN_READ)){
            upDateUnRead();
        }
    }

    //以下为Home键监听，最小化到桌面时也让悬浮窗启动
    @Override
    protected void onResume() {
        super.onResume();
        //registerHomeKeyReceiver(this);
        if (!LinphoneService.isReady()) {
            Logger.d("startService");
            startService(new Intent(Intent.ACTION_MAIN).setClass(this, LinphoneService.class));
        }
        //  getContactsData();
        //wenyeyang
        upDateUnRead();
        mHomelisten.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterHomeKeyReceiver(this);
        mHomelisten.stop();
    }

    private static void registerHomeKeyReceiver(Context context) {
        mHomeKeyReceiver = new HomeWatcherReceiver();
        final IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.registerReceiver(mHomeKeyReceiver, homeFilter);
    }

    private static void unregisterHomeKeyReceiver(Context context) {
        if (null != mHomeKeyReceiver) {
            context.unregisterReceiver(mHomeKeyReceiver);
        }
    }

    public void openMinWindow() {
        if (!FloatWindowService.isStarted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//判断系统版本
                if (!Settings.canDrawOverlays(this)) {
                    new AlertDialog.Builder(this)
                            .setTitle("请开启浮窗权限")
                            .setMessage("你的手机开启悬浮按钮需要获得浮窗权限")
                            .setPositiveButton("开启", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                                            , Uri.parse("package:" + getPackageName()));
                                    startActivityForResult(intent, 990);
                                }
                            }).show();
                } else {
                    //serviceIntent = new Intent(MainActivity.this, FloatWindowService.class);
                    //startService(serviceIntent);
                    //moveTaskToBack(true);
                    App.sContext.openMinWindow(true);
                }
            } else {
                //serviceIntent = new Intent(MainActivity.this, FloatWindowService.class);
                //startService(serviceIntent);
                //moveTaskToBack(true);
                App.sContext.openMinWindow(true);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 990) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Logger.d("授权失败");
                    Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
                } else {
                    Logger.d("授权成功");
                    Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                /*    serviceIntent = new Intent(MainActivity.this, FloatVideoWindowService.class);
                    startService(serviceIntent);
                    moveTaskToBack(true);*/
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] paramArrayOfInt) {
        if (requestCode == READ_CALL_LOG_RESULT) {
            Logger.d(paramArrayOfInt);
            if (!verifyPermissions(paramArrayOfInt)) {
                //还有权限未申请
                showMissingPermissionDialog();
                // if (isNeedCheck) {checkPermissions(needPermissions);} 防止去检测权限，不停的弹框
                //isNeedCheck = false;
            } else {
                //权限都申请了
                switchFragment(mCurrentFragment, listFragment.get(0));
                resteState(0);
                if(mCurrentFragment instanceof CallRecordFragment){
                    ((CallRecordFragment) mCurrentFragment).refreshDate();
                }
            }
        } else if (requestCode == READ_CONTACTS_RESULE) {
            Logger.d(paramArrayOfInt);
            if (!verifyPermissions(paramArrayOfInt)) {
                //还有权限未申请
                showMissingPermissionDialog();
            } else {
                switchFragment(mCurrentFragment, listFragment.get(2));
                resteState(2);
                //权限都申请了
            }
        } else if (requestCode == READ_CONTACTS_RESULE2) {
            Logger.d("权限回调");
            if (!verifyPermissions(paramArrayOfInt)) {
                //还有权限未申请
                //   showMissingPermissionDialog();
            } else {
                // queryContacts();
            }
        }else if(requestCode == PERMISSON_REQUESTCODE){
            if(permissions != null && permissions.length > 0){
                boolean canRefresh = false;
                for(int i = 0; i < permissions.length; i++){
                    if(Manifest.permission.READ_CALL_LOG.equals(permissions[i])){
                        if(paramArrayOfInt != null && paramArrayOfInt.length > i){
                            if(paramArrayOfInt[i] == PackageManager.PERMISSION_GRANTED){
                                canRefresh = true;
                            }
                        }
                        break;
                    }
                }
                if(canRefresh){
                    if(mCurrentFragment instanceof CallRecordFragment){
                        ((CallRecordFragment) mCurrentFragment).refreshDate();
                    }
                }
                /**
                * banlap: bug: 修改权限显示方式 逐个显示
                */
                if (!verifyPermissions(paramArrayOfInt)) {
                    //还有权限未申请
                    showMissingPermissionDialog();
                } else {
                    if (findDeniedPermissions(needPermissions).size() > 0) {
                        checkPermissions(needPermissions);
                    } else {
                        //进入APP主页
                    }
                }
                /**
                 * banlap: bug: 修改权限显示方式 逐个显示  --end
                 */

            }
        }
    }

    /**
     * banlap: bug: 修改权限显示方式 逐个显示
     */
    /**
     * 申请权限
     */
    private void checkPermissions(String... permissions) {
        List<String> needRequestPermissonList = findDeniedPermissions(permissions);//找需要申请的权限
        if (null != needRequestPermissonList && needRequestPermissonList.size() > 0) {
            Logger.d(needRequestPermissonList.size() + "个需要申请");
            //ActivityCompat.requestPermissions(this, needRequestPermissonList.toArray(new String[needRequestPermissonList.size()]), PERMISSON_REQUESTCODE);
            ActivityCompat.requestPermissions(this, new String[]{needRequestPermissonList.get(0)}, PERMISSON_REQUESTCODE);

        }
    }

    /**
     * 获取权限集中需要申请权限的列表
     */
    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissonList = new ArrayList<String>();
        for (String perm : permissions) {
            //shouldShowRequestPermissionRationale返回false是操作了拒绝且不再访问，返回true是拒绝
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                needRequestPermissonList.add(perm);
            }
        }
        return needRequestPermissonList;
    }

    /** banlap: bug: 修改权限显示方式 逐个显示  --end */



    private boolean switchFragment(Fragment from, Fragment to) {
        boolean isRepeatClick = true;
        if (from != to) {
            isRepeatClick = false;
            mCurrentFragment = to;
            if (to.isAdded()) {
                mFragmentManager.beginTransaction().hide(from).show(to).commit();
            } else {
                mFragmentManager.beginTransaction().hide(from).add(R.id.ll_content, to).commit();
            }
        }

        return isRepeatClick;
    }

    private void resteState(int position) {
        for (TextView textView : listTv) {
            textView.setSelected(textView == listTv.get(position));

        }
    }
    private void navMove(int rightOrLeft){
        int currentFragmentIndex = listFragment.indexOf(contactsListFragment);
        currentFragmentIndex += rightOrLeft;
        if (currentFragmentIndex > 4)currentFragmentIndex = 4;
        if (currentFragmentIndex < 0)currentFragmentIndex = 0;
        switchFragment(mCurrentFragment,listFragment.get(currentFragmentIndex));
    }


    public String setIsDeleteFlag(String isDelete){
        return isDelete;
    }

    @Override
    public void isDeleteNum(boolean isDelete) {
        mIsDeleteAllNum = isDelete;
    }

    //重写onKeyDown
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(getClass().getName(), "onKeyDown:keyCode === " + keyCode);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Logger.d("MainActivity返回");
            //banlap: 判断如果在配置界面则返回到拨号界面
            if(mCurrentFragment == listFragment.get(4)) {
                switchFragment(mCurrentFragment, listFragment.get(0));
                resteState(0);
            } else {
                //banlap： 点击返回键 获取CallRecordFragment设置的TAG，并传递信息
                CallRecordFragment mCallRecordFm = (CallRecordFragment) MainActivity.this.getSupportFragmentManager().findFragmentByTag("callRecordFrag");
                mCallRecordFm.setIsDeleteNum();

                //banlap： 如果回调数据为true 则当前PhoneKeyBoard没有号码需要删除，并返回界面
                if(mIsDeleteAllNum) {
                    Intent home = new Intent(Intent.ACTION_MAIN);
                    // home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    home.addCategory(Intent.CATEGORY_HOME);
                    startActivity(home);
                }
            }
            return true;
        }
        /** banlap：bug：实体键 拨号键和 挂机键不操作 */
        if (keyCode == KeyEvent.KEYCODE_CALL) {
            Logger.d("MainActivity不操作");
            if(mCurrentFragment == listFragment.get(0)) {
                //banlap： 点击返回键 获取CallRecordFragment设置的TAG，并传递信息
                CallRecordFragment mCallRecordFm2 = (CallRecordFragment) MainActivity.this.getSupportFragmentManager().findFragmentByTag("callRecordFrag");
                mCallRecordFm2.setIsCall();
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_ENDCALL) {
            Logger.d("MainActivity不操作");
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            Logger.d("MainActivity不操作");
            return true;
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_0:
                isClickKeyboard = true;
                break;
            case KeyEvent.KEYCODE_1:
                isClickKeyboard = true;
                break;
            case KeyEvent.KEYCODE_2:
                isClickKeyboard = true;
                break;
            case KeyEvent.KEYCODE_3:
                isClickKeyboard = true;
                break;
            case KeyEvent.KEYCODE_4:
                isClickKeyboard = true;
                break;
            case KeyEvent.KEYCODE_5:
                isClickKeyboard = true;
                break;
            case KeyEvent.KEYCODE_6:
                isClickKeyboard = true;
                break;
            case KeyEvent.KEYCODE_7:
                isClickKeyboard = true;
                break;
            case KeyEvent.KEYCODE_8:
                isClickKeyboard = true;
                break;
            case KeyEvent.KEYCODE_9:
                isClickKeyboard = true;
                break;
            case KeyEvent.KEYCODE_STAR:
                isClickKeyboard = true;
                break;
            case KeyEvent.KEYCODE_POUND:
                isClickKeyboard = true;
                break;
        }

        /** banlap：bug：实体键 拨号键和 挂机键不操作  --end*/

        /*
        switch (keyCode){
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                navMove(1);
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                navMove(-1);
                break;
        }
        */
        if (mCurrentFragment instanceof CallRecordFragment){
            ((CallRecordFragment) mCurrentFragment).onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    private void enableHomeKeyDispatched(boolean enable) {
        final Window window = getWindow();
        final WindowManager.LayoutParams lp = window.getAttributes();
        if (enable) {
            lp.flags |=FLAG_HOMEKEY_DISPATCHED;
        } else {
            lp.flags &= ~FLAG_HOMEKEY_DISPATCHED;
        }
        window.setAttributes(lp);

    }
        @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        MyLog.d("-------APP----关掉");
        // closeSipProfile();


    }
    protected void upDateUnRead(){
        new Thread(() -> {
            if (!LinphoneService.isReady())return;
            Core core = LinphoneService.getCore();
            try {
                while (core==null){
                    Thread.sleep(200);
                    core =LinphoneService.getCore();
                }
                if (core.getDefaultProxyConfig()==null)return;
                String username = core.getDefaultProxyConfig().getContact().getUsername();
                unRaed = 0;
                for (ChatRoom r :
                        core.getChatRooms()) {
                    if (r.getLocalAddress().getUsername().equals(username))unRaed+=r.getUnreadMessagesCount();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> {
                text_unread.setText(String.valueOf(unRaed));
                if (unRaed == 0) text_unread.setVisibility(View.INVISIBLE);
                else text_unread.setVisibility(View.VISIBLE);
            });

        }).start();
    }



    /*@Override
    protected void onNewIntent(Intent intent) {
        super .onNewIntent(intent);
    }*/
    /*@Override
    public void onAttachedToWindow () {

        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        super.onAttachedToWindow();
    }*/
}
