package com.aice.chinamapview;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.aice.chinamapview.adapter.ProvinceAdapter;
import com.aice.chinamapview.databinding.ActivitySwiprefreshappbarBinding;
import com.aice.chinamapview.listener.AppBarLayoutStateChangeListener;
import com.aice.chinamapview.model.MyColorArea;
import com.aice.chinamapview.util.ColorChangeUtil;
import com.google.android.material.appbar.AppBarLayout;
import com.wxy.chinamapview.model.ChinaMapModel;
import com.wxy.chinamapview.view.ChinaMapView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static com.aice.chinamapview.listener.AppBarLayoutStateChangeListener.State.EXPANDED;

public class SwipRefreshAppbarActivity extends AppCompatActivity {
    private ActivitySwiprefreshappbarBinding mBinding;
    private ChinaMapModel chinaMapModel;
    private HashMap<String, List<MyColorArea>> colorView_hashmap;
    private int currentColor = 0;
    private List<String> list;
    private ProvinceAdapter adapter;
    private AppBarLayoutStateChangeListener.State appbarState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_swiprefreshappbar);
        //初始化map
        initMap();
        //设置颜色渐变条
        setColorView();
        initRecycleView();
        //初始化地图颜色
        intMapColor();
        initAppbarListener();
        initSwipRefresh();
        mBinding.btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String namestring = ColorChangeUtil.nameStrings[++currentColor % ColorChangeUtil.nameStrings.length];
                mBinding.btnChange.setText(namestring);
                mBinding.colorView.setList(colorView_hashmap.get(namestring));
                //重置map各省份颜色
                ColorChangeUtil.changeMapColors(chinaMapModel, namestring);
                mBinding.chinamapView.notifyDataChanged();
            }
        });
    }

    private void initSwipRefresh() {
        mBinding.swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mBinding.chinamapView.setEnableTouch(false);
                //模拟耗时
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String nameString = ColorChangeUtil.nameStrings[++currentColor % ColorChangeUtil.nameStrings.length];
                        mBinding.btnChange.setText(nameString);
                        mBinding.colorView.setList(colorView_hashmap.get(nameString));
                        //重置map各省份颜色
                        ColorChangeUtil.changeMapColors(chinaMapModel, nameString);
                        mBinding.chinamapView.notifyDataChanged();
                        mBinding.swipe.setRefreshing(false);
                        if (appbarState == EXPANDED) {
                            mBinding.swipe.setEnabled(true);
                            mBinding.chinamapView.setEnableTouch(true);

                        } else {
                            mBinding.swipe.setEnabled(false);
                            mBinding.chinamapView.setEnableTouch(false);

                        }
                    }
                }, 2000);
            }
        });
    }

    private void initAppbarListener() {
        mBinding.appbarLayout.addOnOffsetChangedListener(new AppBarLayoutStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                appbarState = state;
                switch (state) {
                    case EXPANDED:
                        mBinding.swipe.setEnabled(true);
                        mBinding.chinamapView.setEnableTouch(true);
                        break;
                    case COLLAPSED:
                    case INTERMEDIATE:
                        mBinding.chinamapView.setEnableTouch(false);
                        if (!mBinding.swipe.isRefreshing()) {
                            mBinding.swipe.setEnabled(false);
                        }
                        break;
                }
            }
        });
    }

    private void intMapColor() {
        mBinding.btnChange.setText(ColorChangeUtil.nameStrings[0]);
        ColorChangeUtil.changeMapColors(chinaMapModel, ColorChangeUtil.nameStrings[currentColor]);
        mBinding.chinamapView.notifyDataChanged();
    }

    private void initRecycleView() {
        list = new ArrayList<>();
        for (int i = 0; i < ColorChangeUtil.province_datas.length; i++) {
            list.add(ColorChangeUtil.province_datas[i]);
        }
        adapter = new ProvinceAdapter(R.layout.recycle_province_item, list);
        mBinding.recycle.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mBinding.recycle.setAdapter(adapter);
    }

    /**
     * 设置颜色渐变条
     */
    private void setColorView() {
        colorView_hashmap = new HashMap<>();
        for (int i = 0; i < ColorChangeUtil.nameStrings.length; i++) {
            String colors[] = ColorChangeUtil.colorStrings[i].split(",");
            String texts[] = ColorChangeUtil.textStrings[i].split(",");
            List<MyColorArea> list = new ArrayList<>();
            for (int j = 0; j < colors.length; j++) {
                MyColorArea c = new MyColorArea();
                c.setColor(Color.parseColor(colors[j]));
                c.setText(texts[j]);
                list.add(c);
            }
            colorView_hashmap.put(ColorChangeUtil.nameStrings[i], list);
        }
        mBinding.colorView.setList(colorView_hashmap.get(ColorChangeUtil.nameStrings[0]));
    }

    private void initMap() {
        chinaMapModel = mBinding.chinamapView.getChinaMapModel();
        //传数据
        mBinding.chinamapView.setScaleMax(3);
        mBinding.chinamapView.setScaleMin(1);
        mBinding.chinamapView.setOnProvinceClickLisener(new ChinaMapView.onProvinceClickLisener() {
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
        mBinding.chinamapView.setOnPromiseParentTouchListener(new ChinaMapView.onPromiseParentTouchListener() {
            @Override
            public void onPromiseTouch(boolean promise) {
                mBinding.swipe.setEnabled(promise);
                banAppBarScroll(promise);
                Log.v("Map", promise + "");
            }
        });
    }

    private void banAppBarScroll(boolean isScroll) {
        for (int i = 0; i < mBinding.appbarLayout.getChildCount(); i++) {
            View mAppBarChildAt = mBinding.appbarLayout.getChildAt(i);
            AppBarLayout.LayoutParams mAppBarParams = (AppBarLayout.LayoutParams) mAppBarChildAt.getLayoutParams();
            if (isScroll) {
                mAppBarParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
                mAppBarChildAt.setLayoutParams(mAppBarParams);
            } else {
                mAppBarParams.setScrollFlags(0);
            }
        }
    }
}
