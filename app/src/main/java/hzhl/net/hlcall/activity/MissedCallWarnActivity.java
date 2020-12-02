package hzhl.net.hlcall.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import hzhl.net.hlcall.App;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.entity.SettingEntity;
import hzhl.net.hlcall.entity.SettingEntityDao;
import hzhl.net.hlcall.utils.MyLog;

public class MissedCallWarnActivity extends BaseActivity {
    @Bind(R.id.iv_never)
    ImageView ivNever;
    @Bind(R.id.iv_one)
    ImageView ivOne;
    @Bind(R.id.iv_two)
    ImageView ivTwo;
    @Bind(R.id.iv_three)
    ImageView ivThree;
    @Bind(R.id.iv_four)
    ImageView ivFour;
    @Bind(R.id.iv_five)
    ImageView ivFive;
    @Bind(R.id.tv_never)
    TextView tvNever;
    @Bind(R.id.tv_one)
    TextView tvOne;
    @Bind(R.id.tv_two)
    TextView tvTwo;
    @Bind(R.id.tv_three)
    TextView tvThree;
    @Bind(R.id.tv_four)
    TextView tvFour;
    @Bind(R.id.tv_five)
    TextView tvFive;


    private List<ImageView> imageViewList;
    private List<TextView> textViewList;
    private SettingEntityDao settingEntityDao;
    private SettingEntity settingEntity;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_missed_call_warn;
    }

    @Override
    protected String getTopTitle() {
        return null;
    }

    @Override
    protected void init() {
        setLeftTv("未接来电提醒");
        imageViewList = Arrays.asList(ivNever, ivOne, ivTwo, ivThree, ivFour, ivFive);
        textViewList = Arrays.asList(tvNever, tvOne, tvTwo, tvThree, tvFour, tvFive);
        initViewChoose(0);
        settingEntityDao = App.getDaoInstant().getSettingEntityDao();
        settingEntity = settingEntityDao.load(1L);
        if (settingEntity == null) {
            settingEntity = new SettingEntity();
            settingEntityDao.insertOrReplace(settingEntity);
        } else {
            MyLog.d("posi--" + settingEntity.getMissedCallWarn());
            initViewChoose(settingEntity.getMissedCallWarn());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @OnClick({R.id.ll_never, R.id.ll_one, R.id.ll_two, R.id.ll_three, R.id.ll_four
            , R.id.ll_five})
    public void onClickVoid(View view) {
        switch (view.getId()) {
            case R.id.ll_never:
                initViewChoose(0);
                saveSettingEntity(0);
                break;
            case R.id.ll_one:
                initViewChoose(1);
                saveSettingEntity(1);
                break;
            case R.id.ll_two:
                initViewChoose(2);
                saveSettingEntity(2);
                break;
            case R.id.ll_three:
                initViewChoose(3);
                saveSettingEntity(3);
                break;
            case R.id.ll_four:
                initViewChoose(4);
                saveSettingEntity(4);
                break;
            case R.id.ll_five:
                initViewChoose(5);
                saveSettingEntity(5);
                break;
        }
    }

    protected void initViewChoose(int position) {
        for (int i = 0; i < imageViewList.size(); i++) {
            imageViewList.get(i).setVisibility(i == position ? View.VISIBLE : View.INVISIBLE);
            textViewList.get(i).setSelected(i == position);
        }
    }

    protected void saveSettingEntity(int count) {
        settingEntity.setMissedCallWarn(count);
        settingEntityDao.insertOrReplace(settingEntity);
        finish();
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
