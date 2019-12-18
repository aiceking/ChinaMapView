package com.aice.chinamapview;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wxy.chinamapview.model.ChinaMapModel;
import com.wxy.chinamapview.model.ProvinceModel;
import com.wxy.chinamapview.view.ChinaMapView;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import top.defaults.colorpicker.ColorPickerPopup;

public class NormalActivity extends AppCompatActivity {

    @BindView(R.id.map)
    ChinaMapView map;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.btn_province_color)
    Button btnProvinceColor;
    @BindView(R.id.btn_border_unselect_color)
    Button btnBorderUnselectColor;
    @BindView(R.id.btn_border_select_color)
    Button btnBorderSelectColor;
    private ChinaMapModel chinaMapModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);
        ButterKnife.bind(this);
        chinaMapModel = map.getChinaMapModel();
        map.setScaleMin(1);
        map.setScaleMax(3);
        map.setOnProvinceClickLisener(new ChinaMapView.onProvinceClickLisener() {
            @Override
            public void onSelectProvince(String provinceName) {
                tvName.setText(provinceName);
            }
        });
        String s="";
        Log.v("xixi=",chinaMapModel.getProvinceslist().size()+"");
        for(ProvinceModel provinceModel:chinaMapModel.getProvinceslist()){
            s=s+provinceModel.getName()+",";
        }
        Log.v("xixi=",s);

    }

    @OnClick({R.id.btn_province_color, R.id.btn_border_unselect_color, R.id.btn_border_select_color})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_province_color:
                new ColorPickerPopup.Builder(this)
                        .initialColor(Color.RED) // Set initial color
                        .enableBrightness(true) // Enable brightness slider or not
                        .enableAlpha(true) // Enable alpha slider or not
                        .okTitle("确定")
                        .cancelTitle("取消")
                        .showIndicator(true)
                        .showValue(true)
                        .build()
                        .show(btnProvinceColor, new ColorPickerPopup.ColorPickerObserver() {
                            @Override
                            public void onColorPicked(int color) {
                                for (ProvinceModel provinceModel:chinaMapModel.getProvinceslist()){
                                    provinceModel.setColor(color);
                                }
                                map.notifyDataChanged();
                            }
                        });
                break;
            case R.id.btn_border_unselect_color:
                new ColorPickerPopup.Builder(this)
                        .initialColor(Color.RED) // Set initial color
                        .enableBrightness(true) // Enable brightness slider or not
                        .enableAlpha(true) // Enable alpha slider or not
                        .okTitle("确定")
                        .cancelTitle("取消")
                        .showIndicator(true)
                        .showValue(true)
                        .build()
                        .show(btnBorderUnselectColor, new ColorPickerPopup.ColorPickerObserver() {
                            @Override
                            public void onColorPicked(int color) {
                                for (ProvinceModel provinceModel:chinaMapModel.getProvinceslist()){
                                    provinceModel.setNormalBordercolor(color);
                                }
                                map.notifyDataChanged();
                            }
                        });
                break;
            case R.id.btn_border_select_color:
                new ColorPickerPopup.Builder(this)
                        .initialColor(Color.RED) // Set initial color
                        .enableBrightness(true) // Enable brightness slider or not
                        .enableAlpha(true) // Enable alpha slider or not
                        .okTitle("确定")
                        .cancelTitle("取消")
                        .showIndicator(true)
                        .showValue(true)
                        .build()
                        .show(btnBorderSelectColor, new ColorPickerPopup.ColorPickerObserver() {
                            @Override
                            public void onColorPicked(int color) {
                                for (ProvinceModel provinceModel:chinaMapModel.getProvinceslist()){
                                    provinceModel.setSelectBordercolor(color);
                                }
                                map.notifyDataChanged();
                            }
                        });
                break;
        }
    }
}
