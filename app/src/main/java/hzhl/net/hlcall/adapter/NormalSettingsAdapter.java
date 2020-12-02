package hzhl.net.hlcall.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import hzhl.net.hlcall.R;
import hzhl.net.hlcall.bean.Settings;

public class NormalSettingsAdapter extends ArrayAdapter<Settings> {

    private int newResourceId;
    private SettingItemClickListener mSettingItemClickListener = null;
    public NormalSettingsAdapter(Context context, int resourceId, List<Settings> settingsList){
        super(context, resourceId, settingsList);
        newResourceId = resourceId;
    }

    public interface SettingItemClickListener {
        void OnSettingItemClickListener(View v, String itemName, int position);
    }

    public void setItemClickListener(SettingItemClickListener settingItemClickListener){
        mSettingItemClickListener = settingItemClickListener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Settings settings = getItem(position);
        View view = LayoutInflater.from (getContext ()).inflate (newResourceId, parent, false);

        LinearLayout settingsItem = view.findViewById(R.id.ll_item_normal);
        TextView settingsName = view.findViewById (R.id.tv_item_normal_settings);
        ImageView settingImage = view.findViewById (R.id.iv_item_normal_settings);


        settingsName.setText(settings.getSettingName());
        settingImage.setImageResource (settings.getImageId());

        settingsItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getContext(),"你点击了第"+position+"项"+"你选择"+settings.getSettingName(),Toast.LENGTH_SHORT).show();
                mSettingItemClickListener.OnSettingItemClickListener(view,settings.getSettingName() ,position);
            }
        });


        return view;
    }


}
