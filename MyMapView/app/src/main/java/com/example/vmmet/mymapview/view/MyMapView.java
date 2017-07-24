package com.example.vmmet.mymapview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.vmmet.mymapview.bean.Lasso;
import com.example.vmmet.mymapview.bean.MyMap;
import com.example.vmmet.mymapview.bean.Province;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vmmet on 2016/8/4.
 */
public class MyMapView extends View{
    //模式 NONE：无 MOVE：移动 ZOOM:缩放
    private static final int NONE = 0;
    private static final int MOVE = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;// 默认模式
    private float beforeLength = 0,afterLength = 0;	// 两触点距离
    private float downX = 0;	//单触点x坐标
    private float downY = 0;	//单触点y坐标
    private float scale_temp=1; //缩放比例
    private float downMidX=0,downMidY=0;  //缩放的中心位置坐标
    private static float scale_max = 3;	//scale的最大值
    private  static float scale_min = 1;	//scale的最小值
    private float OnMoveX=0,OnMoveY=0;          //正在单指滑动的总XY距离
    private float UpMoveX=0,UpMoveY=0;          //完成单指滑动的总XY距离
    private float offX=0,offY=0;                //单指滑动的XY距离
    private float Width=0,Height=0;             //View的宽度和高度
    private Paint paint,linepaint;
    private Matrix myMatrix;                    //用来完成缩放
    private boolean isFirst;               //只在第一次传数据绘图时加载
    private String provincename="";
    private boolean criticalflag;          //拖拽的临界值标志位
    /**
     * 用于存放矩阵的9个值
     */
    private final float[] matrixValues = new float[9];
    private MyMap map;
    private float map_scale=0;
    private onProvinceClickLisener onProvinceClickLisener;
    public MyMapView(Context context) {
        super(context);
    }
    public void setOnChoseProvince(onProvinceClickLisener lisener){
        this.onProvinceClickLisener=lisener;
    }
    public MyMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint=new Paint();
        paint.setColor(Color.BLUE);
        paint.setAntiAlias(true);
        linepaint=new Paint();
        linepaint.setColor(Color.GRAY);
        linepaint.setAntiAlias(true);
        linepaint.setStrokeWidth(1);
        linepaint.setStyle(Paint.Style.STROKE);
        myMatrix=new Matrix();
    }
    public MyMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width=MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, (int) map.getMax_y());
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
    public void setMap(MyMap map){
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
        Width=getWidth();
        Height=getHeight();
        //首先重置所有点的坐标，使得map适应屏幕大小
            if (map!=null){
                map_scale=Width/map.getMax_x();
            }
            scalePoints(canvas,map_scale);
            isFirst=false;
        }else{
        canvas.concat(myMatrix);
        canvas.translate(OnMoveX, OnMoveY);
        drawMap(canvas);
        }
        super.onDraw(canvas);
    }
    private void drawMap(Canvas canvas) {
        //linepaint.setStrokeWidth(1/getScale());
        if (map.getProvinceslist().size()>0){
            int b=0;
            for (int i=0;i<map.getProvinceslist().size();i++){
                if (map.getProvinceslist().get(i).getLinecolor()==Color.BLACK){
                    b=i;
                }else{
                    paint.setColor(map.getProvinceslist().get(i).getColor());
                    linepaint.setColor(map.getProvinceslist().get(i).getLinecolor());
                    for (Path p:map.getProvinceslist().get(i).getListpath()){
                        canvas.drawPath(p, paint);
                        canvas.drawPath(p, linepaint);
                    }
                }
            }
            paint.setColor(map.getProvinceslist().get(b).getColor());
            linepaint.setColor(map.getProvinceslist().get(b).getLinecolor());
            for (Path p:map.getProvinceslist().get(b).getListpath()){
                canvas.drawPath(p, paint);
                canvas.drawPath(p, linepaint);
            }
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    onTouchDown(event);
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    // 多点触摸
                    onPointerDown(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                     onTouchMove(event);
                    break;
                case MotionEvent.ACTION_UP:
                   // 必须是单指移动
                    if (mode==MOVE){
                    offX = (event.getX() - downX)/getScale();//X轴移动距离
                    offY = (event.getY() - downY)/getScale();//y轴移动距离
                     if (!criticalflag)  {
                    UpMoveX=UpMoveX+offX;
                    UpMoveY=UpMoveY+offY;}
                    }
                    mode = NONE;
                    if (Math.abs(event.getX()-downX)<10&&Math.abs(event.getY()-downY)<10){
                        RectF rectF=getMatrixRectF();
                        //判断点的是哪个省，然后相应的省变颜色
                        changeProvinceColor(event,rectF);
                    }
                    break;
                // 多点松开
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
        }
        return true;
    }
    //滑动
    private void onTouchMove(MotionEvent event) {
        //双指缩放操作
        if (mode == ZOOM) {
            afterLength = getDistance(event);// 获取两点的距离
            float gapLength = afterLength - beforeLength;// 变化的长度
            if (Math.abs(gapLength)>10&&beforeLength!=0){
                if (gapLength>0){
                if (getScale()<scale_max){
                    scale_temp=afterLength/beforeLength;
                }else{
                    scale_temp = scale_max / getScale();
                }}else{
                    if (getScale()>scale_min){
                        scale_temp=afterLength/beforeLength;
                    }else{
                        scale_temp = scale_min / getScale();
                    }
                }
                //设置缩放比例和缩放中心
                myMatrix.postScale(scale_temp, scale_temp, downMidX, downMidY);
                invalidate();
                beforeLength = afterLength;
            }
        }
        //单指拖动操作
        else if(mode == MOVE){
            // 计算实际距离
             offX = (event.getX() - downX)/getScale();//X轴移动距离
             offY = (event.getY() - downY)/getScale();//y轴移动距离
            OnMoveX=UpMoveX+offX;
            OnMoveY=UpMoveY+offY;
            RectF rectF=getMatrixRectF();
            if (rectF.left+OnMoveX*getScale()>=Width/2){
                UpMoveX=(Width/2-rectF.left)/getScale();
                criticalflag=true;
            }else if (rectF.right+OnMoveX*getScale()<=Width/2){
                UpMoveX=(Width/2-rectF.right)/getScale();
                criticalflag=true;
            }
            else if (rectF.top+OnMoveY*getScale()>=Height/2){
                UpMoveY=(Height/2-rectF.top)/getScale();
                criticalflag=true;
            }
            else if (rectF.bottom+OnMoveY*getScale()<=Height/2){
                UpMoveY=(Height/2-rectF.bottom)/getScale();
                criticalflag=true;
            }else{
                criticalflag=false;
                invalidate();
            }
        }
    }
    //单触点操作
    private void onTouchDown(MotionEvent event) {
        //触电数为1，即单点操作
        if(event.getPointerCount()==1){
            mode = MOVE;
            downX = event.getX();
            downY = event.getY();
        }
    }
    //多触点操作
    private void onPointerDown(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            mode = ZOOM;
            beforeLength = getDistance(event);
            downMidX = getMiddleX(event);
            downMidY=getMiddleY(event);
        }
    }
    // 获取两点的距离
    private float getDistance(MotionEvent event){
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }
    private float getMiddleX(MotionEvent event){
        return (event.getX(1)+event.getX(0))/2;
    }
    private float getMiddleY(MotionEvent event){
        return (event.getY(1)+event.getY(0))/2;
    }
    /**
     * 获得当前的缩放比例
     *
     * @return
     */
    public final float getScale() {
        myMatrix.getValues(matrixValues);
        if (matrixValues[Matrix.MSCALE_X]==0){
            return 1;
        }else{
        return matrixValues[Matrix.MSCALE_X];
        }
    }
    /**
     * 一个坐标点，以某个点为缩放中心，缩放指定倍数，求这个坐标点在缩放后的新坐标值。
     * @param targetPointX 坐标点的X
     * @param targetPointY 坐标点的Y
     * @param scaleCenterX 缩放中心的X
     * @param scaleCenterY 缩放中心的Y
     * @param scale 缩放倍数
     * @return 坐标点的新坐标
     * ********************************这里不适用，所以没有用到************************
     */
    private PointF scaleByPoint(float targetPointX,float targetPointY,float scaleCenterX,float scaleCenterY,float scale){
//        Matrix matrix = new Matrix();
//        // 然后再以某个点为中心进行缩放
//        matrix.preTranslate(targetPointX,targetPointY);
//        matrix.postScale(scale,scale,scaleCenterX,scaleCenterY);
//        float[] values = new float[9];
//        matrix.getValues(values);
//        return new PointF(values[Matrix.MTRANS_X],values[Matrix.MTRANS_Y]);
        //因为矩阵计算会导致卡顿，所以使用以下方式
//         设宽高的缩放比例都为s,缩放中心为（x0,y0）缩放之前的（x,y）
//         将变成(  x0-(x0-x)*s,  y0-(y0-y)*s  )
        return new PointF(scaleCenterX-(scaleCenterX-targetPointX)*scale,
                scaleCenterY-(scaleCenterY-targetPointY)*scale);
    }
    //第一次绘制，缩小map到View指定大小
    private void scalePoints(Canvas canvas,float scale) {
        if (map.getProvinceslist().size()>0)
            //map的左右上下4个临界点
            map.setMax_x(map.getMax_x()*scale);
        map.setMin_x(map.getMin_x()*scale);
        map.setMax_y(map.getMax_y()*scale);
        map.setMin_y(map.getMin_y()*scale);
            for (Province province:map.getProvinceslist()){
                paint.setColor(province.getColor());
                List<Lasso> listLasso=new ArrayList<>();
                List<Path> pathList=new ArrayList<>();
                for (Path p:province.getListpath()){
                    //遍历Path中的所有点，重置点的坐标
                    Path newpath=resetPath(p, scale, listLasso);
                    pathList.add(newpath);
                    canvas.drawPath(newpath,paint);
                    canvas.drawPath(newpath,linepaint);
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
    //判断点的是哪个省，然后相应的省变颜色
    private void changeProvinceColor(MotionEvent event,RectF rectF) {
        for (Province province:map.getProvinceslist()){
            //province.setColor(Color.BLUE);
            province.setLinecolor(Color.GRAY);
        }
        for (Province p:map.getProvinceslist()){
            for (Lasso lasso:p.getPathLasso()){
                PointF pf=new PointF(event.getX() / getScale() - rectF.left / getScale()-OnMoveX
                        ,event.getY() / getScale() - rectF.top/getScale()-OnMoveY);
                if (lasso.contains(pf.x,pf.y)){
                    provincename=p.getName();
                    //p.setColor(Color.RED);
                    p.setLinecolor(Color.BLACK);
                    invalidate();
                    //暴露到Activity中的接口，把省的名字传过去
                    onProvinceClickLisener.onChose(provincename);
                    break;
                }
            }
        }
    }
    /**
     * 根据当前图片的Matrix获得图片的范围
     */
    private RectF getMatrixRectF() {
        Matrix matrix = myMatrix;
        RectF rect = new RectF();
            rect.set(0, 0, Width, Height);
            matrix.mapRect(rect);
        return rect;
    }
    public interface onProvinceClickLisener{
        public void onChose(String provincename);
    }
}
