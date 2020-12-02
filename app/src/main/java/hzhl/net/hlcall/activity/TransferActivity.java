package hzhl.net.hlcall.activity;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.OnClick;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.view.MyDialog;

public class TransferActivity extends BaseActivity {
    @Bind(R.id.tv_alway)
    TextView tvAlway;
    @Bind(R.id.tv_connected)
    TextView tvConnected;
    @Bind(R.id.tv_timeout)
    TextView tvTimeout;
    @Bind(R.id.tv_error)
    TextView tvError;

    private MyDialog mDialogType;
    private TextView tvTitle, tvTitle2;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_transfer;
    }

    @Override
    protected String getTopTitle() {
        return null;
    }

    @Override
    protected void init() {
        setLeftTv("来电转移");
        initTypeDialog();
    }

    @OnClick({R.id.ll_alway, R.id.ll_connected, R.id.ll_timeout, R.id.ll_error})
    public void onClickVoid(View view) {
        switch (view.getId()) {
            case R.id.ll_alway:
                showDialog(getString(R.string.alway), getString(R.string.alway2));
                break;
            case R.id.ll_connected:
                showDialog(getString(R.string.connected), getString(R.string.connected2));
                break;
            case R.id.ll_timeout:
                showDialog(getString(R.string.timeout), getString(R.string.timeout2));
                break;
            case R.id.ll_error:
                showDialog(getString(R.string.call_error), getString(R.string.call_error2));
                break;
        }
    }

    private void showDialog(String title, String title2) {
        tvTitle.setText(title);
        tvTitle2.setText(title2);
        mDialogType.show();
    }

    protected void initTypeDialog() {
        //设置来电转接方式 弹窗
        MyDialog.Builder builder = new MyDialog.Builder(this);
        mDialogType = builder.view(R.layout.dialog_transfer)
                .style(R.style.dialog)
                .widthdp(300)
                .cancelTouchout(true)
                .build();

        tvTitle = mDialogType.getView().findViewById(R.id.tv_title);
        tvTitle2 = mDialogType.getView().findViewById(R.id.tv_title2);
        EditText editNumber = mDialogType.getView().findViewById(R.id.edit_number);

        //添加“确定”按钮
        builder.addViewOnclick(R.id.tv_yes, v -> mDialogType.dismiss());
        //添加“取消”按钮
        builder.addViewOnclick(R.id.tv_no, v -> mDialogType.dismiss());
    }

}
