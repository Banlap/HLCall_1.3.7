package hzhl.net.hlcall.activity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import hzhl.net.hlcall.App;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.entity.ContactsListEntity;
import hzhl.net.hlcall.entity.BlacklistEntity;
import hzhl.net.hlcall.entity.BlacklistEntityDao;

public class TongXunLuInfoActivity extends BaseActivity {
    @Bind(R.id.tv_add_blacklist)
    TextView tvAddBlacklist;
    @Bind(R.id.tv_number)
    TextView tvNumber;
    @Bind(R.id.tv_name)
    TextView tvName;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_tong_xun_lu_info;
    }

    @Override
    protected String getTopTitle() {
        return null;
    }

    @Override
    protected void init() {
        setLeftTv("返回");
        setRightTv("完成");
        tvAddBlacklist.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        tvAddBlacklist.getPaint().setAntiAlias(true);//抗锯齿
        Intent intent = getIntent();
        ContactsListEntity entity = intent.getParcelableExtra("AddressListEntity");
        if (entity != null) {
           // tvNumber.setText(entity.getNumber());
            tvName.setText(entity.getName());
        }
    }

    @Override
    protected void onClickRightTv(View view) {
        super.onClickRightTv(view);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @OnClick({R.id.tv_add_blacklist})
    public void onClickVoid(View view) {
        switch (view.getId()) {
            case R.id.tv_add_blacklist:
                String number = tvNumber.getText().toString();
                if (number.isEmpty()) {
                    return;
                }
                BlacklistEntityDao blacklistEntityDao = App.getDaoInstant().getBlacklistEntityDao();
                List<BlacklistEntity> list = blacklistEntityDao.loadAll();
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getUser().equals(number)) {
                        showToast("不能重复添加");
                       return;
                    }
                }
                BlacklistEntity blacklistEntity=new BlacklistEntity();
                blacklistEntity.setUser(number);
                if (list.size() >= 20) {
                    showToast("最多添加20条黑名单");
                    return;
                }
                blacklistEntity.setUser(number);
                blacklistEntityDao.insertOrReplace(blacklistEntity);
                showToast("添加成功");
                finish();
                break;
        }
    }
}
