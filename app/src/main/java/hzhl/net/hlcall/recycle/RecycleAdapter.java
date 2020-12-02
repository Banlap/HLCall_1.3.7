package hzhl.net.hlcall.recycle;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import hzhl.net.hlcall.utils.WK;

/**
 * Created by elileo on 2017/3/22.
 */
public abstract class RecycleAdapter<T> extends RecyclerView.Adapter<BaseViewHolder>{
    public static final int TYPE_FOOTER_VIEW = 100001;

    private Context mContext;

    private final Object mLock = new Object();

    private List<T> mObjects;

    private int[] mResources;

    private LayoutInflater mInflater;

    private RelativeLayout mFooterLayout;

    private View mLoadingMoreView;

    public RecycleAdapter(Context context, int... resources) {
        this(context, new ArrayList<T>(), resources);
    }

    public RecycleAdapter(Context context, List<T> objects, int... resources) {
        this.mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResources  = resources;
        mObjects = objects;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_FOOTER_VIEW){
            if (mFooterLayout == null) {
                mFooterLayout = new RelativeLayout(mContext);
            }
            return new BaseViewHolder(mFooterLayout);
        }
        return new BaseViewHolder(mInflater.inflate(mResources[viewType], parent, false));
    }

    @Override
    public void onBindViewHolder(BaseViewHolder baseViewHolder, int position) {
        if(baseViewHolder.getItemViewType() != TYPE_FOOTER_VIEW) {
            bindView(baseViewHolder.itemView, mObjects.get(position), position);
        }else {
            baseViewHolder.setVisibility(isLoadMore());
        }
    }

    protected abstract void bindView(View view, T item, int position);

    public T getItem(int position) {
        return mObjects.get(position);
    }

    public int getPosition(T item) {
        return mObjects.indexOf(item);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mObjects.size() + getFooterViewCount();
    }

    public void addAll(Collection<? extends T> collection) {
        synchronized (mLock) {
            addAllIfNotEmpty(collection);
        }
        notifyDataSetChanged();
    }

    public void addLoadMore(Collection<? extends T> collection){
        synchronized (mLock) {
            mObjects.addAll(collection);
            notifyDataSetChanged();
        }
    }

    public void addAll(T... items) {
        synchronized (mLock) {
            addAllIfNotEmpty(items);
        }
        notifyDataSetChanged();
    }

    public void replace(Collection<? extends T> collection) {
        synchronized (mLock) {
            clearAllData();
            addAllIfNotEmpty(collection);
        }

        notifyDataSetChanged();
    }


    public void removeItem(int position){
        synchronized (mLock){
            List<T> list = getAvailableList();
            list.remove(position);
        }
//        notifyItemRemoved(position);
    }
    private List<T> getAvailableList() {
        List<T> list = mObjects;
        return list;
    }

    public List<T> getDataSource() {
        return getAvailableList();
    }


    public void clear() {
        synchronized (mLock) {
            clearAllData();
        }
        notifyDataSetChanged();
    }

    private void clearAllData() {
        List<T> list = getAvailableList();
        if (list != null) {
            list.clear();
        }
    }

    private void addAllIfNotEmpty(Collection<? extends T> collection) {
        if (WK.empty(collection)) {
            return;
        }
        List<T> list = getAvailableList();
        if (list != null) {
            list.addAll(collection);
        }
    }


    private void addAllIfNotEmpty(T... items) {
        if (WK.empty(items)) {
            return;
        }
        List<T> list = getAvailableList();
        if (list != null) {
            Collections.addAll(list, items);
        }
    }

    private void removeFooterView() {
        mFooterLayout.removeAllViews();
    }

    private void addFooterView(View footerView) {
        if (footerView == null) {
            return;
        }

        if (mFooterLayout == null) {
            mFooterLayout = new RelativeLayout(mContext);
        }
        removeFooterView();
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mFooterLayout.addView(footerView, params);
    }

    public void setLoadingView(int resId){
        setLoadingView(WK.inflate(mContext, resId));
    }

    public void setLoadingView(View loadingView) {
        mLoadingMoreView = loadingView;
        addFooterView(mLoadingMoreView);
    }

    public int getFooterViewCount() {
        if(needFootView()){
            return !mObjects.isEmpty() ? 1 : 0;
        }
        return 0;
    }

    public boolean needFootView(){
        return true;
    }

    protected abstract boolean isLoadMore();
}
