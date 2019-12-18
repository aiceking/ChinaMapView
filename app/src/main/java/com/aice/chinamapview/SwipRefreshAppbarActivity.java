package com.aice.chinamapview;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;

import com.aice.chinamapview.adapter.ProvinceAdapter;
import com.aice.chinamapview.listener.AppBarLayoutStateChangeListener;
import com.aice.chinamapview.model.MycolorArea;
import com.aice.chinamapview.util.ColorChangeUtil;
import com.aice.chinamapview.view.ColorView;
import com.google.android.material.appbar.AppBarLayout;
import com.wxy.chinamapview.model.ChinaMapModel;
import com.wxy.chinamapview.view.ChinaMapView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.aice.chinamapview.listener.AppBarLayoutStateChangeListener.State.EXPANDED;

public class SwipRefreshAppbarActivity extends AppCompatActivity {

    @BindView(R.id.chinamap_view)
    ChinaMapView chinamapView;
    @BindView(R.id.color_view)
    ColorView colorView;
    @BindView(R.id.btn_change)
    Button btnChange;
    @BindView(R.id.appbar_layout)
    AppBarLayout appbarLayout;
    @BindView(R.id.recycle)
    RecyclerView recycle;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;

    private ChinaMapModel chinaMapModel;
    private HashMap<String, List<MycolorArea>> colorView_hashmap;
    private int currentColor = 0;
    private List<String> list;
    private ProvinceAdapter adapter;
    private AppBarLayoutStateChangeListener.State appbarState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swiprefreshappbar);
        ButterKnife.bind(this);
        //初始化map
        initMap();
        //设置颜色渐变条
        setColorView();
        initRecycleView();
        //初始化地图颜色
        intMapColor();
        initAppbarListener();
        initSwipRefresh();
    }

    private void initSwipRefresh() {
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                chinamapView.setEnableTouch(false);
                //模拟耗时
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String namestring = ColorChangeUtil.nameStrings[++currentColor % ColorChangeUtil.nameStrings.length];
                        btnChange.setText(namestring);
                        colorView.setList(colorView_hashmap.get(namestring));
                        //重置map各省份颜色
                        ColorChangeUtil.changeMapColors(chinaMapModel, namestring);
                        chinamapView.notifyDataChanged();
                        swipe.setRefreshing(false);
                        if (appbarState==EXPANDED){
                            swipe.setEnabled(true);
                            chinamapView.setEnableTouch(true);

                        }else {
                            swipe.setEnabled(false);
                            chinamapView.setEnableTouch(false);

                        }
                    }
                },2000);
            }
        });
    }

    private void initAppbarListener() {
        appbarLayout.addOnOffsetChangedListener(new AppBarLayoutStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                appbarState=state;
                switch (state) {
                    case EXPANDED:
                        swipe.setEnabled(true);
                        chinamapView.setEnableTouch(true);
                        break;
                    case COLLAPSED:
                    case INTERMEDIATE:
                        chinamapView.setEnableTouch(false);
                        if (!swipe.isRefreshing()){
                            swipe.setEnabled(false);}
                        break;
                }
            }
        });
    }

    private void intMapColor() {
        btnChange.setText(ColorChangeUtil.nameStrings[0]);
        ColorChangeUtil.changeMapColors(chinaMapModel, ColorChangeUtil.nameStrings[currentColor]);
        chinamapView.notifyDataChanged();
    }

    private void initRecycleView() {
        list=new ArrayList<>();
        for (int i = 0; i< ColorChangeUtil.province_datas.length; i++){
            list.add(ColorChangeUtil.province_datas[i]);
        }
        adapter = new ProvinceAdapter(R.layout.recycle_province_item, list);
        recycle.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recycle.setAdapter(adapter);
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
        chinaMapModel = chinamapView.getChinaMapModel();
        //传数据
        chinamapView.setScaleMax(3);
        chinamapView.setScaleMin(1);
        chinamapView.setOnProvinceClickLisener(new ChinaMapView.onProvinceClickLisener() {
            @Override
            public void onSelectProvince(String provinceName) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).contains(provinceName)) {
                        String s = list.get(i);
                        list.remove(i);
                        list.add(0, s);
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        });
        chinamapView.setOnPromiseParentTouchListener(new ChinaMapView.onPromiseParentTouchListener() {
            @Override
            public void onPromiseTouch(boolean promise) {
                swipe.setEnabled(promise);
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
        chinamapView.notifyDataChanged();
    }
}
