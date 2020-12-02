package hzhl.net.hlcall.fragment;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ClipPagerTitleView;

import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.activity.BaseFragment;
import hzhl.net.hlcall.adapter.ViewPagerFgAdapter;
import hzhl.net.hlcall.listener.CallItemClickListener;
import hzhl.net.hlcall.listener.CallNumChangeListener;
import hzhl.net.hlcall.utils.MyLog;
import hzhl.net.hlcall.utils.SizeUtils;

/**
 * Created by guang on 2019/7/29.
 */

public class CallRecordFragment extends BaseFragment implements CallRecordAllFg.OnAfterDelListener, CallRecordMissCallFg.OnAfterDelListener,
        CallItemClickListener, CallNumChangeListener {
    @Bind(R.id.viewpager)
    ViewPager mViewPager;
    @Bind(R.id.magic_indicator)
    MagicIndicator mMagicIndicator;
    @Bind(R.id.tv_finish)
    TextView tvFinish;
    @Bind(R.id.iv_modify)
    ImageView ivModify;
    @Bind(R.id.text_username)
    TextView text_username;

    @Bind(R.id.fl_phone_key_board)
    FrameLayout mPhoneKeyBoardLayout;

    private List<String> strings;
    private List<BaseFragment> fragments = new ArrayList<>();
    private OnModifyCallRecordListener onModifyCallRecordListener;
    private CallRecordAllFg callRecordAllFg;
    private CallRecordMissCallFg callRecordMissCallFg;
    private int type = 0;
    public static final int WRITE_CALL_LOG_RESULT = 1119;

    private PhoneKeyBoardFragment mPhoneKeyBoardFragment;

    private Core core;
    private CoreListenerStub coreListener = new CoreListenerStub(){
        @Override
        public void onRegistrationStateChanged(Core lc, ProxyConfig cfg, RegistrationState cstate, String message) {
            super.onRegistrationStateChanged(lc, cfg, cstate, message);
            upDateStatus();
        }
    };


    @Override
    public void onCallRecordAllAfterDel() {
        finishModifyInCallRecordAll();
    }

    @Override
    public void onCallRecordMissCallAfterDel() {
        finishModifyInCallRecordMissCall();
    }

    @Override
    public void onCallItemClick(String number) {
        if(!isPhoneKeyBoardShow){
            isPhoneKeyBoardShow = true;
            showPhoneKeyBoardFragment();
        }

        if(mPhoneKeyBoardFragment != null){
            mPhoneKeyBoardFragment.setCallNumberText(number);
        }
    }

    @Override
    public void callNumChange(String num) {
        callRecordAllFg.searchNum(num);
        callRecordMissCallFg.searchNum(num);
    }

    public void onKeyDown(int keyCode, KeyEvent event) {
        mPhoneKeyBoardFragment.onKeyDown(keyCode, event);
    }

    public interface OnModifyCallRecordListener {
        void modifyCallRecord(String type);
    }

    public void setModifyCallRecordListener(OnModifyCallRecordListener onModifyCallRecordListener) {
        this.onModifyCallRecordListener = onModifyCallRecordListener;
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_callrecord;
    }

    private boolean isPhoneKeyBoardShow = true;
    private void showPhoneKeyBoardFragment(){
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTrans = fragmentManager.beginTransaction();
        mPhoneKeyBoardLayout.setVisibility(View.VISIBLE);
        if(mPhoneKeyBoardFragment == null){
            mPhoneKeyBoardFragment = (PhoneKeyBoardFragment) fragmentManager.findFragmentByTag("fmPhoneKeyboard");
            mPhoneKeyBoardFragment = new PhoneKeyBoardFragment();
            mPhoneKeyBoardFragment.setCallNumChangeListener(this);
        }
        fragmentTrans.replace(R.id.fl_phone_key_board, mPhoneKeyBoardFragment, "fmPhoneKeyboard").commit();

        fragmentManager.executePendingTransactions();
    }

    private void hidePhoneKeyBoardFragment(){
        if (mPhoneKeyBoardFragment != null) {
            getChildFragmentManager().beginTransaction().remove(mPhoneKeyBoardFragment).commit();
            mPhoneKeyBoardLayout.setVisibility(View.GONE);
        }
    }


    public void switchPhoneKeyBoard(boolean isRepeatClick){
        if(!isRepeatClick){
            return;
        }
        if(isPhoneKeyBoardShow){
            hidePhoneKeyBoardFragment();
        }else{
            showPhoneKeyBoardFragment();
        }
        isPhoneKeyBoardShow = !isPhoneKeyBoardShow;
    }

    @Override
    protected void initView() {
        callRecordAllFg = new CallRecordAllFg();
        callRecordMissCallFg = new CallRecordMissCallFg();
        fragments.add(callRecordAllFg);
        fragments.add(callRecordMissCallFg);
        callRecordAllFg.setOnAfterDelListener(this);
        callRecordMissCallFg.setOnAfterDelListener(this);
        callRecordAllFg.setCallItemClickListener(this);
        callRecordMissCallFg.setCallItemClickListener(this);
        ViewPagerFgAdapter viewPagerFgAdapter = new ViewPagerFgAdapter(getFragmentManager(), fragments);
        mViewPager.setAdapter(viewPagerFgAdapter);
        strings = Arrays.asList("全部通话", "未接电话");
        CommonNavigator commonNavigator = new CommonNavigator(getmContext());
        commonNavigator.setAdjustMode(true);
        commonNavigator.setLeftPadding(20);//坐边距
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return strings == null ? 0 : strings.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                MyLog.d("index:" + index);
                ClipPagerTitleView clipPagerTitleView = new ClipPagerTitleView(context);
                clipPagerTitleView.setText(strings.get(index));
                clipPagerTitleView.setTextSize(SizeUtils.dip2px(getmContext(), 16));
                clipPagerTitleView.setTextColor(getResources().getColor(R.color.gray));
                clipPagerTitleView.setClipColor(getResources().getColor(R.color.blue_font));
                clipPagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MyLog.d("index```:" + index);
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
        showPhoneKeyBoardFragment();

        //wenyeyang
        if (LinphoneService.isReady()){
            core = LinphoneService.getCore();
            if (core!=null)core.addListener(coreListener);
        }
        upDateStatus();
    }

    private void upDateStatus() {
        try {
            if (!LinphoneService.isReady()) return;
            Core core = LinphoneService.getCore();
            if (core != null) {
                if (core.getDefaultProxyConfig() != null) {
                    String state = getStatus(core.getDefaultProxyConfig().getState());
                    text_username.setText(
                            String.format("%s  %s", core.getDefaultProxyConfig().getContact().getUsername(), state)
                    );
                } else {
                    text_username.setText("未注册");
                    text_username.setTextColor(getResources().getColor(R.color.red_font_btn));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private String getStatus(RegistrationState state) {
        int red = getResources().getColor(R.color.red_font_btn);
        int blue = getResources().getColor(R.color.blue_font);
        if (state == RegistrationState.Ok) {
            text_username.setTextColor(blue);
            return "已登录";
        } else if (state == RegistrationState.Progress) {
            text_username.setTextColor(blue);
            return "登录中";
        } else if (state == RegistrationState.Failed) {
            text_username.setTextColor(red);
            return "登录失败";
        } else {
            text_username.setTextColor(red);
            return "未登录";
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (core!=null)core.removeListener(coreListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @OnClick({R.id.iv_modify, R.id.tv_finish})
    public void onClickVoid(View view) {
        int item = mViewPager.getCurrentItem();
        MyLog.d("item-" + item);
        switch (view.getId()) {
            case R.id.iv_modify:
                if (isOpenPermission(Manifest.permission.WRITE_CALL_LOG)) {
                    if (0 == item) {
                        onModifyCallRecordListener.modifyCallRecord("modify");
                        callRecordAllFg.showChoose("modify");
                        ivModify.setVisibility(View.GONE);
                        tvFinish.setVisibility(View.VISIBLE);
                    } else if (1 == item) {
                        onModifyCallRecordListener.modifyCallRecord("modify");
                        callRecordMissCallFg.showChoose("modify");
                        ivModify.setVisibility(View.GONE);
                        tvFinish.setVisibility(View.VISIBLE);
                    }
                    hidePhoneKeyBoardFragment();
                    isPhoneKeyBoardShow = false;
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_CALL_LOG}
                            , WRITE_CALL_LOG_RESULT);
                }

                break;
            case R.id.tv_finish:
                if (0 == item) {
                    finishModifyInCallRecordAll();
                } else if (1 == item) {
                    finishModifyInCallRecordMissCall();
                }
                break;
        }
    }

    /**
     * 全部通讯录完成修改
     */
    private void finishModifyInCallRecordAll() {
        onModifyCallRecordListener.modifyCallRecord("finish");
        callRecordAllFg.showChoose("finish");
        ivModify.setVisibility(View.VISIBLE);
        tvFinish.setVisibility(View.GONE);
    }

    /**
     * 全部通讯录完成修改
     */
    private void finishModifyInCallRecordMissCall() {
        onModifyCallRecordListener.modifyCallRecord("finish");
        callRecordMissCallFg.showChoose("finish");
        ivModify.setVisibility(View.VISIBLE);
        tvFinish.setVisibility(View.GONE);
    }

    /**
     * 刷新全部通话和未接通话数据
     */
    public void refreshDate() {
        if (callRecordAllFg != null && callRecordMissCallFg != null) {
            callRecordAllFg.refreshDate();
            callRecordMissCallFg.refreshDate();
        }
    }

    /**
     * 传递 activity中点击实体键 返回键 信息
     */
    public void setIsDeleteNum(){
        PhoneKeyBoardFragment mPhoneKeyBoardFm = (PhoneKeyBoardFragment) getChildFragmentManager().findFragmentByTag("fmPhoneKeyboard");
        mPhoneKeyBoardFm.delNumberCallback();
    }

    public void setIsCall(){
        PhoneKeyBoardFragment mPhoneKeyBoardFm2 = (PhoneKeyBoardFragment) getChildFragmentManager().findFragmentByTag("fmPhoneKeyboard");
        mPhoneKeyBoardFm2.isCall();
    }

}
