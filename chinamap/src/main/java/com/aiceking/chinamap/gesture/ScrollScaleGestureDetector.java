package com.aiceking.chinamap.gesture;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**打造一个万能的缩放平移手势帮助类*/
public class ScrollScaleGestureDetector {
    private float beforeLength ,afterLength ;	// 两触点距离
    private float downX ;	//单触点x坐标
    private float downY ;	//单触点y坐标
    private float onMoveDownY ;	//移动的前一个Y坐标
    private float onMoveDownX ;	//移动的前一个X坐标
    private float scale_temp; //缩放比例
    private float downMidX,downMidY;  //缩放的中心位置坐标
    private float offX,offY;          //单指滑动的XY距离
    //模式 NONE：无 MOVE：移动 ZOOM:缩放
    private   int NONE ;
    private   int MOVE ;
    private   int ZOOM ;
    private int mode = NONE;
    private int borderLeft;
    private int borderRight;
    private int borderTop;
    private int borderBottom;
    private int scaleMax;
    private int scalemin;
    private View view;
    private Matrix myMatrix;    //用来完成缩放
    private final float[] matrixValues;
    private OnScrollScaleGestureListener onScrollScaleGestureListener;
    public ScrollScaleGestureDetector(View view,OnScrollScaleGestureListener onScrollScaleGestureListener){
        this.view=view;
        NONE=0;//无
        MOVE=1;//移动
        ZOOM=2;//缩放
        mode=NONE;// 默认模式
        scale_temp=1;//默认缩放比例
        myMatrix=new Matrix();
        matrixValues=new float[9]; //存放矩阵的9和值
        this.onScrollScaleGestureListener=onScrollScaleGestureListener;
    }
    public void setBorderLeft(int borderLeft) {
        this.borderLeft = borderLeft;
    }

    public void setBorderRight(int borderRight) {
        this.borderRight = borderRight;
    }

    public void setBorderTop(int borderTop) {
        this.borderTop = borderTop;
    }

    public void setBorderBottom(int borderBottom) {
        this.borderBottom = borderBottom;
    }

    public void setScaleMax(int scaleMax) {
        if (scaleMax<=1){
            this.scaleMax=1;
        }else{
            this.scaleMax=scaleMax;
        }
    }

    public void setScalemin(int scalemin) {
        if (scalemin<=0){
            this.scalemin=0;
        }else{
            this.scalemin=scalemin;
        }
    }

    public void connect(Canvas canvas) {
        canvas.concat(myMatrix);
    }

    public interface OnScrollScaleGestureListener{
        void onClick(float x,float y);
    }
    //单触点操作
    private void onTouchDown(MotionEvent event) {
        //触电数为1，即单点操作
        if(event.getPointerCount()==1){
            mode = MOVE;
            downX = event.getX();
            downY = event.getY();
            onMoveDownX=event.getX();
            onMoveDownY=event.getY();
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
    //滑动
    private void onTouchMove(MotionEvent event) {
        //双指缩放操作
        if (mode == ZOOM) {
            afterLength = getDistance(event);// 获取两点的距离
            float gapLength = afterLength - beforeLength;// 变化的长度
            if (Math.abs(gapLength)>10&&beforeLength!=0){
                if (gapLength>0){
                    if (scaleMax!=0) {
                        if (getScale() < scaleMax) {
                            scale_temp = afterLength / beforeLength;
                        } else {
                            scale_temp = scaleMax / getScale();
                        }
                    }else {
                        scale_temp = afterLength / beforeLength;
                    }
                }else{
                    if (scalemin!=0){
                    if (getScale()>scalemin){
                        scale_temp=afterLength/beforeLength;
                    }else{
                        scale_temp = scalemin / getScale();
                    }
                    }else {
                        scale_temp=afterLength/beforeLength;
                    }
                }
                //设置缩放比例和缩放中心
                myMatrix.postScale(scale_temp, scale_temp, downMidX, downMidY);
                RectF rectF=getMatrixRectF();
                if (rectF.left>=view.getWidth()/2){
                    myMatrix.postTranslate(view.getWidth()/2-rectF.left,0);
                }
                if (rectF.right<=view.getWidth()/2){
                    myMatrix.postTranslate(view.getWidth()/2-rectF.right,0);
                }
                if (rectF.top>=view.getHeight()/2){
                    myMatrix.postTranslate(0,view.getHeight()/2-rectF.top);
                }
                if (rectF.bottom<=view.getHeight()/2){
                    myMatrix.postTranslate(0,view.getHeight()/2-rectF.bottom);
                }

                view.invalidate();
                beforeLength = afterLength;
            }
        }
        //单指拖动操作
        else if(mode == MOVE){
            // 计算实际距离
            offX = event.getX() - onMoveDownX;//X轴移动距离
            offY = event.getY() - onMoveDownY;//y轴移动距离
            RectF rectF=getMatrixRectF();
            if (rectF.left+offX>=view.getWidth()/2){
                offX=view.getWidth()/2-rectF.left;
            }
            if (rectF.right+offX<=view.getWidth()/2){
                offX=view.getWidth()/2-rectF.right;
            }
            if (rectF.top+offY>=view.getHeight()/2){
                offY=view.getHeight()/2-rectF.top;
            }
            if (rectF.bottom+offY<=view.getHeight()/2){
                offY=view.getHeight()/2-rectF.bottom;
            }
            myMatrix.postTranslate(offX,offY);
            view.invalidate();
            onMoveDownX=event.getX();
            onMoveDownY=event.getY();
        }
    }
    public boolean onTouchEvent(MotionEvent event){
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
                mode = NONE;
                if (Math.abs(event.getX()-downX)<10&&Math.abs(event.getY()-downY)<10){
                    if (onScrollScaleGestureListener!=null){
                        RectF rectF=getMatrixRectF();
                        PointF pf=new PointF((event.getX() -rectF.left)/getScale()
                                ,(event.getY() -rectF.top)/getScale());
                        onScrollScaleGestureListener.onClick(pf.x,pf.y);
                    }
                }
                break;
            // 多点松开
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
        }
        return true;
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
     * 根据当前图片的Matrix获得图片的范围
     */
    private RectF getMatrixRectF() {
        Matrix matrix = myMatrix;
        RectF rect = new RectF();
        rect.set(0, 0, view.getWidth(), view.getHeight());
        matrix.mapRect(rect);
        return rect;
    }
}
