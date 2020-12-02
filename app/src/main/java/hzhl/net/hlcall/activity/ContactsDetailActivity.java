package hzhl.net.hlcall.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.luck.picture.lib.tools.ToastManage;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import hzhl.net.hlcall.App;
import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.adapter.NumberListAdapter;
import hzhl.net.hlcall.entity.BlacklistEntity;
import hzhl.net.hlcall.entity.BlacklistEntityDao;
import hzhl.net.hlcall.entity.ContactsListEntity;
import hzhl.net.hlcall.entity.NumberEntity;
import hzhl.net.hlcall.utils.BitmapToByteUtil;
import hzhl.net.hlcall.utils.ContactsUtil;
import hzhl.net.hlcall.utils.LinphoneUtils;

public class ContactsDetailActivity extends BaseActivity implements NumberListAdapter.OnRecyclerViewItemClickListener {
    @Bind(R.id.recy_number)
    RecyclerView mRecyclerView;
    @Bind(R.id.tv_add_blacklist)
    TextView tvAddBlacklist;
    @Bind(R.id.tv_name)
    TextView tvName;
    @Bind(R.id.tv_company)
    TextView tvCompany;
    @Bind(R.id.tv_job)
    TextView tvJob;
    @Bind(R.id.circle_head)
    CircleImageView circleIvHead;

    private List<NumberEntity> list = new ArrayList<>();
    private NumberListAdapter adapter;
    private ContactsListEntity entity;
    public static final int MODIFY = 121;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_contacts_detail;
    }

    @Override
    protected String getTopTitle() {
        return null;
    }

    @Override
    protected void init() {
        setLeftTv("返回");
        setRightTv("修改");
        tvAddBlacklist.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        tvAddBlacklist.getPaint().setAntiAlias(true);//抗锯齿
        adapter = new NumberListAdapter(this, list);
        adapter.setOnItemClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.shape_line));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setAdapter(adapter);
        Intent intent = getIntent();
        entity = intent.getParcelableExtra("AddressListEntity");
        setContactsInfo(entity);
    }

    @Override
    protected void onClickRightTv(View view) {
        super.onClickRightTv(view);
        Intent intent = new Intent(this, ContactsModifyActivity.class);
        intent.putExtra("AddressListEntity", entity);
        startActivityForResult(intent, MODIFY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @OnClick({R.id.tv_add_blacklist, R.id.tv_detail_msg, R.id.tv_detail_video,
            R.id.tv_detail_meeting, R.id.tv_detail_pic})
    public void onClickVoid(View view) {
        switch (view.getId()) {
            case R.id.tv_add_blacklist:

                break;
            case R.id.tv_detail_pic:
            case R.id.tv_detail_msg:
                if(entity.getNumberList() != null && entity.getNumberList().size() > 0){
                    LinphoneUtils.sendMessage(this,entity.getNumberList().get(0).getNumber(),entity.getName());
                }
                break;
            case R.id.tv_detail_video:
                if(entity.getNumberList() != null && entity.getNumberList().size() > 0){
                    LinphoneUtils.call(this, entity.getNumberList().get(0).getNumber(), true);
                }
                break;
            case R.id.tv_detail_meeting:
                ToastManage.s(this, "会议室");
                break;
        }
    }

    private void setContactsInfo(ContactsListEntity contactsListEntity) {
        if (contactsListEntity != null) {
            entity = ContactsUtil.searchContactInId(this, entity.getId());
            //  tvNumber.setText(entity.getNumber());
            tvName.setText(entity.getName());
            tvCompany.setText(entity.getCompany());
            tvJob.setText(entity.getJob());
            if (entity.getBytes() != null) {
                Bitmap photoBitmap = BitmapToByteUtil.Bytes2Bimap(entity.getBytes());
                Glide.with(this).load(photoBitmap).into(circleIvHead);
            } else {
                Glide.with(this).load(R.drawable.icon_touxiang).into(circleIvHead);
            }
            list = entity.getNumberList();
            adapter.setData(list);
        }
    }

    private void addBlack(String number) {
        if (number.isEmpty()) {
            return;
        }
        BlacklistEntityDao blacklistEntityDao = App.getDaoInstant().getBlacklistEntityDao();
        List<BlacklistEntity> list = blacklistEntityDao.loadAll();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUser().equals(number)) {
                showToast("不能重复添加");
                return;
            }
        }
        BlacklistEntity blacklistEntity = new BlacklistEntity();
        blacklistEntity.setUser(number);
        if (list.size() >= 20) {
            showToast("最多添加20条黑名单");
            return;
        }
        blacklistEntity.setUser(number);
        blacklistEntityDao.insertOrReplace(blacklistEntity);
        showToast("添加成功");
        finish();
    }

    @Override
    public void OnItemClick(int position) {

    }

    @Override
    public void OnItemCall(int position) {
        String number = list.get(position).getNumber();
        PopupWindow popupWindow = LinphoneUtils.setCallPopupWindow(this, number, false);
        if (popupWindow != null) {
            popupWindow.showAtLocation(popupWindow.getContentView(), Gravity.BOTTOM, 0, 0);
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.alpha = 0.5f;
            getWindow().setAttributes(lp);
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            WindowManager.LayoutParams lp = getWindow().getAttributes();
                            lp.alpha = 1f;
                            getWindow().setAttributes(lp);
                        }
                    },300);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == MODIFY && resultCode == RESULT_OK) {
                entity = data.getParcelableExtra("AddressListEntity");
                setContactsInfo(entity);
            }
        }
    }
}
