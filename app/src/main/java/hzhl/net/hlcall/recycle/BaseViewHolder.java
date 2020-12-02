package hzhl.net.hlcall.recycle;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by elileo on 18/5/30.
 */
public class BaseViewHolder extends RecyclerView.ViewHolder{
    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public void setVisibility(boolean isVisible){
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if(isVisible){
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }else{

            params.width = 0;
            params.height = 0;
        }
        itemView.setLayoutParams(params);
        itemView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
}
