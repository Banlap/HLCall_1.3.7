package hzhl.net.hlcall.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.entity.LocalMedia;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import hzhl.net.hlcall.R;
import hzhl.net.hlcall.api.Api;
import hzhl.net.hlcall.api.MsgList;
import hzhl.net.hlcall.utils.FileUtils;
import hzhl.net.hlcall.utils.MyLog;
import io.itit.itf.okhttp.Response;
import io.itit.itf.okhttp.callback.Callback;
import okhttp3.Call;

/**
 * Created by guang on 2018/6/25.
 */

public class MovingImageAdapter extends RecyclerView.Adapter<MovingImageAdapter.ViewHolder> {
    private Context context;
    private List<MsgList.ItemsBean.PicsBean> paths;
    private LayoutInflater layoutInflater;
    private OnRecyclerViewItemClickListener itemClickListener;


    public interface OnRecyclerViewItemClickListener {
        void OnItemClick(int position);

    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public MovingImageAdapter(Context context, List<MsgList.ItemsBean.PicsBean> list) {
        this.context = context;
        this.paths = list;
        this.layoutInflater = LayoutInflater.from(context);
        setItemClickListener();
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        MsgList.ItemsBean.PicsBean entity = paths.get(position);
        Glide.with(holder.imageView).load(entity).into(holder.imageView);
        File file = new File(FileUtils.getImageDirectory(context,entity.getFile_name()));
        if (file.exists()){
            Glide.with(holder.imageView)
                    .load(file)
                    .into(holder.imageView);
        }else {
            new Thread(() -> {
                Api.download(entity.getUuid(), new Callback() {
                    @Override
                    public void onFailure(Call call, Exception e, int id) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response, int id) {
                        MyLog.d("Image :" + file.getName());
                        try {
                          //  if (!file.isDirectory())file.mkdirs();
                            if (file.exists()) file.createNewFile();
                            FileOutputStream out = new FileOutputStream(file);
                            out.write(response.body().bytes());
                            out.close();
                            Activity activity = (Activity) context;
                            if (activity==null || activity.isDestroyed())return;
                            //通知系统相册更新
                            /*MediaStore.Images.Media.insertImage(context.getContentResolver(),
                                    file.getPath(),file.getName() , null);*/
                            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
                            activity.runOnUiThread(() -> {
                                Glide.with(holder.imageView)
                                        .load(file)
                                        .into(holder.imageView);
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
            }).start();
        }

        holder.itemView.setOnClickListener(v -> itemClickListener.OnItemClick(position));

    }

    public void setData(List<MsgList.ItemsBean.PicsBean> list) {
        this.paths = list;
        setItemClickListener();
        notifyDataSetChanged();
    }

    public void addData(List<MsgList.ItemsBean.PicsBean> paths){
        this.paths.addAll(paths);
        setItemClickListener();
        notifyDataSetChanged();
    }

    public void addData(MsgList.ItemsBean.PicsBean path){
        this.paths.add(path);
        setItemClickListener();
        notifyDataSetChanged();
    }

    public List<MsgList.ItemsBean.PicsBean> getPaths() {
        return paths;
    }

    public void setItemClickListener() {
        this.itemClickListener = position -> {
            List<LocalMedia> localMediaList = new ArrayList<>();
            for (MsgList.ItemsBean.PicsBean p:paths) {
                LocalMedia localMedia = new LocalMedia();
                localMedia.setPath(FileUtils.getImageDirectory(context,p.getFile_name()));
                localMediaList.add(localMedia);
            }

            PictureSelector.create((Activity) context)
                    .themeStyle(R.style.picture_custom_style)
                    .openExternalPreview(position, localMediaList);
        };
    }



    @Override
    public int getItemCount() {
        return paths == null ? 0 : paths.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.image);
        }
    }
}
