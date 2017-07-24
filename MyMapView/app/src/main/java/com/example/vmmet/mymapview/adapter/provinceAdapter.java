package com.example.vmmet.mymapview.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.vmmet.mymapview.R;
import java.util.List;

/**
 * Created by Vmmet on 2016/10/10.
 */
public class provinceAdapter extends BaseAdapter{
    private Context context;
    private List<String> list;
    private int position;
    public provinceAdapter(Context context, List<String> list){
        this.context=context;
        this.list=list;
    }

    public void setPosition(int position) {
        this.position = position;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }
    @Override
    public Object getItem(int i) {
        return list.get(i);
    }
    @Override
    public long getItemId(int i) {
        return i;
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(
                    R.layout.listview_province_item, null);
            viewHolder = new ViewHolder();
            viewHolder.province_name = (TextView) view
                    .findViewById(R.id.province_name);
            viewHolder.province_elec_rise = (TextView) view
                    .findViewById(R.id.province_elec_rise);
            viewHolder.province_accumulative_rise = (TextView) view
                    .findViewById(R.id.province_accumulative_rise);
            viewHolder.province_elec_hours = (TextView) view
                    .findViewById(R.id.province_elec_hours);
            viewHolder.province_accumulative_hours = (TextView) view
                    .findViewById(R.id.province_accumulative_hours);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.province_name.setText(list.get(i).split("_")[0]);
        viewHolder.province_elec_rise.setText(list.get(i).split("_")[1]+"%");
        viewHolder.province_accumulative_rise.setText(list.get(i).split("_")[2]+"%");
        viewHolder.province_elec_hours.setText(list.get(i).split("_")[3]);
        viewHolder.province_accumulative_hours.setText(list.get(i).split("_")[4]);
        if (i==position){
            viewHolder.province_name.setTextColor(Color.RED);
        }else{
            viewHolder.province_name.setTextColor(Color.BLACK);
        }
        return view;
    }
    private class ViewHolder {
        TextView province_name,province_elec_rise,
                province_accumulative_rise,province_elec_hours,
                province_accumulative_hours;
    }
}
