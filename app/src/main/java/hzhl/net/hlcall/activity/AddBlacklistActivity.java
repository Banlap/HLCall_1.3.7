package hzhl.net.hlcall.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import hzhl.net.hlcall.App;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.entity.BlacklistEntity;
import hzhl.net.hlcall.entity.BlacklistEntityDao;
import hzhl.net.hlcall.entity.DaoSession;
import hzhl.net.hlcall.entity.SipProfileEntity;
import hzhl.net.hlcall.entity.SipProfileEntityDao;
import hzhl.net.hlcall.view.MyDialog;

public class AddBlacklistActivity extends BaseActivity {
    @Bind(R.id.edit_number)
    EditText mEditNumber;

    private BlacklistEntityDao blacklistEntityDao;
    private BlacklistEntity blacklistEntity;
    private Intent intent;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_add_blacklist;
    }

    @Override
    protected String getTopTitle() {
        return null;
    }

    @Override
    protected void init() {
        setLeftTv("添加名单");
        DaoSession daoSession = App.getDaoInstant();
        blacklistEntityDao = daoSession.getBlacklistEntityDao();
        intent = getIntent();
        blacklistEntity = (BlacklistEntity) intent.getSerializableExtra("blacklistEntity");
        if (blacklistEntity != null) {
            mEditNumber.setText(blacklistEntity.getUser());
        } else {
            blacklistEntity = new BlacklistEntity();
        }
    }


    @OnClick({R.id.tv_cancel, R.id.tv_save})
    public void onClickVoid(View view) {
        switch (view.getId()) {
            case R.id.tv_save:
                saveBlack();
                break;
            case R.id.tv_cancel:
                finish();
                break;
        }
    }

    protected void saveBlack() {
        String number = mEditNumber.getText().toString().trim();
        if (number.isEmpty()) {
            showToast("请输入号码");
            return;
        }
        List<BlacklistEntity> list = blacklistEntityDao.loadAll();
        if (list.size() >= 20) {
            showToast("最多添加20条黑名单");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUser().equals(number)) {
                showToast("不能重复添加");
                return;
            }
        }
        blacklistEntity.setUser(number);
        blacklistEntityDao.insertOrReplace(blacklistEntity);
        intent.putExtra("blacklistEntity", blacklistEntity);
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
