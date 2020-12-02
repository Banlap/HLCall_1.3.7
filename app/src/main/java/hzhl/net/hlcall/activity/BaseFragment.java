package hzhl.net.hlcall.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import butterknife.ButterKnife;
import hzhl.net.hlcall.R;

/**
 * Created by guang on 2018/6/21.
 */

public abstract class BaseFragment extends Fragment {
    private Context mContext;
    private View mContentView;

    protected abstract int getLayoutResID();

    protected abstract void initView();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(getLayoutResID(), container, false);
        mContext = getContext();
        ButterKnife.bind(this, mContentView);
        initView();
        return mContentView;
    }


    //加载沉浸式状态栏
    public void steepTitle() {
        if (Build.VERSION.SDK_INT >= 21) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //注意要清除 FLAG_TRANSLUCENT_STATUS flag
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.toolbar));
        }
    }

    public Context getmContext() {
        return mContext;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    public View getContentView() {
        return mContentView;
    }

    public void jump(Class<? extends Activity> aClass) {
        Intent intent = new Intent(mContext, aClass);
        startActivity(intent);
    }

    public void jump(Class<? extends Activity> aClass, String key, int value) {
        Intent intent = new Intent(mContext, aClass);
        intent.putExtra(key, value);
        startActivity(intent);
    }

    public void showToast(String s) {
        getActivity().runOnUiThread(() -> Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show());

    }

    public boolean isOpenPermission(String permission) {
        int checkPermission = getmContext().getPackageManager().checkPermission(permission, getmContext().getPackageName());
        if (checkPermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

}
