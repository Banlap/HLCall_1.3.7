package hzhl.net.hlcall.activity;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.adapter.RecordsListAdapter;
import hzhl.net.hlcall.recording.Recording;
import hzhl.net.hlcall.utils.FileUtils;
import hzhl.net.hlcall.view.MyDialog;

public class RecordsActivity extends BaseActivity implements RecordsListAdapter.OnRecyclerViewItemClickListener {
    @Bind(R.id.recy_sip)
    RecyclerView mRecyclerView;
    @Bind(R.id.ll_bottom)
    LinearLayout llBottom;
    @Bind(R.id.tv_all_choose)
    TextView tvAllChoose;

    private RecordsListAdapter adapter;
    private List<Recording> mRecordings = new ArrayList<>();
    private AudioManager mAudioManager;
    public static final int WRITE_EXTERNAL_STORAGE_RESULT = 1118;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_records;
    }

    @Override
    protected String getTopTitle() {
        return "录音记录";
    }

    @Override
    protected void init() {
        setRightIv(R.drawable.icon_bianjiaaa);
        adapter = new RecordsListAdapter(this, mRecordings);
        adapter.setOnItemClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.shape_line));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setAdapter(adapter);
        if (LinphoneService.isReady()) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        mAudioManager.setSpeakerphoneOn(true);
        removeDeletedRecordings();
        searchForRecordings();
        adapter.setData(mRecordings);

    }

    @OnClick({R.id.tv_del, R.id.tv_all_choose})
    public void onClickVoid(View view) {
        switch (view.getId()) {
            case R.id.tv_del:
                if (adapter.getSelectedItemCount() > 0) {
                    showDelDialog();
                }
                break;
            case R.id.tv_all_choose:
                if (view.isSelected()) {
                    adapter.deselectAll();
                    view.setSelected(false);
                } else {
                    adapter.selectAll();
                    view.setSelected(true);
                }
                break;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        mAudioManager.setSpeakerphoneOn(false);
        // Close all opened mRecordings
        for (Recording r : mRecordings) {
            if (!r.isClosed()) {
                if (r.isPlaying()) r.pause();
                r.close();
            }
        }
    }

    @Override
    protected void onClickRightIv(View view) {
        super.onClickRightIv(view);
        if (isOpenPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            enterEditionMode();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                    , WRITE_EXTERNAL_STORAGE_RESULT);
        }

    }

    @Override
    protected void onClickRightTv(View view) {
        super.onClickRightTv(view);
        quitEditionMode();
    }

    @Override
    protected void onDestroy() {
        mRecyclerView = null;
        mRecordings = null;
        adapter = null;
        super.onDestroy();
    }

    @Override
    public void OnItemClick(int position) {
        if (adapter.isEditionEnabled()) {
            adapter.toggleSelection(position);
        }
    }

    public void enterEditionMode() {
        if (adapter.getItemCount() > 0) {
            setRightTv("完成");
            setRightIvGone();
            llBottom.setVisibility(View.VISIBLE);
            adapter.enableEdition(true);
        }
    }

    private void quitEditionMode() {
        if (adapter.getItemCount() > 0) {
            setRightIv(R.drawable.icon_bianjiaaa);
            setRightTvGone();
            tvAllChoose.setSelected(false);
            llBottom.setVisibility(View.GONE);
            adapter.enableEdition(false);
        }
    }
    private void quitEditionMode2() {
            setRightIv(R.drawable.icon_bianjiaaa);
            setRightTvGone();
            tvAllChoose.setSelected(false);
            llBottom.setVisibility(View.GONE);
            adapter.enableEdition(false);

    }

    private void removeDeletedRecordings() {
        String recordingsDirectory = FileUtils.getRecordingsDirectory(this);
        File directory = new File(recordingsDirectory);

        if (directory.exists() && directory.isDirectory()) {
            File[] existingRecordings = directory.listFiles();

            for (Recording r : mRecordings) {
                boolean exists = false;
                for (File f : existingRecordings) {
                    if (f.getPath().equals(r.getRecordPath())) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) mRecordings.remove(r);
            }

            Collections.sort(mRecordings);
        }
    }

    private void searchForRecordings() {
            String recordingsDirectory = FileUtils.getRecordingsDirectory(RecordsActivity.this);
            File directory = new File(recordingsDirectory);

            if (directory.exists() && directory.isDirectory()) {
                File[] existingRecordings = directory.listFiles();
                if (existingRecordings == null) return;

                for (File f : existingRecordings) {
                    boolean exists = false;
                    for (Recording r : mRecordings) {
                        if (r.getRecordPath().equals(f.getPath())) {
                            exists = true;
                            break;
                        }
                    }

                    if (!exists) {

                        if (Recording.RECORD_PATTERN.matcher(f.getPath()).matches()) {
                            mRecordings.add(new Recording(RecordsActivity.this, f.getPath()));
                        }
                    }
                }
                Collections.sort(mRecordings);
            }

    }

    protected void showDelDialog() {
        //设置网络传输方式 弹窗
        MyDialog.Builder builder = new MyDialog.Builder(this);
        MyDialog myDialog = builder.view(R.layout.dialog_del)
                .style(R.style.dialog)
                .widthdp(250)
                .cancelTouchout(true)
                .build();
        TextView tvDel = myDialog.getView().findViewById(R.id.tv_del);
        TextView tvCancel = myDialog.getView().findViewById(R.id.tv_cancel);
        tvDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteSelection();
                quitEditionMode2();
                myDialog.dismiss();
            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quitEditionMode2();
                myDialog.dismiss();
            }
        });
        myDialog.show();
    }

    public void onDeleteSelection() {
        Object[] objects = new Object[adapter.getSelectedItemCount()];
        int index = 0;
        for (Integer j : adapter.getSelectedItems()) {
            objects[index] = adapter.getItem(j);
            index++;
        }
        int size = adapter.getSelectedItemCount();
        for (int i = 0; i < objects.length; i++) {
            Recording record = (Recording) objects[i];

            if (record.isPlaying()) record.pause();
            record.close();

            File recordingFile = new File(record.getRecordPath());
            if (recordingFile.delete()) {
                mRecordings.remove(record);
            }
        }
      //  hideRecordingListAndDisplayMessageIfEmpty();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] paramArrayOfInt) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_RESULT) {
            if (!verifyPermissions(paramArrayOfInt)) {
                //还有权限未申请
                showMissingPermissionDialog();
                // if (isNeedCheck) {checkPermissions(needPermissions);} 防止去检测权限，不停的弹框
                //isNeedCheck = false;
            } else {
                enterEditionMode();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
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
