package hzhl.net.hlcall.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.linphone.core.ChatMessage;
import org.linphone.core.ChatRoom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.utils.LinphoneUtils;
import hzhl.net.hlcall.utils.WK;

public class MsgConversationAdapter extends RecyclerView.Adapter<MsgConversationAdapter.ConversationHolder>{
    private LayoutInflater layoutInflater;
    private Context mContext;
    private List<ChatRoom> list = new ArrayList<>();

    private final ClickListener mListener;

    public MsgConversationAdapter(Context context, List<ChatRoom> list, ClickListener listener) {
        this.mContext = context;
        this.list.addAll(list);
        this.layoutInflater = LayoutInflater.from(context);
        this.mListener = listener;
    }


    @NonNull
    @Override
    public ConversationHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = layoutInflater.inflate(R.layout.item_conversation_msg, viewGroup, false);
        MsgConversationAdapter.ConversationHolder viewHolder = new MsgConversationAdapter.ConversationHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ConversationHolder conversationHolder, int position) {
        ChatRoom room = list.get(position);
        ChatMessage lastMsg = room.getLastMessageInHistory();
        room.setUserData(conversationHolder);
        if(lastMsg != null){
            conversationHolder.tvMsgDate.setText(LinphoneUtils.timestampToHumanDate(mContext, room.getLastUpdateTime(), R.string.msg_date_format));
            if(lastMsg.isText()){
                conversationHolder.tvMsg.setText(lastMsg.getTextContent());
            }else{
                conversationHolder.tvMsg.setText("[图片]");
            }
            String unReadNum = String.valueOf(room.getUnreadMessagesCount());
            SpannableStringBuilder unReadBuilder = new SpannableStringBuilder();
            unReadBuilder.append("（");
            unReadBuilder.append(unReadNum);
            unReadBuilder.append("/");
            unReadBuilder.append(String.valueOf(room.getHistorySize()));
            unReadBuilder.append("）");
            if(!"0".equals(unReadNum)){
                unReadBuilder.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.red_font_btn)), 1,
                        unReadNum.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            conversationHolder.tvNumber.setText(unReadBuilder);

            conversationHolder.tvNick.setText(getContact(room));
        }
        conversationHolder.rlRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    mListener.onItemClicked(room, position);
                }
            }
        });

    }

    private String getContact(ChatRoom mRoom) {
       //wenyeyang
        //return mRoom.getPeerAddress().getUsername();
        String displayName = mRoom.getSubject();

        if(TextUtils.isEmpty(displayName)){
            displayName = mRoom.getPeerAddress().getDisplayName();
        }
        if(WK.empty(displayName)){
            displayName = mRoom.getPeerAddress().getUsername();
        }
        return displayName;


    }

    public void refresh() {
        list.clear();

        ChatRoom[] rooms = LinphoneService.getCore().getChatRooms();

        List<ChatRoom> roomsList = Arrays.asList(rooms);
        list.addAll(roomsList);
        //wenyeyang
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class ConversationHolder extends RecyclerView.ViewHolder {
        private TextView tvNick;
        private TextView tvNumber;
        private TextView tvMsg;
        private TextView tvMsgDate;
        private RelativeLayout rlRootView;

        public ConversationHolder(View itemView) {
            super(itemView);
            this.tvNick = itemView.findViewById(R.id.item_msg_name_tv);
            this.tvNumber = itemView.findViewById(R.id.item_msg_num_tv);
            this.tvMsg = itemView.findViewById(R.id.item_msg_last_tv);
            tvMsgDate = itemView.findViewById(R.id.item_msg_date_tv);
            rlRootView = itemView.findViewById(R.id.item_msg_rl);
        }
    }

    public interface ClickListener {
        void onItemClicked(ChatRoom room, int position);
    }
}
