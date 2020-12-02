package hzhl.net.hlcall.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import hzhl.net.hlcall.R;
import hzhl.net.hlcall.entity.NumberEntity;

/**
 * Created by guang on 2018/6/25.
 */

public class NumberListAdapter extends RecyclerView.Adapter<NumberListAdapter.ViewHolder> {
    private Context context;
    private List<NumberEntity> list;
    private LayoutInflater layoutInflater;
    private OnRecyclerViewItemClickListener itemClickListener;

    public interface OnRecyclerViewItemClickListener {
        void OnItemClick(int position);
        void OnItemCall(int position);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public NumberListAdapter(Context context, List<NumberEntity> list) {
        this.context = context;
        this.list = list;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_number_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.tvNumber.setText(list.get(position).getNumber());
        holder.ivCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.OnItemCall(holder.getAdapterPosition());
                }
            }
        });
        holder.llItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    //itemClickListener.OnItemClick(position);
                    itemClickListener.OnItemCall(holder.getAdapterPosition());
                }
            }
        });
    }

    public void setData(List<NumberEntity> list) {
        this.list = list;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout llItem;
        private TextView tvNumber;
        private ImageView ivCall;

        public ViewHolder(View itemView) {
            super(itemView);
            this.llItem = itemView.findViewById(R.id.ll_item);
            this.tvNumber = itemView.findViewById(R.id.tv_number);
            this.ivCall = itemView.findViewById(R.id.iv_call);
        }
    }
}
