package hzhl.net.hlcall.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;

import java.util.List;

import hzhl.net.hlcall.R;
import hzhl.net.hlcall.entity.IsChooseEntity;

/**
 * Created by guang on 2018/6/25.
 */

public class SipListAdapter extends RecyclerView.Adapter<SipListAdapter.ViewHolder> {
    private Context context;
    private List<ProxyConfig> list;
    private List<IsChooseEntity> isChooseEntityList;
    private LayoutInflater layoutInflater;
    private OnRecyclerViewItemClickListener itemClickListener;

    public interface OnRecyclerViewItemClickListener {
        void OnItemClick(int position);

        void OnItemChoose(int position);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public SipListAdapter(Context context, List<ProxyConfig> list, List<IsChooseEntity> isChooseEntityList) {
        this.context = context;
        this.list = list;
        this.isChooseEntityList = isChooseEntityList;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_sip_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        ProxyConfig entity = list.get(position);
        IsChooseEntity isChooseEntity = isChooseEntityList.get(position);
        holder.tvUser.setText(entity.getIdentityAddress().getUsername());
        switch (entity.getIdentityAddress().getTransport().toInt()) {
            case 0:
                holder.tvType.setText("UDP");
                break;
            case 1:
                holder.tvType.setText("TCP");
                break;
        }
        holder.tvState.setText(getStatus(entity.getState()));

        if (isChooseEntity.getIsChoose()) {
            holder.ivChoose.setSelected(true);
        } else {
            holder.ivChoose.setSelected(false);
        }

        holder.ivChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.OnItemChoose(position);
                }
            }
        });
        holder.llItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.OnItemClick(position);
                }
            }
        });
    }

    private String getStatus(RegistrationState state) {
        if (state == RegistrationState.Ok) {
            return "已登录";
        } else if (state == RegistrationState.Progress) {
            return "登录中";
        } else if (state == RegistrationState.Failed) {
            return "登录失败";
        } else {
            return "未登录";
        }
    }

    public void setData(List<ProxyConfig> list, List<IsChooseEntity> isChooseEntityList) {
        this.list = list;
        this.isChooseEntityList = isChooseEntityList;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout llItem;
        private TextView tvUser;
        private TextView tvType;
        private TextView tvState;
        private ImageView ivChoose;

        public ViewHolder(View itemView) {
            super(itemView);
            this.llItem = itemView.findViewById(R.id.ll_item);
            this.tvUser = itemView.findViewById(R.id.tv_user);
            this.tvType = itemView.findViewById(R.id.tv_type);
            this.tvState = itemView.findViewById(R.id.tv_state);
            this.ivChoose = itemView.findViewById(R.id.iv_choose);
        }
    }
}
