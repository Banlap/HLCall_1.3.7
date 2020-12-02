package hzhl.net.hlcall.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hzhl.net.hlcall.R;
import hzhl.net.hlcall.entity.ContactsListEntity;
import hzhl.net.hlcall.utils.BitmapToByteUtil;

/**
 * Created by user on 2017/4/5.
 * 通讯录列表适配器
 * dana
 */

public class AddressListAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<ContactsListEntity> list;
    private boolean isSearch; // 是否搜索出来的结果
    private int positionTag;
    private boolean isClick = true;

    public AddressListAdapter(Context context, ArrayList<ContactsListEntity> list) {
        this.list = list;
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
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

    class ViewHolder {
        private LinearLayout llLetter;
        private TextView tvLetter, tvName, tvNumber;
        private CircleImageView circleImageView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.item_address_list, parent, false);
            viewHolder.llLetter = convertView.findViewById(R.id.ll_letter);
            viewHolder.tvLetter = (TextView) convertView.findViewById(R.id.item_address_list_tv_catalog);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_name_address);
            viewHolder.tvNumber = (TextView) convertView.findViewById(R.id.tv_number_address);
            viewHolder.circleImageView = convertView.findViewById(R.id.circle_head);
            viewHolder.circleImageView.setTag(R.id.circle_head, position);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvName.setText(list.get(position).getName());
        int id = (int) viewHolder.circleImageView.getTag(R.id.circle_head);
        if (id == position) {
        }
        if (list.get(position).getBytes() != null) {
            Bitmap photoBitmap = BitmapToByteUtil.Bytes2Bimap(list.get(position).getBytes());
            //Glide.with(context).load(photoBitmap).into(viewHolder.circleImageView);
        }

        //  viewHolder.tvNumber.setText(contactsListEntity.getNumber());

        // 根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);
        // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (position == getPositionForSection(section)) {
            if (!isSearch) {
                viewHolder.llLetter.setVisibility(View.VISIBLE);
                viewHolder.tvLetter.setText(list.get(position).getSortLetters());
            } else {
                // 搜索出来的结果，不展示拼音提示行
                viewHolder.llLetter.setVisibility(View.GONE);
            }
        } else {
            viewHolder.llLetter.setVisibility(View.GONE);
        }


        return convertView;
    }

    public void setData(ArrayList<ContactsListEntity> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    /**
     * 根据分类的首字母的Char ascii值,获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = list.get(i).getSortLetters();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    public void setSearch(boolean isSearch) {
        this.isSearch = isSearch;
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    public int getSectionForPosition(int position) {
        return list.get(position).getSortLetters().charAt(0);
    }

    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     *
     * @param list
     */
    public void updateListView(ArrayList<ContactsListEntity> list) {
        this.list = list;
        notifyDataSetChanged();
    }


}
