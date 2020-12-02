package hzhl.net.hlcall.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import hzhl.net.hlcall.R;

/**
 * Created by guang on 2018/6/25.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private Context context;
    private List<String> paths;
    private LayoutInflater layoutInflater;
    private OnRecyclerViewItemClickListener itemClickListener;

    public interface OnRecyclerViewItemClickListener {
        void OnItemClick(int position);

    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public ImageAdapter(Context context, List<String> list) {
        this.context = context;
        this.paths = list;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if (position == paths.size()){
            Glide.with(holder.imageView).load(R.mipmap.icon_tianjia).into(holder.imageView);
            holder.itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.OnItemClick(position);
                }
            });
            return;
        }
        String entity = paths.get(position);
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                removeData(entity);
            }
        });
        Glide.with(holder.imageView).load(entity).into(holder.imageView);


    }

    public void setData(List<String> list) {
        this.paths = list;
        notifyDataSetChanged();
    }

    public void addData(List<String> paths){
        for (String s:paths
             ) {
            if (this.paths.size() >= 19)break;
            this.paths.add(s);
        }
        //this.paths.addAll(paths);
        notifyDataSetChanged();
    }

    public void addData(String path){
        this.paths.add(path);
        notifyDataSetChanged();
    }

    public void removeData(String path){
        this.paths.remove(path);
        notifyDataSetChanged();
    }

    public List<String> getPaths() {
        return paths;
    }

    @Override
    public int getItemCount() {
        if (paths!=null && paths.size()>=19)return paths.size();
        return paths == null ? 1 : paths.size()+1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.image);
        }
    }
}
