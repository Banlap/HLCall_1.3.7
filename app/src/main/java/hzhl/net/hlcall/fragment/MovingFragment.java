package hzhl.net.hlcall.fragment;


import android.Manifest;
import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.TextView;

import org.linphone.core.Core;
import org.linphone.core.ProxyConfig;

import java.util.Collections;

import butterknife.Bind;
import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.activity.BaseFragment;
import hzhl.net.hlcall.activity.MovingPubActivity;
import hzhl.net.hlcall.adapter.MovingAdapter;
import hzhl.net.hlcall.api.Api;
import hzhl.net.hlcall.api.MsgList;
import hzhl.net.hlcall.utils.DataCache;
import hzhl.net.hlcall.utils.GsonUtil;
import io.itit.itf.okhttp.Response;
import io.itit.itf.okhttp.callback.Callback;
import okhttp3.Call;

import static hzhl.net.hlcall.activity.RecordsActivity.WRITE_EXTERNAL_STORAGE_RESULT;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovingFragment extends BaseFragment {
    public static boolean isRefresh = false; //onResume是否刷新
    @Bind(R.id.rec_moving)
    RecyclerView rec_moving;
    @Bind(R.id.ll_right)
    View ll_right;
    @Bind(R.id.tv_left)
    TextView tv_left;
    private DataCache dataCache;
    private String name;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_moving;
    }

    @Override
    protected void initView() {
        tv_left.setText("请先登录");
        dataCache = new DataCache(getActivity());
        String ip = dataCache.getString("moving_ip");
        if (ip != null&&!ip.isEmpty()) Api.setIP(ip);

        if (!isOpenPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                    , WRITE_EXTERNAL_STORAGE_RESULT);
        }

        rec_moving.setLayoutManager(new LinearLayoutManager(getActivity()));
        getMsg(30);



    }
    private void getMsg(int dd) {

        name = checkLogin();
        if (name == null)return;
        /*
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
                            if (isDetached())return;
                            showToast("获取巡检记录失败,请检查巡检服务器IP");
                        }

                        @Override
                        public void onResponse(Call call, Response response, int id) {
                            MsgList msgList = GsonUtil.fromJson(response,MsgList.class);
                            String result= "";
                            if (msgList == null)result = "数据格式错误,请检查巡检服务器IP";
                            if (msgList != null && msgList.getItems() == null)result ="暂无数据";
                            if (isDetached())return;
                            if (!result.isEmpty()){
                                showToast(result);
                                return;
                            }
                            getActivity().runOnUiThread(() -> {
                                Collections.reverse(msgList.getItems());
                                rec_moving.setAdapter(new MovingAdapter(getActivity(),msgList.getItems()));
                            });
                        }
                    }
            );
        }).start();

    }

    private String checkLogin(){
        Core core;
        ProxyConfig config;
        if (!LinphoneService.isReady())return null;
        core = LinphoneService.getCore();
        if (core == null)return null;
        config = core.getDefaultProxyConfig();
        if (config == null)return null;
        if (config.getContact() == null)return null;

        tv_left.setText("编辑");
        ll_right.setOnClickListener(this::onClickRightTv);
        String name = config.getContact().getUsername();
        if (!name.equals(this.name)){
            isRefresh = true;
        }
        return name;
    }
    @Override
    public void onResume() {
        super.onResume();
        checkLogin();
        if (isRefresh){
            isRefresh = false;
            getMsg(30);
        }

    }

    protected void onClickRightTv(View view) {
        //jump(MovingPubActivity.class);
        startActivityForResult(new Intent(getActivity(), MovingPubActivity.class),1001);
    }



}
