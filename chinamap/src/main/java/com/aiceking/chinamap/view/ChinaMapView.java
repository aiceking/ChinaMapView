package com.aiceking.chinamap.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.aiceking.chinamap.gesture.ScrollScaleGestureDetector;
import com.aiceking.chinamap.model.ChinaMapModel;
import com.aiceking.chinamap.model.Lasso;
import com.aiceking.chinamap.model.ProvinceModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vmmet on 2016/8/4.
 */
public class ChinaMapView extends View{
    private float viewWidth;           //View的宽度
    private Paint innerPaint,outerPaint;//画省份的内部画笔和外圈画笔
    private boolean isFirst; //是否是第一次绘制,用于最初的适配
    private ScrollScaleGestureDetector scrollScaleGestureDetector;//自定义的缩放拖拽手势帮助类
    private ChinaMapModel map;
    private float map_scale=0;
    private ScrollScaleGestureDetector.OnScrollScaleGestureListener onScrollScaleGestureListener=new ScrollScaleGestureDetector.OnScrollScaleGestureListener() {
        @Override
        public void onClick(float x, float y) {
            for (ProvinceModel province:map.getProvinceslist()){
                province.setLinecolor(Color.GRAY);
            }
            for (ProvinceModel p:map.getProvinceslist()){
                for (Lasso lasso:p.getPathLasso()){
                    if (lasso.contains(x, y)){
                        //p.setColor(Color.RED);
                        p.setLinecolor(Color.BLACK);
                        onProvinceClickLisener.onChose(p.getName());
                        invalidate();
                        //暴露到Activity中的接口，把省的名字传过去
                        return;
                    }
                }
            }
        }
    };
    private onProvinceClickLisener onProvinceClickLisener;
    public ChinaMapView(Context context) {
        super(context);
    }
    public void setOnChoseProvince(onProvinceClickLisener lisener){
        this.onProvinceClickLisener=lisener;
    }
    public ChinaMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        innerPaint=new Paint();
        innerPaint.setColor(Color.BLUE);
        innerPaint.setAntiAlias(true);
        innerPaint.setDither(true);
        outerPaint=new Paint();
        outerPaint.setColor(Color.GRAY);
        outerPaint.setAntiAlias(true);
        outerPaint.setStrokeWidth(1);
        outerPaint.setStyle(Paint.Style.STROKE);
        outerPaint.setDither(true);
        // 设置光源的方向
        float[] direction = new float[]{ 1, 1, 1 };
        //设置环境光亮度
        float light = 0.4f;
        // 选择要应用的反射等级
        float specular = 6;
        // 向mask应用一定级别的模糊
        float blur = 3.5f;
        EmbossMaskFilter emboss=new EmbossMaskFilter(direction,light,specular,blur);
        //浮雕效果
        outerPaint.setMaskFilter(emboss);
        scrollScaleGestureDetector=new ScrollScaleGestureDetector(this,onScrollScaleGestureListener);

    }
    public ChinaMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width=MeasureSpec.getSize(widthMeasureSpec);
        if (map!=null){
            map_scale=width/map.getMax_x();
        }
        int height=(int) (map.getMax_y()*map_scale);
        setMeasuredDimension(width, height);
    }

    public void setMap(ChinaMapModel map){
        this.map=map;
        isFirst=true;
        invalidate();
    }
    public void chingeMapColors(){
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if (isFirst){
        viewWidth=getWidth()-getPaddingLeft()-getPaddingRight();
        //首先重置所有点的坐标，使得map适应屏幕大小
            if (map!=null){
                map_scale=viewWidth/map.getMax_x();
            }
            scalePoints(canvas,map_scale);
            isFirst=false;
        }else{
            scrollScaleGestureDetector.connect(canvas);
            scrollScaleGestureDetector.setScaleMax(3);
            scrollScaleGestureDetector.setScalemin(1);
            drawMap(canvas);
        }
        super.onDraw(canvas);
    }
    private void drawMap(Canvas canvas) {
        if (map.getProvinceslist().size()>0){
            int b=0;
            for (int i=0;i<map.getProvinceslist().size();i++){
                if (map.getProvinceslist().get(i).getLinecolor()==Color.BLACK){
                    b=i;
                }else{
                    innerPaint.setColor(map.getProvinceslist().get(i).getColor());
                    outerPaint.setColor(map.getProvinceslist().get(i).getLinecolor());
                    for (Path p:map.getProvinceslist().get(i).getListpath()){
                        canvas.drawPath(p, innerPaint);
                        canvas.drawPath(p, outerPaint);
                    }
                }
            }
            innerPaint.setColor(map.getProvinceslist().get(b).getColor());
            outerPaint.setColor(map.getProvinceslist().get(b).getLinecolor());
            for (Path p:map.getProvinceslist().get(b).getListpath()){
                canvas.drawPath(p, innerPaint);
                canvas.drawPath(p, outerPaint);
            }
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return scrollScaleGestureDetector.onTouchEvent(event);
    }

    //第一次绘制，缩小map到View指定大小
    private void scalePoints(Canvas canvas,float scale) {
        if (map.getProvinceslist().size()>0)
            //map的左右上下4个临界点
            map.setMax_x(map.getMax_x()*scale);
        map.setMin_x(map.getMin_x()*scale);
        map.setMax_y(map.getMax_y()*scale);
        map.setMin_y(map.getMin_y()*scale);
            for (ProvinceModel province:map.getProvinceslist()){
                innerPaint.setColor(province.getColor());
                List<Lasso> listLasso=new ArrayList<>();
                List<Path> pathList=new ArrayList<>();
                for (Path p:province.getListpath()){
                    //遍历Path中的所有点，重置点的坐标
                    Path newpath=resetPath(p, scale, listLasso);
                    pathList.add(newpath);
                    canvas.drawPath(newpath,innerPaint);
                    canvas.drawPath(newpath,outerPaint);
                }
                    province.setListpath(pathList);
                //拿到path转换之后的Lasso对象，用来点击的是哪个省份,即判断点是否在path画出的区域内
                province.setPathLasso(listLasso);
            }
    }
    private Path resetPath(Path path,float scale,List<Lasso> listLasso) {
        List<PointF> list=new ArrayList<>();
        PathMeasure pathmesure=new PathMeasure(path,true);
        float[] s=new float[2];
        for (int i=0;i<pathmesure.getLength();i=i+2) {
            pathmesure.getPosTan(i, s, null);
            PointF p=new PointF(s[0]*scale,s[1]*scale);
            list.add(p);
        }
        Lasso lasso=new Lasso(list);
        listLasso.add(lasso);
        Path path1=new Path();
        for (int i=0;i<list.size();i++){
            if (i==0){
                path1.moveTo(list.get(i).x,list.get(i).y);
            }else{
                path1.lineTo(list.get(i).x, list.get(i).y);
            }
        }
        path1.close();
        return path1;
    }
    public interface onProvinceClickLisener{
        public void onChose(String provincename);
    }
}
