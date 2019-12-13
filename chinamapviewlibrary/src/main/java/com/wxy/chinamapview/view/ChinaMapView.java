package com.wxy.chinamapview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.wxy.chinamapview.gesture.MyGestureDector;
import com.wxy.chinamapview.gesture.MyScaleGestureDetector;
import com.wxy.chinamapview.model.ChinaMapModel;
import com.wxy.chinamapview.model.ProvinceModel;
import com.wxy.chinamapview.util.ChinaMapSvgUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class ChinaMapView extends View {
    private Paint innerPaint,outerPaint;//画省份的内部画笔和外圈画笔
    private boolean isFirst; //是否是第一次绘制,用于最初的适配
    private ChinaMapModel chinaMapModel;
    private float[] matrixValues = new float[9];
    private int viewWidth;
    private int viewHeight;
    private int mapWidth,mapHeight;//map初始宽度和高度
    private   int DEFUALT_VIEW_WIDTH;//设置默认的宽
    private float map_scale;//初始适配缩放值
    private int selectPosition=-1;
    private int scaleMax=2;//缩放的最大倍数
    private int scaleMin=1;//缩放的最小倍数
    private Matrix myMatrix;    //用来完成缩放
    private String isRestore;
    private float translateXRatio,translateYRatio;
    private onProvinceClickLisener onProvinceClickLisener;//省份点击回调
    private MyGestureDector myGestureDector;//拖动惯性滑动点击帮助类
    private MyScaleGestureDetector scaleGestureDetector;
    private onPromiseParentTouchListener onPromiseParentTouchListener;//是否允许父控件拦截事件
    public void setEnableTouch(boolean enableTouch) {
        this.enableTouch = enableTouch;
    }

    private boolean enableTouch;
    public void setOnPromiseParentTouchListener(ChinaMapView.onPromiseParentTouchListener onPromiseParentTouchListener) {
        this.onPromiseParentTouchListener = onPromiseParentTouchListener;
        myGestureDector.setOnPromiseParentTouchListener(onPromiseParentTouchListener);
    }

    public void setOnProvinceClickLisener(ChinaMapView.onProvinceClickLisener onProvinceClickLisener) {
        this.onProvinceClickLisener = onProvinceClickLisener;
    }
    public ChinaMapModel getChinaMapModel() {
        return chinaMapModel;
    }
    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putBoolean("isFirst", isFirst);
        bundle.putString("isRestore", "isRestore");
        bundle.putInt("selectPosition", selectPosition);
        myMatrix.getValues(matrixValues);
        bundle.putFloatArray("matrixValues",matrixValues);
        bundle.putFloat("translateXRatio",matrixValues[Matrix.MTRANS_X]/getWidth());
        bundle.putFloat("translateYRatio",matrixValues[Matrix.MTRANS_Y]/getHeight());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            isFirst=bundle.getBoolean("isFirst");
            isRestore=bundle.getString("isRestore");
            selectPosition=bundle.getInt("selectPosition");
            matrixValues=bundle.getFloatArray("matrixValues");
             translateXRatio=bundle.getFloat("translateXRatio");
             translateYRatio=bundle.getFloat("translateYRatio");
            state = bundle.getParcelable("superState");
            super.onRestoreInstanceState(state);
        }
    }

    public void notifyDataChanged(){
        invalidate();
    }
    public ChinaMapView(Context context) {
        this(context,null);
    }

    public ChinaMapView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ChinaMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //拿到SVG文件，解析成对象
        chinaMapModel = new ChinaMapSvgUtil(context).getProvinces();
        isFirst=true;
        enableTouch=true;
        //初始化省份内部画笔
        innerPaint=new Paint();
        innerPaint.setAntiAlias(true);
        //初始化省份外框画笔
        outerPaint=new Paint();
        outerPaint.setAntiAlias(true);
        outerPaint.setStrokeWidth(1);
        outerPaint.setStyle(Paint.Style.STROKE);
        //wrap宽度为屏幕宽度
        DEFUALT_VIEW_WIDTH=context.getResources().getDisplayMetrics().widthPixels;
        //初始化手势帮助类
        myMatrix=new Matrix();
        scaleGestureDetector=new MyScaleGestureDetector(context,new MyScaleGestureDetector.OnScaleGestureListener(){
            @Override
            public boolean onScale(MyScaleGestureDetector detector) {
                float scale = getScale();
                float yFactor = detector.getScaleFactor();
                        if (yFactor * scale < scaleMin){
                            yFactor = scaleMin / scale;}
                        if (yFactor * scale > scaleMax){
                            yFactor = scaleMax / scale;}
                myMatrix.postScale(yFactor, yFactor, detector.getFocusX(), detector.getFocusY());
                resetTranslate();
                invalidate();
                return true;
            }
            @Override
            public boolean onScaleBegin(MyScaleGestureDetector detector) {
                getParent().requestDisallowInterceptTouchEvent(true);
                if (onPromiseParentTouchListener!=null){
                    onPromiseParentTouchListener.onPromiseTouch(false);
                }
                return true;
            }
            @Override
            public void onScaleEnd(MyScaleGestureDetector detector) {
                if (onPromiseParentTouchListener!=null){
                    onPromiseParentTouchListener.onPromiseTouch(true);
                }
            }
        });
        myGestureDector=new MyGestureDector(context,this, myMatrix, new MyGestureDector.OnGestureClickListener() {
            @Override
            public void onClick(int x, int y) {
                //只有点击在某一个省份内才会触发省份选择接口
                for (ProvinceModel p:chinaMapModel.getProvinceslist()){
                    for (Region region:p.getRegionList()){
                        if (region.contains((int)x, (int)y)){
                            //重置上一次选中省份的状态
                            if (selectPosition!=-1){
                            chinaMapModel.getProvinceslist().get(selectPosition).setSelect(false);
                            }
                            //设置新的选中的省份
                            p.setSelect(true);
                            invalidate();
                            return;

                        }
                    }
                }
            }
        });
    }
    //重置因缩放导致的边界越界
    private void resetTranslate() {
        if (mapHeight!=0&&mapWidth!=0&&viewHeight!=0&&viewWidth!=0){
            RectF rectF=getMatrixRectF();
            //其实，因为精度问题，只会有这两种情况，(float)viewWidth/(float)viewHeight和(float)mapWidth/(float)mapHeight始终不相等。
            //即view的长宽比和map缩放后的长宽比基本一致的情况，按照下面任何一种情况都可以处理
            // (float)viewWidth/(float)viewHeight+"="+(float)mapWidth/(float)mapHeight);
            float dx=0,dy=0;
            if ((float)viewWidth/(float)viewHeight<(float)mapWidth/(float)mapHeight){
                if (rectF.left>=viewWidth/2){
                    dx=viewWidth/2-rectF.left;
                }
                if (rectF.right<=viewWidth/2){
                    dx=viewWidth/2-rectF.right;
                }
                if (rectF.bottom-(viewHeight-mapHeight)*getScale()<=mapHeight/2){
                    dy=mapHeight/2-(rectF.bottom-(viewHeight-mapHeight)*getScale());
                }
                if (rectF.top>=viewHeight-mapHeight/2){
                    dy=viewHeight-mapHeight/2-rectF.top;
                }
            }else  {
                if (rectF.top>=viewHeight/2){
                    dy=viewHeight/2-rectF.top;
                }
                if (rectF.bottom<=viewHeight/2){
                    dy=viewHeight/2-rectF.bottom;
                }
                if (rectF.left>=viewWidth-mapWidth/2){
                    dx=viewWidth-mapWidth/2-rectF.left;
                }
                if (rectF.right-(viewWidth-mapWidth)*getScale()<=mapWidth/2){
                    dx=mapWidth/2-(rectF.right-(viewWidth-mapWidth)*getScale());
                }
            }
            myMatrix.postTranslate(dx,dy);
        }
    }

    //设置最大缩放倍数，最小为1
    public void setScaleMax(int scaleMax) {
        if (scaleMax<=1){
            this.scaleMax=1;
        }else{
            this.scaleMax=scaleMax;
        }
    }
    //设置最小缩放倍数，最小为0
    public void setScaleMin(int scaleMin) {
        if (scaleMin<=0){
            this.scaleMin=0;
        }else{
            this.scaleMin=scaleMin;
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (chinaMapModel==null){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }else {
            int width=0,height=0;
            int width_specMode= MeasureSpec.getMode(widthMeasureSpec);
            int height_specMode= MeasureSpec.getMode(heightMeasureSpec);
            switch (width_specMode){
                //宽度精确值
                case MeasureSpec.EXACTLY:
                        map_scale=(float) MeasureSpec.getSize(widthMeasureSpec)/(chinaMapModel.getMaxX()-chinaMapModel.getMinX());
                    switch (height_specMode){
                        //高度精确值
                        case MeasureSpec.EXACTLY:
                            width= MeasureSpec.getSize(widthMeasureSpec);
                            height= MeasureSpec.getSize(heightMeasureSpec);
                            break;
                        case MeasureSpec.AT_MOST:
                        case MeasureSpec.UNSPECIFIED:
                            width= MeasureSpec.getSize(widthMeasureSpec);
                            if ( getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec)!=0) {
                                height = Math.min((int) ((chinaMapModel.getMaxY() - chinaMapModel.getMinY()) * map_scale), getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
                            }else {
                                height =(int) ((chinaMapModel.getMaxY() - chinaMapModel.getMinY()) * map_scale);
                            }
                            break;
                    }
                    break;
                //宽度wrap_content
                case MeasureSpec.AT_MOST:
                case MeasureSpec.UNSPECIFIED:
                    switch (height_specMode){
                        //高度精确值
                        case MeasureSpec.EXACTLY:
                            map_scale=(float)MeasureSpec.getSize(heightMeasureSpec)/(chinaMapModel.getMaxY()-chinaMapModel.getMinY());
                            height= MeasureSpec.getSize(heightMeasureSpec);
                            if (getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec)!=0) {
                                width = Math.min((int) ((chinaMapModel.getMaxX() - chinaMapModel.getMinX()) * map_scale), getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec));
                            }else {
                                width =(int) (chinaMapModel.getMaxX() - chinaMapModel.getMinX());
                            }
                            break;
                        case MeasureSpec.AT_MOST:
                        case MeasureSpec.UNSPECIFIED:
                            width=DEFUALT_VIEW_WIDTH;
                            map_scale=width/(chinaMapModel.getMaxX()-chinaMapModel.getMinX());
                            height= (int) ((chinaMapModel.getMaxY()-chinaMapModel.getMinY())*map_scale);
                            break;
                    }
                    break;
            }

            setMeasuredDimension(width,height);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (chinaMapModel==null)return;
        //保证只在初次绘制的时候进行缩放适配
        if (isFirst){
            viewWidth=getWidth();
            viewHeight=getHeight();
            //提供给滑动帮助类，以便控制滑动边界
            myGestureDector.setViewWidthAndHeight(viewWidth,viewHeight);
            //首先重置所有点的坐标，使得map适应屏幕大小
            if ((float)viewWidth/(float)viewHeight>(chinaMapModel.getMaxX()-chinaMapModel.getMinX())/(chinaMapModel.getMaxY()-
                    chinaMapModel.getMinY())){
                map_scale=viewHeight/(chinaMapModel.getMaxY()-chinaMapModel.getMinY());
            }else {
                map_scale=viewWidth/(chinaMapModel.getMaxX()-chinaMapModel.getMinX());
            }
            //缩放所有Path
            scalePoints(canvas,map_scale);
            isFirst=false;

        }else {
            //恢复状态至屏幕旋转之前
            if (!TextUtils.isEmpty(isRestore)){
                viewWidth=getWidth();
                viewHeight=getHeight();
                //提供给滑动帮助类，以便控制滑动边界
                myGestureDector.setViewWidthAndHeight(viewWidth,viewHeight);
                //首先重置所有点的坐标，使得map适应屏幕大小
                if ((float)viewWidth/(float)viewHeight>(chinaMapModel.getMaxX()-chinaMapModel.getMinX())/(chinaMapModel.getMaxY()-
                        chinaMapModel.getMinY())){
                    map_scale=viewHeight/(chinaMapModel.getMaxY()-chinaMapModel.getMinY());
                }else {
                    map_scale=viewWidth/(chinaMapModel.getMaxX()-chinaMapModel.getMinX());
                }
                //缩放所有Path
                scalePoints(null,map_scale);
                matrixValues[Matrix.MTRANS_X]=getWidth()*translateXRatio;
                matrixValues[Matrix.MTRANS_Y]=getHeight()*translateYRatio;
                myMatrix.setValues(matrixValues);
                isRestore="";
            }
            //关联缩放和平移后的矩阵
            canvas.concat(myMatrix);
            drawMap(canvas);
        }
    }

    //用于事件拦截,是否消费事件
    public boolean consumeEvent(MotionEvent event){
        boolean consume=false;
        RectF rectF=getMatrixRectF();
        //把坐标换算到初始坐标系，用于判断点击坐标是否在某个省份内
        PointF pf=new PointF((event.getX() -rectF.left)/getScale()
                ,(event.getY() -rectF.top)/getScale());
        for (ProvinceModel p:chinaMapModel.getProvinceslist()){
            for (Region region:p.getRegionList()){
                if (region.contains((int)pf.x, (int)pf.y)){
                    consume=true;
                    break;
                }
            }
        }
        return consume;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (enableTouch){
         scaleGestureDetector.onTouchEvent(event);
        if (!scaleGestureDetector.isInProgress()) {
            myGestureDector.onTouchEvent(event);
        }
        return true;
        }else {
            return super.onTouchEvent(event);
        }
    }



    private void drawMap(Canvas canvas) {
        if (chinaMapModel.getProvinceslist().size()>0){
            outerPaint.setStrokeWidth(1);
            //首先记录下点击的省份的下标，先把其他的省份绘制完，
            for (int i=0;i<chinaMapModel.getProvinceslist().size();i++){
                if (chinaMapModel.getProvinceslist().get(i).isSelect()){
                    selectPosition=i;
                }else{
                    //此时绘制其他省份，边框画笔的宽度为1
                    innerPaint.setColor(chinaMapModel.getProvinceslist().get(i).getColor());
                    outerPaint.setColor(chinaMapModel.getProvinceslist().get(i).getNormalBordercolor());
                    for (Path p:chinaMapModel.getProvinceslist().get(i).getListpath()){
                        canvas.drawPath(p, innerPaint);
                        canvas.drawPath(p, outerPaint);
                    }
                }
            }
            if (selectPosition!=-1){
                //暴露到Activity中的接口，把省的名字传过去
                if (onProvinceClickLisener!=null){
                    onProvinceClickLisener.onSelectProvince(chinaMapModel.getProvinceslist().get(selectPosition).getName());
                }
            //再绘制点击所在的省份,此时画笔宽度设为2.5，以达到着重显示的效果
            innerPaint.setColor(chinaMapModel.getProvinceslist().get(selectPosition).getColor());
            outerPaint.setColor(chinaMapModel.getProvinceslist().get(selectPosition).getSelectBordercolor());
            outerPaint.setStrokeWidth(2.5f);
            for (Path p:chinaMapModel.getProvinceslist().get(selectPosition).getListpath()){
                canvas.drawPath(p, innerPaint);
                canvas.drawPath(p, outerPaint);
            }
        }
        }
    }
    //第一次绘制，缩小map到View指定大小
    private void scalePoints(Canvas canvas,float scale) {
        if (chinaMapModel.getProvinceslist().size()>0)
            //map的左右上下4个临界点
            chinaMapModel.setMaxX(chinaMapModel.getMaxX()*scale);
        chinaMapModel.setMinX(chinaMapModel.getMinX()*scale);
        chinaMapModel.setMaxY(chinaMapModel.getMaxY()*scale);
        chinaMapModel.setMinY(chinaMapModel.getMinY()*scale);
        //为滑动手势设置边界
        mapWidth=(int)( chinaMapModel.getMaxX()-chinaMapModel.getMinX());
        mapHeight=(int)(chinaMapModel.getMaxY()-chinaMapModel.getMinY());
        myGestureDector.setMapWidthAndHeight(mapWidth,mapHeight);
        //重设所有点
        for (ProvinceModel province:chinaMapModel.getProvinceslist()){
            innerPaint.setColor(province.getColor());
            List<Region> regionList=new ArrayList<>();
            List<Path> pathList=new ArrayList<>();
            innerPaint.setColor(province.getColor());
            outerPaint.setColor(province.getNormalBordercolor());
            for (Path p:province.getListpath()){
                //遍历Path中的所有点，重置点的坐标
                Path newpath=resetPath(p, scale, regionList);
                pathList.add(newpath);
                if (canvas!=null){
                canvas.drawPath(newpath,innerPaint);
                canvas.drawPath(newpath,outerPaint);
                }
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
            PointF p=new PointF((int)s[0]*scale,(int)s[1]*scale);
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
         void onSelectProvince(String provinceName);
    }
    public interface onPromiseParentTouchListener{
        void onPromiseTouch(boolean promise);
    }
    private   float getScale() {
        myMatrix.getValues(matrixValues);
        if (matrixValues[Matrix.MSCALE_X]==0){
            return 1;
        }else{
            return matrixValues[Matrix.MSCALE_X];
        }
    }
    private RectF getMatrixRectF() {
        RectF rect = new RectF();
        rect.set(0, 0, viewWidth, viewHeight);
        myMatrix.mapRect(rect);
        return rect;
    }
}
