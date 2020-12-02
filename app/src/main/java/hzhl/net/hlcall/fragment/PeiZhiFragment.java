package hzhl.net.hlcall.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.linphone.core.Core;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hzhl.net.hlcall.ActivityCollector;
import hzhl.net.hlcall.App;
import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.activity.AboutMeActivity;
import hzhl.net.hlcall.activity.BaseFragment;
import hzhl.net.hlcall.activity.BlacklistActivity;
import hzhl.net.hlcall.activity.MissedCallWarnActivity;
import hzhl.net.hlcall.activity.MovingActivity;
import hzhl.net.hlcall.activity.NormalSettingsActivity;
import hzhl.net.hlcall.activity.RecordsActivity;
import hzhl.net.hlcall.activity.RingtoneActivity;
import hzhl.net.hlcall.activity.SipSettingActivity;
import hzhl.net.hlcall.activity.TransferActivity;
import hzhl.net.hlcall.activity.VoiceSettingActivity;
import hzhl.net.hlcall.entity.SettingEntity;
import hzhl.net.hlcall.entity.SettingEntityDao;
import hzhl.net.hlcall.view.MyDialog;
import hzhl.net.hlwork.ui.setting.VideoSettingActivity;
import hzhl.net.hlwork.ui.work.WorkTabActivity;

/**
 * Created by guang on 2019/7/29.
 */

public class PeiZhiFragment extends BaseFragment {
    @Bind(R.id.iv_AutoAnswerCall)
    ImageView ivAutoAnswerCall;
    @Bind(R.id.iv_AutoAnswerCall_video)
    ImageView iv_AutoAnswerCall_video;
    @Bind(R.id.iv_bohao_type)
    ImageView ivBoHaoType;
    @Bind(R.id.iv_float)
    ImageView ivFloat;
    @Bind(R.id.iv_autoStart)
    ImageView ivAutoStart;

    private SettingEntityDao settingEntityDao;
    private SettingEntity settingEntity;
    private MyDialog myDialog;
    private ImageView ivSip;
    private ImageView ivSIM;
    public static final int READ_EXTERNAL_STORAGE_RESULT = 1117;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_peizhi;
    }

    @Override
    protected void initView() {
        initSipTypeDialog();
        settingEntityDao = App.getDaoInstant().getSettingEntityDao();
        settingEntity = settingEntityDao.load(1L);
        if (settingEntity == null) {
            settingEntity = new SettingEntity();
            settingEntityDao.insertOrReplace(settingEntity);
            ivSip.setSelected(true);//默认选择sip呼叫方式
        } else {
            ivAutoAnswerCall.setSelected(settingEntity.getIsAutoAnswerCall());
            iv_AutoAnswerCall_video.setSelected(settingEntity.getIsAutoAnswerCallVideo());
            ivBoHaoType.setSelected(settingEntity.getIsBoHaoType());
            ivFloat.setSelected(settingEntity.getIsFloat());
            ivAutoStart.setSelected(settingEntity.getIsAutoStart());
            int boHaoType = settingEntity.getBoHaoType();
            if (0 == boHaoType) {
                ivSip.setSelected(true);
            } else if (1 == boHaoType) {
                ivSIM.setSelected(true);
            }
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, super.onCreateView(inflater, container, savedInstanceState));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @OnClick({R.id.ll_sip, R.id.ll_blacklist, R.id.ll_missedCallWarn, R.id.iv_AutoAnswerCall
            , R.id.iv_bohao_type, R.id.iv_float, R.id.tv_finish, R.id.ll_transfer, R.id.ll_voice
            , R.id.ll_feature, R.id.iv_autoStart, R.id.ll_about_me, R.id.ll_records,R.id.iv_AutoAnswerCall_video
            ,R.id.ll_moving,R.id.ll_work,R.id.ll_video, R.id.ll_normal
    })
    public void onClickVoid(View view) {
        switch (view.getId()) {
            case R.id.ll_normal:
                jump(NormalSettingsActivity.class);
                break;
            case R.id.ll_sip:
                jump(SipSettingActivity.class);
                break;
            case R.id.ll_blacklist:
                jump(BlacklistActivity.class);
                break;
            case R.id.ll_missedCallWarn:
                jump(MissedCallWarnActivity.class);
                break;
            case R.id.iv_AutoAnswerCall:
                if (view.isSelected()) {
                    view.setSelected(false);
                    iv_AutoAnswerCall_video.setSelected(false);
                    settingEntity.setIsAutoAnswerCallVideo(false);
                    settingEntity.setIsAutoAnswerCall(false);
                } else {
                    view.setSelected(true);
                    settingEntity.setIsAutoAnswerCall(true);
                }
                settingEntityDao.insertOrReplace(settingEntity);
                break;
            case R.id.iv_AutoAnswerCall_video:
                if (view.isSelected()) {
                    view.setSelected(false);
                    settingEntity.setIsAutoAnswerCallVideo(false);
                } else {
                    view.setSelected(true);
                    ivAutoAnswerCall.setSelected(true);
                    settingEntity.setIsAutoAnswerCall(true);
                    settingEntity.setIsAutoAnswerCallVideo(true);
                }
                settingEntityDao.insertOrReplace(settingEntity);
                break;
            case R.id.iv_bohao_type:
                if (view.isSelected()) {
                    view.setSelected(false);
                    settingEntity.setIsBoHaoType(false);
                } else {
                    view.setSelected(true);
                    settingEntity.setIsBoHaoType(true);
                    myDialog.show();
                }
                settingEntityDao.insertOrReplace(settingEntity);
                break;
            case R.id.iv_float:
                if (!openFloatPermission()) {
                    return;
                }
                if (view.isSelected()) {
                    view.setSelected(false);
                    settingEntity.setIsFloat(false);
                } else {
                    view.setSelected(true);
                    settingEntity.setIsFloat(true);

                }
                settingEntityDao.insertOrReplace(settingEntity);
                break;
            case R.id.iv_autoStart:
                if (view.isSelected()) {
                    view.setSelected(false);
                    settingEntity.setIsAutoStart(false);
                } else {
                    showMissingPermissionDialog();
                    view.setSelected(true);
                    settingEntity.setIsAutoStart(true);
                }
                settingEntityDao.insertOrReplace(settingEntity);
                break;
            case R.id.tv_finish:
                ActivityCollector.finishAll();
                break;
            case R.id.ll_transfer:
                jump(TransferActivity.class);
                break;
            case R.id.ll_voice:
                jump(VoiceSettingActivity.class);
                break;
            case R.id.ll_video:
                jump(VideoSettingActivity.class);
                break;
            case R.id.ll_feature:
                jump(RingtoneActivity.class);
                break;
            case R.id.ll_about_me:
                jump(AboutMeActivity.class);
                break;
            case R.id.ll_moving:
                Core core;
                ProxyConfig config;
                if (!LinphoneService.isReady())return;
                core = LinphoneService.getCore();
                if (core == null)return;
                config = core.getDefaultProxyConfig();
                if (config == null || config.getState() != RegistrationState.Ok ){
                    showToast("请先登录");
                    return;
                }
                jump(MovingActivity.class);
                break;
            case R.id.ll_records:
                if (isOpenPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    jump(RecordsActivity.class);
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
                            , READ_EXTERNAL_STORAGE_RESULT);
                }
                break;
            case R.id.ll_work:
                    jump(WorkTabActivity.class);
                break;
        }
    }

    protected void initSipTypeDialog() {
        //设置网络传输方式 弹窗
        MyDialog.Builder builder = new MyDialog.Builder(getmContext());
        myDialog = builder.view(R.layout.dialog_bohao_type)
                .style(R.style.dialog)
                .widthdp(250)
                .cancelTouchout(true)
                .build();
        ivSip = myDialog.getView().findViewById(R.id.iv_sip);
        ivSIM = myDialog.getView().findViewById(R.id.iv_sim);
        LinearLayout llSip = myDialog.getView().findViewById(R.id.ll_sip);
        LinearLayout llSIM = myDialog.getView().findViewById(R.id.ll_SIM);
        llSip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivSip.setSelected(true);
                ivSIM.setSelected(false);
            }
        });
        llSIM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivSip.setSelected(false);
                ivSIM.setSelected(true);
            }
        });

        builder.addViewOnclick(R.id.tv_yes, new View.OnClickListener() {
            @Override
            //添加“确定”按钮
            public void onClick(View v) {
                if (!ivSip.isSelected() && !ivSIM.isSelected()) {
                    showToast("请选择呼叫类型");
                    return;
                }
                settingEntity.setBoHaoType(ivSip.isSelected() ? 0 : 1);
                settingEntityDao.insertOrReplace(settingEntity);
                myDialog.dismiss();
            }
        });
        builder.addViewOnclick(R.id.tv_no, new View.OnClickListener() {
            @Override
            //添加“取消”按钮
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
    }

    /**
     * 判断是否打开悬浮权限
     */
    private boolean openFloatPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//判断系统版本
            if (!Settings.canDrawOverlays(getmContext())) {
                new AlertDialog.Builder(getmContext())
                        .setTitle("请开启显示悬浮窗设置")
                        .setMessage("悬浮功能需要您允许显示悬浮窗、后台弹出界面")
                        .setPositiveButton("开启", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getmContext().getPackageName()));
                                startActivityForResult(intent, 990);
                            }
                        }).show();
                return false;
            }
        }
        return true;
    }

    public void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getmContext());
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
                        goToSetting(getmContext());
                    }
                });
        builder.setCancelable(false);
        AlertDialog dialog = builder.show();
    }

    public void goToSetting(Context context) {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


}
