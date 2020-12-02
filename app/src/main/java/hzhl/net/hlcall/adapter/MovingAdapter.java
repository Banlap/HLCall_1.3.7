package hzhl.net.hlcall.adapter;

import android.content.Context;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import hzhl.net.hlcall.R;
import hzhl.net.hlcall.api.MsgList;

/**
 * Created by guang on 2018/6/25.
 */

public class MovingAdapter extends RecyclerView.Adapter<MovingAdapter.ViewHolder> {
    private static final int FOOT = 0;
    private static final int CONTENT = 1;
    private Context context;
    private List<MsgList.ItemsBean> list;
    private int onLoad = 5;
    private LayoutInflater layoutInflater;
    private OnRecyclerViewItemClickListener itemClickListener;



    public interface OnRecyclerViewItemClickListener {
        void OnItemClick(int position);

    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public MovingAdapter(Context context, List<MsgList.ItemsBean> list) {
        this.context = context;
        this.list = list;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == FOOT) view = layoutInflater.inflate(R.layout.item_foot, parent, false);
        else view = layoutInflater.inflate(R.layout.item_moving, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder holder, final int position) {
        if (holder.getAdapterPosition() >= getItemCount()-1){
            holder.itemView.setOnClickListener(v -> {
                onLoad += 5;
                if (onLoad > list.size())onLoad=list.size();
                notifyDataSetChanged();
            });
            return;
        }
        MsgList.ItemsBean entity = list.get(position);
        holder.tv_time.setText(entity.getCreate_at());
        if (entity.getTitle()!=null)
        holder.tv_address.setText(entity.getTitle().replace("null",""));
        holder.tv_content.setText(entity.getMsg());
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.OnItemClick(holder.getAdapterPosition());
            }
        });

        holder.rec_image.setLayoutManager(new GridLayoutManager(context,3));
        holder.rec_image.setAdapter(new MovingImageAdapter(context,entity.getPics()));

    }

    public void setData(List<MsgList.ItemsBean> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (list == null || list.size()==0)return FOOT;
        return position==getItemCount()-1 ? FOOT:CONTENT;
    }

    @Override
    public int getItemCount() {
        if (list == null)return 0;
        if (list.size() == 0)return 0;
        if (list.size()>onLoad)return onLoad+1;
        return  list.size()+1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_time;
        private TextView tv_content;
        private TextView tv_address;
        private ImageView image1;
        private ImageView image2;
        private ImageView image3;
        private RecyclerView rec_image;

        public ViewHolder(View itemView) {
            super(itemView);
            this.tv_time = itemView.findViewById(R.id.tv_time);
            this.tv_content = itemView.findViewById(R.id.tv_content);
            this.tv_address = itemView.findViewById(R.id.tv_address);
            this.rec_image = itemView.findViewById(R.id.rec_image);
            /*this.image1 = itemView.findViewById(R.id.image1);
            this.image2 = itemView.findViewById(R.id.image2);
            this.image3 = itemView.findViewById(R.id.image3);*/
        }
    }
}
