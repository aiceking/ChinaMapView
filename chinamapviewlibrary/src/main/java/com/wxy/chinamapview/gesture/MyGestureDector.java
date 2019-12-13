package com.wxy.chinamapview.gesture;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import com.wxy.chinamapview.model.ProvinceModel;
import com.wxy.chinamapview.view.ChinaMapView;

public class MyGestureDector {
    private Context context;
    private int mActivePointerId;
    protected float downX=0,downY=0;
    protected float mLastX = 0,mLastY=0;
    private float onMoveDownY ;	//移动的前一个Y坐标
    private float onMoveDownX ;	//移动的前一个X坐标
    private boolean isConsume;
    private float[] matrixValues = new float[9];
    private  final int INVALID_POINTER = -1;
    private Matrix matrix;
    private ChinaMapView view;//持有View用于重绘
    private OnGestureClickListener onGestureClickListener;
    private int mapWidth,mapHeight;//map初始宽度和高度
    private int viewWidth,viewHeight;//map初始宽度和高度
    private ChinaMapView.onPromiseParentTouchListener onPromiseParentTouchListener;

    public void setOnPromiseParentTouchListener(ChinaMapView.onPromiseParentTouchListener onPromiseParentTouchListener) {
        this.onPromiseParentTouchListener = onPromiseParentTouchListener;
    }

    public MyGestureDector(Context context, ChinaMapView view, Matrix matrix, OnGestureClickListener onGestureClickListener){
        this.context=context;
        this.matrix=matrix;
        this.onGestureClickListener=onGestureClickListener;
        this.view=view;
    }
    public void setMapWidthAndHeight(int mapWidth, int mapHeight){
        this.mapWidth=mapWidth;
        this.mapHeight=mapHeight;
    }
    public void setViewWidthAndHeight(int viewWidth, int viewHeight){
        this.viewWidth=viewWidth;
        this.viewHeight=viewHeight;
    }
    public boolean onTouchEvent(MotionEvent event){

        switch (event.getAction()& MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId=event.getPointerId(0);
                downX= event.getX();
                downY= event.getY();
                onMoveDownX= event.getX();
                onMoveDownY=event.getY();
                isConsume=view.consumeEvent(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //如果有新的手指按下，就直接把它当作当前活跃的指针
                final int index = event.getActionIndex();
                mActivePointerId = event.getPointerId(index);
                //并且刷新上一次记录的旧坐标值
                downX= event.getX(index);
                downY= event.getY(index);
                onMoveDownX= event.getX(index);
                onMoveDownY= event.getY(index);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isConsume){
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    if (onPromiseParentTouchListener!=null){
                        onPromiseParentTouchListener.onPromiseTouch(false);
                    }
                }else {
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                    if (onPromiseParentTouchListener!=null){
                        onPromiseParentTouchListener.onPromiseTouch(true);
                    }
                    break;
                }
                    int activePointerIndex = event.findPointerIndex(mActivePointerId);
                    if (activePointerIndex == INVALID_POINTER) {
                        break;
                    }
                    float dx =  event.getX(activePointerIndex) - onMoveDownX;
                float dy =  event.getY(activePointerIndex) - onMoveDownY;
                matrix.getValues(matrixValues);
                if (mapHeight!=0&&mapWidth!=0&&viewHeight!=0&&viewWidth!=0){
                    RectF rectF=getMatrixRectF();
                    //其实，因为精度问题，只会有这两种情况，(float)viewWidth/(float)viewHeight和(float)mapWidth/(float)mapHeight始终不相等。
                    //即view的长宽比和map缩放后的长宽比基本一致的情况，按照下面任何一种情况都可以处理
                    // (float)viewWidth/(float)viewHeight+"="+(float)mapWidth/(float)mapHeight);
                    if ((float)viewWidth/(float)viewHeight<(float)mapWidth/(float)mapHeight){
                        if (rectF.left+dx>=viewWidth/2){
                            dx=viewWidth/2-rectF.left;
                        }
                        if (rectF.right+dx<=viewWidth/2){
                            dx=viewWidth/2-rectF.right;
                        }
                        if (rectF.bottom-(viewHeight-mapHeight)*getScale()+dy<=mapHeight/2){
                            dy=mapHeight/2-(rectF.bottom-(viewHeight-mapHeight)*getScale());
                        }
                        if (rectF.top+dy>=viewHeight-mapHeight/2){
                            dy=viewHeight-mapHeight/2-rectF.top;
                        }
                    }else  {
                        if (rectF.top+dy>=viewHeight/2){
                            dy=viewHeight/2-rectF.top;
                        }
                        if (rectF.bottom+dy<=viewHeight/2){
                            dy=viewHeight/2-rectF.bottom;
                        }
                        if (rectF.left+dx>=viewWidth-mapWidth/2){
                            dx=viewWidth-mapWidth/2-rectF.left;
                        }
                        if (rectF.right-(viewWidth-mapWidth)*getScale()+dx<=mapWidth/2){
                            dx=mapWidth/2-(rectF.right-(viewWidth-mapWidth)*getScale());
                        }
                    }
                }
                matrix.postTranslate(dx,dy);
                    view.invalidate();
                    onMoveDownX=  event.getX(activePointerIndex);
                    onMoveDownY= event.getY(activePointerIndex);
                break;
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER;
                if (onPromiseParentTouchListener!=null){
                    onPromiseParentTouchListener.onPromiseTouch(true);
                }
                break;
            case MotionEvent.ACTION_UP:
                mActivePointerId = INVALID_POINTER;
                float clickX = downX -  event.getX();
                float clickY = downY -  event.getY();
                if (Math.abs(clickX)<= ViewConfiguration.get(context).getScaledTouchSlop()&&
                        Math.abs(clickY)<= ViewConfiguration.get(context).getScaledTouchSlop()){
                    if (onGestureClickListener!=null){
                        RectF rectF=getMatrixRectF();
                        //把坐标换算到初始坐标系，用于判断点击坐标是否在某个省份内
                        PointF pf=new PointF((event.getX() -rectF.left)/getScale()
                                ,(event.getY() -rectF.top)/getScale());
                        onGestureClickListener.onClick((int) pf.x,(int)pf.y);

                    }
                }
                if (onPromiseParentTouchListener!=null){
                    onPromiseParentTouchListener.onPromiseTouch(true);
                }
                break;
        }
        if (mActivePointerId != INVALID_POINTER) {
            mLastX =  event.getX(event.findPointerIndex(mActivePointerId));
            mLastY= event.getY(event.findPointerIndex(mActivePointerId));
        }else {
            mLastX =  event.getX();
            mLastY =  event.getY();
        }

        return true;
    }
    private void onSecondaryPointerUp(MotionEvent ev) {
        int pointerIndex = ev.getActionIndex();
        int pointerId = ev.getPointerId(pointerIndex);
        //如果抬起的那根手指，刚好是当前活跃的手指，那么
        if (pointerId == mActivePointerId) {
            //另选一根手指，并把它标记为活跃（皇帝驾崩，太子登基）
            int newPointerIndex =  pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
            //把上一次记录的坐标，更新为新手指的当前坐标
            downX =  ev.getX(newPointerIndex);
            downY =  ev.getY(newPointerIndex);
            onMoveDownX= ev.getX(newPointerIndex);
            onMoveDownY=  ev.getY(newPointerIndex);
        }

    }

    public interface OnGestureClickListener{
        void onClick(int x,int y);
    }
    private RectF getMatrixRectF() {
        RectF rect = new RectF();
        rect.set(0, 0, viewWidth, viewHeight);
        matrix.mapRect(rect);
        return rect;
    }
    /**
     * 获得当前的缩放比例
     *
     * @return
     */
    public  float getScale() {
        matrix.getValues(matrixValues);
        if (matrixValues[Matrix.MSCALE_X]==0){
            return 1;
        }else{
            return matrixValues[Matrix.MSCALE_X];
        }
    }
}
