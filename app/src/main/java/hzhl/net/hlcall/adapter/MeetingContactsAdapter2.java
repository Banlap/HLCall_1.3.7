package hzhl.net.hlcall.adapter;

import android.app.Activity;
import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Conference;
import org.linphone.core.ConferenceParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListener;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Reason;

import java.util.ArrayList;
import java.util.List;

import hzhl.net.hlcall.App;
import hzhl.net.hlcall.CallManager;
import hzhl.net.hlcall.LinphoneService;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.entity.ContactsEntity;
import hzhl.net.hlcall.entity.MeetingEntity;
import hzhl.net.hlcall.utils.MyLog;

/**
 * Created by guang on 2018/6/25.
 */

public class MeetingContactsAdapter2 extends RecyclerView.Adapter<MeetingContactsAdapter2.ViewHolder> {

    public static final int DEL_MODE = 1;
    public static final int MUTE_MODE = 2;

    private Context context;
    private List<ContactsEntity> list = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private OnRecyclerViewItemClickListener itemClickListener;
    private ArrayMap<String, Call> callMap = new ArrayMap<>();
    private ArrayMap<String, String> stateMap = new ArrayMap<>();
    private ArrayMap<String, Address> addressMap = new ArrayMap<>();
    private Core core;
    private Conference conference;
    private MeetingEntity meetingEntity;
    private int mode=0;
    private long lastCall = 0;
    private CoreListener coreListener = new CoreListenerStub(){

        @Override
        public void onCallStateChanged(Core lc, Call call, Call.State state, String message) {
            upDateState(call);
            if (context!=null){
                Activity activity = (Activity)context;
                if (!activity.isDestroyed()) activity.runOnUiThread(() -> notifyDataSetChanged());
                else removeCoreListener();
            }
            MyLog.e("wen "+core.getCallsNb());
        }
    };

    public abstract static class OnRecyclerViewItemClickListener {
        public void onItemClick(ContactsEntity entity){}
        public void onCallClick(ContactsEntity entity){}
        public void onAddClick(ContactsEntity entity){}
        public void onEnd(){}

    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public MeetingContactsAdapter2(Context context, MeetingEntity entity) {
        this.context = context;
        if (entity.getContacts()==null)entity.setContacts(list);
        this.list.addAll(entity.getContacts());
        this.layoutInflater = LayoutInflater.from(context);
        this.core = LinphoneService.getCore();
        this.meetingEntity = entity;
        if (core == null)return;
        //core.leaveConference();
        core.terminateConference();
        ConferenceParams conferenceParams = core.createConferenceParams();
        conference = core.createConferenceWithParams(conferenceParams);
        core.addListener(coreListener);

    }

    public MeetingContactsAdapter2(List<ContactsEntity> list) {
        this.list.addAll(list);
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meeting_contacts, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder holder, final int position) {
        ContactsEntity entity;
        if (position == 0){
            entity = new ContactsEntity("主持人","8009");
            holder.iv_head.setImageResource(R.drawable.conf_audio_call_user);
            holder.tv_name.setText(entity.getName());
            holder.tv_status.clearComposingText();
            holder.iv_operate.setVisibility(View.GONE);
            return;
        }else if (position - 1 < list.size()){
            entity = list.get(position - 1);
        }else {
            entity = new ContactsEntity("点击邀请",null);
            holder.iv_head.setImageResource(R.drawable.conf_add_user);
            holder.tv_name.setText(entity.getName());
            holder.tv_status.setText("");
            holder.iv_operate.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onAddClick(entity);
                }
            });
            return;
        }
        String state = stateMap.get(entity.getNumber());
        if (state==null){
            state = "";
            stateMap.put(entity.getNumber(),state);
        }
        holder.tv_status.setText(state);

        holder.iv_head.setImageResource(R.drawable.conf_audio_call_user);
        holder.tv_name.setText(entity.getName());
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(entity);
            }
        });
        Call call = callMap.get(entity.getNumber());

        if (mode == DEL_MODE){
            holder.iv_operate.setVisibility(View.VISIBLE);
            holder.iv_operate.setBackgroundResource(R.drawable.conf_trash_unpressed);
            holder.iv_operate.setOnClickListener(v -> {
                v.setVisibility(View.GONE);
                holder.iv_operate.setBackgroundResource(R.drawable.conf_trash_unpressed);
                list.remove(entity);
                MeetingEntity meeting = App.getDaoInstant().getMeetingEntityDao().load(meetingEntity.getId());
                meeting.setContacts(list);
                App.getDaoInstant().getMeetingEntityDao().update(meeting);
                //if (conference!=null)conference.removeParticipant(addressMap.get(entity.getName()));
                if (call!=null&&call.getConference()!=null)call.getConference().removeParticipant(call.getRemoteAddress());
                removeCall(entity);
                notifyDataSetChanged();
            });
        }else if (mode == MUTE_MODE){
            holder.iv_operate.setVisibility(View.VISIBLE);
            if (call != null) {
                boolean b = call.getSpeakerMuted();
                if (b)holder.iv_operate.setBackgroundResource(R.drawable.conf_btn_mute_pressed);
                else holder.iv_operate.setBackgroundResource(R.drawable.conf_btn_not_mute_pressed);
            }
            holder.iv_operate.setOnClickListener(v -> {
                if (call != null) {
                    boolean b = call.getSpeakerMuted();
                    call.setSpeakerMuted(!b);
                }
                notifyDataSetChanged();
            });
        }else {
            if (addressMap.get(entity.getNumber()) == null)setRedail(holder.iv_operate,entity);
            else {
                holder.iv_operate.setVisibility(View.GONE);
            }
        }
        //upDateState(call,holder.tv_status);


    }

    public void setData(List<ContactsEntity> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void upDateCall(boolean isCall,MeetingEntity meetingEntity) {
        for1:
        for (ContactsEntity e:meetingEntity.getContacts()
             ) {
            for (ContactsEntity e2:list) {
                if (e2.getNumber().equals(e.getNumber()))continue for1;
            }
            if (isCall)addToCall(e);
            list.add(e);
        }
        if (isCall)call();
        notifyDataSetChanged();
    }


    public void addToCall(ContactsEntity e){
        if (core == null)return;
        if (conference == null)return;
        if (e.getNumber().isEmpty())return;
        Address address = getAddress(e.getNumber());
        if (address == null)return;
        addressMap.put(e.getNumber(),address);
    }

    public void removeCall(ContactsEntity e){
        stateMap.remove(e.getNumber());
        if (core == null)return;
        for (Call call:core.getCalls()
             ) {
            if (call.getRemoteAddress().getUsername().equals(e.getNumber())){
                core.removeFromConference(call);
                call.terminate();
                return;
            }
        }

    }

    public void  initCall(){
        for (ContactsEntity e:list) {
            addToCall(e);
        }
        //addToCall(new ContactsEntity("主持人","8009"));
        call();
    }

    public void setMode(int mode) {
        this.mode = mode;
        notifyDataSetChanged();
    }

    public int getMode() {
        return mode;
    }

    private void call(){

        MyLog.e("wen "+core.getCallsNb());

        CallParams params = core.createCallParams(null);
        CallManager.getInstance().mBandwidthManager.updateWithProfileSettings(params);
        params.enableVideo(false);


        core.enterConference();
        conference = core.getConference();
        if (conference == null){
            ConferenceParams conferenceParams = core.createConferenceParams();
            conferenceParams.enableVideo(false);
            conference = core.createConferenceWithParams(conferenceParams);
        }
        if (conference != null) {
            conference.inviteParticipants(getAddress(), params);
        }else {
            Toast.makeText(context, "会议未准备好", Toast.LENGTH_SHORT).show();
            return;
        }
        /*for (Call c:core.getCalls()) {
            callMap.put(c.getRemoteAddress().getUsername(),c);
        }
        core.addAllToConference();
        //core.enterConference();
        MyLog.e("wen getConferenceSize"+core.getConferenceSize());*/
    }

    private Address[] getAddress(){
        return addressMap.values().toArray(new Address[]{});
    }

    private void setRedail(ImageView v,ContactsEntity entity){



        v.setVisibility(View.VISIBLE);
        v.setBackgroundResource(R.drawable.conf_redail_unpressed);
        v.setOnClickListener(v1 -> {

            long callTime = System.currentTimeMillis();
            if (callTime - lastCall < 2000 && lastCall!=0){
                Toast.makeText(context, "操作过于频繁", Toast.LENGTH_SHORT).show();
                return;
            }
            lastCall = callTime;

            if (core!=null && core.getCallsNb() ==0 && itemClickListener!=null){
                itemClickListener.onCallClick(entity);
            }
            v1.setVisibility(View.GONE);
            addToCall(entity);
            call();
        });
    }

    @Override
    public int getItemCount() {
        return 6;
    }

    private Address getAddress(String number){
        if (core == null)return null;
        return core.getDefaultProxyConfig().normalizeSipUri(number);
    }


    private void upDateState(Call call){
        if (call!=null) {

            Call.State state = call.getState();
            Reason reason = call.getErrorInfo().getReason();
            String number = call.getRemoteAddress().getUsername();
            if (core!=null) {
                callMap.put(number, call);
                core.addAllToConference();
                core.enterConference();
                MyLog.e("wen getConferenceSize"+core.getConferenceSize());
            }
            if (state == Call.State.End || state == Call.State.Error) {
                // Convert Core message for internalization
                MyLog.d("wen",call.getErrorInfo().getReason().name()+"");
                MyLog.d("wen",call.getCallLog().getStatus().name()+"");
                if (reason == Reason.Declined) {
                    stateMap.put(number,"通话被拒绝");
                } else if (reason == Reason.NotFound) {
                    stateMap.put(number,"找不到用户");
                    // showToast(getString(R.string.error_user_not_found));
                } else if (reason == Reason.NotAcceptable) {
                    stateMap.put(number,"不兼容媒体参数");
                } else if (reason == Reason.Busy) {
                    stateMap.put(number,"用户繁忙");
                } else {
                    stateMap.put(number,"已挂机");
                }

                addressMap.remove(number);
                MyLog.d("Call State" + "通话断开");

            } else if (state == Call.State.Connected) {
                stateMap.put(number,"通话中");

                if (core!=null) {
                    callMap.put(number, call);
                    core.addAllToConference();
                    MyLog.e("wen getConferenceSize"+core.getConferenceSize());
                }

                MyLog.d("Call State" + "通话连接");

            } else if (Call.State.OutgoingInit == state
                    || Call.State.OutgoingProgress == state
                    || Call.State.OutgoingRinging == state
                    || Call.State.OutgoingEarlyMedia == state) {
                stateMap.put(number,"拨号中");

                MyLog.d("Call State" + "正在拨号");

            }
        }
    }

    public void removeCoreListener(){
        if (core!=null)core.removeListener(coreListener);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_name;
        private TextView tv_status;
        private ImageView iv_head;
        private ImageView iv_operate;
        private TextView tv_contacts;

        public ViewHolder(View itemView) {
            super(itemView);
            this.tv_name = itemView.findViewById(R.id.tv_name);
            this.tv_status = itemView.findViewById(R.id.tv_status);
            this.iv_head = itemView.findViewById(R.id.iv_head);
            this.iv_operate = itemView.findViewById(R.id.iv_operate);
            this.tv_contacts = itemView.findViewById(R.id.tv_contacts);
        }
    }
}
