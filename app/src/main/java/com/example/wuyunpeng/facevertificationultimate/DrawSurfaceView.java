package com.example.wuyunpeng.facevertificationultimate;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by wuyunpeng on 2016/7/27.
 */

/************************************************************************
 *             绘制自定义相机中间层视图surfaceView类				        *
 ************************************************************************/
public class DrawSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
   //中间层surfaceviewHoldere类私有变量
    private SurfaceHolder mSurfaceHolder = null;

    //构造函数，自定义控件标准构造函数的写法必须是(Context，AttributeSet)
    public DrawSurfaceView(Context context, AttributeSet attributeSet){
        super(context,attributeSet);
        this.setZOrderOnTop(true);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);//设置成透明
    }

    //surfaceView的三个回调函数，一般在surfaceviewChanged中执行绘制的内容
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
         darwPicture();//画出中间层绘制的内容
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    //绘制中间层矩形区域和遮挡区域函数
    public void darwPicture()
    {
        Canvas canvas = mSurfaceHolder.lockCanvas();//获得用来在surfaceview上的绘制的画布
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        Paint p = new Paint();
        p.setAlpha(90);
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.model),null, new Rect(0,0,getWidth(),getHeight()),p);
       /* canvas.drawColor(Color.TRANSPARENT);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStrokeWidth(5);
        p.setColor(Color.RED);
        p.setStyle(Paint.Style.STROKE);
        canvas.drawRect(getWidth()/5,getHeight()/4,getWidth()*4/5,getHeight()*3/4,p);

        p.setColor(Color.GRAY);
        p.setStyle(Paint.Style.FILL);
        p.setAlpha(180);
        canvas.drawRect(0,0,getWidth(),getHeight()/4,p);
        canvas.drawRect(0,getHeight()/4,getWidth()/5,3*getHeight()/4,p);
        canvas.drawRect(4*getWidth()/5,getHeight()/4,getWidth(),3*getHeight()/4,p);
        canvas.drawRect(0,3*getHeight()/4,getWidth(),getHeight(),p);
       */
        mSurfaceHolder.unlockCanvasAndPost(canvas);//完成绘制工作后，调用此函数，绘制内容会显示在surfaceview上
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
