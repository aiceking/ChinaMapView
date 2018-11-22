package com.aiceking.chinamap.old;

import android.graphics.PointF;

import java.util.List;

/**
 * Created by Vmmet on 2016/7/29.
 */
public class Lasso {
    // polygon coordinates
    private float[] mPolyX, mPolyY;
    // number of size in polygon
    private int mPolySize;
    public Lasso(float[] px, float[] py, int ps) {
        this.mPolyX = px;
        this.mPolyY = py;
        this.mPolySize = ps;
    }
    public Lasso(List<PointF> pointFs) {
        this.mPolySize = pointFs.size();
        this.mPolyX = new float[this.mPolySize];
        this.mPolyY = new float[this.mPolySize];

        for (int i = 0; i < this.mPolySize; i++) {
            this.mPolyX[i] = pointFs.get(i).x;
            this.mPolyY[i] = pointFs.get(i).y;
        }
       // Log.d("lasso", "lasso size:" + mPolySize);
    }
    public boolean contains(float x, float y) {
        boolean result = false;
        for (int i = 0, j = mPolySize - 1; i < mPolySize; j = i++) {
            if ((mPolyY[i] < y && mPolyY[j] >= y)
                    || (mPolyY[j] < y && mPolyY[i] >= y)) {
                if (mPolyX[i] + (y - mPolyY[i]) / (mPolyY[j] - mPolyY[i])
                        * (mPolyX[j] - mPolyX[i]) < x) {
                    result = !result;
                }
            }
        }
        return result;
    }
}
