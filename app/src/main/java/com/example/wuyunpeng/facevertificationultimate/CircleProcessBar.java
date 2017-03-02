package com.example.wuyunpeng.facevertificationultimate;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by wuyunpeng on 2016/9/12.
 */

//自定义控件的步骤
public class CircleProcessBar extends View {

    private Paint mCirclePaint;// 画实心圆的画笔

    private Paint mRingPaint;// 画圆环的画笔

    private Paint mTextPaint;// 画字体的画笔

    private int mCircleColor;// 圆形颜色

    private int mRingColor;// 圆环颜色

    private float mRadius;// 半径

    private float mRingRadius;// 圆环半径

    private float mStrokeWidth;// 圆环宽度

    private int mXCenter;// 圆心x坐标

    private int mYCenter;// 圆心y坐标

    private float mTxtWidth;// 字的长度

    private float mTxtHeight;// 字的高度

    private int mTotalProgress = 100;// 总进度

    private int mProgress;// 当前进度

    public CircleProcessBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initAttrs(context,attributeSet);//初始化自定义空间的属性
        initVariable();
    }

    private void initAttrs(Context context, AttributeSet attributeSet) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.CircleProcessBar,0,0);//从attrs.xml获得属性
        mRadius = typedArray.getDimension(R.styleable.CircleProcessBar_radius,80);
        mStrokeWidth = typedArray.getDimension(R.styleable.CircleProcessBar_strokeWidth,10);
        mCircleColor = typedArray.getColor(R.styleable.CircleProcessBar_circleColor,0xFFFFFFFF);
        mRingColor = typedArray.getColor(R.styleable.CircleProcessBar_ringColor,0xFF00FF00);
        mRingRadius = mRadius + mStrokeWidth ;//圆环半径
    }

    private void initVariable(){
        //画实心圆
        mCirclePaint = new Paint();//圆形画笔
        mCirclePaint.setAntiAlias(true);//设置抗锯齿特性，如果没有调用这个方法，会不美观
        mCirclePaint.setColor(mCircleColor);
        mCirclePaint.setStyle(Paint.Style.FILL);
        //画圆环
        mRingPaint = new Paint();
        mRingPaint.setAntiAlias(true);
        mRingPaint.setColor(mRingColor);
        mRingPaint.setStyle(Paint.Style.STROKE);
        mRingPaint.setStrokeWidth(mStrokeWidth);
       //画字体
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setARGB(255, 0, 255, 0);
        mTextPaint.setTextSize(mRadius );
        //Android获取字体高度和设置行高
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        mTxtHeight = (int) Math.ceil(fm.descent - fm.ascent);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mXCenter = getWidth()/2;
        mYCenter = getHeight()/2;

        canvas.drawCircle(mXCenter,mYCenter,mRadius,mCirclePaint);

        if (mProgress > 0){
            RectF oval = new RectF();
            oval.left = mXCenter - mRingRadius;
            oval.top = mYCenter - mRingRadius;
            oval.right = mRingRadius * 2 + mXCenter - mRingRadius;
            oval.bottom = mRingRadius * 2 + mYCenter - mRingRadius;
            canvas.drawArc(oval,-90,((float)mProgress/mTotalProgress) * 360,false,mRingPaint);
            String txt = mProgress + "%";
            mTxtWidth = mTextPaint.measureText(txt, 0, txt.length());//获取字体宽度
            canvas.drawText(txt, mXCenter - mTxtWidth / 2, mYCenter + mTxtHeight / 4, mTextPaint);
        }
    }


    public void setProgress(int progress){
        mProgress = progress;
        postInvalidate();//使用这个函数，可以在非主线程中更新ui
    }
}