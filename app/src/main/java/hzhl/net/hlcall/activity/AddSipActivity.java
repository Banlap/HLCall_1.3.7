package hzhl.net.hlcall.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import org.linphone.core.AccountCreator;
import org.linphone.core.Core;
import org.linphone.core.ProxyConfig;
import org.linphone.core.TransportType;

import butterknife.Bind;
import butterknife.OnClick;
import hzhl.net.hlcall.BuildConfig;
import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.api.Api;
import hzhl.net.hlcall.entity.SipProfileEntityDao;
import hzhl.net.hlcall.utils.DataCache;
import hzhl.net.hlcall.utils.MyLog;
import hzhl.net.hlcall.view.LastInputEditText;
import hzhl.net.hlcall.view.MyDialog;

public class AddSipActivity extends BaseActivity {
    public static final String MOV_IP = "mov_ip";

    @Bind(R.id.edit_domain)
    LastInputEditText mEditDomain;
    @Bind(R.id.edit_mov_domain)
    LastInputEditText edit_mov_domain;
    @Bind(R.id.edit_user)
    LastInputEditText mEditUser;
    @Bind(R.id.edit_code)
    LastInputEditText mEditCode;
    @Bind(R.id.edit_shou_quan_user)
    LastInputEditText mEditShouQuanUser;
    @Bind(R.id.tv_type)
    TextView mTvType;
    @Bind(R.id.tv_save)
    TextView mTvSave;
    @Bind(R.id.tv_app_user)
    TextView mTvAppUser;
    private DataCache dataCache;
    private SipProfileEntityDao sipProfileEntityDao;
    private ProxyConfig proxyConfig;//ProxyConfigList点击进来此页面的proxyConfig
    private Intent intent;
    private MyDialog mDialogSipType;
    private TextView tvUdp;
    private TextView tvTcp;
    private AccountCreator mAccountCreator;
    private Core core;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_add_sip;
    }

    @Override
    protected String getTopTitle() {
        return null;
    }

    @Override
    protected void init() {
        setLeftTv("设置服务器与账号");
        setRightTv("删除");
        initSipTypeDialog();
        dataCache = new DataCache(this);
        intent = getIntent();
        if (BuildConfig.DEBUG) {
            mEditUser.setText("8008");
            mEditDomain.setText("192.168.0.245:5060");
            edit_mov_domain.setText("192.168.0.245:5060");
            mEditCode.setText("1");
        }
        if (LinphoneService.isReady()) {
            mAccountCreator = LinphoneService.getCore().createAccountCreator(null);
            core = LinphoneService.getCore();
            if (core != null) {
                String idkey = intent.getStringExtra("Idkey");
                proxyConfig = core.getProxyConfigByIdkey(idkey);
                if (proxyConfig != null) {
                    mEditUser.setText(proxyConfig.getIdentityAddress().getUsername());
                    mEditCode.setText(proxyConfig.getIdentityAddress().getPassword());
                    mEditDomain.setText(proxyConfig.getIdentityAddress().getDomain()+":"+ proxyConfig.getIdentityAddress().getPort());
                    edit_mov_domain.setText(dataCache.getString(proxyConfig.getIdentityAddress().getUsername()+MOV_IP));
                    mTvAppUser.setText(proxyConfig.getIdentityAddress().getUsername());
                    switch (proxyConfig.getIdentityAddress().getTransport().toInt()) {
                        case 0:
                            mTvType.setText("UDP");
                            tvUdp.setSelected(true);
                            tvTcp.setSelected(false);
                            break;
                        case 1:
                            mTvType.setText("TCP");
                            tvUdp.setSelected(false);
                            tvTcp.setSelected(true);
                            break;
                    }
                }
            }


        }
    }

    protected void initSipTypeDialog() {
        //设置网络传输方式 弹窗
        MyDialog.Builder builder = new MyDialog.Builder(this);
        mDialogSipType = builder.view(R.layout.dialog_sip_type)
                .style(R.style.dialog)
                .widthdp(250)
                .cancelTouchout(true)
                .build();

        tvUdp = mDialogSipType.getView().findViewById(R.id.tv_udp);
        tvTcp = mDialogSipType.getView().findViewById(R.id.tv_tcp);
        tvUdp.setSelected(true);//默认UDP网络传输方式
        tvUdp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvUdp.setSelected(true);
                tvTcp.setSelected(false);
            }
        });
        tvTcp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvUdp.setSelected(false);
                tvTcp.setSelected(true);
            }
        });

        builder.addViewOnclick(R.id.tv_yes, new View.OnClickListener() {
            @Override
            //添加“确定”按钮
            public void onClick(View v) {
                if (!tvUdp.isSelected() && !tvTcp.isSelected()) {
                    showToast("请选择网络传输方式");
                    return;
                }
                String type = tvUdp.isSelected() ? "UDP" : "TCP";
                mTvType.setText(type);
                mDialogSipType.dismiss();
            }
        });
        builder.addViewOnclick(R.id.tv_no, new View.OnClickListener() {
            @Override
            //添加“取消”按钮
            public void onClick(View v) {
                mDialogSipType.dismiss();
            }
        });
    }

    @Override
    protected void onClickRightTv(View view) {
        super.onClickRightTv(view);
        if (proxyConfig == null) {
            showToast("当前账户未保存");
        } else {
            if (core != null) {
                core.removeProxyConfig(proxyConfig);
            }
            // 如果当前代理配置已被删除，则设置新的默认代理配置
            if (core != null && core.getDefaultProxyConfig() == null) {
                ProxyConfig[] proxyConfigs = core.getProxyConfigList();
                if (proxyConfigs.length > 0) {
                    core.setDefaultProxyConfig(proxyConfigs[0]);
                    Api.setIP(dataCache.getString(proxyConfigs[0].getContact().getUsername()+MOV_IP));
                }else {
                    Api.setIP("");
                }
            }

            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @OnClick({R.id.tv_cancel, R.id.tv_save, R.id.ll_sip_type})
    public void onClickVoid(View view) {
        switch (view.getId()) {
            case R.id.tv_save:
                saveSipProfile();
                break;
            case R.id.tv_cancel:
                finish();
                break;
            case R.id.ll_sip_type:
                mDialogSipType.show();
                break;
        }
    }

    protected void saveSipProfile() {
        if (mAccountCreator == null) {
            Logger.d("mAccountCreator==null");
            return;
        }
        if (mEditDomain.getText().toString().trim().isEmpty()) {
            showToast("请输入SIP服务器IP地址+端口");
            return;
        }
        if (mEditUser.getText().toString().trim().isEmpty()) {
            showToast("请输入用户名");
            return;
        }
        if (mEditCode.getText().toString().trim().isEmpty()) {
            showToast("请输入密码");
            return;
        }
        if (edit_mov_domain.getText().toString().trim().isEmpty()) {
            showToast("请输入巡检服务器IP");
            return;
        }


        mAccountCreator.setUsername(mEditUser.getText().toString());
        mAccountCreator.setDomain(mEditDomain.getText().toString());
        mAccountCreator.setPassword(mEditCode.getText().toString());

        if ("UDP".equals(mTvType.getText().toString())) {
            mAccountCreator.setTransport(TransportType.Udp);
        } else if ("TCP".equals(mTvType.getText().toString())) {
            mAccountCreator.setTransport(TransportType.Tcp);
        }else {
            mAccountCreator.setTransport(TransportType.Udp);
        }

        if (core != null) {
            if (proxyConfig != null) {
                //先清除选中进来的proxyConfig
                core.removeProxyConfig(proxyConfig);

            }
            //这将自动创建代理配置和身份验证信息，并将它们添加到核心
            // This will automatically create the proxy config and auth info and add them to the Core
            ProxyConfig cfg = mAccountCreator.createProxyConfig();

            MyLog.d(cfg.getDomain());
            // Make sure the newly created one is the default确保新创建的是默认值
            LinphoneService.getCore().setDefaultProxyConfig(cfg);

        }


        String movIp = edit_mov_domain.getText().toString();
        dataCache.putString(mAccountCreator.getUsername()+MOV_IP,movIp);
        Api.setIP(movIp);


        // sipProfileEntityDao.insertOrReplace(sipProfileEntity);
        /*DataCache dataCache = getDataCache();
        dataCache.put("sipEntity", sipEntity);*/
        intent.putExtra("type", "modify");
        setResult(RESULT_OK, intent);
        showToast("添加成功");
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //重写onKeyDown
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(getClass().getName(), "onKeyDown:keyCode === " + keyCode);
        /** banlap：bug：实体键 拨号键和 挂机键不操作 */
        if (keyCode == KeyEvent.KEYCODE_CALL) {
            Logger.d("MainActivity不操作");
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_ENDCALL) {
            Logger.d("MainActivity不操作");
            return true;
        }
        /** banlap：bug：实体键 拨号键和 挂机键不操作  --end*/
        return super.onKeyDown(keyCode, event);
    }

}
