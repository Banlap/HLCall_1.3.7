package hzhl.net.hlcall.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.constant.Constants;
import hzhl.net.hlcall.entity.ContactsListEntity;
import hzhl.net.hlcall.utils.BitmapToByteUtil;
import hzhl.net.hlcall.utils.ContactsUtil;
import hzhl.net.hlcall.utils.StringUtil;
import hzhl.net.hlcall.view.LastInputEditText;

public class ContactsAddActivity extends BaseActivity {
    @Bind(R.id.edit_name)
    LastInputEditText editName;
    @Bind(R.id.edit_company)
    LastInputEditText editCompany;
    @Bind(R.id.edit_job)
    LastInputEditText editJob;
    @Bind(R.id.edit_number1)
    LastInputEditText editNumber1;
    @Bind(R.id.edit_number2)
    LastInputEditText editNumber2;
    @Bind(R.id.ll_number2)
    LinearLayout llNumber2;
    @Bind(R.id.circle_head)
    CircleImageView circleIvHead;
    private static final int WRITE_CONTACTS_RESULE = 115;
    private static final int READ_EXTERNAL_STORAGE = 116;
    private static final int CAMERA = 117;
    private byte[] photoByte;

    @Override
    protected int getLayoutResID() {
        return (R.layout.activity_contacts_add);
    }

    @Override
    protected String getTopTitle() {
        return "添加联系人";
    }

    @Override
    protected void init() {
        editNumber1.addTextChangedListener(textWatcher);
        Intent intent=getIntent();
        String number=intent.getStringExtra("number");
        editNumber1.setText(number);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @OnClick({R.id.tv_cancel, R.id.tv_save, R.id.circle_head})
    public void onClickVoid(View view) {
        switch (view.getId()) {
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
        String name = editName.getText().toString().trim();
        String number1 = editNumber1.getText().toString().trim();
        String number2 = editNumber2.getText().toString().trim();
        String company = editCompany.getText().toString().trim();
        String job = editJob.getText().toString().trim();
        if (StringUtil.isEmpty(name)) {
            showToast("请输入名字");
            return;
        }
        if (StringUtil.isEmpty(number1)) {
            showToast("请输入号码");
            return;
        }
        ContactsListEntity entity = new ContactsListEntity();
        entity.setJob(job);
        entity.setCompany(company);
        entity.setName(name);
        if (photoByte != null) {
            entity.setBytes(photoByte);
            Logger.d(photoByte);
        }
        boolean isSave;
        if (ContactsUtil.searchContact(this,entity.getName())!=null){
            showToast("该联系人已存在");
            isSave = false;
        }
        else isSave = ContactsUtil.writeContact(this, entity, number1, number2);
        if (isSave) {
            Toast.makeText(this, "保存联系人成功", Toast.LENGTH_SHORT).show();
            EventBus.getDefault().post(Constants.EVENT_UPDATE_CONTACTS);
            finish();
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
                new AlertDialog.Builder(ContactsAddActivity.this);
        listDialog.setTitle("头像");
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                String s = items[which];
                if ("删除照片".equals(s)) {
                    photoByte = null;
                    Glide.with(ContactsAddActivity.this).load(R.drawable.icon_touxiang).into(circleIvHead);
                } else if ("拍照".equals(s)) {
                    if (isOpenPermission(Manifest.permission.CAMERA)) {
                        openCamera();
                    } else {
                        ActivityCompat.requestPermissions(ContactsAddActivity.this
                                , new String[]{Manifest.permission.CAMERA}
                                , CAMERA);
                    }
                } else if ("从相册中选择照片".equals(s)) {
                    if (isOpenPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        openAlbum();
                    } else {
                        ActivityCompat.requestPermissions(ContactsAddActivity.this
                                , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
                                , READ_EXTERNAL_STORAGE);
                    }
                }
            }
        });
        listDialog.show();
    }

    private void openCamera() {
        PictureSelector.create(ContactsAddActivity.this)
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
        PictureSelector.create(ContactsAddActivity.this)
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


    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                llNumber2.setVisibility(View.GONE);
            } else {
                llNumber2.setVisibility(View.VISIBLE);
            }
        }
    };

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
