package hzhl.net.hlcall.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.LinearLayout;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.activity.BaseFragment;
import hzhl.net.hlcall.adapter.TongHuaMissCallAdapter;
import hzhl.net.hlcall.constant.Constants;
import hzhl.net.hlcall.entity.TongHuaEntity;
import hzhl.net.hlcall.listener.CallItemClickListener;
import hzhl.net.hlcall.utils.TimeUtil;

/**
 * Created by guang on 2019/8/9.
 */

public class CallRecordMissCallFg extends BaseFragment implements TongHuaMissCallAdapter.OnRecyclerViewItemClickListener {
    @Bind(R.id.recy_missed_call_tonghua)
    RecyclerView mRecyclerView;
    @Bind(R.id.ll_bottom)
    LinearLayout llBottom;
    private List<TongHuaEntity> dataList = new ArrayList<>();
    private ContentResolver resolver;
    private Uri callUri = CallLog.Calls.CONTENT_URI;
    private String[] columns = {CallLog.Calls.CACHED_NAME// 通话记录的联系人
            , CallLog.Calls.NUMBER// 通话记录的电话号码
            , CallLog.Calls.DATE// 通话记录的日期
            , CallLog.Calls.DURATION// 通话时长
            , CallLog.Calls.TYPE};// 通话类型}
    private TongHuaMissCallAdapter adapter;
    private List<TongHuaEntity> chooseList = new ArrayList<>();
    private OnAfterDelListener onAfterDelListener;

    private CallItemClickListener callItemClickListener;

    public void setCallItemClickListener(CallItemClickListener callItemClickListener){
        this.callItemClickListener = callItemClickListener;
    }

    /**
     * 删除通讯记录之后的接口
     */
    public interface OnAfterDelListener {
        void onCallRecordMissCallAfterDel();
    }

    public void setOnAfterDelListener(OnAfterDelListener listener) {
        onAfterDelListener = listener;
    }

    public void searchNum(String num){
        List<TongHuaEntity> searchList = new ArrayList();
        for(TongHuaEntity entity : dataList){
            if(entity.getNumber().contains(num)){
                searchList.add(entity);
            }
        }
        adapter.setData(searchList, num);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fg_callrecord_missed_call;
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        adapter = new TongHuaMissCallAdapter(getmContext(), dataList);
        adapter.setOnItemClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getmContext(), LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getmContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.shape_line));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setAdapter(adapter);
        refreshDate();
    }


    @Override
    public void OnItemClick(TongHuaEntity entity, int position) {
        if(callItemClickListener != null){
            String number = entity.getNumber();
            callItemClickListener.onCallItemClick(number);
        }
//        PopupWindow popupWindow = LinphoneUtils.setCallPopupWindow(getmContext(), number, true);
//        if (popupWindow != null) {
//            popupWindow.showAtLocation(popupWindow.getContentView(), Gravity.BOTTOM, 0, 0);
//            WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
//            lp.alpha = 0.5f;
//            getActivity().getWindow().setAttributes(lp);
//            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
//                @Override
//                public void onDismiss() {
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
//                            lp.alpha = 1f;
//                            getActivity().getWindow().setAttributes(lp);
//                        }
//                    }, 300);
//                }
//            });
//        }
    }

    @Override
    public void OnChoose(List<TongHuaEntity> chooseList1) {
        chooseList = chooseList1;
    }

    //订阅EventBus事件，刷新数据
    @Subscribe
    public void onEvent(String event) {
        if (event.equals(Constants.EVENT_UPDATE_CALL_RECORD)) {
            refreshDate();
            //     Logger.d("刷新");
        }
    }

    @SuppressLint("MissingPermission")
    @OnClick({R.id.tv_del, R.id.tv_all_choose})
    public void onClickVoid(View view) {
        switch (view.getId()) {
            case R.id.tv_del:
                for (int i = 0; i < chooseList.size(); i++) {
                    int result = resolver.delete(callUri, CallLog.Calls._ID + "=?", new String[]{chooseList.get(i).getId()});
                    if (result > 0) {
                        Logger.d("deleted success:" + chooseList.get(i).getNumber());
                        dataList.remove(chooseList.get(i));
                    } else {
                        Logger.d("deleted fail:" + chooseList.get(i).getNumber());
                    }
                }
                refreshDate();
                if (onAfterDelListener != null) {
                    onAfterDelListener.onCallRecordMissCallAfterDel();
                }
                break;
            case R.id.tv_all_choose:
                if (view.isSelected()) {
                    adapter.setAllChoose(false);
                    view.setSelected(false);
                } else {
                    adapter.setAllChoose(true);
                    view.setSelected(true);
                }
                break;
        }
    }

    public void refreshDate() {
        if (adapter != null) {
            dataList = getDataList();
            adapter.setData(dataList, "");
        }
    }

    /**
     * 读取数据
     *
     * @return 读取到的数据
     */
    private List<TongHuaEntity> getDataList() {
        List<TongHuaEntity> list = new ArrayList<>();
        // 1.获得ContentResolver
        resolver = getmContext().getContentResolver();
        if (ContextCompat.checkSelfPermission(getmContext(), Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            // 2.利用ContentResolver的query方法查询通话记录数据库
            /**
             * @param uri 需要查询的URI，（这个URI是ContentProvider提供的）
             * @param projection 需要查询的字段
             * @param selection sql语句where之后的语句
             * @param selectionArgs ?占位符代表的数据
             * @param sortOrder 排序方式
             */
            Cursor cursor = resolver.query(callUri, // 查询通话记录的URI
                    null
                    , null, null, CallLog.Calls.DEFAULT_SORT_ORDER// 按照时间逆序排列，最近打的最先显示
            );
            // 3.通过Cursor获得数据
            int i = 0;
            int count = getResources().getInteger(R.integer.call_record_list_items);
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); (!cursor.isAfterLast()) && i < count; cursor.moveToNext(), i++) {
                    String id = cursor.getString(cursor.getColumnIndex(CallLog.Calls._ID));
                    String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                    String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                    long dateLong = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(dateLong));
                    String time = new SimpleDateFormat("HH:mm").format(new Date(dateLong));
                    int duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));
                    int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                    String typeS = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
                    //Logger.d(id);
                    // MyLog.d(number + "--typeS:" + typeS);
                    // MyLog.d("NEW:"+NEW);
        /*    int incoming=cursor.getInt(cursor.getColumnIndex(CallLog.Calls.INCOMING_TYPE));
            int missed=cursor.getInt(cursor.getColumnIndex(CallLog.Calls.MISSED_TYPE));
            int outgoing=cursor.getInt(cursor.getColumnIndex(CallLog.Calls.OUTGOING_TYPE));*/

                    String dayCurrent = new SimpleDateFormat("dd").format(new Date());
                    String dayRecord = new SimpleDateFormat("dd").format(new Date(dateLong));
                    String typeString = "";
                    switch (type) {
                        case CallLog.Calls.INCOMING_TYPE:
                            //"打入"
                            typeString = "打入";
                            break;
                        case CallLog.Calls.OUTGOING_TYPE:
                            //"打出"
                            typeString = "打出";
                            break;
                        case CallLog.Calls.MISSED_TYPE:
                            //"未接"
                            typeString = "未接";
                            break;
                        default:
                            break;
                    }
         /*   if (MobileUtil.isMobileNO(number)) {
            }*/
                    String dayString = "";
                    if ((Integer.parseInt(dayCurrent)) == (Integer.parseInt(dayRecord))) {
                        //今天
                        dayString = "今天";
                    } else if ((Integer.parseInt(dayCurrent) - 1) == (Integer.parseInt(dayRecord))) {
                        //昨天
                        dayString = "昨天";
                    } else {
                        //前天
                        dayString = "前天";
                    }
                    //只显示48小时以内通话记录，防止通     //话记录数据过多影响加载速度
                  /*  long day_lead = TimeStampUtil.compareDayTime(date);
                    if (day_lead < 2) {
                    }*/
                    if ("未接".equals(typeString)) {
                        TongHuaEntity entity = new TongHuaEntity();
                        //"未备注联系人"
                        entity.setId(id);
                        entity.setName((name == null) ? "未备注联系人" : name);
                        entity.setNumber(number);
                        entity.setDate(date);//通话日期
                        entity.setDuration(TimeUtil.getTime(duration));//时长
                        entity.setType(typeString);//类型
                        entity.setTime(time);//通话时间
                        entity.setDayString(dayString);//今天，昨天，前天
                        list.add(entity);
                    }
                }
            }
            //防止内存溢出，cursor关掉
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }
    //修改事件，显示选择按钮

    public void showChoose(String type) {
        if ("modify".equals(type)) {
            adapter.showChoose(true);
            llBottom.setVisibility(View.VISIBLE);
        } else {
            adapter.showChoose(false);
            llBottom.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
