package hzhl.net.hlcall.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hzhl.net.hlcall.R;
import hzhl.net.hlcall.entity.TongHuaEntity;

/**
 * Created by guang on 2018/6/25.
 */

public class TongHuaAdapter extends RecyclerView.Adapter<TongHuaAdapter.ViewHolder> {
    private Context context;
    private List<TongHuaEntity> list;
    private LayoutInflater layoutInflater;
    private OnRecyclerViewItemClickListener itemClickListener;
    private List<TongHuaEntity> chooseList=new ArrayList<>();

    private String searchNum = "";

    public interface OnRecyclerViewItemClickListener {
        void OnItemClick(TongHuaEntity tongHuaEntity, int position);
       void OnChoose(List<TongHuaEntity> chooseList);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public TongHuaAdapter(Context context, List<TongHuaEntity> list) {
        this.context = context;
        this.list = list;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_tonghua, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final TongHuaEntity entity = list.get(position);
        // 通话类型
        if ("打入".equals(entity.getType())) { //"打入"
            holder.ivType.setImageResource(R.drawable.icon_yijiedianh);
//            holder.tvNumber.setTextColor(context.getResources().getColor(R.color.gray));
        } else if ("打出".equals(entity.getType())) {  //"打出"
            holder.ivType.setImageResource(R.drawable.icon_weijietong);
//            holder.tvNumber.setTextColor(context.getResources().getColor(R.color.black_title_font));
        } else if ("未接".equals(entity.getType())) { //"未接来电"
            holder.ivType.setImageResource(R.drawable.icon_weijiedianhua);
//            holder.tvNumber.setTextColor(context.getResources().getColor(R.color.red_font_btn));
        }
        /**
         * 通话记录的联系人
         */
        if ("未备注联系人".equals(entity.getName())) {// 通话记录的联系人
            String phoneNumStr = entity.getNumber();
            SpannableStringBuilder builder = new SpannableStringBuilder(phoneNumStr);
            if(!TextUtils.isEmpty(searchNum) && phoneNumStr.contains(searchNum)){
                int indexOf = phoneNumStr.indexOf(searchNum);
                builder.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.red_font_btn)),indexOf,
                        indexOf + searchNum.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }else{
                holder.tvNumber.setTextColor(context.getResources().getColor(R.color.black_title_font));
            }
            holder.tvNumber.setText(builder);// 通话记录的联系人
        } else {
            String phoneNumStr = entity.getNumber();
            String displayNameStr = entity.getName();
            SpannableStringBuilder builder = new SpannableStringBuilder(displayNameStr + "(" + phoneNumStr + ")");
            if(!TextUtils.isEmpty(searchNum) && phoneNumStr.contains(searchNum)){
                int indexOf = phoneNumStr.indexOf(searchNum);
                builder.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.red_font_btn)),indexOf + displayNameStr.length() + 1,
                        indexOf + searchNum.length() + displayNameStr.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }else{
                holder.tvNumber.setTextColor(context.getResources().getColor(R.color.black_title_font));
            }
            holder.tvNumber.setText(builder);// 通话记录的联系人
        }

        holder.tvTime.setText(entity.getDate());
        holder.tvDuration.setText(entity.getDuration());
        if (entity.isSHow()) {
            holder.ivChoose.setVisibility(View.VISIBLE);
        } else {
            holder.ivChoose.setVisibility(View.GONE);
        }
        if (entity.isChoose()) {
            holder.ivChoose.setSelected(true);
        } else {
            holder.ivChoose.setSelected(false);
        }
        holder.ivChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    v.setSelected(false);
                    entity.setChoose(false);
                    chooseList.remove(entity);
                } else {
                    v.setSelected(true);
                    entity.setChoose(true);
                    chooseList.add(entity);
                }

                if (itemClickListener != null) {
                    itemClickListener.OnChoose(chooseList);
                }
            }
        });
        holder.llItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.OnItemClick(entity, position);
                }
            }
        });
    }

    public void setData(List<TongHuaEntity> list, String searchNum) {
        this.searchNum = searchNum;
        this.list = list;
        notifyDataSetChanged();
    }


    public void showChoose(boolean isShow) {
        for (TongHuaEntity entity : list) {
            entity.setSHow(isShow);
        }
        notifyDataSetChanged();
    }

    public void setAllChoose(boolean isChoose) {
        for (TongHuaEntity entity : list) {
            entity.setChoose(isChoose);
        }
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
        private TextView tvDuration;
        private TextView tvTime;
        private ImageView ivChoose;

        public ViewHolder(View itemView) {
            super(itemView);
            this.llItem = itemView.findViewById(R.id.ll_item);
            this.tvNumber = itemView.findViewById(R.id.tv_number);
            this.ivType = itemView.findViewById(R.id.iv_type);
            this.tvDuration = itemView.findViewById(R.id.tv_duration);
            this.tvTime = itemView.findViewById(R.id.tv_time);
            this.ivChoose = itemView.findViewById(R.id.iv_choose);
        }
    }
}
