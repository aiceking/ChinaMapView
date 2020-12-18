package com.aice.chinamapview;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.aice.chinamapview.databinding.ActivityNormalBinding;
import com.wxy.chinamapview.model.ChinaMapModel;
import com.wxy.chinamapview.model.ProvinceModel;
import com.wxy.chinamapview.view.ChinaMapView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import top.defaults.colorpicker.ColorPickerPopup;

public class NormalActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityNormalBinding mBinding;
    private ChinaMapModel chinaMapModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_normal);
        chinaMapModel = mBinding.map.getChinaMapModel();
        mBinding.map.setScaleMin(1);
        mBinding.map.setScaleMax(3);
        mBinding.map.setOnProvinceClickLisener(provinceName -> mBinding.tvName.setText(provinceName));
        mBinding.btnProvinceColor.setOnClickListener(this);
        mBinding.btnBorderUnselectColor.setOnClickListener(this);
        mBinding.btnBorderSelectColor.setOnClickListener(this);
        mBinding.btnProvinceNameColor.setOnClickListener(this);
        mBinding.checkboxProvinceName.setOnClickListener(this);
        mBinding.checkboxProvinceMoveScale.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
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
                        .show(mBinding.btnProvinceColor, new ColorPickerPopup.ColorPickerObserver() {
                            @Override
                            public void onColorPicked(int color) {
                                for (ProvinceModel provinceModel:chinaMapModel.getProvincesList()){
                                    provinceModel.setColor(color);
                                }
                                mBinding.map.notifyDataChanged();
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
                        .show(mBinding.btnBorderUnselectColor, new ColorPickerPopup.ColorPickerObserver() {
                            @Override
                            public void onColorPicked(int color) {
                                for (ProvinceModel provinceModel:chinaMapModel.getProvincesList()){
                                    provinceModel.setNormalBorderColor(color);
                                }
                                mBinding.map.notifyDataChanged();
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
                        .show(mBinding.btnBorderSelectColor, new ColorPickerPopup.ColorPickerObserver() {
                            @Override
                            public void onColorPicked(int color) {
                                for (ProvinceModel provinceModel:chinaMapModel.getProvincesList()){
                                    provinceModel.setSelectBorderColor(color);
                                }
                                mBinding.map.notifyDataChanged();
                            }
                        });
                break;
            case R.id.btn_province_name_color:
                new ColorPickerPopup.Builder(this)
                        .initialColor(Color.RED) // Set initial color
                        .enableBrightness(true) // Enable brightness slider or not
                        .enableAlpha(true) // Enable alpha slider or not
                        .okTitle("确定")
                        .cancelTitle("取消")
                        .showIndicator(true)
                        .showValue(true)
                        .build()
                        .show(mBinding.btnBorderSelectColor, new ColorPickerPopup.ColorPickerObserver() {
                            @Override
                            public void onColorPicked(int color) {
                                for (ProvinceModel provinceModel:chinaMapModel.getProvincesList()){
                                    provinceModel.setNameColor(color);
                                }
                                mBinding.map.notifyDataChanged();
                            }
                        });
                break;
            case R.id.checkbox_province_name:
                chinaMapModel.setShowName(mBinding.checkboxProvinceName.isChecked());
                mBinding.map.notifyDataChanged();
                break;
            case R.id.checkbox_province_move_scale:
                mBinding.map.setEnableScroll(!mBinding.checkboxProvinceMoveScale.isChecked());
                break;
        }
    }
}
