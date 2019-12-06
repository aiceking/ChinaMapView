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
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
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
    private int selectPosition;
    private ScrollScaleGestureDetector.OnScrollScaleGestureListener onScrollScaleGestureListener=new ScrollScaleGestureDetector.OnScrollScaleGestureListener() {
        @Override
        public void onClick(float x, float y) {
            //只有点击在某一个省份内才会触发省份选择接口
            for (ProvinceModel p:map.getProvinceslist()){
                for (Region region:p.getRegionList()){
                    if (region.contains((int)x, (int)y)){
                        //重置上一次选中省份的状态
                        map.getProvinceslist().get(selectPosition).setSelect(false);
                        map.getProvinceslist().get(selectPosition).setLinecolor(Color.GRAY);
                        //设置新的选中的省份
                        p.setSelect(true);
                        p.setLinecolor(Color.BLACK);
                        //暴露到Activity中的接口，把省的名字传过去
                        onProvinceClickLisener.onChose(p.getName());
                        invalidate();
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
    //初始化准备工作
    public ChinaMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //初始化省份内部画笔
        innerPaint=new Paint();
        innerPaint.setColor(Color.BLUE);
        innerPaint.setAntiAlias(true);
        //初始化省份外框画笔
        outerPaint=new Paint();
        outerPaint.setColor(Color.GRAY);
        outerPaint.setAntiAlias(true);
        outerPaint.setStrokeWidth(1);
        outerPaint.setStyle(Paint.Style.STROKE);
        //初始化手势帮助类
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
        Log.v("xixi=","onMeasure");
        //不管高度的设置Mode是什么，直接把View的高度按照宽度适配的缩放倍数进行适配
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
        requestLayout();
    }
    public void chingeMapColors(){
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        //保证只在初次绘制的时候进行缩放适配
        if (isFirst){
        viewWidth=getWidth()-getPaddingLeft()-getPaddingRight();
        //首先重置所有点的坐标，使得map适应屏幕大小
            if (map!=null){
                map_scale=viewWidth/map.getMax_x();
            }
            //缩放所有Path
            scalePoints(canvas,map_scale);
            isFirst=false;
        }else{
            //关联缩放和平移后的矩阵
            scrollScaleGestureDetector.connect(canvas);
            scrollScaleGestureDetector.setScaleMax(3);//最大缩放倍数
            scrollScaleGestureDetector.setScalemin(1);//最小缩放倍数
            //绘制Map
            drawMap(canvas);
        }
        super.onDraw(canvas);
    }
//绘制整个Map
    private void drawMap(Canvas canvas) {
        if (map.getProvinceslist().size()>0){
            outerPaint.setStrokeWidth(1);
            //首先记录下点击的省份的下标，先把其他的省份绘制完，
            for (int i=0;i<map.getProvinceslist().size();i++){
                if (map.getProvinceslist().get(i).isSelect()){
                    selectPosition=i;
                }else{
                    //此时绘制其他省份，边框画笔的宽度为1
                    innerPaint.setColor(map.getProvinceslist().get(i).getColor());
                    outerPaint.setColor(map.getProvinceslist().get(i).getLinecolor());
                    for (Path p:map.getProvinceslist().get(i).getListpath()){
                        canvas.drawPath(p, innerPaint);
                        canvas.drawPath(p, outerPaint);
                    }
                }
            }
            //再绘制点击所在的省份,此时画笔宽度设为2.5，以达到着重显示的效果
            innerPaint.setColor(map.getProvinceslist().get(selectPosition).getColor());
            outerPaint.setColor(map.getProvinceslist().get(selectPosition).getLinecolor());
            outerPaint.setStrokeWidth(2.5f);
            for (Path p:map.getProvinceslist().get(selectPosition).getListpath()){
                canvas.drawPath(p, innerPaint);
                canvas.drawPath(p, outerPaint);
            }
        }
    }
//    private void drawMap(Canvas canvas) {
//        if (map.getProvinceslist().size()>0){
//            outerPaint.setStrokeWidth(1);
//            //首先记录下点击的省份的下标，先把其他的省份绘制完，
//            for (int i=0;i<map.getProvinceslist().size();i++){
//                if (map.getProvinceslist().get(i).isSelect()){
//                    outerPaint.setStrokeWidth(2.5f);
//                    outerPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//                    selectPosition=i;
//                }else{
//                    outerPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
//                    outerPaint.setXfermode(null);
//                    outerPaint.setStrokeWidth(1);
//                    //此时绘制其他省份，边框画笔的宽度为1
//                    outerPaint.setXfermode(null);
//                }
//                innerPaint.setColor(map.getProvinceslist().get(i).getColor());
//                outerPaint.setColor(map.getProvinceslist().get(i).getLinecolor());
//                for (Path p:map.getProvinceslist().get(i).getListpath()){
//                    canvas.drawPath(p, innerPaint);
//                    canvas.drawPath(p, outerPaint);
//                }
//            }
//            //再绘制点击所在的省份,此时画笔宽度设为2.5，以达到着重显示的效果
//            innerPaint.setColor(map.getProvinceslist().get(selectPosition).getColor());
//            outerPaint.setColor(map.getProvinceslist().get(selectPosition).getLinecolor());
//            outerPaint.setStrokeWidth(2.5f);
//            for (Path p:map.getProvinceslist().get(selectPosition).getListpath()){
//                canvas.drawPath(p, innerPaint);
//                canvas.drawPath(p, outerPaint);
//            }
//        }
//    }
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
                List<Region> regionList=new ArrayList<>();
                List<Path> pathList=new ArrayList<>();
                for (Path p:province.getListpath()){
                    //遍历Path中的所有点，重置点的坐标
                    Path newpath=resetPath(p, scale, regionList);
                    pathList.add(newpath);
                    canvas.drawPath(newpath,innerPaint);
                    canvas.drawPath(newpath,outerPaint);
                }
                    province.setListpath(pathList);
                //判断点是否在path画出的区域内
                province.setRegionList(regionList);
            }
    }

    private Path resetPath(Path path,float scale,List<Region> regionList) {
        List<PointF> list=new ArrayList<>();
        PathMeasure pathmesure=new PathMeasure(path,true);
        float[] s=new float[2];
        //按照缩放倍数重置Path内的所有点
        for (int i=0;i<pathmesure.getLength();i=i+2) {
            pathmesure.getPosTan(i, s, null);
            PointF p=new PointF(s[0]*scale,s[1]*scale);
            list.add(p);
        }
        //重绘缩放后的Path
        Path path1=new Path();
        for (int i=0;i<list.size();i++){
            if (i==0){
                path1.moveTo(list.get(i).x,list.get(i).y);
            }else{
                path1.lineTo(list.get(i).x, list.get(i).y);
            }
        }
        path1.close();
        //构造Path对应的Region,用于判断点击的点是否在Path内
        RectF rf=new RectF();
        Region re=new Region();
        path1.computeBounds(rf,true);
        re.setPath(path1,new Region((int)rf.left,(int)rf.top,(int)rf.right,(int)rf.bottom));
        regionList.add(re);
        return path1;
    }
    //选中所点击的省份
    public interface onProvinceClickLisener{
        public void onChose(String provincename);
    }
}
