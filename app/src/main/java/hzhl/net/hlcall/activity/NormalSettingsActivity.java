package hzhl.net.hlcall.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import hzhl.net.hlcall.App;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.adapter.NormalSettingsAdapter;
import hzhl.net.hlcall.bean.Settings;
import hzhl.net.hlcall.entity.SettingEntity;
import hzhl.net.hlcall.entity.SettingEntityDao;
import hzhl.net.hlwork.ui.setting.VideoSettingActivity;

public class NormalSettingsActivity extends BaseActivity implements View.OnClickListener{

    @Bind(R.id.lv_NormalSettings)
    ListView mNSListView;

    @Bind(R.id.iv_setting_float)
    ImageView iv_setting_float;
    @Bind(R.id.iv_setting_auto_call)
    ImageView iv_setting_auto_call;
    @Bind(R.id.iv_setting_auto_video_call)
    ImageView iv_setting_auto_video_call;
    @Bind(R.id.iv_setting_auto_start)
    ImageView iv_setting_auto_start;

    private NormalSettingsAdapter mNSAdapter = null;
    private List<Settings> settingList = new ArrayList<>();

    private SettingEntity settingEntity;
    private SettingEntityDao settingEntityDao;

    private boolean isTapFloat = false;
    private boolean isAutoCall = false;
    private boolean isAutoVideo = false;
    private boolean isAutoStart = false;

    SharedPreferences mContextSp;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_normal_settings;
    }

    @Override
    protected String getTopTitle() {
        return null;
    }

    @Override
    protected void init() {
        setLeftTv("普通设置");

        mContextSp  = this.getSharedPreferences( "testContextSp", Context.MODE_PRIVATE );

        settingEntityDao = App.getDaoInstant().getSettingEntityDao();
        settingEntity = settingEntityDao.load(1L);
        if (settingEntity == null) {
            settingEntity = new SettingEntity();
            settingEntityDao.insertOrReplace(settingEntity);
        } else {
            int boHaoType = settingEntity.getBoHaoType();
        }

        /*mNSAdapter = new NormalSettingsAdapter(NormalSettingsActivity.this, R.layout.item_normal_settings, settingList);
        mNSListView.setAdapter(mNSAdapter);*/


        //悬浮
        int imageFloat = mContextSp.getInt( "ImageFloat", 1000);
        isTapFloat = mContextSp.getBoolean( "isImageFloat", false);
        //自动接听
        int autoCall = mContextSp.getInt( "AutoCall", 1000);
        isAutoCall = mContextSp.getBoolean( "isAutoCall", false);
        //自动接听
        int autoVideo = mContextSp.getInt( "AutoVideo", 1000);
        isAutoVideo = mContextSp.getBoolean( "isAutoVideo", false);
        //自动接听
        int autoStart = mContextSp.getInt( "AutoStart", 1000);
        isAutoStart = mContextSp.getBoolean( "isAutoStart", false);


        if (imageFloat == 1000) {
            iv_setting_float.setImageResource(R.drawable.selector_peizhi_on_off);
        } else {
            iv_setting_float.setImageResource(imageFloat);
        }
        if (autoCall == 1000) {
            iv_setting_auto_call.setImageResource(R.drawable.selector_peizhi_on_off);
        } else {
            iv_setting_auto_call.setImageResource(autoCall);
        }
        if (autoVideo == 1000) {
            iv_setting_auto_video_call.setImageResource(R.drawable.selector_peizhi_on_off);
        } else {
            iv_setting_auto_video_call.setImageResource(autoVideo);
        }
        if (autoStart == 1000) {
            iv_setting_auto_start.setImageResource(R.drawable.selector_peizhi_on_off);
        } else {
            iv_setting_auto_start.setImageResource(autoStart);
        }

        /*Settings settings1 = new Settings("黑名单", R.drawable.icon_fanhx);
        settingList.add(settings1);
        Settings settings2 = new Settings("视频", R.drawable.icon_fanhx);
        settingList.add(settings2);
        Settings settings3 = new Settings("音频", R.drawable.icon_fanhx);
        settingList.add(settings3);
        Settings settings4 = new Settings("铃声设置", R.drawable.icon_fanhx);
        settingList.add(settings4);
        if (imageFloat == 1000) {
            Settings settings5 = new Settings("悬浮", R.drawable.selector_peizhi_on_off);
            settingList.add(settings5);
        } else {
            Settings settings5 = new Settings("悬浮", imageFloat);
            settingList.add(settings5);
        }
        if (autoCall == 1000) {
            Settings settings6 = new Settings("自动接听", R.drawable.selector_peizhi_on_off);
            settingList.add(settings6);
        } else {
            Settings settings6 = new Settings("自动接听", autoCall);
            settingList.add(settings6);
        }
        if (autoVideo == 1000) {
            Settings settings7 = new Settings("自动视频接听", R.drawable.selector_peizhi_on_off);
            settingList.add(settings7);
        } else {
            Settings settings7 = new Settings("自动视频接听", autoVideo);
            settingList.add(settings7);
        }
        if (autoStart == 1000) {
            Settings settings8 = new Settings("开机自启动", R.drawable.selector_peizhi_on_off);
            settingList.add(settings8);
        } else {
            Settings settings8 = new Settings("开机自启动", autoStart);
            settingList.add(settings8);
        }


            mNSAdapter.setItemClickListener(new NormalSettingsAdapter.SettingItemClickListener() {
            @Override
            public void OnSettingItemClickListener(View v, String itemName, int position) {
                System.out.println("输出为： " + position + "  and  " + itemName);

                if(position==0) {
                    //黑名单
                    Intent intent = new Intent(NormalSettingsActivity.this, BlacklistActivity.class);
                    startActivity(intent);
                } else if (position == 1) {
                    //视频
                    Intent intent = new Intent(NormalSettingsActivity.this, VideoSettingActivity.class);
                    startActivity(intent);
                } else if (position == 2) {
                    //音频
                    Intent intent = new Intent(NormalSettingsActivity.this, VoiceSettingActivity.class);
                    startActivity(intent);
                } else if (position == 3) {
                    //铃声设置
                    Intent intent = new Intent(NormalSettingsActivity.this, RingtoneActivity.class);
                    startActivity(intent);
                } else if (position == 4) {
                    isTapFloat =  mContextSp.getBoolean( "isImageFloat", false);
                    SharedPreferences.Editor editor = mContextSp.edit();
                    if (openFloatPermission()) {
                        if(!isTapFloat) {
                            settingEntity.setIsFloat(true);
                            mNSAdapter.getItem(4).setImageId(R.drawable.icon_kai);
                            mNSAdapter.notifyDataSetChanged();
                            editor.putInt( "ImageFloat", R.drawable.icon_kai );
                            editor.putBoolean("isImageFloat", true);
                            editor.apply();
                        } else {
                            settingEntity.setIsFloat(false);
                            mNSAdapter.getItem(4).setImageId(R.drawable.icon_guanq);
                            mNSAdapter.notifyDataSetChanged();
                            editor.putInt( "ImageFloat", R.drawable.icon_guanq);
                            editor.putBoolean("isImageFloat", false);
                            editor.apply();
                        }
                        settingEntityDao.insertOrReplace(settingEntity);
                    }

                } else if (position == 5) {
                    isAutoCall = mContextSp.getBoolean( "isAutoCall", false);
                    SharedPreferences.Editor editor = mContextSp.edit();
                    if(!isAutoCall) {
                        settingEntity.setIsAutoAnswerCall(true);
                        mNSAdapter.getItem(5).setImageId(R.drawable.icon_kai);
                        mNSAdapter.notifyDataSetChanged();
                        editor.putInt( "AutoCall", R.drawable.icon_kai );
                        editor.putBoolean("isAutoCall", true);
                        editor.apply();
                    } else {
                        settingEntity.setIsAutoAnswerCall(false);
                        settingEntity.setIsAutoAnswerCallVideo(false);
                        mNSAdapter.getItem(5).setImageId(R.drawable.icon_guanq);
                        mNSAdapter.notifyDataSetChanged();
                        editor.putInt( "AutoCall", R.drawable.icon_guanq );
                        editor.putBoolean("isAutoCall", false);
                        editor.apply();
                    }
                    settingEntityDao.insertOrReplace(settingEntity);

                } else if (position == 6) {
                    isAutoVideo = mContextSp.getBoolean( "isAutoVideo", false);
                    SharedPreferences.Editor editor = mContextSp.edit();
                    if(!isAutoVideo) {
                        settingEntity.setIsAutoAnswerCall(true);
                        settingEntity.setIsAutoAnswerCallVideo(true);
                        mNSAdapter.getItem(6).setImageId(R.drawable.icon_kai);
                        mNSAdapter.notifyDataSetChanged();
                        editor.putInt( "AutoVideo", R.drawable.icon_kai );
                        editor.putBoolean("isAutoVideo", true);
                        editor.apply();
                    } else {
                        settingEntity.setIsAutoAnswerCallVideo(false);
                        mNSAdapter.getItem(6).setImageId(R.drawable.icon_guanq);
                        mNSAdapter.notifyDataSetChanged();
                        editor.putInt( "AutoVideo", R.drawable.icon_guanq );
                        editor.putBoolean("isAutoVideo", false);
                        editor.apply();
                    }
                    settingEntityDao.insertOrReplace(settingEntity);
                } else if (position == 7) {
                    isAutoStart = mContextSp.getBoolean( "isAutoStart", false);
                    SharedPreferences.Editor editor = mContextSp.edit();
                    if(!isAutoStart) {
                        settingEntity.setIsAutoStart(true);
                        mNSAdapter.getItem(7).setImageId(R.drawable.icon_kai);
                        mNSAdapter.notifyDataSetChanged();
                        editor.putInt( "AutoStart", R.drawable.icon_kai );
                        editor.putBoolean("isAutoStart", true);
                        editor.apply();
                    } else {
                        settingEntity.setIsAutoStart(false);
                        mNSAdapter.getItem(7).setImageId(R.drawable.icon_guanq);
                        mNSAdapter.notifyDataSetChanged();
                        editor.putInt( "AutoStart", R.drawable.icon_guanq );
                        editor.putBoolean("isAutoStart", false);
                        editor.apply();
                    }
                }
            }
        });*/
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @OnClick({R.id.ll_setting_blacklist, R.id.ll_setting_video, R.id.ll_setting_voice,
                R.id.ll_setting_sound, R.id.ll_setting_float, R.id.ll_setting_auto_call,
                R.id.ll_setting_auto_video_call, R.id.ll_setting_auto_start})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.ll_setting_blacklist:
                //黑名单
                Intent intentBlack = new Intent(NormalSettingsActivity.this, BlacklistActivity.class);
                startActivity(intentBlack);
                break;
            case R.id.ll_setting_video:
                //视频
                Intent intentVideo = new Intent(NormalSettingsActivity.this, VideoSettingActivity.class);
                startActivity(intentVideo);
                break;
            case R.id.ll_setting_voice:
                //音频
                Intent intentVoice = new Intent(NormalSettingsActivity.this, VoiceSettingActivity.class);
                startActivity(intentVoice);
                break;
            case R.id.ll_setting_sound:
                //铃声设置
                Intent intentSound = new Intent(NormalSettingsActivity.this, RingtoneActivity.class);
                startActivity(intentSound);
                break;
            case R.id.ll_setting_float:
                isTapFloat =  mContextSp.getBoolean( "isImageFloat", false);
                SharedPreferences.Editor editor1 = mContextSp.edit();
                if (openFloatPermission()) {
                    if(!isTapFloat) {
                        settingEntity.setIsFloat(true);
                        iv_setting_float.setImageResource(R.drawable.icon_kai);
                        editor1.putInt( "ImageFloat", R.drawable.icon_kai );
                        editor1.putBoolean("isImageFloat", true);
                        editor1.apply();
                    } else {
                        settingEntity.setIsFloat(false);
                        iv_setting_float.setImageResource(R.drawable.icon_guanq);
                        editor1.putInt( "ImageFloat", R.drawable.icon_guanq);
                        editor1.putBoolean("isImageFloat", false);
                        editor1.apply();
                    }
                    settingEntityDao.insertOrReplace(settingEntity);
                }
                break;
            case R.id.ll_setting_auto_call:
                isAutoCall = mContextSp.getBoolean( "isAutoCall", false);
                SharedPreferences.Editor editor2 = mContextSp.edit();
                if(!isAutoCall) {
                    settingEntity.setIsAutoAnswerCall(true);
                    iv_setting_auto_call.setImageResource(R.drawable.icon_kai);
                    editor2.putInt( "AutoCall", R.drawable.icon_kai );
                    editor2.putBoolean("isAutoCall", true);
                    editor2.apply();
                } else {
                    settingEntity.setIsAutoAnswerCall(false);
                    settingEntity.setIsAutoAnswerCallVideo(false);
                    iv_setting_auto_call.setImageResource(R.drawable.icon_guanq);
                    editor2.putInt( "AutoCall", R.drawable.icon_guanq );
                    editor2.putBoolean("isAutoCall", false);
                    editor2.apply();
                }
                settingEntityDao.insertOrReplace(settingEntity);

                break;
            case R.id.ll_setting_auto_video_call:
                isAutoVideo = mContextSp.getBoolean( "isAutoVideo", false);
                SharedPreferences.Editor editor3 = mContextSp.edit();
                if(!isAutoVideo) {
                    settingEntity.setIsAutoAnswerCall(true);
                    settingEntity.setIsAutoAnswerCallVideo(true);
                    iv_setting_auto_video_call.setImageResource(R.drawable.icon_kai);
                    editor3.putInt( "AutoVideo", R.drawable.icon_kai );
                    editor3.putBoolean("isAutoVideo", true);
                    editor3.apply();
                } else {
                    settingEntity.setIsAutoAnswerCallVideo(false);
                    iv_setting_auto_video_call.setImageResource(R.drawable.icon_guanq);
                    editor3.putInt( "AutoVideo", R.drawable.icon_guanq );
                    editor3.putBoolean("isAutoVideo", false);
                    editor3.apply();
                }
                settingEntityDao.insertOrReplace(settingEntity);
                break;
            case R.id.ll_setting_auto_start:
                isAutoStart = mContextSp.getBoolean( "isAutoStart", false);
                SharedPreferences.Editor editor4 = mContextSp.edit();
                if(!isAutoStart) {
                    settingEntity.setIsAutoStart(true);
                    iv_setting_auto_start.setImageResource(R.drawable.icon_kai);
                    editor4.putInt( "AutoStart", R.drawable.icon_kai );
                    editor4.putBoolean("isAutoStart", true);
                    editor4.apply();
                } else {
                    settingEntity.setIsAutoStart(false);
                    iv_setting_auto_start.setImageResource(R.drawable.icon_guanq);
                    editor4.putInt( "AutoStart", R.drawable.icon_guanq );
                    editor4.putBoolean("isAutoStart", false);
                    editor4.apply();
                }
                settingEntityDao.insertOrReplace(settingEntity);
                break;
        }
    }
    /**
     * 判断是否打开悬浮权限
     */
    private boolean openFloatPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//判断系统版本
            if (!android.provider.Settings.canDrawOverlays(NormalSettingsActivity.this)) {
                new AlertDialog.Builder(NormalSettingsActivity.this)
                        .setTitle("请开启显示悬浮窗设置")
                        .setMessage("悬浮功能需要您允许显示悬浮窗、后台弹出界面")
                        .setPositiveButton("开启", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + NormalSettingsActivity.this.getPackageName()));
                                startActivityForResult(intent, 990);
                            }
                        }).show();
                return false;
            }
        }
        return true;
    }

    public void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NormalSettingsActivity.this);
        builder.setTitle("提示");
        builder.setMessage("请打开自启动。\n请点击\"设置\"-打开自启动\n部分手机适用这种方式打开");
        // 拒绝, 退出应用
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.setPositiveButton("设置",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToSetting(NormalSettingsActivity.this);
                    }
                });
        builder.setCancelable(false);
        AlertDialog dialog = builder.show();
    }

    public void goToSetting(Context context) {
        Intent intent = new Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }

    //重写onKeyDown
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(getClass().getName(), "onKeyDown:keyCode === " + keyCode);
        //Toast.makeText(NormalSettingsActivity.this, "keyCode:" + keyCode, Toast.LENGTH_LONG).show();
        if (keyCode == KeyEvent.KEYCODE_BACK) {

        }
        if (keyCode == KeyEvent.KEYCODE_CALL) {
            return true;
        }
        if(keyCode == 6 || keyCode == 3 || keyCode == 0 ||  keyCode == 26){
            Log.d(getClass().getName(), "onKeyDown:keyCode ===||||| " + keyCode);
        }
        return super.onKeyDown(keyCode, event);
    }
}
