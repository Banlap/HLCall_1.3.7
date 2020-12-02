package hzhl.net.hlcall.adapter;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hzhl.net.hlcall.R;
import hzhl.net.hlcall.entity.ContactsEntity;

/**
 * Created by guang on 2018/6/25.
 */

public class IntercomContactsAdapter extends RecyclerView.Adapter<IntercomContactsAdapter.ViewHolder> {
    private Context context;
    private List<ContactsEntity> list = new ArrayList<>();

    private OnRecyclerViewItemClickListener itemClickListener;

    public interface OnRecyclerViewItemClickListener {
        void OnItemClick(ContactsEntity entity);

    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.itemClickListener = listener;
    }


    public IntercomContactsAdapter(List<ContactsEntity> list) {
        this.list.addAll(list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contacts_sl, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        ContactsEntity entity = list.get(position);
        holder.tv_name.setText(entity.getName());
        holder.tv_number.setText(entity.getNumber());
    }

    public void setData(List<ContactsEntity> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_name;
        private TextView tv_number;
        private CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            this.tv_name = itemView.findViewById(R.id.tv_name);
            this.tv_number = itemView.findViewById(R.id.tv_number);
            this.checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
