package hzhl.net.hlcall.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.OnClick;
import hzhl.net.hlcall.App;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.activity.BaseFragment;
import hzhl.net.hlcall.activity.BohaoActivity;
import hzhl.net.hlcall.activity.TongXunLuInfoActivity;
import hzhl.net.hlcall.entity.ContactsListEntity;
import hzhl.net.hlcall.entity.SettingEntity;
import hzhl.net.hlcall.entity.SettingEntityDao;
import hzhl.net.hlcall.utils.LinphoneUtils;
import hzhl.net.hlcall.utils.StringUtil;

/**
 * Created by guang on 2019/8/9.
 */

public class ContactsDetailFg extends BaseFragment {
    @Bind(R.id.tv_number)
    TextView tvNumber;
    private boolean isSipBohaoType = true;//是否sip方式拨号
    private ContactsListEntity entity;

    @Override
    protected int getLayoutResID() {
        return R.layout.fg_xongxunlu_detail_1;
    }

    @Override
    protected void initView() {
        Bundle bundle = getArguments();
        entity = (ContactsListEntity) bundle.getSerializable("AddressListEntity");
        if (entity != null) {
           // tvNumber.setText(entity.getNumber());
        }
    }

    @OnClick({R.id.tv_modify, R.id.tv_del, R.id.iv_call})
    public void onClickVoid(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.tv_modify:
                intent = new Intent(getmContext(), TongXunLuInfoActivity.class);
                intent.putExtra("AddressListEntity", entity);
                startActivity(intent);
                break;
            case R.id.tv_del:


                break;
            case R.id.iv_call:
                String number = tvNumber.getText().toString().trim();
                break;
        }
    }

}
