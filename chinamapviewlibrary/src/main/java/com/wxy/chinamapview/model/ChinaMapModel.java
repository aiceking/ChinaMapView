package com.wxy.chinamapview.model;

import java.util.List;

/**
 * Created by Vmmet on 2016/7/29.
 */
public class ChinaMapModel {
    private float maxX;
    private float minX;
    private float maxY;
    private float minY;
    private boolean isShowName;

    public boolean isShowName() {
        return isShowName;
    }

    public void setShowName(boolean showName) {
        isShowName = showName;
    }

    private List<ProvinceModel> provincesList;

    public float getMaxX() {
        return maxX;
    }

    public void setMaxX(float maxX) {
        this.maxX = maxX;
    }

    public float getMinX() {
        return minX;
    }

    public void setMinX(float minX) {
        this.minX = minX;
    }

    public float getMaxY() {
        return maxY;
    }

    public void setMaxY(float maxY) {
        this.maxY = maxY;
    }

    public float getMinY() {
        return minY;
    }

    public void setMinY(float minY) {
        this.minY = minY;
    }

    public List<ProvinceModel> getProvincesList() {
        return provincesList;
    }
    public void setProvincesList(List<ProvinceModel> provincesList) {
        this.provincesList = provincesList;
    }


}
