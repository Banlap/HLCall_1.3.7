package hzhl.net.hlcall.recycle;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import android.view.View;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.castorflex.android.verticalviewpager.VSwipeRefreshLayout;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.utils.TimeUtils;
import hzhl.net.hlcall.utils.WK;

/**
 * Created by elileo on 2017/3/22.
 */
public abstract class RecycleFragment<T> extends BaseFragment {
    public static final int REPLACE_ALL = 0;
    public static final int LOAD_MORE = 1;

    private long mValidTime = TimeUtils.MINUTES.toMillis(1);

    private long mLastRefreshTime = 0;

    private RecycleAdapter<T> mAdapter;

    protected RecyclerView mPullView;

    private VSwipeRefreshLayout mPullRefreshView;

    private TextView mEmptyView;
    private TextView mEmptyUploadView;
    private View mEmptyLayout;
    private View mLoadingView;
    private TextView mErrorView;

    private Handler mHandler = new Handler();

    private int mEmptyResId = R.string.empty_list;

    private boolean mIncreaseAble = false;

    private AtomicBoolean isLoadDataState = new AtomicBoolean(false);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new RecycleAdapter<T>(getActivity(), getItemLayoutIds()){

            @Override
            public int getItemViewType(int position) {
                if(isFooterView(position)){
                    return TYPE_FOOTER_VIEW;
                }
                return getCustomItemViewType(position);
            }

            @Override
            protected void bindView(View view, T item, int position) {
                bindViewInfo(view, item, position);
            }

            @Override
            protected boolean isLoadMore() {
                return mIncreaseAble;
            }

            @Override
            public boolean needFootView() {
                return isNeedAddFootView();
            }
        };

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected int getCustomItemViewType(int position) {
        return 0;
    }

    public RecycleAdapter getAdapter(){
        return mAdapter;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        setLastRefreshTime(0);
        super.onViewCreated(view, savedInstanceState);
        mPullView = (RecyclerView) view.findViewById(R.id.pull_view);
        mPullRefreshView = (VSwipeRefreshLayout) view.findViewById(R.id.pull_refresh_view);
        mEmptyLayout = view.findViewById(R.id.recycle_empty_layout);
        mEmptyUploadView = (TextView) view.findViewById(R.id.recycle_empty_btn);
        mEmptyUploadView.setVisibility(isNeedEmptyButton() ? View.VISIBLE : View.GONE);
        mEmptyUploadView.setText(emptyBtnText());
        mEmptyView = (TextView) view.findViewById(R.id.recycle_empty);
        mLoadingView = view.findViewById(R.id.recycle_loading);
        mErrorView = (TextView) view.findViewById(R.id.recycle_error);
        mPullRefreshView.setEnabled(isNeedRefresh());

        ((SimpleItemAnimator)mPullView.getItemAnimator()).setSupportsChangeAnimations(false);

        init();

        mHandler.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        });
    }

    private void init(){
        mEmptyUploadView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emptyBtnClick();
            }
        });
        mErrorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoadingView.setVisibility(View.VISIBLE);
                refresh();
            }
        });

        mPullView.setHasFixedSize(true);
        mPullView.setLayoutManager(getLayoutManager());
        if(needDivider()){
            mPullView.addItemDecoration(getDivider());
        }
        mAdapter.setLoadingView(loadingResId());

        mPullView.setAdapter(mAdapter);

        setEmptyResId(mEmptyResId);
        mPullView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && mIncreaseAble && isNeedAddFootView()) {
                    if (findLastVisibleItemPosition(mPullView.getLayoutManager()) + 1 == getCount()) {
                        if(!isLoadDataState.get() && !mPullRefreshView.isRefreshing()){
                            isLoadDataState.set(true);
                            mPullRefreshView.setEnabled(false);
                            startRefresh(LOAD_MORE);
                        }
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        mPullRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startRefresh(REPLACE_ALL);
            }
        });
    }

    public void endRefresh(List<T> data, int type){
        endRefresh(type);
        if(WK.empty(data) && type == REPLACE_ALL){
            showEmptyView();
            mPullRefreshView.setRefreshing(false);
        }else if(type == REPLACE_ALL){
            replaceAll(data);
            mPullRefreshView.setRefreshing(false);
    }else if(type == LOAD_MORE){
        loadMore(data);
        isLoadDataState.set(false);
        if(isNeedRefresh()){
            mPullRefreshView.setEnabled(true);
        }
    }
    }

    private void endRefresh(int type){
        if(type == REPLACE_ALL) {
            setLastRefreshTime(System.currentTimeMillis());
            mLoadingView.setVisibility(View.GONE);
            mPullRefreshView.setRefreshing(false);
        }
    }

    protected void replaceAll(List<? extends T> data) {
        mAdapter.replace(data);
        showPullView();
    }

    protected void loadMore(List<? extends T> data) {
        if (!WK.empty(data)) {
            mAdapter.addLoadMore(data);
        } else {
            onNoLoadMoreRefresh();
        }
    }

    private  void onNoLoadMoreRefresh(){
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        if(WK.empty(mAdapter.getDataSource())){
            showEmptyView();
        }
        mAdapter.notifyDataSetChanged();
    }

    public T getItem(int position) {
        return mAdapter.getItem(position);
    }

    protected void showEmptyView(){
        mPullView.setVisibility(View.INVISIBLE);
        mLoadingView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.GONE);
        mEmptyLayout.setVisibility(View.VISIBLE);
    }

    protected void showPullView(){
        mPullView.setVisibility(View.VISIBLE);
        mLoadingView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.GONE);
        mEmptyLayout.setVisibility(View.GONE);
    }

    protected void showErrorView(){
        mPullView.setVisibility(View.INVISIBLE);
        mLoadingView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.VISIBLE);
        mEmptyLayout.setVisibility(View.GONE);
    }

    protected void showLoadingView(){
        mPullView.setVisibility(View.INVISIBLE);
        mLoadingView.setVisibility(View.VISIBLE);
        mErrorView.setVisibility(View.GONE);
        mEmptyLayout.setVisibility(View.GONE);
    }

    protected abstract int[] getItemLayoutIds();

    public abstract RecyclerView.LayoutManager getLayoutManager();

    public abstract RecyclerView.ItemDecoration getDivider();

    protected int getCount() {
        return mAdapter == null ? 0 : mAdapter.getItemCount();
    }

    public boolean needDivider(){
        return false;
    }

    public void setEmptyResId(int emptyResId) {
        mEmptyResId = emptyResId;
        if (mEmptyView != null) {
            mEmptyView.setText(mEmptyResId);
        }
    }

    public void setEmptyIcon(int id) {
        if (null == mEmptyView) {
            return;
        }

        Drawable d = null;
        if (-1 != id) {
            d = getResources().getDrawable(id);
        }
        d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight());
        mEmptyView.setCompoundDrawables(null, d, null, null);
    }

    public void setEmptyDrawPadding(int dp){
        if(mEmptyView != null){
            mEmptyView.setCompoundDrawablePadding(dp);
        }
    }

    public void setEmptyTextColor(int color) {
        if (null != mEmptyView) {
            mEmptyView.setTextColor(color);
        }
    }

    public long getValidTime() {
        return mValidTime;
    }

    public void setValidTime(long mValidTime) {
        this.mValidTime = mValidTime;
    }

    protected void setLastRefreshTime(long time) {
        mLastRefreshTime = time;
    }

    public long getLastRefreshTime() {
        return mLastRefreshTime;
    }

    protected abstract void startRefresh(int type);

    protected abstract void bindViewInfo(View view, T info, int position);

//    @Override
//    public void onVisibleToUser() {
//        super.onVisibleToUser();
//        refreshOnVisibleToUser();
//    }

//    protected void refreshOnVisibleToUser() {
//        if (needRefreshOnVisibleToUser()) {
//            realRefreshOnVisibleToUser();
//        }
//    }

//    protected boolean needRefreshOnVisibleToUser() {
//        return System.currentTimeMillis() - getLastRefreshTime() > mValidTime;
//    }
//
//    protected void realRefreshOnVisibleToUser() {
//        refresh(REPLACE_ALL);
//    }

    public void refresh() {
        refresh(REPLACE_ALL);
    }

    public void refresh(int refreshType) {
        if (mErrorView.getVisibility()== View.VISIBLE){
            mErrorView.setVisibility(View.GONE);
        }
        realRefresh(refreshType);
    }

    protected void realRefresh(int refreshType) {
        showLoadingIfNecessary();
        startRefresh(refreshType);
    }

    protected void showLoadingIfNecessary() {
        if(!isNeedShowLoadingFirstTime()) {
            mLoadingView.setVisibility(View.GONE);
        }
    }

    public boolean isPullViewVisible() {
        return mPullView.getVisibility() == View.VISIBLE;
    }


    public boolean isNeedShowLoadingFirstTime(){
        return true;
    }

    public boolean isNeedCheckNet(){
        return true;
    }

    public boolean isReceiverSocketSuccess(){
        return true;
    }

    public void setIncreaseAbleable(boolean increaseAble) {
        if (mIncreaseAble == increaseAble) {
            return;
        }
        mIncreaseAble = increaseAble;
    }

    private boolean isFooterView(int position){
        return  position >= getCount() - 1 && isNeedAddFootView();
    }

    public boolean isNeedAddFootView(){
        return true;
    }

    private int findLastVisibleItemPosition(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        }
        return -1;
    }

    public void restartRequest(){
        mPullView.setVisibility(View.INVISIBLE);
        mLoadingView.setVisibility(View.VISIBLE);
        mErrorView.setVisibility(View.GONE);
        mEmptyLayout.setVisibility(View.GONE);
    }

    public boolean isLoading(){
        return mLoadingView.getVisibility() == View.VISIBLE;
    }

    protected int loadingResId(){
        return R.layout.recycle_loading_more_view;
    }
    public void scrollToTop(){
        if(mPullView != null){
            mPullView.scrollToPosition(0);
        }
    }

    public boolean isNeedRefresh(){
        return true;
    }

    public boolean isNeedEmptyButton(){
        return false;
    }

    public String emptyBtnText(){
        return "";
    }

    public void emptyBtnClick(){

    }

    public void removeItem(int position){
        mAdapter.removeItem(position);
        notifyDataSetChanged();
    }

    public void changeItem(int position){
        mAdapter.notifyItemChanged(position);
    }

    public int getPosition(T t){
        return mAdapter.getPosition(t);
    }
}
