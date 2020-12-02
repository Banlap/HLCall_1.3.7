package hzhl.net.hlcall.recycle;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by elileo on 18/5/30.
 */
public abstract class BaseFragment extends Fragment {

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){
            onInVisibleToUser();
        }else{
            onVisibleToUser();
        }
    }

    /**
     * Indicates fragment is becoming visible to user, in this case, below conditions are true:
     * 1. {@link #getUserVisibleHint()}
     * 2. {@link #isResumed()}
     * <p/>
     * Called in {@link #onResume()} or {@link #setUserVisibleHint(boolean)}
     */
    public void onVisibleToUser() {
    }

    /**
     * Indicates fragment is becoming invisible to user, in this case, below conditions are false:
     * 1. {@link #getUserVisibleHint()}
     * 2. {@link #isResumed()}
     * <p/>
     * Called in {@link #onPause()} or {@link #setUserVisibleHint(boolean)}
     */
    public void onInVisibleToUser() {

    }

    public void fragmentVisible(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getFragmentLayoutId(), container, false);
        initView(view);
        return view;
    }

    public abstract int getFragmentLayoutId();
    public abstract void initView(View view);
}
