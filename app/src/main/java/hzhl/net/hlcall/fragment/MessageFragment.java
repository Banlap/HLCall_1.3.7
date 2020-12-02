package hzhl.net.hlcall.fragment;

import android.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.linphone.core.Address;
import org.linphone.core.ChatMessage;
import org.linphone.core.ChatRoom;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.activity.BaseFragment;
import hzhl.net.hlcall.activity.NewChatActivity;
import hzhl.net.hlcall.adapter.MsgConversationAdapter;
import hzhl.net.hlcall.adapter.MsgConversationAdapter2;
import hzhl.net.hlcall.utils.ContactsUtil;
import hzhl.net.hlcall.utils.DataCache;
import hzhl.net.hlcall.utils.MyLog;
import hzhl.net.hlcall.utils.WK;

/**
 * Created by guang on 2019/7/29.
 */

public class MessageFragment extends BaseFragment implements MsgConversationAdapter.ClickListener {
    @Bind(R.id.msg_list_view)
    RecyclerView mMsgListView;
    @Bind(R.id.iv_send_message)
    View iv_send_message;
    private ContactsListViewModel viewModel;

    //private MsgConversationAdapter mConversationAdapter;
    private MsgConversationAdapter2 mConversationAdapter;

    private Core core;
    private AlertDialog.Builder normalDialog;//对话框,防止多次弹出

    private CoreListenerStub mListener;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_message;
    }

    @Override
    protected void initView() {
        viewModel = ViewModelProviders.of(getActivity()).get(ContactsListViewModel.class);
        if(LinphoneService.isReady()){
            core = LinphoneService.getCore();
            ChatRoom[] rooms = core.getChatRooms();
            List<ChatRoom> roomList = Arrays.asList(rooms);
            //wenyeyang
            for (ChatRoom r:roomList
                 ) {
                MyLog.d("PeerAddress"+ r.getPeerAddress().asString());
                MyLog.d("LocalAddress"+r.getLocalAddress().asString());
            }

            //mConversationAdapter = new MsgConversationAdapter(getActivity(), roomList, this);
            mConversationAdapter = new MsgConversationAdapter2(getActivity(),this);
            mMsgListView.setAdapter(mConversationAdapter);
            mConversationAdapter.setLongClickListener((name, chatRoom1, chatRoom2, v) -> {
                PopupMenu popup = new PopupMenu(getActivity(), v);
                popup.inflate(R.menu.chat_room_menu);
                popup.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()){
                        case R.id.delete:
                            showDellDialog(() -> {
                                if (chatRoom1!=null)chatRoom1.deleteHistory();
                                if (chatRoom2!=null)chatRoom2.deleteHistory();
                                mConversationAdapter.removeRoom(name);
                            }, () -> {
                                return;
                            });

                            break;
                    }
                    return true;
                });
                popup.show();
            });

            mConversationAdapter.refresh();
        }

        mListener = new CoreListenerStub() {
            @Override
            public void onMessageSent(Core core, ChatRoom room, ChatMessage message) {
                refreshChatRoom(room);
            }

            @Override
            public void onMessageReceived(Core core, ChatRoom cr, ChatMessage message) {
                new DataCache(getActivity()).putString(cr.getPeerAddress().getUsername(),cr.getPeerAddress().asStringUriOnly());
                refreshChatRoom(cr);
            }

            @Override
            public void onMessageReceivedUnableDecrypt(
                    Core core, ChatRoom room, ChatMessage message) {
                refreshChatRoom(room);
            }

            @Override
            public void onChatRoomRead(Core core, ChatRoom room) {
                refreshChatRoom(room);
            }

            @Override
            public void onChatRoomStateChanged(
                    Core core, ChatRoom cr, ChatRoom.State state) {
                if (state == ChatRoom.State.Created) {
                    refreshChatRoom(cr);
                }
            }
        };

        if (core != null) {
            core.addListener(mListener);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mMsgListView.setLayoutManager(layoutManager);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), layoutManager.getOrientation());
        itemDecoration.setDrawable(getResources().getDrawable(R.drawable.shape_divider));
        mMsgListView.addItemDecoration(itemDecoration);


        refreshChatRoomsList();

        //wenyeyang
        /*iv_send_message.setOnClickListener(v -> PopWindow.init(getActivity())
                .inflaterLayout(R.layout.popup_list,
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        ActionBar.LayoutParams.WRAP_CONTENT)
                .addBackground()
                .create(v1 -> {
                    ListView listView = v1.findViewById(R.id.act_address_list_listview);
                    viewModel.getList()
                            .observe(MessageFragment.this, contactsListEntities -> {
                                listView.setAdapter(new AddressListAdapter(getActivity(),contactsListEntities));
                            });
                    viewModel.getContactsListTask();
                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        viewModel.getContactsListEntityForId(viewModel.getContactsListEntity(position).getId());
                        viewModel.getContact()
                                .observe(MessageFragment.this, contactsListEntity -> {
                                    if (contactsListEntity.getNumberList()==null
                                    ||contactsListEntity.getNumberList().size()==0)
                                        return;
                                    String number =  contactsListEntity.getNumberList().get(0).getNumber();
                                    String name =  contactsListEntity.getName();

                                    LinphoneUtils.sendMessage(getActivity(),number,name);

                                    viewModel.getList().removeObservers(MessageFragment.this);
                                    PopWindow.dismiss();
                                });
                    });

                })
                .show(v, Gravity.CENTER,0,0));*/

        iv_send_message.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), NewChatActivity.class));
        });

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        MyLog.d("onHiddenChanged:" + hidden);
        if (!hidden)refreshChatRoomsList();
    }



    @Override
    public void onResume() {
        super.onResume();
        MyLog.d("onResume:");
        core.addListener(mListener);
        refreshChatRoomsList();
    }

    @Override
    public void onPause() {
        super.onPause();
        core.removeListener(mListener);
        MyLog.d("onPause:");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (core != null) {
            core.removeListener(mListener);
        }
    }
    private void refreshChatRoom(ChatRoom cr) {
        MyLog.d("refreshChatRoom:");
        //wenyeyang
        refreshChatRoomsList();
        /*
        MsgConversationAdapter.ConversationHolder holder = (MsgConversationAdapter.ConversationHolder) cr.getUserData();
        if (holder != null) {
            int position = holder.getAdapterPosition();
            if (position == 0) {
                mConversationAdapter.notifyItemChanged(0);
            } else {
                refreshChatRoomsList();
            }
        } else {
            refreshChatRoomsList();
        }

         */
    }

    private void refreshChatRoomsList() {
        mConversationAdapter.refresh();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);

    }


    @OnClick({})
    public void onClickVoid(View view) {
        switch (view.getId()) {
        }
    }

    @Override
    public void onItemClicked(ChatRoom room, int position) {
        if (room != null) {
            //TODO 跳转聊天
            String number = room.getPeerAddress().getUsername();
            String displayName = ContactsUtil.getNameFormNumber(getActivity(),number);
            //
            if(WK.empty(displayName)){
                displayName = number;
            }
            Intent intent = new Intent(getActivity(), NewChatActivity.class);
            intent.putExtra(NewChatActivity.REMOTE_DISPLAY_NAME, displayName);
            //intent.putExtra(ChatActivity.REMOTE_SIP_URI, room.getPeerAddress().getUsername());
            //Address address = room.getPeerAddress().clone();
            //address.setDomain(core.getDefaultProxyConfig().getDomain());
            intent.putExtra(NewChatActivity.REMOTE_SIP_URI, number);
            startActivity(intent);

            //wenyeyang

        }
    }

    /**
     * 对话框
     */
    private void showDellDialog(Runnable yes,Runnable no) {
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        normalDialog = new AlertDialog.Builder(getActivity());
        //   normalDialog.setIcon(R.drawable.icon_dialog);
        normalDialog.setTitle("确认删除?");
        normalDialog.setPositiveButton("确定",
                (dialog, which) -> {
                    //...To-do
                    yes.run();
                });
        normalDialog.setNegativeButton("拒绝",
                (dialog, which) -> {
                    //...To-do
                    no.run();
                });
        // 显示
        normalDialog.show();
    }
}
