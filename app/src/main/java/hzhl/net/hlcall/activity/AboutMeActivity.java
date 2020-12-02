package hzhl.net.hlcall.activity;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.utils.PackageUtils;

public class AboutMeActivity extends AppCompatActivity {

    @Bind(R.id.tv_app_version)
    TextView tvAppVersion;
    @Bind(R.id.tv_company_name)
    TextView tvCompanyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);
        ButterKnife.bind(this);
        tvCompanyName .getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG );
        tvCompanyName.getPaint().setAntiAlias(true);//抗锯齿
        tvAppVersion.setText(String.format("HLCall Version %s", PackageUtils.getVersionName(this)));
    }

    @OnClick(R.id.tv_company_name)
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_company_name:
                //代码实现跳转
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(getString(R.string.company_address));//此处填链接
                intent.setData(content_url);
                startActivity(intent);
                break;
        }
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
