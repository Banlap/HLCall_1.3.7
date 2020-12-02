package hzhl.net.hlcall.adapter;

import android.content.Context;
import androidx.collection.ArrayMap;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import hzhl.net.hlcall.R;
import hzhl.net.hlcall.entity.ContactsEntity;

/**
 * Created by guang on 2018/6/25.
 */

public class ContactsSlAdapter extends RecyclerView.Adapter<ContactsSlAdapter.ViewHolder> {
    private Context context;
    private List<ContactsEntity> list = new ArrayList<>();
    private List<ContactsEntity> resList = new ArrayList<>();

    private LayoutInflater layoutInflater;
    private OnRecyclerViewItemClickListener itemClickListener;
    private ArrayMap<ContactsEntity,Boolean> map = new ArrayMap<>();
    private int maxCount = 0;

    public interface OnRecyclerViewItemClickListener {
        void OnItemClick(ContactsEntity entity);

    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public ContactsSlAdapter(Context context, List<ContactsEntity> list) {
        this.context = context;
        this.list.addAll(list);
        this.resList.addAll(list);
        this.layoutInflater = LayoutInflater.from(context);
    }

    public ContactsSlAdapter(List<ContactsEntity> list) {
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
        Boolean b = map.get(entity);
        if (b == null) b = false;
        holder.tv_name.setText(entity.getName());
        holder.tv_number.setText(String.valueOf(entity.getNumber()));
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.OnItemClick(entity);
            }
            holder.checkBox.callOnClick();
        });
        holder.checkBox.setVisibility(View.VISIBLE);
        holder.checkBox.setChecked(b);
        holder.checkBox.setOnClickListener(v -> {

            Boolean b2 = map.get(entity);
            if (b2 == null) b2 = false;
            if (b2)map.remove(entity);
            else {
                if (maxCount!=0 && map.keySet().size()>= maxCount){
                    Toast.makeText(context, "最多可用添加5个成员", Toast.LENGTH_SHORT).show();
                    return;
                }
                map.put(entity, !b2);
            }
            holder.checkBox.setChecked(!b2);
        });
    }

    public void setData(List<ContactsEntity> list) {
        this.list = list;
        this.resList = list;
        notifyDataSetChanged();
    }

    public List<ContactsEntity> getSl(){
        return new ArrayList<>(map.keySet());
    }

    public void search(String keyword){
        map.clear();
        List<ContactsEntity> newList = new ArrayList<>();
        ContactsEntity entity = new ContactsEntity(keyword,keyword);
        newList.add(entity);
        for (ContactsEntity e:resList) {
            if (e.getNumber().contains(keyword))newList.add(e);
            if (e.getNumber().equals(keyword))newList.remove(entity);
        }
        this.list = newList;
        notifyDataSetChanged();
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
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
