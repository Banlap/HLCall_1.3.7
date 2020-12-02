package hzhl.net.hlcall.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.adapter.NumberListModifyAdapter;
import hzhl.net.hlcall.constant.Constants;
import hzhl.net.hlcall.entity.ContactsListEntity;
import hzhl.net.hlcall.entity.NumberEntity;
import hzhl.net.hlcall.utils.BitmapToByteUtil;
import hzhl.net.hlcall.utils.ContactsUtil;
import hzhl.net.hlcall.view.LastInputEditText;

public class ContactsModifyActivity extends BaseActivity implements NumberListModifyAdapter.OnRecyclerViewUpdateListener {
    @Bind(R.id.edit_name)
    LastInputEditText editName;
    @Bind(R.id.edit_company)
    LastInputEditText editCompany;
    @Bind(R.id.edit_job)
    LastInputEditText editJob;
    @Bind(R.id.circle_head)
    CircleImageView circleIvHead;
    @Bind(R.id.ll_controls_numbers)
    LinearLayout llContactsNumber;
    @Bind(R.id.recy_number)
    RecyclerView mRecyclerView;
    private static final int WRITE_CONTACTS_RESULE = 115;
    private static final int READ_EXTERNAL_STORAGE = 116;
    private static final int CAMERA = 117;
    private ArrayList<NumberEntity> list = new ArrayList<>();
    private ContactsListEntity contactsListEntity;
    private NumberListModifyAdapter adapter;
    private Intent intent;
    private byte[] photoByte;
    private boolean isDelPhoto = false;//是否删除原有头像

    @Override
    protected int getLayoutResID() {
        return (R.layout.activity_contacts_modify);
    }

    @Override
    protected String getTopTitle() {
        return "修改联系人";
    }

    @Override
    protected void init() {
        setRightTv("删除");
        adapter = new NumberListModifyAdapter(this, list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.shape_line));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setAdapter(adapter);
        intent = getIntent();
        contactsListEntity = intent.getParcelableExtra("AddressListEntity");
        if (contactsListEntity != null) {
            editName.setText(contactsListEntity.getName());
            editCompany.setText(contactsListEntity.getCompany());
            editJob.setText(contactsListEntity.getJob());
            if (contactsListEntity.getBytes() != null) {
                photoByte = contactsListEntity.getBytes();
                Bitmap photoBitmap = BitmapToByteUtil.Bytes2Bimap(photoByte);
                Glide.with(this).load(photoBitmap).into(circleIvHead);
            }
            list = contactsListEntity.getNumberList();
            adapter.setData(list);
        }
    }

    @Override
    protected void onClickRightTv(View view) {
        super.onClickRightTv(view);
        showDellDialog();
    }

    @Override
    public void updateList(ArrayList<NumberEntity> strings) {
        list = strings;
        if (contactsListEntity != null) {
            contactsListEntity.setNumberList(list);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @OnClick({R.id.add_number, R.id.tv_cancel, R.id.tv_save, R.id.circle_head})
    public void onClickVoid(View view) {
        switch (view.getId()) {
            case R.id.add_number:
                list.add(new NumberEntity());
                adapter.setData(list);
                break;
            case R.id.tv_save:
                if (isOpenPermission(Manifest.permission.WRITE_CONTACTS)) {
                    saveContacts();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS}
                            , WRITE_CONTACTS_RESULE);
                }
                break;
            case R.id.tv_cancel:
                finish();
                break;
            case R.id.circle_head:
                showListDialog();
                break;
        }
    }


    private void saveContacts() {
        if (contactsListEntity != null) {
            contactsListEntity.setName(editName.getText().toString().trim());
            contactsListEntity.setCompany(editCompany.getText().toString().trim());
            contactsListEntity.setJob(editJob.getText().toString().trim());
            if (photoByte != null) {
                contactsListEntity.setBytes(photoByte);
            } else {
                //没设置头像图片photoByte为null,而且确认删除头像
                if (isDelPhoto) {
                    contactsListEntity.setBytes(null);//清楚旧图片
                    ContactsUtil.deletePhoto(this, contactsListEntity.getId());
                }
            }
            boolean result = ContactsUtil.updateContacts(this, contactsListEntity, contactsListEntity.getId());
            if (result) {
                showToast("修改成功");
                EventBus.getDefault().post(Constants.EVENT_UPDATE_CONTACTS);
                intent.putExtra("AddressListEntity", contactsListEntity);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                showToast("修改失败");
            }
        }
    }

    private void showListDialog() {
        String[] items;
        if (photoByte != null) {
            items = new String[]{"删除照片", "拍照", "从相册中选择照片"};
        } else {
            items = new String[]{"拍照", "从相册中选择照片"};
        }
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(ContactsModifyActivity.this);
        listDialog.setTitle("头像");
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                String s = items[which];
                if ("删除照片".equals(s)) {
                    isDelPhoto = true;
                    photoByte = null;
                    Glide.with(ContactsModifyActivity.this).load(R.drawable.icon_touxiang).into(circleIvHead);
                } else if ("拍照".equals(s)) {
                    if (isOpenPermission(Manifest.permission.CAMERA)) {
                        openCamera();
                    } else {
                        ActivityCompat.requestPermissions(ContactsModifyActivity.this
                                , new String[]{Manifest.permission.CAMERA}
                                , CAMERA);
                    }
                } else if ("从相册中选择照片".equals(s)) {
                    if (isOpenPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        openAlbum();
                    } else {
                        ActivityCompat.requestPermissions(ContactsModifyActivity.this
                                , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
                                , READ_EXTERNAL_STORAGE);
                    }
                }
            }
        });
        listDialog.show();
    }

    private void openCamera() {
        PictureSelector.create(ContactsModifyActivity.this)
                .openCamera(PictureMimeType.ofImage())//全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                //    .maxSelectNum(1)// 最大图片选择数量 int
                //  .minSelectNum(1)// 最小选择数量 int
                .imageSpanCount(4)// 每行显示个数 int
                .selectionMode(PictureConfig.SINGLE)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                .previewImage(true)// 是否可预览图片 true or false
                .isCamera(true)// 是否显示拍照按钮 true or false
                .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                .sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                .setOutputCameraPath("/CustomPath")// 自定义拍照保存路径,可不填
                .enableCrop(true)// 是否裁剪 true or false
                .compress(true)// 是否压缩 true or false
                .freeStyleCropEnabled(true)// 裁剪框是否可拖拽 true or false
                .circleDimmedLayer(true)// 是否圆形裁剪 true or false
                .showCropFrame(false)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
                .showCropGrid(false)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
                .openClickSound(false)// 是否开启点击声音 true or false
                .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中) true or false
                .minimumCompressSize(100)// 小于100kb的图片不压缩
                .synOrAsy(true)//同步true或异步false 压缩 默认同步
                .rotateEnabled(true) // 裁剪是否可旋转图片 true or false
                .isDragFrame(true)// 是否可拖动裁剪框(固定)
                .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
    }

    public void openAlbum() {
        // 进入相册 以下是例子：用不到的api可以不写
        PictureSelector.create(ContactsModifyActivity.this)
                .openGallery(PictureMimeType.ofImage())//全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                //    .maxSelectNum(1)// 最大图片选择数量 int
                //  .minSelectNum(1)// 最小选择数量 int
                .imageSpanCount(4)// 每行显示个数 int
                .selectionMode(PictureConfig.SINGLE)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                .previewImage(true)// 是否可预览图片 true or false
                .isCamera(true)// 是否显示拍照按钮 true or false
                .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                .sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                .setOutputCameraPath("/CustomPath")// 自定义拍照保存路径,可不填
                .enableCrop(true)// 是否裁剪 true or false
                .compress(true)// 是否压缩 true or false
                .freeStyleCropEnabled(true)// 裁剪框是否可拖拽 true or false
                .circleDimmedLayer(true)// 是否圆形裁剪 true or false
                .showCropFrame(false)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
                .showCropGrid(false)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
                .openClickSound(false)// 是否开启点击声音 true or false
                .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中) true or false
                .minimumCompressSize(100)// 小于100kb的图片不压缩
                .synOrAsy(true)//同步true或异步false 压缩 默认同步
                .rotateEnabled(true) // 裁剪是否可旋转图片 true or false
                .isDragFrame(true)// 是否可拖动裁剪框(固定)
                .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 图片、视频、音频选择结果回调
                    List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                    // 例如 LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true  注意：音视频除外
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true  注意：音视频除外
                    // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的

                    LocalMedia localMedia = selectList.get(0);
                    if (localMedia.isCompressed()) {
                        String photoPath = localMedia.getCompressPath();
                        File file = new File(photoPath);
                        Glide.with(this).load(file).into(circleIvHead);
                        photoByte = BitmapToByteUtil.editContactPicture(this, photoPath);
                    } else if (localMedia.isCut()) {
                        String photoPath = localMedia.getCutPath();
                        File file = new File(photoPath);
                        Glide.with(this).load(file).into(circleIvHead);
                        photoByte = BitmapToByteUtil.editContactPicture(this, photoPath);
                    }
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] paramArrayOfInt) {
        if (requestCode == WRITE_CONTACTS_RESULE) {
            if (!verifyPermissions(paramArrayOfInt)) {
                //还有权限未申请
                showMissingPermissionDialog();
                // if (isNeedCheck) {checkPermissions(needPermissions);} 防止去检测权限，不停的弹框
                //isNeedCheck = false;
                return;
            }
            saveContacts();

        } else if (requestCode == READ_EXTERNAL_STORAGE) {
            if (!verifyPermissions(paramArrayOfInt)) {
                showMissingPermissionDialog();
                return;
            }
            openAlbum();
        } else if (requestCode == CAMERA) {
            if (!verifyPermissions(paramArrayOfInt)) {
                showMissingPermissionDialog();
                return;
            }
            openCamera();
        }
    }

    private void showDellDialog() {
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(ContactsModifyActivity.this);
        //   normalDialog.setIcon(R.drawable.icon_dialog);
        normalDialog.setTitle("删除该联系人？");
        normalDialog.setPositiveButton("确定",
                (dialog, which) -> {
                    //...To-do
                    if (contactsListEntity != null) {
                        boolean isDel = ContactsUtil.deleteContact(ContactsModifyActivity.this, contactsListEntity.getId());
                        if (isDel) {
                            EventBus.getDefault().post(Constants.EVENT_UPDATE_CONTACTS);
                            finish();
                        }
                    }
                });
        normalDialog.setNegativeButton("关闭",
                (dialog, which) -> {
                    //...To-do
                });
        // 显示
        normalDialog.show();
    }

}
