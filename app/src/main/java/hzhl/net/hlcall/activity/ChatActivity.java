package hzhl.net.hlcall.activity;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.linphone.core.Address;
import org.linphone.core.ChatMessage;
import org.linphone.core.ChatRoom;
import org.linphone.core.ChatRoomCapabilities;
import org.linphone.core.ChatRoomListener;
import org.linphone.core.Content;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.EventLog;
import org.linphone.core.Factory;
import org.linphone.core.Reason;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import hzhl.net.hlcall.App;
import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.adapter.ChatAdapter;
import hzhl.net.hlcall.chat.ChatScrollListener;
import hzhl.net.hlcall.utils.DataCache;
import hzhl.net.hlcall.utils.FileUtils;
import hzhl.net.hlcall.utils.LinphoneUtils;
import hzhl.net.hlcall.utils.MyLog;
import hzhl.net.hlcall.utils.PhotoUtils;
import hzhl.net.hlcall.utils.TextCopyUtil;
import hzhl.net.hlcall.utils.WK;
@Deprecated
public class ChatActivity extends BaseActivity implements ChatRoomListener{
    private static final int MESSAGES_PER_PAGE = 20;
    public static final int TAKE_CAMERA_REQUEST = 0X102;
    public static final int PHOTO_SELECT_REQUEST = 0X103;
    public static final String REMOTE_SIP_URI = "remote_sip_uri";
    public static final String REMOTE_DISPLAY_NAME = "remote_display_name";
    @Bind(R.id.history_msg_list)
    RecyclerView messageRecycleView;

    @Bind(R.id.chat_bottom_msg_et)
    EditText mMsgSendET;
    @Bind(R.id.chat_bottom_more_iv)
    ImageView mMoreIV;
    @Bind(R.id.chat_bottom_send_tv)
    TextView mSendTV;
    @Bind(R.id.chat_bottom_menu_ll)
    LinearLayout mMorePanelLayout;
    @Bind(R.id.chat_menu_pic_tv)
    TextView mPicTV;
    @Bind(R.id.chat_menu_camera_tv)
    TextView mCameraTV;
    @Bind(R.id.chat_menu_video_tv)
    TextView mVideoTV;
    @Bind(R.id.chat_menu_call_tv)
    TextView mCallTV;

    private String mRemoteSipUri;
    private String mRemoteSipName;
    private List<EventLog> mEventLogList = new ArrayList<>();
    private ChatAdapter mChatAdapter;
    private ChatRoom mChatRoom;
    private ChatRoom rChatRoom;
    //分页
    private int mChatRoomCount = 0;
    private int rChatRoomCount = 0;
    private int pageSize = 20;


    private Core core;
    DataCache dataCache;
    private CoreListenerStub coreListener = new CoreListenerStub(){
        @Override
        public void onMessageReceived(Core lc, ChatRoom room, ChatMessage message) {
            if (!room.getPeerAddress().getUsername().equals(mChatRoom.getPeerAddress().getUsername()))return;
            rChatRoomCount++;
            ChatMessage msg = message;
            if (msg.getErrorInfo() != null
                    && msg.getErrorInfo().getReason() == Reason.UnsupportedContent) {
                org.linphone.core.tools.Log.w(
                        "[Chat Messages Fragment] Message received but content is unsupported, do not display it");
                return;
            }

            if (!msg.hasTextContent() && msg.getFileTransferInformation() == null) {
                org.linphone.core.tools.Log.w(
                        "[Chat Messages Fragment] Message has no text or file transfer information to display, ignoring it...");
                return;
            }
            EventLog[] eventLogs = room.getHistoryEvents(MESSAGES_PER_PAGE);
            mChatAdapter.addToHistory(eventLogs[eventLogs.length-1]);
            room.markAsRead();
            scrollToBottom();
        }
    };

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_chat;
    }

    @Override
    protected String getTopTitle() {
        return "";
    }

    @Override
    protected void init() {
        mRemoteSipUri = getIntent().getStringExtra(REMOTE_SIP_URI);
        mRemoteSipName = getIntent().getStringExtra(REMOTE_DISPLAY_NAME);
        dataCache = new DataCache(this);
        MyLog.d(mRemoteSipUri);

        closeHideKeyboard();
        setTopTitle(mRemoteSipName);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        messageRecycleView.setLayoutManager(layoutManager);

        ChatScrollListener chatScrollListener =
                new ChatScrollListener(layoutManager) {
                    @Override
                    public void onLoadMore(int totalItemsCount) {
                        //loadMoreData(totalItemsCount);
                        loadMore();
                    }
                };
        messageRecycleView.addOnScrollListener(chatScrollListener);

        core = LinphoneService.getCore();
        if(core.getDefaultProxyConfig() == null){
            showToast("请先登录");
            finish();
            return;
        }

        core.addListener(coreListener);

        //Address addressToCall = core.getDefaultProxyConfig().normalizeSipUri(mRemoteSipUri);
        //mChatRoom = core.getChatRoomFromUri(addressToCall.asStringUriOnly());
        //mChatRoom = core.getChatRoomFromUri(mRemoteSipUri);
        mChatRoom = core.getChatRoom(core.createAddress(mRemoteSipUri),
               // core.getDefaultProxyConfig().getIdentityAddress());
               core.getDefaultProxyConfig().getContact());

        MyLog.d("addressLocal"+mChatRoom.getLocalAddress().asString());
        MyLog.d("addressLocal"+mChatRoom.getPeerAddress().asString());
        MyLog.d("addressLocal"+core.getDefaultProxyConfig().getIdentityAddress().asStringUriOnly());
        mEventLogList = new ArrayList<>(Arrays.asList(mChatRoom.getHistoryMessageEvents(MESSAGES_PER_PAGE))) ;
        //wenyeyang
        rChatRoom = core.getChatRoom(
                core.createAddress(dataCache.getString(mChatRoom.getPeerAddress().getUsername())),
                core.getDefaultProxyConfig().getContact());
        if (!rChatRoom.equals(mChatRoom))
        mEventLogList.addAll(Arrays.asList(rChatRoom.getHistoryMessageEvents(MESSAGES_PER_PAGE)));
        rChatRoomCount += pageSize;
        mChatRoomCount += pageSize;
        /*String local = mChatRoom.getLocalAddress().getUsername();
        String peer = mChatRoom.getPeerAddress().getUsername();
        for (ChatRoom r : core.getChatRooms()) {
            if (!r.getLocalAddress().asStringUriOnly().contains(local))continue;
            if (!r.getPeerAddress().asStringUriOnly().contains(peer))continue;
            mEventLogList.addAll(Arrays.asList(r.getHistoryEvents(MESSAGES_PER_PAGE)));
        }*/

        rChatRoom.markAsRead();
        Collections.sort(mEventLogList, (o1, o2) -> Long.compare(o1.getCreationTime(),o2.getCreationTime()));
        mChatAdapter = new ChatAdapter(this, mEventLogList);
        messageRecycleView.setAdapter(mChatAdapter);
        mChatRoom.addListener(this);
        mChatRoom.markAsRead();
        //wenyeyang
        mChatAdapter.setMessageOnClickListener((event, v) -> {
           // Toast.makeText(ChatActivity.this, event.getChatMessage().getTextContent(), Toast.LENGTH_SHORT).show();
            PopupMenu popup = new PopupMenu(this, v);
            popup.inflate(R.menu.message_menu);
            popup.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()){
                    case R.id.delete:
                       mChatAdapter.deleteHistory(event);
                       event.deleteFromDatabase();
                       if (mChatRoom.getHistoryEventsSize()==0)core.deleteChatRoom(mChatRoom);
                       if (rChatRoom.getHistoryEventsSize()==0)core.deleteChatRoom(rChatRoom);
                       break;
                    case R.id.copy:
                        if (TextCopyUtil.copy(event.getChatMessage().getTextContent(),this)){
                        Toast.makeText(this, "复制成功!", Toast.LENGTH_LONG).show();
                        }
                        break;

                }
                return false;
            });
            popup.show();
        });


        mMsgSendET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(TextUtils.isEmpty(s.toString())){
                    mMoreIV.setVisibility(View.VISIBLE);
                    mSendTV.setVisibility(View.GONE);
                }else{
                    mMoreIV.setVisibility(View.GONE);
                    mSendTV.setVisibility(View.VISIBLE);
                }
            }
        });
        mMsgSendET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMorePanelLayout.setVisibility(View.GONE);
            }
        });
    }

    private void loadMoreData(final int totalItemsCount) {
        App.runAsync(
                () -> {
                    int maxSize;
                    if (mChatRoom.hasCapability(ChatRoomCapabilities.OneToOne.toInt())) {
                        maxSize = mChatRoom.getHistorySize();
                    } else {
                        maxSize = mChatRoom.getHistoryEventsSize();
                    }
                    if (totalItemsCount < maxSize) {
                        int upperBound = totalItemsCount + MESSAGES_PER_PAGE;
                        if (upperBound > maxSize) {
                            upperBound = maxSize;
                        }
                        EventLog[] newLogs;
                        if (mChatRoom.hasCapability(ChatRoomCapabilities.OneToOne.toInt())) {
                            newLogs = mChatRoom.getHistoryRangeMessageEvents(
                                            totalItemsCount, upperBound);
                        } else {
                            newLogs = mChatRoom.getHistoryRangeEvents(totalItemsCount, upperBound);
                        }
                        ArrayList<EventLog> logsList = new ArrayList<>(Arrays.asList(newLogs));
                        //wenyeyang
                        logsList.addAll(Arrays.asList(rChatRoom.getHistoryRangeMessageEvents(totalItemsCount,upperBound)));
                        Collections.sort(mEventLogList, (o1, o2) -> Long.compare(o1.getCreationTime(),o2.getCreationTime()));
                        //
                        mChatAdapter.addAllToHistory(logsList);
                    }
                });
    }

    private void loadMore() {
        App.runAsync(new Runnable() {
                         @Override
                         public void run() {
                             ArrayList<EventLog> eventLogs = new ArrayList<>();
                             EventLog[] newLogs = mChatRoom.getHistoryRangeEvents(mChatRoomCount, mChatRoomCount+pageSize);
                             eventLogs.addAll(Arrays.asList(newLogs));
                             //wenyeyang
                             if (!rChatRoom.equals(mChatRoom))
                             eventLogs.addAll(Arrays.asList(rChatRoom.getHistoryRangeEvents(rChatRoomCount,rChatRoomCount+pageSize)));
                             Collections.sort(eventLogs, (o1, o2) -> Long.compare(o1.getCreationTime(),o2.getCreationTime()));
                             rChatRoomCount += pageSize;
                             mChatRoomCount += pageSize;
                             //
                             mChatAdapter.addAllToHistory(eventLogs);
                         }
                     }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mChatRoom != null){
            mChatRoom.removeListener(this);
        }
        if(mChatAdapter != null){
            mChatAdapter.clear();
        }
        if (core!=null){
            core.removeListener(coreListener);
        }
    }

    @OnClick({R.id.chat_menu_pic_tv, R.id.chat_menu_camera_tv,
            R.id.chat_menu_video_tv, R.id.chat_menu_call_tv,
            R.id.chat_bottom_more_iv, R.id.chat_bottom_send_tv})
    public void voidClick(View view){
        switch (view.getId()){
            case R.id.chat_menu_pic_tv:
                Intent photoIntent = new Intent(this, PhotoSelectActivity.class);
                startActivityForResult(photoIntent, PHOTO_SELECT_REQUEST);
                break;
            case R.id.chat_menu_camera_tv:
                launchTakeImageWithCameraIntent();
                break;
            case R.id.chat_menu_video_tv:
                LinphoneUtils.call(this, mRemoteSipUri, true);
                break;
            case R.id.chat_menu_call_tv:
                LinphoneUtils.call(this, mRemoteSipUri, false);
                break;
            case R.id.chat_bottom_more_iv:
                if(mMorePanelLayout.getVisibility() == View.VISIBLE){
                    mMorePanelLayout.setVisibility(View.GONE);
                }else{
                    hideSoftInput(view.getWindowToken());
                    mMorePanelLayout.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.chat_bottom_send_tv:
                sendMessage(mMsgSendET.getText().toString(), 0);
                mMsgSendET.setText("");
                break;
        }
    }

    private void sendMessage(String content, int type) {
        //ChatMessage msg = mChatRoom.createEmptyMessage();
        //msg.getToAddress().setDomain("192.168.0.200");
        //msg.getFromAddress().setDomain("192.168.0.200");
        //msg.getLocalAddress().setDomain("192.168.0.200");
        ChatMessage msg = mChatRoom.createEmptyMessage();

        MyLog.d("onMessageReceived: getFromAddress"+msg.getFromAddress().asStringUriOnly());
        MyLog.d("onMessageReceived: getToAddress"+msg.getToAddress().asStringUriOnly());
        MyLog.d("onMessageReceived: getLocalAddress"+msg.getLocalAddress().asStringUriOnly());
        if(type == 0){
            boolean hasText = content != null && content.length() > 0;
            if (hasText) {
                msg.addTextContent(content);
            }
        }else if(type == 1){
            String fileName = content.substring(content.lastIndexOf("/") + 1);
            String extension = FileUtils.getExtensionFromFileName(fileName);
            Content msgContent = Factory.instance().createContent();
            msgContent.setType("image");
            msgContent.setSubtype(extension);
            msgContent.setName(fileName);
            msgContent.setFilePath(content);
            ChatMessage fileMessage = mChatRoom.createFileTransferMessage(msgContent);
            fileMessage.send();
        }

        if (msg.getContents().length > 0) {
            msg.send();
            //Test.send(msg.getFromAddress().getUsername(), msg.getToAddress().getUsername(), msg.getTextContent());

        }
    }

    private void sendMultiImageMsg(List<String> filePathList){
        if(!WK.empty(filePathList)){
            for(String content : filePathList){
                String fileName = content.substring(content.lastIndexOf("/") + 1);
                String extension = FileUtils.getExtensionFromFileName(fileName);
                Content msgContent = Factory.instance().createContent();
                msgContent.setType("image");
                msgContent.setSubtype(extension);
                msgContent.setName(fileName);
                msgContent.setFilePath(content);
                ChatMessage fileMessage = mChatRoom.createFileTransferMessage(msgContent);
                fileMessage.send();
            }
        }
    }

    private void scrollToBottom() {
         if (messageRecycleView.getLayoutManager()!=null)messageRecycleView.getLayoutManager().scrollToPosition(0);
    }

    @Override
    public void onUndecryptableMessageReceived(ChatRoom chatRoom, ChatMessage chatMessage) {
    }

    @Override
    public void onConferenceLeft(ChatRoom chatRoom, EventLog eventLog) {

    }

    @Override
    public void onStateChanged(ChatRoom chatRoom, ChatRoom.State state) {
    }

    @Override
    public void onParticipantAdded(ChatRoom chatRoom, EventLog eventLog) {
    }

    @Override
    public void onEphemeralEvent(ChatRoom chatRoom, EventLog eventLog) {

    }

    @Override
    public void onSubjectChanged(ChatRoom chatRoom, EventLog eventLog) {
    }

    @Override
    public void onIsComposingReceived(ChatRoom chatRoom, Address address, boolean b) {
    }

    @Override
    public void onConferenceAddressGeneration(ChatRoom chatRoom) {
    }

    @Override
    public void onChatMessageSent(ChatRoom chatRoom, EventLog eventLog) {
        mChatAdapter.addToHistory(eventLog);
        //wenyeyang
        scrollToBottom();
    }

    @Override
    public void onChatMessageReceived(ChatRoom chatRoom, EventLog event) {
        /*
        chatRoom.markAsRead();
        ChatMessage msg = event.getChatMessage();
        if (msg.getErrorInfo() != null
                && msg.getErrorInfo().getReason() == Reason.UnsupportedContent) {
            org.linphone.core.tools.Log.w(
                    "[Chat Messages Fragment] Message received but content is unsupported, do not display it");
            return;
        }

        if (!msg.hasTextContent() && msg.getFileTransferInformation() == null) {
            org.linphone.core.tools.Log.w(
                    "[Chat Messages Fragment] Message has no text or file transfer information to display, ignoring it...");
            return;
        }

        mChatAdapter.addToHistory(event);
        scrollToBottom();

         */
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
        if(requestCode == PHOTO_SELECT_REQUEST){
            if(resultCode ==  PHOTO_SELECT_REQUEST){
                List<String> selectList =  data.getStringArrayListExtra(PhotoSelectActivity.KEY_SELECT);
                sendMultiImageMsg(selectList);
            }
        }else if(requestCode == TAKE_CAMERA_REQUEST){
            if(resultCode == RESULT_OK){
                sendMessage(mCurrentPhotoPath, 1);
            }
            mCurrentPhotoPath = null;
        }
    }

    @Override
    public void onChatMessageShouldBeStored(ChatRoom chatRoom, ChatMessage chatMessage) {
    }

    @Override
    public void onParticipantAdminStatusChanged(ChatRoom chatRoom, EventLog eventLog) {
    }

    @Override
    public void onMessageReceived(ChatRoom chatRoom, ChatMessage chatMessage) {
    }

    @Override
    public void onEphemeralMessageDeleted(ChatRoom chatRoom, EventLog eventLog) {

    }

    @Override
    public void onParticipantDeviceRemoved(ChatRoom chatRoom, EventLog eventLog) {
    }

    @Override
    public void onParticipantRemoved(ChatRoom chatRoom, EventLog eventLog) {
    }

    @Override
    public void onEphemeralMessageTimerStarted(ChatRoom chatRoom, EventLog eventLog) {

    }

    @Override
    public void onParticipantRegistrationUnsubscriptionRequested(ChatRoom chatRoom, Address address) {
    }

    @Override
    public void onConferenceJoined(ChatRoom chatRoom, EventLog eventLog) {
    }

    @Override
    public void onSecurityEvent(ChatRoom chatRoom, EventLog eventLog) {
    }

    @Override
    public void onParticipantRegistrationSubscriptionRequested(ChatRoom chatRoom, Address address) {
    }

    @Override
    public void onParticipantDeviceAdded(ChatRoom chatRoom, EventLog eventLog) {
    }
}
