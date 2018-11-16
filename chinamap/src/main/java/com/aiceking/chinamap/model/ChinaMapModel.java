package com.aiceking.chinamap.model;

import java.util.List;

/**
 * Created by Vmmet on 2016/7/29.
 */
public class ChinaMapModel {
    private float Max_x;
    private float Min_x;
    private float Max_y;
    private float Min_y;

    public float getMin_x() {
        return Min_x;
    }

    public void setMin_x(float min_x) {
        Min_x = min_x;
    }

    public float getMax_y() {
        return Max_y;
    }

    public void setMax_y(float max_y) {
        Max_y = max_y;
    }

    public float getMin_y() {
        return Min_y;
    }

    public void setMin_y(float min_y) {
        Min_y = min_y;
    }

    private List<ProvinceModel> provinceslist;

    public float getMax_x() {
        return Max_x;
    }

    public List<ProvinceModel> getProvinceslist() {
        return provinceslist;
    }

    public void setProvinceslist(List<ProvinceModel> provinceslist) {
        this.provinceslist = provinceslist;
    }

    public void setMax_x(float max_x) {
        Max_x = max_x;
    }

}
