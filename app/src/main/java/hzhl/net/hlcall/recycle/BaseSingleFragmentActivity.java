package hzhl.net.hlcall.recycle;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

/**
 * create by elileo on 2018/5/31
 */
public abstract class BaseSingleFragmentActivity<T extends Fragment> extends AppCompatActivity {
    protected T mFragment;
    private String mFragmentTag;
    private int mContainerId;

    protected abstract int getFragmentContainerId();

    protected abstract T getFragment(Intent data);

    protected abstract String getFragmentTag();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFragmentTag = getFragmentTag();
        mContainerId = getFragmentContainerId();
        mFragment = (T)getSupportFragmentManager().findFragmentByTag(mFragmentTag);
    }

    @Override
    protected void onResume() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(mFragment == null){
            Intent data = getIntent();
            mFragment = getFragment(data);

            if (mFragment == null) {
                throw new RuntimeException("getFragment(data) return null");
            }

            transaction.add(mContainerId, mFragment, mFragmentTag);
        }
        transaction.show(mFragment).commitAllowingStateLoss();
        super.onResume();
    }
}
