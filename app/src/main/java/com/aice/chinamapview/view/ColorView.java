package com.aice.chinamapview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.aice.chinamapview.model.MycolorArea;
import java.util.List;
/**
 * Created by Vmmet on 2016/9/29.
 */
public class ColorView extends View {
    private Paint colorPaint;
    private List<MycolorArea> list;
    public ColorView(Context context) {
        super(context);
    }

    public ColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        colorPaint=new Paint();
        colorPaint.setAntiAlias(true);
        colorPaint.setTextAlign(Paint.Align.CENTER);
    }

    public ColorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
public void setList(List<MycolorArea> list){
    this.list=list;
    invalidate();
}
    @Override
    protected void onDraw(Canvas canvas) {
        if (list==null)return;
        if (list.size()>0){
            int width_average=getWidth()/list.size();
            for (int i=0;i<list.size();i++){
                colorPaint.setColor(list.get(i).getColor());
                canvas.drawRect(i * width_average, 0, (i + 1) * width_average, getHeight() / 3, colorPaint);
                colorPaint.setColor(Color.BLACK);
                colorPaint.setTextSize(getHeight()/3);
                canvas.drawText(list.get(i).getText(),width_average/2+i * width_average,getHeight()/3*5/2,colorPaint);
            }
        }
        super.onDraw(canvas);
    }
}
