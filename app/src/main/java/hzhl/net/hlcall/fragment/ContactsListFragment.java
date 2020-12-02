package hzhl.net.hlcall.fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.lifecycle.ViewModelStore;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.OnClick;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.activity.BaseFragment;
import hzhl.net.hlcall.activity.ContactsAddActivity;
import hzhl.net.hlcall.activity.ContactsDetailActivity;
import hzhl.net.hlcall.adapter.AddressListAdapter;
import hzhl.net.hlcall.constant.Constants;
import hzhl.net.hlcall.entity.ContactsListEntity;
import hzhl.net.hlcall.utils.CharacterParser;
import hzhl.net.hlcall.utils.ContactsUtil;
import hzhl.net.hlcall.utils.MyLog;
import hzhl.net.hlcall.utils.PinyinComparator;
import hzhl.net.hlcall.utils.StringUtil;
import hzhl.net.hlcall.view.ClearEditText;
import hzhl.net.hlcall.view.SideBar;

/**
 * Created by guang on 2019/7/29.
 */

public class ContactsListFragment extends BaseFragment {
    @Bind(R.id.act_address_list_listview)
    ListView mListView;
    @Bind(R.id.act_address_list_sidrbar)
    SideBar sideBar;
    @Bind(R.id.dialog)
    TextView dialogTextview;
    @Bind(R.id.act_address_list_edt)
    ClearEditText mClearEditText;
    @Bind(R.id.fl_content)
    FrameLayout flContent;
    @Bind(R.id.tv_no_data)
    TextView tvNoData;
    private ContactsListViewModel viewModel;
    private AddressListAdapter addressListAdapter;
    private ContentResolver contentResolver;
    private ArrayList<ContactsListEntity> contactsList = new ArrayList<>();//获取系统联系人的数据
    private ArrayList<ContactsListEntity> dataList = new ArrayList<>();//联系人列表的数据

    private CharacterParser characterParser;// 汉字转换成拼音的类
    private PinyinComparator pinyinComparator; //根据拼音来排列ListView里面的数据类
    private static final int CONTACTS_MSG = 2315;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == CONTACTS_MSG) {
                //更新联系人数据
                updateContacts();
            }
        }
    };

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_contacts_list;
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        viewModel = ViewModelProviders.of(getActivity()).get(ContactsListViewModel.class);
        addressListAdapter = new AddressListAdapter(getmContext(), dataList);
        mListView.setAdapter(addressListAdapter);
        //mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        sideBar.setTextView(dialogTextview);
        // 设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = addressListAdapter.getPositionForSection(s.charAt(0));
                MyLog.d("s-" + s.charAt(0));
                if (position != -1) {
                    mListView.setSelection(position);
                }

            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Logger.d("posi--" + position + " --entity--" + dataList.get(position).getName());
                Intent intent = new Intent(getmContext(), ContactsDetailActivity.class);
                intent.putExtra("AddressListEntity", dataList.get(position));
                startActivity(intent);
            }
        });
        mClearEditText.addTextChangedListener(textWatcher);
        queryContacts();
    }

    @Override
    public void onResume() {
        super.onResume();
        queryContacts();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden)queryContacts();
    }

    private void queryContacts() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //需要在子线程中处理的逻辑
                contactsList = ContactsUtil.queryContacts(getContext());
                //处理完成后给handler发送消息  
                Message msg = new Message();
                msg.what = CONTACTS_MSG;
                handler.sendMessage(msg);
                //wenyeyang
                viewModel.setContactsList(contactsList);
                Logger.d(viewModel);
            }
        }).start();
    }

    public void updateContacts() {
        dataList = contactsList;
        //设置数据拼音的首字母
        setSortedLetters();
        //拼音拼音比对类
        pinyinComparator = new PinyinComparator();
        // 根据a-z进行排序源数据
        Collections.sort(dataList, pinyinComparator);
        addressListAdapter.setData(dataList);
    }

    @Subscribe
    public void onEvent(String event) {
        if (event.equals(Constants.EVENT_UPDATE_CONTACTS)) {
            Logger.d("更新联系人");
            queryContacts();
        }
    }

  /*  @Subscribe(sticky = true)
    public void onEvent(ContactsListEntityEvent event) {
        contactsList = event.getContactsListEntities();
        dataList = contactsList;
        //设置数据拼音的首字母
        setSortedLetters();
        //拼音拼音比对类
        pinyinComparator = new PinyinComparator();
        // 根据a-z进行排序源数据
        Collections.sort(dataList, pinyinComparator);
        addressListAdapter.setData(dataList);
    }*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @OnClick({R.id.iv_add})
    public void onClickVoid(View view) {
        switch (view.getId()) {
            case R.id.iv_add:
                jump(ContactsAddActivity.class);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().unregister(this);
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence s, int i, int i1, int i2) {
            // 这个时候不需要挤压效果 就把他隐藏掉
            // titleLayout.setVisibility(View.GONE);
            Pattern p = Pattern.compile("^[A-Za-z]+$");
            Matcher m = p.matcher(s.toString());
            if (m.matches()) {// 判断如果输入的为英文字母统一转化成小写
                filterData(s.toString().toLowerCase());
            } else {
                // 当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                filterData(s.toString());
            }

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    //设置数据拼音的首字母
    private void setSortedLetters() {
        // 实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();
        for (int i = 0; i < dataList.size(); i++) {
            ContactsListEntity entity = dataList.get(i);
            if (StringUtil.isEmpty(entity.getName())
                    || StringUtil.isEmpty(entity.getName().trim())) {
                entity.setName("#");
            }
            // 汉字转换成拼音
            Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
            if (p.matcher(entity.getName().trim().substring(0, 1)).matches()) {
                // 中文开头
                String pinyin = characterParser.getSelling(entity.getName());
                String sortString = pinyin.substring(0, 1).toUpperCase(
                        Locale.CHINESE);
                // 正则表达式，判断首字母是否是英文字母
                if (sortString.matches("[A-Z]")) {
                    entity.setSortLetters(sortString
                            .toUpperCase(Locale.CHINESE));
                } else {
                    entity.setSortLetters("#");
                }
            } else {
                // 英文
                String sortString = entity.getName().trim().substring(0, 1)
                        .toUpperCase(Locale.CHINESE);
                if (sortString.matches("[A-Z]")) {
                    entity.setSortLetters(sortString
                            .toUpperCase(Locale.CHINESE));
                } else {
                    entity.setSortLetters("#");
                }
            }
        }
    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     *
     * @param filterStr
     */
    private void filterData(String filterStr) {
        dataList = contactsList;//给ListView数据重新赋值
        ArrayList<ContactsListEntity> filterDateList = new ArrayList<>();
        if (TextUtils.isEmpty(filterStr)) {
            //输入框为空
            addressListAdapter.setSearch(false);
            filterDateList = dataList;
        } else {
            addressListAdapter.setSearch(true);
            filterDateList.clear();
            for (ContactsListEntity sortModel : dataList) {
                String name = sortModel.getName();
                // 如果昵称是英文，把第一个字母转换为小写
                Pattern p = Pattern.compile("^[A-Za-z]+$");
                Matcher m = p.matcher(name.substring(0, 1));
                if (m.matches()) {
                    name = name.toLowerCase();
                }
                // 根据条件筛选符合粉丝
                MyLog.d("filterStr:   "+filterStr+"   name:  "+name);
                if (name.contains(filterStr.toLowerCase())
                        ||characterParser.getSelling(name).startsWith(filterStr)
                ) {
                    filterDateList.add(sortModel);
                }
            }
        }

        if (filterDateList.size() == 0) {
            tvNoData.setVisibility(View.VISIBLE);
            flContent.setVisibility(View.GONE);
        } else {
            tvNoData.setVisibility(View.GONE);
            flContent.setVisibility(View.VISIBLE);
            // 根据a-z进行排序
            Collections.sort(filterDateList, pinyinComparator);
            addressListAdapter.updateListView(filterDateList);
            dataList = filterDateList;//更新ListView数据为filterDateList
        }
    }
}
