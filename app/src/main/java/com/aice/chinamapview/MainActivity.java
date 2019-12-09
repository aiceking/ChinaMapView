package com.aice.chinamapview;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

import com.aice.chinamapview.adapter.ProvinceAdapter;
import com.aice.chinamapview.model.MycolorArea;
import com.aice.chinamapview.util.ColorChangeUtil;
import com.aice.chinamapview.view.ColorView;
import com.aice.chinamapview.view.MyListView;
import com.wxy.chinamapview.model.ChinaMapModel;
import com.wxy.chinamapview.model.ProvinceModel;
import com.wxy.chinamapview.util.ChinaMapSvgUtil;
import com.wxy.chinamapview.view.ChinaMapView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.chinamap_view)
    ChinaMapView chinamapView;
    @BindView(R.id.color_view)
    ColorView colorView;
    @BindView(R.id.btn_change)
    Button btnChange;
    @BindView(R.id.lv_province)
    MyListView lvProvince;
    private ChinaMapModel chinaMapModel;
    private HashMap<String, List<MycolorArea>> colorView_hashmap;
    private int currentColor = 0;
    private List<String> list;
    private ProvinceAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //初始化map
        initMap();
        //设置颜色渐变条
        setColorView();
        setListAdapter();
        btnChange.setText(ColorChangeUtil.nameStrings[0]);
        ColorChangeUtil.changeMapColors(chinaMapModel, ColorChangeUtil.nameStrings[currentColor]);
        chinamapView.notifyDataSetChanged();
    }
    private void setListAdapter() {
        list=new ArrayList<>();
        //最后三个是香港，澳门和台湾，不需要
        for (int i = 0; i< ColorChangeUtil.province_datas.length; i++){
            list.add(ColorChangeUtil.province_datas[i]);
        }
        adapter = new ProvinceAdapter(this, list);
        lvProvince.setAdapter(adapter);
    }
    /**
     * 设置颜色渐变条
     */
    private void setColorView() {
        colorView_hashmap = new HashMap<>();
        for (int i = 0; i < ColorChangeUtil.nameStrings.length; i++) {
            String colors[] = ColorChangeUtil.colorStrings[i].split(",");
            String texts[] = ColorChangeUtil.textStrings[i].split(",");
            List<MycolorArea> list = new ArrayList<>();
            for (int j = 0; j < colors.length; j++) {
                MycolorArea c = new MycolorArea();
                c.setColor(Color.parseColor(colors[j]));
                c.setText(texts[j]);
                list.add(c);
            }
            colorView_hashmap.put(ColorChangeUtil.nameStrings[i], list);
        }
        colorView.setList(colorView_hashmap.get(ColorChangeUtil.nameStrings[0]));
    }

    private void initMap() {
        //拿到SVG文件，解析成对象
        chinaMapModel = new ChinaMapSvgUtil(this).getProvinces();
        //传数据
        chinamapView.setScaleMax(3);
        chinamapView.setScaleMin(1);
        chinamapView.setChinaMapModel(chinaMapModel);
        chinamapView.setOnProvinceClickLisener(new ChinaMapView.onProvinceClickLisener() {
            @Override
            public void onSelectProvince(String provinceName) {
                                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).contains(provinceName)) {
                        String s=list.get(i);
                        list.remove(i);
                        list.add(0,s);
                        adapter.setPosition(0);
                        break;
                    }
                }
            }
        });
    }

    @OnClick(R.id.btn_change)
    public void onViewClicked() {
        String namestring = ColorChangeUtil.nameStrings[++currentColor % ColorChangeUtil.nameStrings.length];
        btnChange.setText(namestring);
        colorView.setList(colorView_hashmap.get(namestring));
        //重置map各省份颜色
        ColorChangeUtil.changeMapColors(chinaMapModel, namestring);
        chinamapView.notifyDataSetChanged();
    }
}
