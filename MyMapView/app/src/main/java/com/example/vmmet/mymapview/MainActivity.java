package com.example.vmmet.mymapview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.example.vmmet.mymapview.adapter.provinceAdapter;
import com.example.vmmet.mymapview.bean.MyMap;
import com.example.vmmet.mymapview.bean.MycolorArea;
import com.example.vmmet.mymapview.util.ColorChangeHelp;
import com.example.vmmet.mymapview.util.SvgUtil;
import com.example.vmmet.mymapview.view.ColorView;
import com.example.vmmet.mymapview.view.MyMapView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MyMapView mapview;
    private provinceAdapter adapter;
    private MyMap myMap;
    private Button changeType;
    private ColorView colorView;
    private int currentColor = 0;
    private ListView province_listview;
    private HashMap<String, List<MycolorArea>> colorView_hashmap;
    private List<String> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //设置颜色渐变条
        setColorView();
        //初始化map
        initMap();
        //初始化map各省份颜色
        ColorChangeHelp.changeMapColors(myMap,ColorChangeHelp.nameStrings[0]);
        mapview.chingeMapColors();
        //listview
        setListAdapter();
        changeType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String namestring = ColorChangeHelp.nameStrings[++currentColor % ColorChangeHelp.nameStrings.length];
                changeType.setText(namestring);
                colorView.setList(colorView_hashmap.get(namestring));
                //重置map各省份颜色
                ColorChangeHelp.changeMapColors(myMap, namestring);
                mapview.chingeMapColors();
            }
        });
        mapview.setOnChoseProvince(new MyMapView.onProvinceClickLisener() {
            @Override
            public void onChose(String provincename) {
                //地图点击省份回调接口，listview滚动到相应省份位置
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).contains(provincename)) {
                        adapter.setPosition(i);
                        province_listview.setSelection(i);
                        break;
                    }
                }
            }
        });
    }
    private void setListAdapter() {
        list=new ArrayList<>();
        //最后三个是香港，澳门和台湾，不需要
        for (int i=0;i<ColorChangeHelp.province_datas.length-3;i++){
            list.add(ColorChangeHelp.province_datas[i]);
        }
        adapter = new provinceAdapter(this, list);
        province_listview.setAdapter(adapter);
    }

    private void initView() {
        province_listview = (ListView) findViewById(R.id.province_listview);
        mapview = (MyMapView) findViewById(R.id.view);
        colorView = (ColorView) findViewById(R.id.colorView);
        changeType = (Button) findViewById(R.id.changeType);
        //changeType.setVisibility(View.GONE);
        changeType.setText(ColorChangeHelp.nameStrings[0]);
    }
    /**
     * 设置颜色渐变条
     */
    private void setColorView() {
        colorView_hashmap = new HashMap<>();
        for (int i = 0; i < ColorChangeHelp.nameStrings.length; i++) {
            String colors[] = ColorChangeHelp.colorStrings[i].split(",");
            String texts[] = ColorChangeHelp.textStrings[i].split(",");
            List<MycolorArea> list = new ArrayList<>();
            for (int j = 0; j < colors.length; j++) {
                MycolorArea c = new MycolorArea();
                c.setColor(Color.parseColor(colors[j]));
                c.setText(texts[j]);
                list.add(c);
            }
            colorView_hashmap.put(ColorChangeHelp.nameStrings[i], list);
        }
        colorView.setList(colorView_hashmap.get(ColorChangeHelp.nameStrings[0]));
    }

    private void initMap() {
        //拿到SVG文件，解析成对象
        myMap = new SvgUtil(this).getProvinces();
        //设置缩放最大和最小倍数
        mapview.setMaxScale(3);
        mapview.setMinScale(1);
        //传数据
        mapview.setMap(myMap);
    }

}
