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
import hzhl.net.hlcall.entity.TongHuaEntity;

/**
 * Created by guang on 2018/6/25.
 */

public class CallRecordOneNumberAdapter extends RecyclerView.Adapter<CallRecordOneNumberAdapter.ViewHolder> {
    private Context context;
    private List<TongHuaEntity> list;
    private LayoutInflater layoutInflater;
    private OnRecyclerViewItemClickListener itemClickListener;

    public interface OnRecyclerViewItemClickListener {
        void OnItemClick(int position);

    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public CallRecordOneNumberAdapter(Context context, List<TongHuaEntity> list) {
        this.context = context;
        this.list = list;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_callrecord_one_number, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        /**
         * 通话类型
         */
        final TongHuaEntity entity = list.get(position);
        // 通话类型
        if ("打入".equals(entity.getType())) { //"打入"
            holder.ivType.setImageResource(R.drawable.icon_yijiedianh);
            holder.tvNumber.setTextColor(context.getResources().getColor(R.color.gray));
            holder.tvState.setText("打入");
        } else if ("打出".equals(entity.getType())) {  //"打出"
            holder.ivType.setImageResource(R.drawable.icon_weijietong);
            holder.tvNumber.setTextColor(context.getResources().getColor(R.color.gray));
            holder.tvState.setText("打出");
        } else if ("未接".equals(entity.getType())) { //"未接来电"
            holder.ivType.setImageResource(R.drawable.icon_weijiedianhua);
            holder.tvNumber.setTextColor(context.getResources().getColor(R.color.red_font_btn));
            holder.tvState.setText("未接");
        }else {
            holder.tvState.setText(entity.getType());
        }
        /**
         * 通话记录的联系人
         */
        if ("未备注联系人".equals(entity.getName())) {// 通话记录的联系人
            holder.tvNumber.setText(entity.getNumber());// 通话记录的联系人
        } else {
            holder.tvNumber.setText(entity.getName() + "(" + entity.getNumber() + ")");// 通话记录的联系人
        }

        holder.tvTime.setText(entity.getDate());
        holder.llItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.OnItemClick(position);
                }
            }
        });
    }

    public void setData(List<TongHuaEntity> list) {
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
        private ImageView ivType;
        private TextView tvTime;
        private TextView tvState;

        public ViewHolder(View itemView) {
            super(itemView);
            this.llItem = itemView.findViewById(R.id.ll_item);
            this.tvNumber = itemView.findViewById(R.id.tv_number);
            this.ivType = itemView.findViewById(R.id.iv_type);
            this.tvTime = itemView.findViewById(R.id.tv_time);
            this.tvState = itemView.findViewById(R.id.tv_state);
        }
    }
}
