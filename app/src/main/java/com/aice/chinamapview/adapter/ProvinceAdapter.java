package com.aice.chinamapview.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aice.chinamapview.R;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import androidx.annotation.Nullable;

/**
 * Created by Vmmet on 2016/10/10.
 */
public class ProvinceAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public ProvinceAdapter(int layoutResId, @Nullable List<String> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        ((TextView)helper.getView(R.id.province_name)).setText(item.split("_")[0]);
        ((TextView)helper.getView(R.id.province_elec_rise)).setText(item.split("_")[1]+"%");
        ((TextView)helper.getView(R.id.province_accumulative_rise)).setText(item.split("_")[2]+"%");
        ((TextView)helper.getView(R.id.province_elec_hours)).setText(item.split("_")[3]);
        ((TextView)helper.getView(R.id.province_accumulative_hours)).setText(item.split("_")[4]);
        if (helper.getAdapterPosition()==0){
            ((TextView)helper.getView(R.id.province_name)).setTextColor(Color.RED);
        }else{
            ((TextView)helper.getView(R.id.province_name)).setTextColor(Color.BLACK);
        }

    }
}
