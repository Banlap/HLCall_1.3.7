package hzhl.net.hlcall.activity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import hzhl.net.hlcall.App;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.adapter.ContactsSlAdapter;
import hzhl.net.hlcall.entity.ContactsEntity;
import hzhl.net.hlcall.entity.IntercomEntity;
import hzhl.net.hlcall.utils.ContactsUtil;


public class IntercomAddActivity extends BaseActivity {

    @Bind({R.id.ll_0,R.id.ll_1,R.id.ll_2,R.id.ll_3,R.id.ll_4,R.id.ll_5,R.id.ll_6
            ,R.id.ll_7,R.id.ll_8,R.id.ll_9,R.id.ll_star,R.id.ll_jing})
    List<LinearLayout> lls;
    @Bind(R.id.iv_more)
    ImageView iv_more;
    @Bind(R.id.iv_del)
    ImageView iv_del;
    @Bind(R.id.tv_number)
    TextView tv_number;
    @Bind(R.id.ll_baohao)
    LinearLayout ll_baohao;
    @Bind(R.id.ll_add)
    LinearLayout ll_add;
    @Bind(R.id.rec_contacts_sl)
    RecyclerView rec_contacts_sl;
    @Bind(R.id.text1)
    TextView text1;

    private ContactsSlAdapter adapter;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_meeting_add;
    }

    @Override
    protected String getTopTitle() {
        return "添加成员";
    }

    @Override
    protected void init() {

        text1.setVisibility(View.GONE);
        iv_del.setOnClickListener(v -> {
            addOrDelNumber(-1);
        });
        iv_more.setOnClickListener(v -> {
            if (ll_baohao.getVisibility()==View.GONE){
                ll_baohao.setVisibility(View.VISIBLE);
            }else ll_baohao.setVisibility(View.GONE);
        });

        ll_add.setOnClickListener(v -> {
            addOrDelNumber(-1);
        });

        rec_contacts_sl.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactsSlAdapter(this, ContactsUtil.getList(this));
        rec_contacts_sl.setAdapter(adapter);
        IntercomEntity intercomEntity = (IntercomEntity)getIntent().getSerializableExtra("data");
        String name = getIntent().getStringExtra("name");
        ll_add.setOnClickListener(v -> {
            if (adapter.getSl().size() == 0)return;
            IntercomEntity entity;
            if (intercomEntity == null)
            {
                entity = new IntercomEntity();
                entity.setCreateTime(new Date());
                entity.setName("新对讲");
                entity.setContacts(adapter.getSl());
                long l = App.getDaoInstant()
                        .getIntercomEntityDao()
                        .insert(entity);
                if (l>0)showToast( "添加成功");
            }
            else {

                entity = intercomEntity;
                if (intercomEntity.getContacts() == null)intercomEntity.setContacts(new ArrayList<>());
                int i = 5 - entity.getContacts().size() - adapter.getSl().size();
                /*if (i<0){
                    showToast("只能添加"+(5 - entity.getContacts().size())+"位");
                    return;
                }*/
                for (ContactsEntity e:adapter.getSl()) {
                    for (ContactsEntity e2:intercomEntity.getContacts()) {
                        if (e.getNumber().equals(name)){
                            showToast("不能选择自己");
                            return;
                        }
                        if (e.getNumber().equals(e2.getNumber())) {
                            showToast(e.getNumber() + "已经存在");
                            return;
                        }

                    }
                }

                entity.getContacts().addAll(adapter.getSl());
                App.getDaoInstant().getIntercomEntityDao().update(intercomEntity);
                setResult(1001);
            }
            finish();

        });





    }


    @OnClick({R.id.ll_0,R.id.ll_1,R.id.ll_2,R.id.ll_3,R.id.ll_4,R.id.ll_5,R.id.ll_6,R.id.ll_7,
            R.id.ll_8,R.id.ll_9,R.id.ll_add,R.id.ll_star,R.id.ll_jing})
    public void onClickVoid(View v) {
        if (v instanceof LinearLayout) {
            int i = lls.indexOf(v);
            addOrDelNumber(i);
        }
    }

    private void addOrDelNumber(int i){
        String number = tv_number.getText().toString();
        if (i == -1){
            if (number.isEmpty())return;
            number = number.substring(0,number.length()-1);

        }else {
            if (i == 10)number += "*";
            else if (i == 11)number += "#";
            else number += i;
        }
        tv_number.setText(number);

        adapter.search(number);
    }

}
