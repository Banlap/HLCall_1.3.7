package hzhl.net.hlcall.activity;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import hzhl.net.hlcall.App;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.adapter.BlacklistAdapter;
import hzhl.net.hlcall.entity.BlacklistEntity;
import hzhl.net.hlcall.entity.BlacklistEntityDao;
import hzhl.net.hlcall.entity.DaoSession;
import hzhl.net.hlcall.view.MyDialog;

public class BlacklistActivity extends BaseActivity implements BlacklistAdapter.OnRecyclerViewItemClickListener {
    private static final int ADD_BLACKLIST = 100;
    @Bind(R.id.recy_blacklist)
    RecyclerView mRecyclerView;

    private List<BlacklistEntity> list = new ArrayList<>();

    private BlacklistAdapter adapter;
    private BlacklistEntityDao blacklistEntityDao;
    private BlacklistEntity blacklistEntity;
    private MyDialog myDialog;
    private TextView tvNumber;
    private int item;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_blacklist;
    }

    @Override
    protected String getTopTitle() {
        return null;
    }

    @Override
    protected void init() {
        setLeftTv("黑名单");
        setRightIv(R.drawable.icon_tianjia);
        initSipTypeDialog();
        DaoSession daoSession = App.getDaoInstant();
        blacklistEntityDao = daoSession.getBlacklistEntityDao();
        list = blacklistEntityDao.queryBuilder().limit(20).list();
        adapter = new BlacklistAdapter(this, list);
        adapter.setOnItemClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.shape_line));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onClickRightIv(View view) {
        super.onClickRightIv(view);
        Intent intent = new Intent(this, AddBlacklistActivity.class);
        startActivityForResult(intent, ADD_BLACKLIST);
    }

    @Override
    public void OnItemClick(int position) {
        item = position;
        tvNumber.setText(list.get(position).getUser());
        myDialog.show();
    }

    protected void initSipTypeDialog() {
        //设置网络传输方式 弹窗
        MyDialog.Builder builder = new MyDialog.Builder(this);
        myDialog = builder.view(R.layout.dialog_blacklist_del)
                .style(R.style.dialog)
                .widthdp(250)
                .cancelTouchout(true)
                .build();
        tvNumber = myDialog.getView().findViewById(R.id.tv_number);
        TextView tvDel = myDialog.getView().findViewById(R.id.tv_del);
        TextView tvCancel = myDialog.getView().findViewById(R.id.tv_cancel);
        tvDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blacklistEntityDao.delete(list.get(item));
                list.remove(item);
                adapter.setData(list);
                myDialog.dismiss();
            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == ADD_BLACKLIST) {
                BlacklistEntity blacklistEntity = (BlacklistEntity) data.getSerializableExtra("blacklistEntity");
                list.add(blacklistEntity);
                adapter.setData(list);
            }
        }

    }

    //重写onKeyDown
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(getClass().getName(), "onKeyDown:keyCode === " + keyCode);
        /** banlap：bug：实体键 拨号键和 挂机键不操作 */
        if (keyCode == KeyEvent.KEYCODE_CALL) {
            Logger.d("MainActivity不操作");
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_ENDCALL) {
            Logger.d("MainActivity不操作");
            return true;
        }
        /** banlap：bug：实体键 拨号键和 挂机键不操作  --end*/
        return super.onKeyDown(keyCode, event);
    }
}