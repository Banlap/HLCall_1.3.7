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
import hzhl.net.hlcall.entity.RingToneEntity;

/**
 * Created by guang on 2018/6/25.
 */

public class RingtoneListAdapter extends RecyclerView.Adapter<RingtoneListAdapter.ViewHolder> {
    private Context context;
    private List<RingToneEntity> list;
    private LayoutInflater layoutInflater;
    private OnRecyclerViewItemClickListener itemClickListener;

    public interface OnRecyclerViewItemClickListener {
        void OnItemClick(int position);

    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public RingtoneListAdapter(Context context, List<RingToneEntity> list) {
        this.context = context;
        this.list = list;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_ringtone_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        RingToneEntity entity = list.get(position);
        holder.tvTitle.setText(entity.getRingtone().getTitle(context));
        if (entity.isShow()) {
            holder.ivChoose.setVisibility(View.VISIBLE);
            holder.tvTitle.setSelected(true);
        } else {
            holder.ivChoose.setVisibility(View.INVISIBLE);
            holder.tvTitle.setSelected(false);
        }

        holder.llItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.OnItemClick(position);
                }
            }
        });
    }


    public void setData(List<RingToneEntity> list) {
        this.list = list;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout llItem;
        private TextView tvTitle;
        private ImageView ivChoose;

        public ViewHolder(View itemView) {
            super(itemView);
            this.llItem = itemView.findViewById(R.id.ll_item);
            this.tvTitle = itemView.findViewById(R.id.tv_title);
            this.ivChoose = itemView.findViewById(R.id.iv_choose);
        }
    }
}
