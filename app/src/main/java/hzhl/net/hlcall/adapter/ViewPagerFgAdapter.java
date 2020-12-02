package hzhl.net.hlcall.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

import hzhl.net.hlcall.activity.BaseFragment;

/**
 * Created by user on 2017/2/15.
 */

public class ViewPagerFgAdapter extends FragmentPagerAdapter {
    private List<BaseFragment> list;

    public ViewPagerFgAdapter(FragmentManager fm, List<BaseFragment> listFg) {
        super(fm);
        this.list = listFg;
    }


    @Override
    public Fragment getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list.size();
    }
}
