package hzhl.net.hlcall.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import hzhl.net.hlcall.ActivityCollector;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.utils.DataCache;
import hzhl.net.hlcall.utils.StatusBarUtil;

/**
 * Created by guang on 2018/4/2.
 */

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int PERMISSON_REQUESTCODE = 0;
    private LayoutInflater layoutInflater;
    private TextView mTvTitle;
    private ImageView mIvBack;
    private TextView mTvRight;
    private ImageView mIvRight;
    private TextView mTvLeft;
    private TextView mTvLeft2;

    private DataCache mDataCache;
    private boolean isHideKeyboard = true;
    private ProgressDialog progDialog = null;// 搜索时进度条
    /**
     * 需要进行检测的权限数组
     */
    protected String[] needPermissions = {
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    protected abstract int getLayoutResID();

    protected abstract String getTopTitle();

    protected abstract void init();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(this.getClass().getName(), "onCreate: ");
        setContentView(R.layout.activity_base);
        ActivityCollector.addActivity(this);
        //setRequestedOrientation(ActivityInfo .SCREEN_ORIENTATION_LANDSCAPE);// 横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
        getWindow().setBackgroundDrawableResource(R.drawable.welcome_bg);

        layoutInflater = LayoutInflater.from(this);
        initCommonTitleView();
        addContentView();
        ButterKnife.bind(this);
        mDataCache = new DataCache(this);
        initBase();
        init();

        checkPermissions(needPermissions);


    }

    private void initCommonTitleView() {
        mTvTitle = findViewById(R.id.tv_title);
        mIvBack = findViewById(R.id.iv_back);
        mIvBack.setOnClickListener(this);
        mTvRight = findViewById(R.id.tv_right);
        mTvRight.setOnClickListener(this);
        mIvRight = findViewById(R.id.iv_right);
        mIvRight.setOnClickListener(this);
        mTvLeft = findViewById(R.id.tv_left);
        mTvLeft.setOnClickListener(this);
        mTvLeft2 = findViewById(R.id.tv_left2);
        mTvLeft2.setOnClickListener(this);
    }

    private void addContentView() {
        View view = layoutInflater.inflate(getLayoutResID(), null);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        LinearLayout layoutContent = (LinearLayout) findViewById(R.id.layout_content);
        layoutContent.removeAllViews();
        layoutContent.addView(view);
    }

    private void initBase() {
        StatusBarUtil.setStatusBarLightMode(getWindow());
        // steepTitle();
        setTopTitle(getTopTitle());
        checkPermissions(needPermissions);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_right:
                onClickRightTv(v);
                break;
            case R.id.iv_right:
                onClickRightIv(v);
                break;
            case R.id.tv_left:
                onClickLeftTv(v);
                break;
            case R.id.tv_left2:
                onClickLeft2Tv(v);
                break;

        }
    }


    public void setTopTitle(String title) {
        mTvTitle.setText(title);
    }

    public DataCache getDataCache() {
        return mDataCache;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);

    }


    /**
     * 申请权限
     */
    private void checkPermissions(String... permissions) {
        List<String> needRequestPermissonList = findDeniedPermissions(permissions);//找需要申请的权限
        if (null != needRequestPermissonList && needRequestPermissonList.size() > 0) {
            Logger.d(needRequestPermissonList.size() + "个需要申请");
            //ActivityCompat.requestPermissions(this, needRequestPermissonList.toArray(new String[needRequestPermissonList.size()]), PERMISSON_REQUESTCODE);

            ActivityCompat.requestPermissions(this, new String[]{needRequestPermissonList.get(0)}, PERMISSON_REQUESTCODE);

        }
    }

    /**
     * 获取权限集中需要申请权限的列表
     */
    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissonList = new ArrayList<String>();
        for (String perm : permissions) {
            //shouldShowRequestPermissionRationale返回false是操作了拒绝且不再访问，返回true是拒绝
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                needRequestPermissonList.add(perm);
            }
        }
        return needRequestPermissonList;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] paramArrayOfInt) {
        if (requestCode == PERMISSON_REQUESTCODE) {
            if (!verifyPermissions(paramArrayOfInt)) {
                //还有权限未申请
                showMissingPermissionDialog();
                // if (isNeedCheck) {checkPermissions(needPermissions);} 防止去检测权限，不停的弹框
                //isNeedCheck = false;
            } else {
                //权限都申请了
                if (findDeniedPermissions(needPermissions).size() > 0) {
                    checkPermissions(needPermissions);
                } else {
                    //进入APP主页
                }
            }
        }
    }

    /**
     * 检测是否 所有的权限都已经授权
     */
    public boolean verifyPermissions(int[] grantResults) {
        //防止grantResults长度为0
        if (grantResults==null||grantResults.length==0){
            return false;
        }
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public boolean isOpenPermission(String permission) {
        int checkPermission = getPackageManager().checkPermission(permission, getPackageName());
        if (checkPermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public void showMissingPermissionDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("当前应用缺少必要权限。\n请点击\"设置\"-\"权限\"-打开所需权限");
        // 拒绝, 退出应用
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.setPositiveButton("设置",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToSetting(BaseActivity.this);
                    }
                });
        builder.setCancelable(false);
        AlertDialog dialog = builder.show();
    }

    public void goToSetting(Context context) {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }

    protected void setRightTv(String s) {
        if (mTvRight != null) {
            mTvRight.setText(s);
            mTvRight.setVisibility(View.VISIBLE);
        }
    }

    protected void onClickRightTv(View view) {

    }

    protected void setRightIv(int res) {
        if (mIvRight != null) {
            mIvRight.setImageResource(res);
            mIvRight.setVisibility(View.VISIBLE);
        }
    }

    protected void onClickRightIv(View view) {
    }

    protected void setLeftTv(String s) {
        if (mTvLeft != null) {
            mTvLeft.setText(s);
            mTvLeft.setVisibility(View.VISIBLE);
        }
    }

    protected void onClickLeftTv(View view) {

    }

    protected void setLeft2Tv(String s) {
        if (mTvLeft2 != null) {
            mTvLeft2.setText(s);
            mTvLeft2.setVisibility(View.VISIBLE);
        }
    }

    protected void onClickLeft2Tv(View view) {

    }

    /**
     * 隐藏返回键
     */
    protected void setLeftBackGone() {
        if (mIvBack != null) {
            mIvBack.setVisibility(View.GONE);
        }
    }

    protected void setLeftBackVisible() {
        if (mIvBack != null) {
            mIvBack.setVisibility(View.VISIBLE);
        }
    }

    protected void setRightIvGone() {
        if (mIvRight != null) {
            mIvRight.setVisibility(View.GONE);
        }
    }

    protected void setRightTvGone() {
        if (mTvRight != null) {
            mTvRight.setVisibility(View.GONE);
        }
    }

    protected void setCommonTitleBarGone() {
        LinearLayout llMain = findViewById(R.id.ll_main);
        llMain.removeViewAt(0);
    }

    protected void showToast(String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void jump(Class<? extends Activity> aClass) {
        Intent intent = new Intent(this, aClass);
        startActivity(intent);
    }

    /**
     * 显示进度框
     */
    public void showProgressDialog() {
        if (progDialog == null) {
            progDialog = new ProgressDialog(this);
        }
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在访问");
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    public void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }


    /**
     * 关闭 点击空白位置隐藏软键盘
     */
    protected void closeHideKeyboard() {
        isHideKeyboard = false;
    }


    //加载沉浸式状态栏
    private void steepTitle() {
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //注意要清除 FLAG_TRANSLUCENT_STATUS flag
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.toolbar));
        }
    }


    // 点击空白处软键盘隐藏
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if (isHideKeyboard) {
            if (isShouldHideInput(v, ev)) {
                hideSoftInput(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击
     * EditText时没必要隐藏
     */
    private static boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditView上，和用户用轨迹球选择其他的焦点
        return false;
    }

    /**
     * 多种隐藏软件盘方法的其中一种
     *
     * @param token
     */
    protected void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService
                    (Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
