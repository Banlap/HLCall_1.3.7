package hzhl.net.hlcall.activity;

import android.Manifest;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import org.linphone.core.Core;
import org.linphone.core.ProxyConfig;

import java.util.Collections;

import butterknife.Bind;
import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.adapter.MovingAdapter;
import hzhl.net.hlcall.api.Api;
import hzhl.net.hlcall.api.MsgList;
import hzhl.net.hlcall.utils.DataCache;
import hzhl.net.hlcall.utils.GsonUtil;
import io.itit.itf.okhttp.Response;
import io.itit.itf.okhttp.callback.Callback;
import okhttp3.Call;

import static hzhl.net.hlcall.activity.RecordsActivity.WRITE_EXTERNAL_STORAGE_RESULT;

public class MovingActivity extends BaseActivity {

    @Bind(R.id.rec_moving)
    RecyclerView rec_moving;
    private DataCache dataCache;
    boolean isPub = false;
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_moving;
    }

    @Override
    protected String getTopTitle() {
        return "巡检记录";
    }

    @Override
    protected void init() {
        //setRightIv(R.mipmap.icon_fabiao);
        setRightTv("编辑");
        dataCache = new DataCache(getApplicationContext());
        String ip = dataCache.getString("moving_ip");
        if (ip != null&&!ip.isEmpty()) Api.setIP(ip);

        if (!isOpenPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                    , WRITE_EXTERNAL_STORAGE_RESULT);
        }

        rec_moving.setLayoutManager(new LinearLayoutManager(this));
        getMsg(30);
    }

    private void getMsg(int dd) {
        Core core;
        ProxyConfig config;
        if (!LinphoneService.isReady())return;
        core = LinphoneService.getCore();
        if (core == null)return;
        config = core.getDefaultProxyConfig();
        if (config == null)return;
        if (config.getContact() == null)return;
        String name = config.getContact().getUsername();/*
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        Date endDate = calendar.getTime();
        calendar.add(Calendar.DATE,-dd-1);
        Date startDate = calendar.getTime();
        DateFormat format = new SimpleDateFormat("YYYY-MM-dd hh:MM:ss", Locale.CHINA);*/
        new Thread(() -> {
//            Api.getMsgList(name, format.format(startDate), format.format(endDate),
            Api.getMsgList(name, "", "",
                    new Callback() {
                        @Override
                        public void onFailure(Call call, Exception e, int id) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response, int id) {
                            MsgList msgList = GsonUtil.fromJson(response,MsgList.class);
                            if (msgList == null)return;
                            if (msgList.getItems() == null)return;
                            runOnUiThread(() -> {
                                Collections.reverse(msgList.getItems());
                                rec_moving.setAdapter(new MovingAdapter(MovingActivity.this,msgList.getItems()));
                            });
                        }
                    }
            );
        }).start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPub){
            isPub = false;
            getMsg(30);
        }

    }

    @Override
    protected void onClickRightTv(View view) {
        //jump(MovingPubActivity.class);
        startActivityForResult(new Intent(this,MovingPubActivity.class),1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == 1001)isPub = true;
    }
}
