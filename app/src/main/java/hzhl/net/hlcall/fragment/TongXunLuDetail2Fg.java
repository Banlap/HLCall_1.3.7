package hzhl.net.hlcall.fragment;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.activity.BaseFragment;
import hzhl.net.hlcall.adapter.CallRecordOneNumberAdapter;
import hzhl.net.hlcall.entity.ContactsListEntity;
import hzhl.net.hlcall.entity.TongHuaEntity;

/**
 * Created by guang on 2019/8/9.
 */

public class TongXunLuDetail2Fg extends BaseFragment {
    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;
    private CallRecordOneNumberAdapter adapter;
    private Uri callUri = CallLog.Calls.CONTENT_URI;
    private List<TongHuaEntity> dataList = new ArrayList<>();
    private ContentResolver resolver;
    private ContactsListEntity entity = new ContactsListEntity();

    @Override
    protected int getLayoutResID() {
        return R.layout.fg_xongxunlu_detail_2;
    }

    @Override
    protected void initView() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            entity =  bundle.getParcelable("AddressListEntity");
        }
        //   dataList = getDataList();
        adapter = new CallRecordOneNumberAdapter(getmContext(), dataList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getmContext(), LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getmContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.shape_line));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(adapter);
    }

   /* private List<TongHuaEntity> getDataList() {
        String numberArgs = entity.getNumber().replaceAll(" ", "");//去掉空格
        MyLog.d(numberArgs + "--numberArgs");
        // 1.获得ContentResolver
        resolver = getmContext().getContentResolver();
        if (ContextCompat.checkSelfPermission(getmContext(), Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
        }
        // 2.利用ContentResolver的query方法查询通话记录数据库
        *//**
         * @param uri 需要查询的URI，（这个URI是ContentProvider提供的）
         * @param projection 需要查询的字段
         * @param selection sql语句where之后的语句
         * @param selectionArgs ?占位符代表的数据
         * @param sortOrder 排序方式
         *//*
        Cursor cursor = resolver.query(callUri, // 查询通话记录的URI
                null
                , "number=?", new String[]{numberArgs}, CallLog.Calls.DEFAULT_SORT_ORDER// 按照时间逆序排列，最近打的最先显示
        );
        // 3.通过Cursor获得数据
        List<TongHuaEntity> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(CallLog.Calls._ID));
            String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
            String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            long dateLong = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(dateLong));
            String time = new SimpleDateFormat("HH:mm").format(new Date(dateLong));
            int duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));
            int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
            String typeS = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
            MyLog.d(number + "--typeS:" + typeS);
            // MyLog.d("NEW:"+NEW);
        *//*    int incoming=cursor.getInt(cursor.getColumnIndex(CallLog.Calls.INCOMING_TYPE));
            int missed=cursor.getInt(cursor.getColumnIndex(CallLog.Calls.MISSED_TYPE));
            int outgoing=cursor.getInt(cursor.getColumnIndex(CallLog.Calls.OUTGOING_TYPE));*//*

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
                case 5:
                    typeString = "打入，已挂断";
                    break;
                default:
                    typeString = type + "";
                    break;
            }
         *//*   if (MobileUtil.isMobileNO(number)) {
            }*//*
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
         *//*   long day_lead = TimeStampUtil.compareDayTime(date);
            if (day_lead < 2) {
            }*//*
            TongHuaEntity entity = new TongHuaEntity();
            //"未备注联系人"
            entity.setId(id);
            entity.setName((name == null) ? "未备注联系人" : name);
            entity.setNumber(number);
            entity.setDate(date);//通话日期
            entity.setDuration((duration / 60) + "分钟");//时长
            entity.setType(typeString);//类型
            entity.setTime(time);//通话时间
            entity.setDayString(dayString);//今天，昨天，前天
            list.add(entity);
        }
        //防止内存溢出，cursor关掉
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }*/


}
