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
    private int normalBordercolor;
    private int selectBordercolor;
    private List<Path> listpath;
    private List<Region> regionList;
    private boolean isSelect;
    public int getNormalBordercolor() {
        return normalBordercolor;
    }
    public void setNormalBordercolor(int normalBordercolor) {
        this.normalBordercolor = normalBordercolor;
    }
    public int getSelectBordercolor() {
        return selectBordercolor;
    }
    public void setSelectBordercolor(int selectBordercolor) {
        this.selectBordercolor = selectBordercolor;
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
    public List<Path> getListpath() {
        return listpath;
    }
    public void setListpath(List<Path> listpath) {
        this.listpath = listpath;
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
