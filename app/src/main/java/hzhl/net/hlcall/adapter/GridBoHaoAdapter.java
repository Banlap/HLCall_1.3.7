package hzhl.net.hlcall.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import hzhl.net.hlcall.R;
import hzhl.net.hlcall.entity.GridBoHaoEntity;

/**
 * Created by user on 2017/1/12.
 */

public class GridBoHaoAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<GridBoHaoEntity> list;

    public GridBoHaoAdapter(Context context, List<GridBoHaoEntity> list) {
        this.inflater = LayoutInflater.from(context);
        this.list = list;
    }

    class Viewholder {
        TextView tvZiMu;
        TextView tvNumber;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Viewholder viewholder;
        if (convertView == null) {
            viewholder = new Viewholder();
            convertView = inflater.inflate(R.layout.item_grid_bohao, parent, false);
            viewholder.tvNumber = convertView.findViewById(R.id.tv_number);
            viewholder.tvZiMu = convertView.findViewById(R.id.tv_zimu);
            convertView.setTag(viewholder);
        } else {
            viewholder = (Viewholder) convertView.getTag();
        }
        viewholder.tvZiMu.setText(list.get(position).getZiMu());
        viewholder.tvNumber.setText(list.get(position).getNumber());
        return convertView;
    }

}
