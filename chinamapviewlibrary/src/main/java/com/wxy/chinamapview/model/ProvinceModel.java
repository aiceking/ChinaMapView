package com.wxy.chinamapview.model;

import android.graphics.Path;
import android.graphics.Region;

import java.util.List;

/**
 * Created by Vmmet on 2016/7/29.
 */
public class ProvinceModel {
    private float maxX;
    private float minX;
    private float maxY;
    private float minY;
    private String name;
    private int color;
    private int normalBorderColor;
    private int selectBorderColor;
    private List<Path> listPath;
    private List<Region> regionList;
    private boolean isSelect;
    private float centerX;
    private float centerY;
    private int nameColor;

    public int getNameColor() {
        return nameColor;
    }

    public void setNameColor(int nameColor) {
        this.nameColor = nameColor;
    }

    public float getCenterX() {
        return centerX;
    }

    public void setCenterX(float centerX) {
        this.centerX = centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    public void setCenterY(float centerY) {
        this.centerY = centerY;
    }

    public int getNormalBorderColor() {
        return normalBorderColor;
    }
    public void setNormalBorderColor(int normalBorderColor) {
        this.normalBorderColor = normalBorderColor;
    }
    public int getSelectBorderColor() {
        return selectBorderColor;
    }
    public void setSelectBorderColor(int selectBorderColor) {
        this.selectBorderColor = selectBorderColor;
    }
    public boolean isSelect() {
        return isSelect;
    }
    public void setSelect(boolean select) {
        isSelect = select;
    }
    public List<Region> getRegionList() {
        return regionList;
    }
    public void setRegionList(List<Region> regionList) {
        this.regionList = regionList;
    }
    public List<Path> getListPath() {
        return listPath;
    }
    public void setListPath(List<Path> listPath) {
        this.listPath = listPath;
    }
    public int getColor() {
        return color;
    }
    public void setColor(int color) {
        this.color = color;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
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
}
