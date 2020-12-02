package hzhl.net.hlcall.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import hzhl.net.hlcall.R;
import hzhl.net.hlcall.fragment.ImageFileFragment;
import hzhl.net.hlcall.recycle.BaseSingleFragmentActivity;
import hzhl.net.hlcall.utils.StatusBarUtil;

/**
 * create by elileo on 2019/9/17
 */
public class PhotoSelectActivity extends BaseSingleFragmentActivity<ImageFileFragment> {

    public static final int PHOTO_SELECT_REQUEST = 0X103;
    public static final String KEY_SELECT = "selectList";
    private ImageView mBackIV;
    private FrameLayout mRightLayout;
    private TextView mRightTV;

    @Override
    protected int getFragmentContainerId() {
        return R.id.image_file_container;
    }

    @Override
    protected ImageFileFragment getFragment(Intent data) {
        return new ImageFileFragment();
    }

    @Override
    protected String getFragmentTag() {
        return ImageFileFragment.TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_select);
        StatusBarUtil.setStatusBarLightMode(getWindow());

        init();
    }

    private void init(){
        mBackIV = findViewById(R.id.back_iv);
        mRightLayout = findViewById(R.id.right_fl);
        mRightTV = findViewById(R.id.right_tv);
        mBackIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mRightLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               ArrayList<String> result =  mFragment.getSelectList();
               if(result != null){
                   Intent intent = new Intent();
                   intent.putStringArrayListExtra(KEY_SELECT, result);
                   setResult(PHOTO_SELECT_REQUEST, intent);
                   finish();
               }
            }
        });
    }

    public void setShowNum(int num){
        if(num > 0) {
            mRightTV.setText(getString(R.string.complete_format_tips, num));
        }else{
            mRightTV.setText(getString(R.string.complete_tips));
        }
    }
}
