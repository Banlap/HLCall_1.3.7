package hzhl.net.hlcall.activity;

import android.content.Intent;
import android.net.sip.SipProfile;
import android.os.Bundle;
import android.os.Handler;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.adapter.SipListAdapter;
import hzhl.net.hlcall.api.Api;
import hzhl.net.hlcall.constant.Constants;
import hzhl.net.hlcall.entity.IsChooseEntity;
import hzhl.net.hlcall.utils.DataCache;
import hzhl.net.hlcall.utils.MyLog;

public class SipSettingActivity extends BaseActivity implements SipListAdapter.OnRecyclerViewItemClickListener {
    @Bind(R.id.recy_sip)
    RecyclerView mRecyclerView;
    /*@Bind(R.id.et_pub_ip)
    EditText et_pub_ip;*/
    private List<ProxyConfig> list = new ArrayList<>();
    private List<IsChooseEntity> isChooseEntityList = new ArrayList<>();
    private List<SipProfile> sipProfileList = new ArrayList<>();
    private SipListAdapter adapter;
    public static final int ADD_SIP = 1001;
    public static final int MODIFY_SIP = 1002;
    private Handler handler = new Handler();
    private Core core;

    private DataCache dataCache;
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_sip_setting;
    }

    @Override
    protected String getTopTitle() {
        return null;
    }

    @Override
    protected void init() {
        setLeftTv("设置服务器与账号");
        setRightIv(R.drawable.icon_tianjia);
        dataCache = new DataCache(getApplicationContext());
        /*String ip = dataCache.getString("moving_ip");
        if (ip != null&&!ip.isEmpty()) et_pub_ip.setText(ip);
        else */
        //et_pub_ip.setText(Api.IP);
        adapter = new SipListAdapter(this, list, isChooseEntityList);
        adapter.setOnItemClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.shape_line));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setAdapter(adapter);
        if (LinphoneService.isReady()) {
            core = LinphoneService.getCore();
            if (core != null) {
                core.addListener(coreListenerStub);
            }
        }
        getSipListData();
    }

    private void getSipListData() {
        if (LinphoneService.isReady()) {
            core = LinphoneService.getCore();
            if (core != null) {
                isChooseEntityList.clear();
                list.clear();
                ProxyConfig[] proxyConfigs = core.getProxyConfigList();
                for (int j = 0; j < proxyConfigs.length; j++) {
                    IsChooseEntity isChooseEntity = new IsChooseEntity();
                    if (proxyConfigs[j] == core.getDefaultProxyConfig()) {
                        Logger.d(j);
                        isChooseEntity.setIsChoose(true);
                    }
                    isChooseEntityList.add(isChooseEntity);
                    list.add(proxyConfigs[j]);
                }
                adapter.setData(list, isChooseEntityList);
            }
        }
    }

    CoreListenerStub coreListenerStub = new CoreListenerStub() {
        @Override
        public void onRegistrationStateChanged(Core core, ProxyConfig proxyConfig, RegistrationState state, String message) {
            super.onRegistrationStateChanged(core, proxyConfig, state, message);
            Logger.d(proxyConfig.getIdentityAddress().getUsername() + ";state:" + state + ";message:" + message);
            updateList(proxyConfig);
        }

    };

    @Override
    protected void onClickRightIv(View view) {
        super.onClickRightIv(view);
        Intent intent = new Intent(this, AddSipActivity.class);
        startActivityForResult(intent, ADD_SIP);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void OnItemClick(int position) {
        Intent intent = new Intent(this, AddSipActivity.class);
        Logger.d(list.get(position).getIdkey());
        intent.putExtra("Idkey", list.get(position).getIdkey());
        MyLog.d("posi--" + position);
        intent.putExtra("position", position);
        startActivityForResult(intent, MODIFY_SIP);
    }

    @Override
    public void OnItemChoose(int position) {
        for (int i = 0; i < list.size(); i++) {
            isChooseEntityList.get(i).setIsChoose(false);
        }
        isChooseEntityList.get(position).setIsChoose(true);
        adapter.setData(list, isChooseEntityList);
        if (core != null) {
            Logger.d("moren");
            core.setDefaultProxyConfig(list.get(position));
            showToast("现在选择用户为：" + core.getDefaultProxyConfig().getContact().getUsername());
            //wenyeyang
            Api.setIP(dataCache.getString(core.getDefaultProxyConfig().getContact().getUsername()+AddSipActivity.MOV_IP));
            EventBus.getDefault().post(Constants.EVENT_UPDATE_UN_READ);
        }
        //showToast("现在选择用户为：" + list.get(position).getIdentityAddress().getUsername());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == ADD_SIP) {
                Logger.d("ADD_SIP");
                getSipListData();
            }
            if (requestCode == MODIFY_SIP) {
                Logger.d("MODIFY_SIP");
                getSipListData();
         /*       String type = data.getStringExtra("type");
                final int position = data.getIntExtra("position", 0);
                if ("del".equals(type)) {
                } else if ("modify".equals(type)) {
                    getSipListData();
                }*/
            }
        }
    }


    public void updateList(final ProxyConfig proxyConfig) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (list.contains(proxyConfig)) {
                    int position=0;
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i) == proxyConfig) {
                            position=i;
                        }
                    }
                    list.set(position,proxyConfig);
                    adapter.setData(list,isChooseEntityList);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        if (core != null) {
            core.removeListener(coreListenerStub);
        }
        core = null;
        super.onDestroy();
        /*String ip = et_pub_ip.getText().toString();
        if (ip.isEmpty())return;
        dataCache.putString("moving_ip",ip);
        Api.setIP(ip);*/
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
