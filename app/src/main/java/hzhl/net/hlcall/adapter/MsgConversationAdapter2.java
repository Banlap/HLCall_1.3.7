package hzhl.net.hlcall.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.linphone.core.ChatMessage;
import org.linphone.core.ChatRoom;
import org.linphone.core.Core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.utils.ContactsUtil;
import hzhl.net.hlcall.utils.LinphoneUtils;
import hzhl.net.hlcall.utils.WK;

public class MsgConversationAdapter2 extends RecyclerView.Adapter<MsgConversationAdapter2.ConversationHolder>{
    private static final int MESSAGES_PER_PAGE = 20;
    private LayoutInflater layoutInflater;
    private Context mContext;
    private List<ChatRoom> list = new ArrayList<>();
    private ArrayMap<String,ChatRoom> Rmap = new ArrayMap<>();
    private ArrayMap<String,ChatRoom> Smap = new ArrayMap<>();
    private List<String> names = new ArrayList<>();
    private final MsgConversationAdapter.ClickListener mListener;
    OnItemLongClickListener longClickListener;

    public MsgConversationAdapter2(Context context, MsgConversationAdapter.ClickListener listener) {
        this.mContext = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.mListener = listener;
    }


    @NonNull
    @Override
    public ConversationHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = layoutInflater.inflate(R.layout.item_conversation_msg, viewGroup, false);
        MsgConversationAdapter2.ConversationHolder viewHolder = new MsgConversationAdapter2.ConversationHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ConversationHolder conversationHolder, int position) {
        String name = names.get(position);
        ChatRoom roomS = Smap.get(name);
        ChatRoom roomR = Rmap.get(name);
        int unRead = 0;
        int all = 0;
        conversationHolder.itemView.setOnLongClickListener(v -> {
            if (longClickListener!=null)longClickListener.onItemLongClick(name,roomS,roomR,v);
            return false;
        });
        //
        List<ChatMessage> messages = new ArrayList<>();
        if (roomR!=null) {
            messages.add(roomR.getLastMessageInHistory());
            unRead += roomR.getUnreadMessagesCount();
            all += roomR.getHistoryEventsSize();
        }
        if (roomS!=null) {
            messages.add(roomS.getLastMessageInHistory());
            unRead += roomS.getUnreadMessagesCount();
            all += roomS.getHistoryEventsSize();
        }
        if (messages.size()==0)return;
        Collections.sort(messages, (o1, o2) -> Long.compare(o1.getTime(),o2.getTime()));
        ChatMessage lastMsg = messages.get(messages.size()-1);
        //
        if(lastMsg != null){
            conversationHolder.tvMsgDate.setText(LinphoneUtils.timestampToHumanDate(mContext, lastMsg.getChatRoom().getLastUpdateTime(), R.string.msg_date_format));
            if(lastMsg.isText()){
                conversationHolder.tvMsg.setText(lastMsg.getTextContent());
            }else{
                conversationHolder.tvMsg.setText("[图片]");
            }
            SpannableStringBuilder unReadBuilder = new SpannableStringBuilder();
            unReadBuilder.append("（");
            unReadBuilder.append(String.valueOf(unRead));
            unReadBuilder.append("/");
            unReadBuilder.append(String.valueOf(all));
            unReadBuilder.append("）");
            if(unRead>0){
                unReadBuilder.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.red_font_btn)), 1,
                        String.valueOf(unRead).length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            conversationHolder.tvNumber.setText(unReadBuilder);

            conversationHolder.tvNick.setText(ContactsUtil.getNameFormNumber(mContext,name));
        }
        conversationHolder.rlRootView.setOnClickListener(v -> {
            if(mListener != null){
                mListener.onItemClicked(lastMsg.getChatRoom(), position);
                //ContactsUtil.isExitNumber(mContext,names.get(position));
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

        names.clear();
        Core core = LinphoneService.getCore();
        ChatRoom[] rooms = core.getChatRooms();
        List<ChatRoom> roomsList = Arrays.asList(rooms);
        for (ChatRoom r :roomsList) {
            String name = r.getPeerAddress().getUsername();
            String localName = r.getLocalAddress().getUsername();
            //MyLog.d("localName"+localName +"ContactName" + core.getDefaultProxyConfig().getContact().getUsername());
            if (!localName.contains(core.getDefaultProxyConfig().getContact().getUsername()))continue;
            if (r.getPeerAddress().asStringUriOnly().contains(core.getDefaultProxyConfig().getDomain()))
                Smap.put(r.getPeerAddress().getUsername(),r);
            else {
                Rmap.put(r.getPeerAddress().getUsername(),r);
            }
            if (!names.contains(name))names.add(name);
        }
        //wenyeyang
        notifyDataSetChanged();
    }

    public void removeRoom(String s){
        names.remove(s);
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return names == null ? 0 : names.size();
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

    public interface OnItemLongClickListener{
        void onItemLongClick(String name,ChatRoom chatRoom1,ChatRoom chatRoom2,View v);
    }

    public void setLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }
}
