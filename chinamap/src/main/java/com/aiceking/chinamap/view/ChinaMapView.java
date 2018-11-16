package com.aiceking.chinamap.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import com.aiceking.chinamap.model.ChinaMapModel;
import com.aiceking.chinamap.model.ProvinceModel;
import com.aiceking.chinamap.util.SvgUtil;
import java.util.ArrayList;
import java.util.List;
public class ChinaMapView extends View{
    private ChinaMapModel chinaMapModel;
    private float map_scale;//初始化适配到屏幕宽度的比例
    private  float scale_max ;	//缩放的默认最大值
    private   float scale_min ;	//缩放的默认的最小值
    private Paint ProvinceInnerPaint,//画省份的内部画笔
                  ProvinceOuterPaint;//画省份的边框画笔
    private float Width,Height;      //View的宽度和高度
    private boolean isFirst;         //只在第一次传数据绘图时加载，进行大小适配
    private boolean isScale;         //判断是否在缩放
    private float onMoveX,onMoveY;     //平移的X,Y
    private float onScale;  //手势缩放倍数
    private float onScaleCenterX,onScaleCenterY;  //手势缩放的中心点
    private GestureDetectorCompat gestureDetectorCompat;//滑动和点击手势帮助类
    private ScaleGestureDetector scaleGestureDetector;//缩放手势帮助类
    public void setOnProvinceClickLisener(ChinaMapView.onProvinceClickLisener onProvinceClickLisener) {
        this.onProvinceClickLisener = onProvinceClickLisener;
    }

    private onProvinceClickLisener onProvinceClickLisener;//省份点击接口
    public ChinaMapView(Context context) {
        super(context);
    }

    public ChinaMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        //初始化缩放
        map_scale=1;
        scale_max=3;
        scale_min=1;
        //初始化View的宽度和高度
        Width=0;Height=0;
        //初始化平移的距离
        onMoveX=0;onMoveY=0;
        //初始化手势中心点
        onScaleCenterX=0;onScaleCenterY=0;
        //初始化手势缩放倍数
        onScale=1;
        //初始化省份的内部画笔和边框画笔
        ProvinceInnerPaint=new Paint();
        ProvinceInnerPaint.setColor(Color.WHITE);
        ProvinceInnerPaint.setAntiAlias(true);
        ProvinceOuterPaint=new Paint();
        ProvinceOuterPaint.setColor(Color.GRAY);
        ProvinceOuterPaint.setAntiAlias(true);
        ProvinceOuterPaint.setStrokeWidth(1);
        ProvinceOuterPaint.setStyle(Paint.Style.STROKE);
        //初始化平移和点击手势帮助类
        gestureDetectorCompat=new GestureDetectorCompat(context,simpleOnGestureListener);
        //初始化缩放手势帮助类
        scaleGestureDetector=new ScaleGestureDetector(context,scaleGestureListener);
        //初始化Map数据
        chinaMapModel = new SvgUtil(context).getProvinces();
        isFirst=true;
    }

    public ChinaMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width=MeasureSpec.getSize(widthMeasureSpec);
        //按屏幕宽度进行缩放适配
        if (chinaMapModel!=null){
            map_scale=width/chinaMapModel.getMax_x();
        }
        int height=(int) (chinaMapModel.getMax_y()*map_scale);
        setMeasuredDimension(width, height);
    }
    //设置最大缩放倍数
    public void setMaxScale(float a){
        if (a<=1){
            scale_max=1;
        }else{
            scale_max=a;
        }
    }
    //设置最小缩放倍数
    public void setMinScale(float a){
        if (a<=0){
            scale_min=0;
        }else{
            scale_min=a;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isFirst){
            Width=getWidth();
            Height=getHeight();
            //首先重置所有点的坐标，使得map适应屏幕大小
            if (chinaMapModel!=null){
                map_scale=Width/chinaMapModel.getMax_x();
            }
            scalePoints(canvas,map_scale);
            isFirst=false;
        }else{
//            canvas.concat(myMatrix);
            canvas.scale(onScale,onScale,onScaleCenterX,onScaleCenterY);
            canvas.translate(onMoveX, onMoveY);
            drawMap(canvas);
        }
    }
    private void drawMap(Canvas canvas) {
        if (chinaMapModel.getProvinceslist().size()>0){
            int b=0;
            for (int i=0;i<chinaMapModel.getProvinceslist().size();i++){
                if (chinaMapModel.getProvinceslist().get(i).getLinecolor()==Color.BLACK){
                    b=i;
                }else{
                    ProvinceInnerPaint.setColor(chinaMapModel.getProvinceslist().get(i).getColor());
                    ProvinceOuterPaint.setColor(chinaMapModel.getProvinceslist().get(i).getLinecolor());
                    for (Path p:chinaMapModel.getProvinceslist().get(i).getListpath()){
                        canvas.drawPath(p, ProvinceInnerPaint);
                        canvas.drawPath(p, ProvinceOuterPaint);
                    }
                }
            }
            ProvinceInnerPaint.setColor(chinaMapModel.getProvinceslist().get(b).getColor());
            ProvinceOuterPaint.setColor(chinaMapModel.getProvinceslist().get(b).getLinecolor());
            for (Path p:chinaMapModel.getProvinceslist().get(b).getListpath()){
                canvas.drawPath(p, ProvinceInnerPaint);
                canvas.drawPath(p, ProvinceOuterPaint);
            }
        }
    }
    //第一次绘制，缩小chinaMapModel到View指定大小
    private void scalePoints(Canvas canvas,float scale) {
        if (chinaMapModel.getProvinceslist().size()>0)
            //chinaMapModel的左右上下4个临界点
            chinaMapModel.setMax_x(chinaMapModel.getMax_x()*scale);
        chinaMapModel.setMin_x(chinaMapModel.getMin_x()*scale);
        chinaMapModel.setMax_y(chinaMapModel.getMax_y()*scale);
        chinaMapModel.setMin_y(chinaMapModel.getMin_y()*scale);
        for (ProvinceModel province:chinaMapModel.getProvinceslist()){
            ProvinceInnerPaint.setColor(province.getColor());
            List<Region> listRegion=new ArrayList<>();
            List<Path> pathList=new ArrayList<>();
            for (Path p:province.getListpath()){
                //遍历Path中的所有点，重置点的坐标
                Path newpath=resetPath(p, scale, listRegion);
                pathList.add(newpath);
                canvas.drawPath(newpath,ProvinceInnerPaint);
                canvas.drawPath(newpath,ProvinceOuterPaint);
            }
            province.setListpath(pathList);
            //拿到path转换之后的Region对象，用来点击的是哪个省份,即判断点是否在path画出的区域内
            province.setPathRegion(listRegion);
        }
    }
    private Path resetPath(Path path,float scale,List<Region> listRegion) {
        List<PointF> list=new ArrayList<>();
        PathMeasure pathmesure=new PathMeasure(path,true);
        float[] s=new float[2];
        for (int i=0;i<pathmesure.getLength();i=i+2) {
            pathmesure.getPosTan(i, s, null);
            PointF p=new PointF(s[0]*scale,s[1]*scale);
            list.add(p);
        }
        RectF r=new RectF();
        path.computeBounds(r,true);
        Region region=new Region();
        region.setPath(path,new Region((int)r.left,(int)r.top,(int)r.right,(int)r.bottom));
        listRegion.add(region);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetectorCompat.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }
    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener=new GestureDetector.SimpleOnGestureListener(){

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
                return true;
        }
        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!isScale){
            onMoveX=onMoveX-distanceX/onScale;
            onMoveY=onMoveY-distanceY/onScale;
            if (Math.abs(onMoveX)>=Width/2){
                if (onMoveX>0){
                onMoveX=Width/2;
                }else {
                    onMoveX=-Width/2;
                }
            }
            if (Math.abs(onMoveY)>=Height/2){
                if (onMoveY>0){
                onMoveY=Height/2;
                }else {
                    onMoveY=-Height/2;
                }
            }
            invalidate();
            }
            return true;
        }
    };
    private ScaleGestureDetector.OnScaleGestureListener scaleGestureListener=new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            onScale=onScale* detector.getScaleFactor();
            invalidate();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            isScale=true;
            onScaleCenterX=detector.getFocusX();
            onScaleCenterY=detector.getFocusY();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            isScale=false;
        }
    };
    public interface onProvinceClickLisener{
        public void onChose(String provinceName);
    }
}
