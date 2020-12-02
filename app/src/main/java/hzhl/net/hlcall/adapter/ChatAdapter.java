package hzhl.net.hlcall.adapter;

import android.app.Activity;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.entity.LocalMedia;

import org.linphone.core.ChatMessage;
import org.linphone.core.ChatMessageListenerStub;
import org.linphone.core.Content;
import org.linphone.core.EventLog;
import org.linphone.core.tools.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hzhl.net.hlcall.R;
import hzhl.net.hlcall.utils.FileUtils;
import hzhl.net.hlcall.utils.LinphoneUtils;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatHolder>{
    private LayoutInflater layoutInflater;
    private Activity mContext;
    private List<EventLog> mHistory = new ArrayList<>();
    private ChatMessageListenerStub mListener;
    private OnMessageOnClickListener messageOnClickListener;


    private static final int MAX_TIME_TO_GROUP_MESSAGES = 300;

    public ChatAdapter(Activity context, List<EventLog> history) {
        this.mContext = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.mHistory.addAll(history);

        Collections.reverse(mHistory);

        mListener = new ChatMessageListenerStub() {
                    @Override
                    public void onMsgStateChanged(ChatMessage message, ChatMessage.State state) {
                        ChatHolder holder = (ChatHolder) message.getUserData();
                        if (holder != null) {
                            int position = holder.getAdapterPosition();
                            if (position >= 0) {
                                notifyItemChanged(position);
                            } else {
                                notifyDataSetChanged();
                            }
                        } else {
                            // Just in case, better to refresh the whole view than to miss
                            // an update
                            notifyDataSetChanged();
                        }
                    }
                };
    }

    public void clear() {
        for (EventLog event : mHistory) {
            if (event.getType() == EventLog.Type.ConferenceChatMessage) {
                ChatMessage message = event.getChatMessage();
                message.removeListener(mListener);
            }
        }
        mHistory.clear();
    }

    @Override
    public ChatHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = layoutInflater.inflate(R.layout.item_chat, viewGroup, false);
        ChatAdapter.ChatHolder viewHolder = new ChatAdapter.ChatHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ChatHolder chatHolder, int position) {
        EventLog event = mHistory.get(position);

        if(event.getType() == EventLog.Type.ConferenceChatMessage){
            ChatMessage chatMessage = event.getChatMessage();
            String time = LinphoneUtils.timestampToHumanDate(
                    mContext, chatMessage.getTime(), R.string.messages_date_format);
            if(hasSameTime(chatMessage, position)){
                chatHolder.tvTime.setVisibility(View.GONE);
            }else{
                chatHolder.tvTime.setVisibility(View.VISIBLE);
            }
            chatHolder.tvTime.setText(time);

            if(chatMessage.isOutgoing()){
                chatMessage.setUserData(chatHolder);
                chatMessage.addListener(mListener);

                chatHolder.leftLayout.setVisibility(View.GONE);
                chatHolder.rightLayout.setVisibility(View.VISIBLE);

                ChatMessage.State status = chatMessage.getState();

                if (status == ChatMessage.State.InProgress
                        || status == ChatMessage.State.FileTransferInProgress) {
                    chatHolder.rightStatusIV.setVisibility(View.GONE);
                }else{
                    chatHolder.rightStatusIV.setVisibility(View.VISIBLE);
                    if(status == ChatMessage.State.NotDelivered || status == ChatMessage.State.FileTransferError){
                        chatHolder.rightStatusIV.setImageResource(R.drawable.icon_chat_send_fail);
                    }else{
                        chatHolder.rightStatusIV.setImageResource(R.drawable.icon_chat_send_success);
                    }
                }

                if(chatMessage.hasTextContent()){
                    chatHolder.rightTextTV.setVisibility(View.VISIBLE);
                    chatHolder.rightPicIV.setVisibility(View.GONE);
                    chatHolder.rightTextTV.setText(chatMessage.getTextContent());

                }else{
                    chatHolder.rightTextTV.setVisibility(View.GONE);
                    chatHolder.rightPicIV.setVisibility(View.VISIBLE);
                    Glide.with(mContext).load(chatMessage.getFileTransferInformation().getFilePath()).into(chatHolder.rightPicIV);
                }

                chatHolder.rightPicIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<LocalMedia> localMediaList = new ArrayList<>();
                        LocalMedia localMedia = new LocalMedia();
                        localMedia.setPath(chatMessage.getFileTransferInformation().getFilePath());
                        localMediaList.add(localMedia);
                        PictureSelector.create(mContext)
                                .themeStyle(R.style.picture_custom_style)
                                .openExternalPreview(0, localMediaList);
                    }
                });
            }else{
                chatHolder.leftLayout.setVisibility(View.VISIBLE);
                chatHolder.rightLayout.setVisibility(View.GONE);

                if(chatMessage.hasTextContent()){
                    chatHolder.leftTextTV.setVisibility(View.VISIBLE);
                    chatHolder.leftPicIV.setVisibility(View.GONE);
                    chatHolder.leftTextTV.setText(chatMessage.getTextContent());

                }else{
                    chatHolder.leftTextTV.setVisibility(View.GONE);
                    chatHolder.leftPicIV.setVisibility(View.VISIBLE);
                    Content fileContent = chatMessage.getContents()[0];
                    displayContent(chatMessage, fileContent, chatHolder.leftPicIV);
                }

                chatHolder.leftPicIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<LocalMedia> localMediaList = new ArrayList<>();
                        LocalMedia localMedia = new LocalMedia();
                        localMedia.setPath(chatMessage.getFileTransferInformation().getFilePath());
                        localMediaList.add(localMedia);
                        PictureSelector.create(mContext)
                                .themeStyle(R.style.picture_custom_style)
                                .openExternalPreview(0, localMediaList);
                    }
                });
            }
        }else{
            chatHolder.leftLayout.setVisibility(View.GONE);
            chatHolder.rightLayout.setVisibility(View.GONE);
        }
        //wenyeyang
        View.OnClickListener onClickListener = v -> {
            if (messageOnClickListener!=null)
                messageOnClickListener.onMessageClick(event,v);
        };
        chatHolder.itemView.setOnClickListener(onClickListener);





    }

    private void loadBitmap(String path, ImageView imageView) {
        Glide.with(mContext).load(path).into(imageView);
    }

    private void displayContent(ChatMessage message, Content fileContent, ImageView imageView) {
        String filePath = fileContent.getFilePath();
        if(!TextUtils.isEmpty(filePath) && new File(filePath).exists()){
            loadBitmap(filePath, imageView);
        }else {
            imageView.setImageResource(R.drawable.ic_default);
            String filename = fileContent.getName();
            File file = new File(FileUtils.getStorageDirectory(mContext), filename);

            int prefix = 1;
            while (file.exists()) {
                file = new File(FileUtils.getStorageDirectory(mContext), prefix + "_" + filename);
                Log.w("[Chat Message View] File with that name already exists, renamed to "
                        + prefix
                        + "_"
                        + filename);
                prefix += 1;
            }
            fileContent.setFilePath(file.getPath());
            if (!message.isFileTransferInProgress()) {
                message.downloadContent(fileContent);
            }
        }
    }

    public EventLog getItem(int i) {
        return mHistory.get(i);
    }


    private boolean hasSameTime(ChatMessage message, int position) {
        boolean hasNext = false;

        if (position >= 0 && position < mHistory.size() - 1) {
            EventLog nextEvent = getItem(position + 1);
            if (nextEvent.getType() == EventLog.Type.ConferenceChatMessage) {
                ChatMessage nextMessage = nextEvent.getChatMessage();
                if (message.getTime() - nextMessage.getTime() < MAX_TIME_TO_GROUP_MESSAGES) {
                    hasNext = true;
                }
            }
        }
        return hasNext;
    }

    public void addToHistory(EventLog log) {
        if (mHistory.size()==0)mHistory.add(log);
        else {
            mHistory.add(0, log);
        }
        if (getItemCount()>0)notifyItemInserted(0);
        if (getItemCount()>1)notifyItemChanged(1); // Update second to last item just in case for grouping purposes
    }

    //wenyeyang
    public void deleteHistory(EventLog log) {
        mHistory.remove(log);
        notifyDataSetChanged();
    }

    public void setmHistory(List<EventLog> mHistory) {
        this.mHistory = mHistory;
        notifyDataSetChanged();
    }

    public void setMessageOnClickListener(OnMessageOnClickListener messageOnClickListener) {
        this.messageOnClickListener = messageOnClickListener;
    }
    //

    public void addAllToHistory(ArrayList<EventLog> logs) {
        int currentSize = mHistory.size() - 1;

        mHistory.addAll(logs);
        Collections.sort(mHistory, (o1, o2) -> Long.compare(o1.getCreationTime(),o2.getCreationTime()));
        Collections.reverse(mHistory);
        //notifyItemRangeInserted(currentSize + 1, logs.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mHistory == null ? 0 : mHistory.size();
    }

    public class ChatHolder extends RecyclerView.ViewHolder {
        public TextView tvTime;
        public RelativeLayout leftLayout;
        public TextView leftNickTV;
        public FrameLayout leftContentLayout;
        public TextView leftTextTV;
        public ImageView leftPicIV;

        public RelativeLayout rightLayout;
        public TextView rightNickTV;
        public FrameLayout rightContentLayout;
        public TextView rightTextTV;
        public ImageView rightPicIV;
        public ImageView rightStatusIV;


        public ChatHolder(View view) {
            super(view);
            tvTime = view.findViewById(R.id.item_chat_time);

            leftLayout = view.findViewById(R.id.item_left_message_rl);
            leftNickTV = view.findViewById(R.id.item_left_nick_tv);
            leftContentLayout = view.findViewById(R.id.item_left_content_fl);
            leftTextTV = view.findViewById(R.id.item_left_text_tv);
            leftPicIV = view.findViewById(R.id.item_left_image_iv);

            rightLayout = view.findViewById(R.id.item_right_message_rl);
            rightNickTV = view.findViewById(R.id.item_right_nick_tv);
            rightContentLayout =  view.findViewById(R.id.item_right_content_fl);
            rightTextTV = view.findViewById(R.id.item_right_text_tv);
            rightPicIV = view.findViewById(R.id.item_right_image_iv);
            rightStatusIV = view.findViewById(R.id.item_right_send_status_iv);

        }
    }

    public interface OnMessageOnClickListener{
        void onMessageClick(EventLog event,View v);
    }

}
