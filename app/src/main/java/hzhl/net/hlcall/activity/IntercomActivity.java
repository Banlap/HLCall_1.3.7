package hzhl.net.hlcall.activity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import org.linphone.core.Address;
import org.linphone.core.ChatMessage;
import org.linphone.core.ChatRoom;
import org.linphone.core.Core;
import org.linphone.core.ProxyConfig;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.adapter.IntercomContactsAdapter;
import hzhl.net.hlcall.entity.ContactsEntity;
import hzhl.net.hlcall.entity.IntercomEntity;
import hzhl.net.hlcall.utils.MyLog;


public class IntercomActivity extends BaseActivity {

    @Bind(R.id.rec_intercom_contacts)
    RecyclerView rec_intercom_contacts;
    @Bind(R.id.ll_send)
    View ll_send;
    private IntercomContactsAdapter adapter;
    private IntercomEntity intercomEntity;
    private Core core;
    private ChatRoom mChatRoom;
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_intercom;
    }

    @Override
    protected String getTopTitle() {
        return "对讲";
    }

    @Override
    protected void init() {
        intercomEntity = (IntercomEntity) getIntent().getSerializableExtra("data");
        core = LinphoneService.getCore();
        if(core.getDefaultProxyConfig() == null){
            showToast("请先登录");
            finish();
            return;
        }

        rec_intercom_contacts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new IntercomContactsAdapter(intercomEntity.getContacts());
        rec_intercom_contacts.setAdapter(adapter);

        //ll_send.setOnClickListener(v -> sendMessage("hhhhhhh"));
    }

    private void sendMessage(String content) {
        //ChatMessage msg = mChatRoom.createEmptyMessage();
        //msg.getToAddress().setDomain("192.168.0.200");
        //msg.getFromAddress().setDomain("192.168.0.200");
        //msg.getLocalAddress().setDomain("192.168.0.200");
        ChatMessage msg = mChatRoom.createEmptyMessage();

        MyLog.d("onMessageReceived: getFromAddress"+msg.getFromAddress().asStringUriOnly());
        MyLog.d("onMessageReceived: getToAddress"+msg.getToAddress().asStringUriOnly());
        MyLog.d("onMessageReceived: getLocalAddress"+msg.getLocalAddress().asStringUriOnly());
        boolean hasText = content != null && content.length() > 0;
        if (hasText) {
            msg.addTextContent(content);
        }
        if (msg.getContents().length > 0) {
            msg.send();
            //Test.send(msg.getFromAddress().getUsername(), msg.getToAddress().getUsername(), msg.getTextContent());

        }
    }

    private Address[] getAddress(IntercomEntity intercomEntity){
        ProxyConfig config = core.getDefaultProxyConfig();
        if (config == null)return null;
        List<Address> list = new ArrayList<>();
        for (ContactsEntity e:intercomEntity.getContacts()) {
            list.add(config.normalizeSipUri(e.getNumber()));
        }
        //config.setConferenceFactoryUri("sip:8008@192.168.2.100");
        //config.getIdentityAddress().setUriParam("gruu","1");
        return list.toArray(new Address[]{});
    }
}
