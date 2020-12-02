package hzhl.net.hlcall.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hzhl.net.hlcall.App;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.activity.PhotoSelectActivity;
import hzhl.net.hlcall.chat.ImageFileMgr;
import hzhl.net.hlcall.recycle.RecycleFragment;
import hzhl.net.hlcall.utils.DensityUtil;
import hzhl.net.hlcall.view.AutoAdjustImageView;

/**
 * create by elileo on 2019/9/17
 */
public class ImageFileFragment extends RecycleFragment<String> {
    public static final String TAG = ImageFileFragment.class.getSimpleName();

    private static final int COLUMN = 4;

    private HandlerThread mLoadVideoListThread;
    private Handler mHandler;

    private SparseArray<Boolean> mSelectMap = new SparseArray();
    private List<String> mList;

    private int mCurrentCount = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoadVideoListThread = new HandlerThread(TAG);
        mLoadVideoListThread.start();
        mHandler = new Handler(mLoadVideoListThread.getLooper());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLoadVideoListThread.getLooper().quit();
        mLoadVideoListThread.quit();
    }


    @Override
    protected int[] getItemLayoutIds() {
        return new int[]{R.layout.item_photo};
    }

    @Override
    public RecyclerView.LayoutManager getLayoutManager() {
        return new GridLayoutManager(getActivity(), COLUMN);
    }

    @Override
    public RecyclerView.ItemDecoration getDivider() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int padding = DensityUtil.dip2px(4);
                outRect.left = padding;
                outRect.top = padding;
                int position = parent.getChildLayoutPosition(view) % COLUMN;
                if(position == COLUMN - 1){
                    outRect.right = padding;
                }else{
                    outRect.right = 0;
                }
            }
        };
    }

    @Override
    public boolean needDivider() {
        return true;
    }

    @Override
    protected void startRefresh(int type) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mList = ImageFileMgr.getInstance().getSystemPhotoList();
                App.runAsync(new Runnable() {
                    @Override
                    public void run() {
                        mSelectMap.clear();
                        endRefresh(mList, type);
                    }
                });
            }
        });
    }

    @Override
    protected void bindViewInfo(View view, String info, int position) {
        AutoAdjustImageView iv = view.findViewById(R.id.item_photo_iv);
        ImageView checkIV = view.findViewById(R.id.item_photo_select_iv);
        FrameLayout bgView = view.findViewById(R.id.item_photo_fl);

        boolean isSelect = mSelectMap.get(position, false);
        bgView.setVisibility(isSelect ? View.VISIBLE : View.GONE);
        checkIV.setImageResource(isSelect ? R.drawable.ic_image_checked : R.drawable.ic_image_unchecked);

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean current = mSelectMap.get(position, false);
                if(!current){
                    if(mCurrentCount >= 9){
                        Toast.makeText(getActivity(), "一次选择不能超过9张图片", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mCurrentCount ++;
                }else{
                    mCurrentCount --;
                }
                ((PhotoSelectActivity)getActivity()).setShowNum(mCurrentCount);
                mSelectMap.put(position, !current);
                getAdapter().notifyItemChanged(position);
            }
        });
        File file = new File(info);
        if (file.length()<=0){
            Glide.with(this)
                    .load(file)
                    .placeholder(R.drawable.ic_default)
                    .skipMemoryCache(true) // 不使用内存缓存
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .error(R.drawable.ic_default)
                    .into(iv);
            return;
        }
        Glide.with(this)
                .load(file)
                .placeholder(R.drawable.ic_default)
                .error(R.drawable.ic_default)
                .into(iv);
    }

    public ArrayList<String> getSelectList(){
        if(mCurrentCount == 0){
            return null;
        }else{
            ArrayList<String> selectList = new ArrayList<>();
            for (int i = 0; i < mSelectMap.size(); i++) {
                int key = mSelectMap.keyAt(i);
                if(mSelectMap.get(key)){
                    selectList.add(mList.get(key));
                }
            }
            return selectList;
        }
    }

    @Override
    public int getFragmentLayoutId() {
        return R.layout.pull_recycle_fragment;
    }

    @Override
    public void initView(View view) {

    }

    @Override
    public boolean isNeedRefresh() {
        return false;
    }

    @Override
    public boolean isNeedAddFootView() {
        return false;
    }

    @Override
    public boolean isNeedCheckNet() {
        return false;
    }
}
