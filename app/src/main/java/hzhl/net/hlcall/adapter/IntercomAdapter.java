package hzhl.net.hlcall.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import hzhl.net.hlcall.R;
import hzhl.net.hlcall.entity.ContactsEntity;
import hzhl.net.hlcall.entity.IntercomEntity;

/**
 * Created by guang on 2018/6/25.
 */

public class IntercomAdapter extends RecyclerView.Adapter<IntercomAdapter.ViewHolder> {
    private Context context;
    private List<IntercomEntity> list = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private OnRecyclerViewItemClickListener itemClickListener;

    public interface OnRecyclerViewItemClickListener {
        void OnItemClick(IntercomEntity entity);

    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public IntercomAdapter(Context context, List<IntercomEntity> list) {
        this.context = context;
        Collections.reverse(list);
        this.list.addAll(list);
        this.layoutInflater = LayoutInflater.from(context);
    }


    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_intercom, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        IntercomEntity entity = list.get(position);
        DateFormat format = new SimpleDateFormat("YYYY年MM月dd日 HH:mm:ss",Locale.CHINA);
        //Calendar calendar = Calendar.getInstance(Locale.CHINA);
        //calendar.setTime(entity.getCreateTime());
        //holder.tv_name.setText(entity.getName());
        //holder.tv_day.setText(String.valueOf(calendar.get(Calendar.DATE)));
        //String month = (calendar.get(Calendar.MONTH)+1)+"月";
        //holder.tv_month.setText(month);
        holder.tv_time.setText(format.format(entity.getCreateTime()));
        StringBuilder stringBuilder = new StringBuilder();
        if (entity.getContacts() != null && entity.getContacts().size()>0) {
            for (ContactsEntity e : entity.getContacts()
            ) {
                stringBuilder.append(e.getName())
                        .append(",");
            }
            stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
        }
        holder.tv_contacts.setText(stringBuilder.toString());

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.OnItemClick(entity);
            }
        });

    }

    public void setData(List<IntercomEntity> list) {
        this.list = list;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_name;
        private TextView tv_day;
        private TextView tv_month;
        private TextView tv_time;
        private TextView tv_contacts;

        public ViewHolder(View itemView) {
            super(itemView);
            this.tv_name = itemView.findViewById(R.id.tv_name);
            this.tv_day = itemView.findViewById(R.id.tv_day);
            this.tv_month = itemView.findViewById(R.id.tv_month);
            this.tv_time = itemView.findViewById(R.id.tv_time);
            this.tv_contacts = itemView.findViewById(R.id.tv_contacts);
        }
    }
}
