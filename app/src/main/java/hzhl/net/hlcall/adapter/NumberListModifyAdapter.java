package hzhl.net.hlcall.adapter;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import hzhl.net.hlcall.R;
import hzhl.net.hlcall.entity.NumberEntity;

/**
 * Created by guang on 2018/6/25.
 */

public class NumberListModifyAdapter extends RecyclerView.Adapter<NumberListModifyAdapter.ViewHolder> {
    private Context context;
    private ArrayList<NumberEntity> list;
    private LayoutInflater layoutInflater;
    private OnRecyclerViewUpdateListener itemClickListener;
    private int mCreateTimes = 0;
    /**
     * ViewHolder 绑定次数
     */
    private int mBindTimes = 0;
    /**
     * 已经绑定文本变化监听器
     */
    private final boolean mBoundWatcher = true;


    public interface OnRecyclerViewUpdateListener {


        void updateList(ArrayList<NumberEntity> list);
    }

    public void setOnItemClickListener(OnRecyclerViewUpdateListener listener) {
        this.itemClickListener = listener;
    }

    public NumberListModifyAdapter(Context context, ArrayList<NumberEntity> list) {
        this.context = context;
        this.list = list;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_number_list_modify, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
      //  Logger.d("BindTimes==" + mBindTimes++ + " AdapterPosition==" + holder.getAdapterPosition() + " Position==" + position);
        NumberEntity entity = list.get(holder.getAdapterPosition());
        //Logger.d("id==" + holder.editNumber.getId() + " number==" + entity.getNumber());
        holder.editNumber.setText(entity.getNumber());

        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.remove(holder.getAdapterPosition());
                notifyDataSetChanged();
                if (itemClickListener != null) {
                    itemClickListener.updateList(list);
                }
            }
        });
        // 如果已经绑定文本变化监听器不再次绑定
        if (holder.editNumber.getTag() != null && (boolean) holder.editNumber.getTag()) {
            return;
        }
        holder.editNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
             //   Logger.d("afterTextChanged---" + " AdapterPosition==" + holder.getAdapterPosition() + " Position==" + position + " s==" + s.toString());
                NumberEntity entity = list.get(holder.getAdapterPosition());
                entity.setNumber(s.toString().trim());
                list.set(holder.getAdapterPosition(), entity);
                setData1(list);
                if (itemClickListener != null) {
                    itemClickListener.updateList(list);
                }
            }
        });
        holder.editNumber.setTag(mBoundWatcher);

  /*      Log.i("onBindViewHolder", "BindTimes == " + mBindTimes++ + " Position == " + holder.getAdapterPosition());

        Log.i("TEXT_SHOW", "ShowPosition == " + holder.getAdapterPosition());
        ((SimpleHolder) holder).mEtSimple.setText(mTextCache.get(holder.getAdapterPosition()), "");

        // 如果已经绑定文本变化监听器不再次绑定
        if (((SimpleHolder) holder).mEtSimple.getTag() != null && (boolean) ((SimpleHolder) holder).mEtSimple.getTag()) {
            return;
        }
        ((SimpleHolder) holder).mEtSimple.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // 如果填入数据与缓存数据相同返回
                if (TextUtils.equals(mTextCache.get(holder.getAdapterPosition()), s.toString())) {
                    return;
                }
                mTextCache.put(holder.getAdapterPosition(), s.toString());
                Log.i("TEXT_PUT", "PutPosition == " + holder.getAdapterPosition());
            }
        });
        ((SimpleHolder) holder).mEtSimple.setTag(mBoundWatcher);
*/
    }

    public void setData1(ArrayList<NumberEntity> list) {
        this.list = list;
    }


    public void setData(ArrayList<NumberEntity> list) {
        this.list = list;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout rlItem;
        private EditText editNumber;
        private ImageView ivDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            this.rlItem = itemView.findViewById(R.id.rl_item);
            this.editNumber = itemView.findViewById(R.id.edit_number);
            this.ivDelete = itemView.findViewById(R.id.iv_delete);
        }
    }
}
