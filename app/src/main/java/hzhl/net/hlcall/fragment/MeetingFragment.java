package hzhl.net.hlcall.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.OnClick;
import hzhl.net.hlcall.App;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.activity.BaseFragment;
import hzhl.net.hlcall.activity.IntercomActivity;
import hzhl.net.hlcall.activity.IntercomAddActivity;
import hzhl.net.hlcall.activity.MeetingActivity;
import hzhl.net.hlcall.activity.MeetingAddActivity;
import hzhl.net.hlcall.adapter.IntercomAdapter;
import hzhl.net.hlcall.adapter.MeetingAdapter;
import hzhl.net.hlcall.entity.IntercomEntityDao;
import hzhl.net.hlcall.entity.MeetingEntityDao;

/**
 * Created by guang on 2019/7/29.
 */

public class MeetingFragment extends BaseFragment {
    @Bind(R.id.iv_add)
    ImageView iv_add;
    @Bind(R.id.rec_meeting)
    RecyclerView rec_meeting;
    @Bind(R.id.text_meeting)
    TextView text_meeting;
    @Bind(R.id.text_intercom)
    TextView text_intercom;


    private int type = 0;
    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_meeting;
    }

    @Override
    protected void initView() {
        rec_meeting.setLayoutManager(new LinearLayoutManager(getActivity()));
        setDataMeeting();
        View.OnClickListener listener = v -> {
            switch (v.getId()){
                case R.id.text_meeting:
                    if (type == 0)return;
                    type = 0;
                    initData();
                    text_meeting.setTextColor(getResources().getColor(R.color.black_title_font));
                    text_intercom.setTextColor(getResources().getColor(R.color.colorD));
                    break;
                case R.id.text_intercom:
                    if (type == 1)return;
                    type = 1;
                    initData();
                    text_intercom.setTextColor(getResources().getColor(R.color.black_title_font));
                    text_meeting.setTextColor(getResources().getColor(R.color.colorD));
                    break;
            }
        };
        text_meeting.setOnClickListener(listener);
        text_intercom.setOnClickListener(listener);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void setDataMeeting(){
        MeetingEntityDao meetingEntityDao = App.getDaoInstant().getMeetingEntityDao();;
        MeetingAdapter adapter = new MeetingAdapter(getActivity(), meetingEntityDao.loadAll());
        rec_meeting.setAdapter(adapter);

        adapter.setOnItemClickListener(entity -> {
            Intent intent = new Intent(getActivity(), MeetingActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("data",entity);
            intent.putExtras(bundle);
            startActivity(intent);
        });
    }
    private void setDataIntercom(){
        IntercomEntityDao intercomEntityDao = App.getDaoInstant().getIntercomEntityDao();;
        IntercomAdapter adapter = new IntercomAdapter(getActivity(), intercomEntityDao.loadAll());
        rec_meeting.setAdapter(adapter);
        adapter.setOnItemClickListener(entity -> {
            Intent intent = new Intent(getActivity(), IntercomActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("data",entity);
            intent.putExtras(bundle);
            startActivity(intent);
        });
    }

    private void initData(){
        if (type == 0)setDataMeeting();
        else setDataIntercom();
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        initData();
    }

    @OnClick({R.id.iv_add,R.id.text_meeting,R.id.text_intercom})
    public void onClickVoid(View view) {
        switch (view.getId()) {
            case R.id.iv_add:
                if (type == 0)jump(MeetingAddActivity.class);
                else jump(IntercomAddActivity.class);
                break;
        }

    }
}
