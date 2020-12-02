package hzhl.net.hlcall.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import hzhl.net.hlcall.R;
import hzhl.net.hlcall.entity.BlacklistEntity;

/**
 * Created by guang on 2018/6/25.
 */

public class BlacklistAdapter extends RecyclerView.Adapter<BlacklistAdapter.ViewHolder> {
    private Context context;
    private List<BlacklistEntity> list;
    private LayoutInflater layoutInflater;
    private OnRecyclerViewItemClickListener itemClickListener;

    public interface OnRecyclerViewItemClickListener {
        void OnItemClick(int position);

    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public BlacklistAdapter(Context context, List<BlacklistEntity> list) {
        this.context = context;
        this.list = list;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_black_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        BlacklistEntity entity = list.get(position);
        holder.tvUser.setText(entity.getUser());
        holder.tvCount.setText(String.valueOf(entity.getCount()));
        holder.llItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.OnItemClick(position);
                }
            }
        });
    }

    public void setData(List<BlacklistEntity> list) {
        this.list = list;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout llItem;
        private TextView tvUser;
        private TextView tvCount;

        public ViewHolder(View itemView) {
            super(itemView);
            this.llItem = itemView.findViewById(R.id.ll_item);
            this.tvUser = itemView.findViewById(R.id.tv_user);
            this.tvCount = itemView.findViewById(R.id.tv_count);
        }
    }
}
