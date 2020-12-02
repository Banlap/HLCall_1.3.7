package hzhl.net.hlcall.activity;
import android.Manifest;
import android.annotation.TargetApi;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.orhanobut.logger.Logger;
import com.tencent.bugly.crashreport.CrashReport;

import org.linphone.core.Core;
import org.linphone.core.ProxyConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import hzhl.net.hlcall.App;
import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.adapter.ImageAdapter;
import hzhl.net.hlcall.api.Api;
import hzhl.net.hlcall.api.UpLoad;
import hzhl.net.hlcall.fragment.MovingFragment;
import hzhl.net.hlcall.utils.CompressHelper;
import hzhl.net.hlcall.utils.FileUtils;
import hzhl.net.hlcall.utils.GsonUtil;
import hzhl.net.hlcall.utils.LoadingView;
import hzhl.net.hlcall.utils.MyLog;
import hzhl.net.hlcall.utils.PhotoUtils;
import hzhl.net.hlcall.utils.PopWindow;
import io.itit.itf.okhttp.Response;
import io.itit.itf.okhttp.callback.Callback;
import okhttp3.Call;

public class MovingPubActivity extends BaseActivity {
    public static final int TAKE_CAMERA_REQUEST = 0X102;
    public static final int PHOTO_SELECT_REQUEST = 0X103;
    @Bind(R.id.text_address_head)
    TextView text_address_head;
    @Bind(R.id.text_address)
    TextView text_address;
    @Bind(R.id.text_hint)
    TextView text_hint;
    @Bind(R.id.text_up)
    TextView text_up;
    @Bind(R.id.edit_address)
    EditText edit_address;
    @Bind(R.id.et_content)
    EditText et_content;
    @Bind(R.id.rec_add_image)
    RecyclerView rec_add_image;

    /*@Bind(R.id.item_search_contact)
    ConstraintLayout item_search_contact;*/
    @Bind(R.id.edit_number)
    EditText edit_number;
   /* @Bind(R.id.iv_tonglx)
    ImageView iv_tonglx;
    @Bind(R.id.rec_contacts)
    RecyclerView rec_contacts;*/
    private String address;
    private ImageAdapter adapter;
    boolean sending = false;
    //private ContactsListViewModel viewModel;
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_moving_pub;
    }

    @Override
    protected String getTopTitle() {
        return "巡检功能";
    }

    @Override
    protected void init() {
        initLBS();
        //setRightIv(R.mipmap.icon_fabiao);
        setRightTv("上报");
        //viewModel = ViewModelProviders.of(this).get(ContactsListViewModel.class);



        rec_add_image.setLayoutManager(new GridLayoutManager(this,4));
        adapter = new ImageAdapter(this,new ArrayList<>());
        rec_add_image.setAdapter(adapter);
        adapter.setOnItemClickListener(position -> {
            //launchTakeImageWithCameraIntent();
            /*Intent photoIntent = new Intent(MovingPubActivity.this, PhotoSelectActivity.class);
            startActivityForResult(photoIntent, PHOTO_SELECT_REQUEST);*/


            PopWindow popWindow = PopWindow.init(MovingPubActivity.this);
                    popWindow.inflaterLayout(R.layout.popup_image_sl,
                            ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.WRAP_CONTENT)
                    .addBackground()
                    .create(v1 -> {
                        TextView pic_tv,camera_tv;
                        pic_tv = v1.findViewById(R.id.pic_tv);
                        camera_tv = v1.findViewById(R.id.camera_tv);
                        pic_tv.setOnClickListener(v -> {
                            Intent photoIntent = new Intent(MovingPubActivity.this, PhotoSelectActivity.class);
                            startActivityForResult(photoIntent, PHOTO_SELECT_REQUEST);
                            popWindow.dismiss();
                        });
                        camera_tv.setOnClickListener(v -> {
                            launchTakeImageWithCameraIntent();
                            popWindow.dismiss();
                        });

                    })
                    .show(rec_add_image, Gravity.CENTER,0,0);
        });
        et_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int over = 300-s.length();
                text_hint.setHint(String.format("还能输入%s字",over));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        getPersimmions();
        text_address.setOnClickListener(v -> initLBS());
        text_up.setOnClickListener(this::onClickRightTv);

        /*rec_contacts.setLayoutManager(new LinearLayoutManager(this));
        ContactsAdapter adapter = new ContactsAdapter(new ArrayList<>());
        rec_contacts.setAdapter(adapter);
        item_search_contact.setVisibility(View.VISIBLE);
        edit_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String number = s.toString();
                if (number.isEmpty())return;
                List<ContactsEntity> list = ContactsUtil.getListFormNumber(MovingPubActivity.this,number);
                if (list.isEmpty())list.add(new ContactsEntity(number,number));
                adapter.setData(list);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        adapter.setOnItemClickListener(entity -> {

        });
        iv_tonglx.setOnClickListener(v -> PopWindow.init(MovingPubActivity.this)
                .inflaterLayout(R.layout.popup_list,
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        ActionBar.LayoutParams.WRAP_CONTENT)
                .addBackground()
                .create(v1 -> {
                    ListView listView = v1.findViewById(R.id.act_address_list_listview);
                    viewModel.getList()
                            .observe(MovingPubActivity.this, contactsListEntities -> {
                                listView.setAdapter(new AddressListAdapter(MovingPubActivity.this,contactsListEntities));
                            });
                    viewModel.getContactsListTask();
                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        viewModel.getContactsListEntityForId(viewModel.getContactsListEntity(position).getId())
                                .observe(MovingPubActivity.this, contactsListEntity -> {
                                    if (contactsListEntity.getNumberList()==null
                                            ||contactsListEntity.getNumberList().size()==0)
                                        return;
                                    String number =  contactsListEntity.getNumberList().get(0).getNumber();
                                    String name =  contactsListEntity.getName();
                                    viewModel.getList().removeObservers(MovingPubActivity.this);
                                    edit_number.setText(String.format("%s(%s)", name, number));
                                    PopWindow.dismiss();
                                });
                    });

                })
                .show(v, Gravity.CENTER,0,0));*/

    }

    void initLBS(){
        LocationClient mLocationClient = new LocationClient(this);
        //声明LocationClient类l

        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        //可选，是否需要地址信息，默认为不需要，即参数为false
        //如果开发者需要获得当前点的地址信息，此处必须为true
        //可选，默认false，设置是否开启Gps定位
        option.setOpenGps(true);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setIgnoreKillProcess(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        //option.setIsNeedLocationDescribe(true);
        option.setOpenAutoNotifyMode();
        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
        mLocationClient.setLocOption(option);
        //mLocationClient为第二步初始化过的LocationClient对象
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        //更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明
        mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
                    @Override
                    public void onReceiveLocation(BDLocation location) {
                        //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
                        //以下只列举部分获取地址相关的结果信息
                        //更多结果信息获取说明，请参照类参考中BDLocation类中的说明
                        String describe = location.getLocationDescribe();    //获取详细地址信息
                        String country = location.getCountry();    //获取国家
                        String province = location.getProvince();    //获取省份
                        String city = location.getCity();    //获取城市
                        String district = location.getDistrict();    //获取区县
                        String street = location.getStreet();    //获取街道信息
                        String streetNumber = location.getStreetNumber();
                        address = province + city + district + street ;
                        //+ streetNumber;
                        MyLog.d("wen" + location.getLocType());
                        address = address.replace("null","");
                        text_address.setText(address);
                        mLocationClient.stop();
            }

        });

        mLocationClient.start();
    }

    @Override
    protected void onClickRightTv(View view) {
        String number = edit_number.getText().toString();
        if (sending){
            showToast("请不要重复发送");
            return;
        }
        if (et_content.getText().toString().trim().isEmpty()){
            showToast("内容不能为空");
            return;
        }
        if (number.trim().isEmpty()){
            showToast("收件人不能为空");
            return;
        }
        if ((address==null || address.isEmpty()) && edit_address.getText().toString().isEmpty()){
            showToast("定位失败,您可以手动输入地址");
            return;
        }
        sending = true;

        LoadingView.show(this,true);

        Core core;
        ProxyConfig config;
        if (!LinphoneService.isReady())return;
        core = LinphoneService.getCore();
        if (core == null)return;
        config = core.getDefaultProxyConfig();
        if (config == null)return;
        if (config.getContact() == null)return;
        String name = config.getContact().getUsername();

        List<File> files = new ArrayList<>();


        App.runAsync(() -> {
            for (String s:adapter.getPaths()
            ) {
                File oldFile = new File(s);
                if (oldFile.length()/1024 < 200){
                    files.add(oldFile);
                    continue;
                }
                File newFile = new File(FileUtils.getImageDirectory(MovingPubActivity.this,oldFile.getName()));
                /*if (newFile.exists()){
                    files.add(newFile);
                    continue;
                }*/
                String str=oldFile.getName();
                str=str.substring(0,str.lastIndexOf("."));
                newFile = new CompressHelper.Builder(MovingPubActivity.this)
                        .setMaxWidth(720)  // 默认最大宽度为720
                        .setMaxHeight(960) // 默认最大高度为960
                        .setQuality(80)    // 默认压缩质量为80
                        .setFileName(str) // 文件名称
                        .setCompressFormat(Bitmap.CompressFormat.JPEG) // 设置默认压缩为jpg格式
                        .setDestinationDirectoryPath(FileUtils.getImageDirectory(MovingPubActivity.this,""))//路径
                        .build()
                        .compressToFile(oldFile);
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + newFile.getAbsolutePath())));
                if (newFile.length()==0)continue;
                files.add(newFile);

            }
            Api.upload(name,
                    number,//
                    address+edit_address.getText(),
                    et_content.getText().toString().replace("\n","\\n"),
                    files,
                    new Callback(){
                        @Override
                        public void onFailure(Call call, Exception e, int id) {
                            //showToast(e.getMessage());
                            CrashReport.postCatchedException(e);
                            sending = false;
                            if (files.size() > 9) showToast("发表失败,可能图片太多了");
                            else showToast("发表失败");
                            LoadingView.show(MovingPubActivity.this,false);
                        }

                        @Override
                        public void onResponse(Call call, Response response, int id) {
                            if (response!=null) {
                                LoadingView.show(MovingPubActivity.this,false);
                                try {
                                    String string = response.body().string();
                                    UpLoad upLoad = GsonUtil.fromJson(string,UpLoad.class);
                                    if (upLoad.isSuccess()){
                                        showToast("发表成功");
                                        setResult(1001);
                                        MovingFragment.isRefresh = true;
                                        finish();
                                    }
                                    else {
                                        showToast(upLoad.getMessage());
                                    }
                                    sending = false;
                                } catch (Exception e) {
                                    CrashReport.postCatchedException(e);
                                    if (files.size() > 9) showToast("发表失败,可能图片太多了");
                                    else showToast("发表失败");
                                }
                            }
                        }
                    });
        });


    }

    @Override
    protected void onDestroy() {
        LoadingView.show(MovingPubActivity.this,false);
        super.onDestroy();
    }

    private String mCurrentPhotoPath;
    private void launchTakeImageWithCameraIntent() {
        File file = null;
        try {
            file = PhotoUtils.createImageFile(null);
            mCurrentPhotoPath = file.getAbsolutePath();
        } catch (IOException ex) {
        }
        if (file != null) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(
                    this,
                    App.sContext.getPackageName() + ".file_provider",
                    file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, TAKE_CAMERA_REQUEST);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_SELECT_REQUEST) {
            if (resultCode == PHOTO_SELECT_REQUEST) {
                List<String> selectList = data.getStringArrayListExtra(PhotoSelectActivity.KEY_SELECT);
                for (String s : selectList
                ) {
                    MyLog.d("wen", s + "\n");
                }

                adapter.addData(selectList);
            }
        } else if (requestCode == TAKE_CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                // sendMessage(mCurrentPhotoPath, 1);
                adapter.addData(mCurrentPhotoPath);
            }
            mCurrentPhotoPath = null;
        }
    }




    @TargetApi(23)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            /*
             * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
             */
            // 读写权限
            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            }
            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), 0);
            }
        }
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(permission)) {
                return true;
            } else {
                permissionsList.add(permission);
                return false;
            }
        } else {
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        initLBS();
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
