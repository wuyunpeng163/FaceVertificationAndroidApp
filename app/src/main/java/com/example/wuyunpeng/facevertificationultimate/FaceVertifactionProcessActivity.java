package com.example.wuyunpeng.facevertificationultimate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Locale;

/**
 * Created by 12282 on 2016/10/31.
 */
/************************************************************************
 *                        人脸认证系统App认证主界面                     *
 ************************************************************************/
@SuppressWarnings("deprecation")
public class FaceVertifactionProcessActivity extends Activity implements SurfaceHolder.Callback,View.OnClickListener, android.hardware.Camera.PictureCallback{
    private Camera mCamera = null;//相机对象
    private SurfaceView cameraSurfaceView = null;//实时显示相机画面
    private DrawSurfaceView drawSurfaceView = null;//绘制矩形视图类
    private String TAG = "FaceVertificationActivity";
    private CircleImageButton faceVertificationImageButton = null;//自定义圆形按钮
    private FaceVertificationTask faceVertificationTask = null;//人脸认证异步线程任务
    private TextToSpeech mTextToSpeech  = null;//android自带tts功能的类
    private CircleProcessBar circleProcessBar = null;//自定义圆形进度条按钮
    private FaceVertificationClient faceVertificationClient;//用户对象，记录id号和姓名还有检测到的人脸图片用来同服务器socket通信
    private String ServiceIpAddress = "";

   private class  FaceVertificationTask extends AsyncTask<Void,Integer,Integer>{
       private byte[] mData = null;//采集图片像素数据
       private android.hardware.Camera mCamera = null;//
       private String ip = "";
       private Bitmap imageBitMap = null;
       private int processPrecent = 0;
       private double startTime = 0.0;

       //认证情况结果返回码
       private static final int NotFoundFaceReturnCode = 1;
       private static final int NotCapturePhotoReturnCode = 2;
       private static final int VertificationCorrectReturnCode = 3;
       private static final int VertificationErrorReturnCode = 4;
       private static final int NetworkErrorReturnCode = 5;
       private static final int OtherReturnCode = 6;
       private static final int NetworkConnectTimeOutCode = 7;

       FaceVertificationTask(byte[] data, android.hardware.Camera camera,String ipAddress){
           this.mData = data;
           this.mCamera = camera;
           this.ip = ipAddress;
       }

       //一键认证模块
       //后台需要执行的任务有: 0.人脸检测 1.人脸预处理 2.人脸检测 3.人脸标定 4.人脸特征提取
       private int run() {
           int w = imageBitMap.getWidth();
           int h = imageBitMap.getHeight();
           int[] pixels = new int[w*h];
           imageBitMap.getPixels(pixels,0,w,0,0,w,h);//获取采集图片的所有像素
           try {
               //人脸检测模块
               InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
               File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);//获得人脸检测器文件夹
               File mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt2.xml");//人脸检测器文件
               FileOutputStream os = new FileOutputStream(mCascadeFile);//输出流
               byte[] buffer = new byte[4096];
               int bytesRead;
               while ((bytesRead = is.read(buffer)) != -1) {
                   os.write(buffer, 0, bytesRead);
               }
               is.close();
               os.close();
               // Load the cascade classifier
               boolean isFaceFlag = AlogrithmFromCpp.getFaceDetection(pixels,w,h,mCascadeFile.getAbsolutePath());
               if (!isFaceFlag)
                   return NotFoundFaceReturnCode;
           } catch (Exception e) {
               Log.e("OpenCVActivity", "Error loading cascade", e);
           }
           byte[] face = AlogrithmFromCpp.getResizeImage(pixels,w,h);
           faceVertificationClient.setBufMat(face);
           try {
               Socket sendSocket = new Socket(ip, 1234);//"202.114.15.70"
               sendSocket.setSoTimeout(3000);//设置接收数据的最大延迟时间3s
               sendSocket.setKeepAlive(true);//每隔很长一段时间，定时发送心跳包发送到服务器端
               sendSocket.setSendBufferSize(2048);
               processPrecent += 16;
               //进度条百分比更新 70
               publishProgress(processPrecent);
               OutputStream os = sendSocket.getOutputStream();
               InputStream is = sendSocket.getInputStream();
               int count = 0;
               int start = 0;
               int bagNumber =  (47*3+1)/2 ;
               while ( count < bagNumber){
                   os.write(faceVertificationClient.getBuf(),start ,2048);
                   start += 2048;
                   count++;
                   processPrecent += 1;
                   publishProgress(processPrecent);
               }
               //os.write(faceVertificationClient.getBuf());
               is.read(faceVertificationClient.getBuf());
               os.close();
               is.close();
               sendSocket.close();
               if (faceVertificationClient.getBuf()[0] == 49)//49为1的byte类型的ascii码
               {
                   processPrecent = 100;
                   //进度条百分比更新 100
                   publishProgress(processPrecent);
                   return VertificationCorrectReturnCode;
               }
               else {
                   //进度条百分比更新 100
                   processPrecent = 100;
                   publishProgress(processPrecent);
                   return VertificationErrorReturnCode;
               }
           }catch (ConnectException e){
               e.printStackTrace();
               return NetworkConnectTimeOutCode;
           }
           catch (SocketException e) {
               e.printStackTrace();
               return NetworkErrorReturnCode;
           }catch(SocketTimeoutException e){
               e.printStackTrace();
               return  NetworkConnectTimeOutCode;
           } catch (IOException e) {
               e.printStackTrace();
               return NetworkErrorReturnCode;
           } catch (Exception e) {
               e.printStackTrace();
           }
           return OtherReturnCode;
       }

       //异步任务开启后，因为后续处理会耗时，未避免不友好，显示进出度条，体现友好性
       @Override
       protected void onPreExecute() {
           super.onPreExecute();
           circleProcessBar.setVisibility(View.VISIBLE);

       }

       //利用异步处理，在线程中1.人脸预处理 2.人脸检测 3.人脸标定 4.人脸特征提取等四个模块的功能
       @Override
       protected Integer doInBackground(Void... params) {
           startTime = System.nanoTime();
           //进度条百分比更新 0
           publishProgress(processPrecent);
           mCamera.startPreview();
           if (mData.length != 0) {
               imageBitMap = BitmapFactory.decodeByteArray(mData,0,mData.length);
               imageBitMap = FaceVertifactionProcessActivity.rotaingImageView(270, imageBitMap);//由于手机竖直时会旋转270度
               processPrecent += 10;
               publishProgress(processPrecent);
               //进度条百分比更新 10
           }
           else
               return NotCapturePhotoReturnCode;
           //后期改动暂时省略人脸检测模块,人脸预处理模块
           return run();
       }
       //进度条进度更新


       @Override
       protected void onProgressUpdate(Integer... values) {
           super.onProgressUpdate(values);
           circleProcessBar.setProgress(processPrecent);
       }

       //语音播报人脸认证结果功能代替文字
       @Override
       protected void onPostExecute(Integer integer) {
           super.onPostExecute(integer);
           circleProcessBar.setVisibility(View.GONE);
           double consumeTime = System.nanoTime() - startTime;
           System.out.println("认证耗时" + consumeTime / 1000000 + "毫秒");
           switch (integer){
               case NotCapturePhotoReturnCode:
                   mTextToSpeech.speak("未成功采集图片",TextToSpeech.QUEUE_FLUSH,null);
                   // MyToast myToast1 = new MyToast();
                   // myToast1.show(FaceVertificationActivity.this, (ViewGroup) findViewById(R.id.mytoast_layout_root),"未成功采集图片");
                   break;
               case NotFoundFaceReturnCode:
                   mTextToSpeech.speak("未检测到人脸",TextToSpeech.QUEUE_FLUSH,null);
                   //MyToast myToast2 = new MyToast();
                   //myToast2.show(FaceVertificationActivity.this, (ViewGroup) findViewById(R.id.mytoast_layout_root),"未检测到人脸");
                   break;
               case VertificationCorrectReturnCode:
                   mTextToSpeech.speak("认证成功",TextToSpeech.QUEUE_FLUSH,null);
                   //MyToast myToast3 = new MyToast();
                   //myToast3.show(FaceVertificationActivity.this, (ViewGroup) findViewById(R.id.mytoast_layout_root),"认证成功");
                   break;
               case VertificationErrorReturnCode:
                   mTextToSpeech.speak("认证失败",TextToSpeech.QUEUE_FLUSH,null);
                   //MyToast myToast4 = new MyToast();
                   //myToast4.show(FaceVertificationActivity.this, (ViewGroup) findViewById(R.id.mytoast_layout_root),"认证失败");
                   break;
               case NetworkErrorReturnCode:
                   mTextToSpeech.speak("网路错误",TextToSpeech.QUEUE_FLUSH,null);
                   //MyToast myToast5 = new MyToast();
                   //myToast5.show(FaceVertificationActivity.this, (ViewGroup) findViewById(R.id.mytoast_layout_root),"网路错误");
                   break;
               case NetworkConnectTimeOutCode:
                   mTextToSpeech.speak("连接超时",TextToSpeech.QUEUE_FLUSH,null);
                   //MyToast myToast6 = new MyToast();
                   //myToast6.show(FaceVertificationActivity.this, (ViewGroup) findViewById(R.id.mytoast_layout_root),"连接超时");
                   break;
               default:
                   mTextToSpeech.speak("其它错误",TextToSpeech.QUEUE_FLUSH,null);
                   //MyToast myToast7 = new MyToast();
                   //myToast7.show(FaceVertificationActivity.this, (ViewGroup) findViewById(R.id.mytoast_layout_root),"其它错误");
                   break;
           }
           faceVertificationImageButton.setClickable(true);
       }
   }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.facevertification_layout);
        cameraSurfaceView = (SurfaceView) this.findViewById(R.id.FaceVertificationCameraSurfaceView);
        cameraSurfaceView.setFocusable(true);
        cameraSurfaceView.setFocusableInTouchMode(true);
        cameraSurfaceView.setClickable(false);

        SurfaceHolder mSurfaceHolder = cameraSurfaceView.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.setKeepScreenOn(true);//保持最底层常亮
        mSurfaceHolder.addCallback(this);

        drawSurfaceView = (DrawSurfaceView) this.findViewById(R.id.MainDarwSurfaceView);//绘制视图
        faceVertificationImageButton = (CircleImageButton) this.findViewById(R.id.FaceVertificationImageButton);
        faceVertificationImageButton.setOnClickListener(this);
        faceVertificationImageButton.setBottom(R.id.FaceVertificationCameraSurfaceView);
        circleProcessBar = (CircleProcessBar) this.findViewById(R.id.FaceVetificationProgressBar);
        circleProcessBar.setVisibility(View.GONE);
        faceVertificationClient = new FaceVertificationClient();

        //android TTS功能，语音播报
        mTextToSpeech = new TextToSpeech(this,new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS)
                {
                    int supportedLanguage = mTextToSpeech.setLanguage(Locale.CHINESE);
                    if((supportedLanguage != TextToSpeech.LANG_AVAILABLE) && (supportedLanguage
                            != TextToSpeech.LANG_COUNTRY_AVAILABLE))
                    {
                        Toast.makeText(FaceVertifactionProcessActivity.this,"不支持TTS功能",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //设定Intent
        Intent intent = getIntent();
        faceVertificationClient.setClientId(intent.getStringExtra("id"));
        faceVertificationClient.setClientName(intent.getStringExtra("name"));
        this.ServiceIpAddress = intent.getStringExtra("ip");

    }

    //点击事件响应管理函数
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //拍照回调函数
            case  R.id.FaceVertificationImageButton:
                if (ButtonUtils.isFastClick())
                    return;
                if (mCamera != null) {
                    mCamera.takePicture(null, null, null, FaceVertifactionProcessActivity.this);//调用此函数，拍完后调用onPictureTaken函数
                    faceVertificationImageButton.setClickable(false);//防止暴力点击
                }
                break;
            default:
                break;
        }
    }
    //surfaceview三件套回调函数
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera == null)return;
        mCamera.stopPreview();
        android.hardware.Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPictureFormat(PixelFormat.JPEG);
        // parameters.setPreviewFormat(PixelFormat.JPEG);
        android.hardware.Camera.Size s = getBestSupportedSize(parameters.getSupportedPreviewSizes(),width,height);//获取手机支持的最佳尺寸中的最大尺寸
        //设定参数
        parameters.setPreviewSize(s.width,s.height);
        mCamera.setDisplayOrientation(90);
        mCamera.setParameters(parameters);
        try{
            //  mCamera.setPreviewCallback(this);
            mCamera.startPreview();
        }catch (Exception e){
            mCamera.release();//释放相机资源
            mCamera = null;
        }
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = android.hardware.Camera.open(1);//获取手机后置摄像头
        try{
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);//相机画面实时显示在最底层surfaceview上
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        if (mCamera != null) {
            //mCamera.setPreviewCallback(null);
            mCamera.stopPreview();//停止预览
            mCamera.release();//释放
            mCamera = null;
        }
    }

    /*
    //手机相机获取实时视频帧回调函数
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }*/


    //一键认证回调函数
    @Override
    public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
        if (null != faceVertificationTask){
            switch (faceVertificationTask.getStatus()){
                case RUNNING:
                    return;
                case PENDING:
                    faceVertificationTask.cancel(false);
                    break;
            }
        }
        faceVertificationTask = new FaceVertificationTask(data,camera,ServiceIpAddress);
        faceVertificationTask.execute((Void)null);
    }

    //获取手机相机支持的最佳尺寸大小
    private android.hardware.Camera.Size getBestSupportedSize(List<android.hardware.Camera.Size> sizes, int width, int height){
        android.hardware.Camera.Size bestSizes = sizes.get(0);
        int largestArea = bestSizes.width * bestSizes.height;
        for (android.hardware.Camera.Size size : sizes){
            int area = size.width * size.height;
            if (area > largestArea){
                bestSizes = size;
                largestArea =area;
            }
        }
        return  bestSizes;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    //为了防止该活动被销毁数据未保存，及时保存该活动的临时状态
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG,"onSave");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mTextToSpeech != null)
            mTextToSpeech.shutdown();
    }

    public static Bitmap rotaingImageView(int angle , Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }
}
