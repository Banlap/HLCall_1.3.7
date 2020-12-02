package hzhl.net.hlcall.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hzhl.net.hlcall.R;
import hzhl.net.hlcall.entity.ContactsEntity;

/**
 * Created by guang on 2018/6/25.
 */

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
    private Context context;
    private List<ContactsEntity> list = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private OnRecyclerViewItemClickListener itemClickListener;

    public interface OnRecyclerViewItemClickListener {
        void OnItemClick(ContactsEntity entity);

    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public ContactsAdapter(Context context, List<ContactsEntity> list) {
        this.context = context;
        this.list.addAll(list);
        this.layoutInflater = LayoutInflater.from(context);
    }

    public ContactsAdapter(List<ContactsEntity> list) {
        this.list.addAll(list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contacts, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        ContactsEntity entity = list.get(position);
        holder.tv_name.setText(entity.getName());
        holder.tv_number.setText(String.valueOf(entity.getNumber()));
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.OnItemClick(entity);
            }
        });
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

        public ViewHolder(View itemView) {
            super(itemView);
            this.tv_name = itemView.findViewById(R.id.tv_name);
            this.tv_number = itemView.findViewById(R.id.tv_number);
        }
    }
}
