package hzhl.net.hlcall.activity;

import android.content.Context;
import android.content.Intent;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ClipPagerTitleView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.adapter.ViewPagerFgAdapter;
import hzhl.net.hlcall.entity.ContactsListEntity;
import hzhl.net.hlcall.fragment.ContactsDetailFg;
import hzhl.net.hlcall.fragment.TongXunLuDetail2Fg;
import hzhl.net.hlcall.utils.SizeUtils;

public class ContactsListItemActivity extends BaseActivity {
    @Bind(R.id.viewpager)
    ViewPager mViewPager;
    @Bind(R.id.magic_indicator)
    MagicIndicator mMagicIndicator;
    @Bind(R.id.tv_name)
    TextView tvName;
    private List<String> strings;
    private List<BaseFragment> fragments = new ArrayList<>();

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_contacts_list_item;
    }

    @Override
    protected String getTopTitle() {
        return null;
    }

    @Override
    protected void init() {
        setLeftTv("返回");
        Intent intent = getIntent();
        ContactsListEntity contactsListEntity = (ContactsListEntity) intent.getParcelableExtra("AddressListEntity");
        ContactsDetailFg contactsDetailFg = new ContactsDetailFg();
        TongXunLuDetail2Fg tongXunLuDetail2Fg = new TongXunLuDetail2Fg();
        if (contactsListEntity != null) {
            tvName.setText(contactsListEntity.getName());
            Bundle bundle = new Bundle();
            bundle.putParcelable("AddressListEntity", contactsListEntity);
            contactsDetailFg.setArguments(bundle);
            tongXunLuDetail2Fg.setArguments(bundle);
        }
        fragments.add(contactsDetailFg);
        fragments.add(tongXunLuDetail2Fg);
        ViewPagerFgAdapter viewPagerFgAdapter = new ViewPagerFgAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(viewPagerFgAdapter);
        strings = Arrays.asList("详情", "通话记录");
        CommonNavigator commonNavigator = new CommonNavigator(this);
        commonNavigator.setAdjustMode(true);
        commonNavigator.setLeftPadding(20);//坐边距
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return strings == null ? 0 : strings.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                ClipPagerTitleView clipPagerTitleView = new ClipPagerTitleView(context);
                clipPagerTitleView.setText(strings.get(index));
                clipPagerTitleView.setTextSize(SizeUtils.dip2px(ContactsListItemActivity.this, 16));
                clipPagerTitleView.setTextColor(getResources().getColor(R.color.gray));
                clipPagerTitleView.setClipColor(getResources().getColor(R.color.blue_font));
                clipPagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewPager.setCurrentItem(index);
                    }
                });

                return clipPagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator indicator = new LinePagerIndicator(context);
                indicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
                indicator.setYOffset(UIUtil.dip2px(context, 8));
                indicator.setLineHeight(UIUtil.dip2px(context, 2));
                indicator.setRoundRadius(UIUtil.dip2px(context, 3));
                indicator.setColors(context.getResources().getColor(R.color.blue_font));
                return indicator;
            }

        });
        mMagicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(mMagicIndicator, mViewPager);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
