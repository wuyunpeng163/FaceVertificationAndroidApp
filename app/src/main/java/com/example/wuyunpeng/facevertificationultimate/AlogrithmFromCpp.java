package com.example.wuyunpeng.facevertificationultimate;

/**
 * Created by 12282 on 2016/11/1.
 */
public class AlogrithmFromCpp {
    static {
        System.loadLibrary("AlogrithmFromCpp");
    }
    public static native byte[] getResizeImage(int[] imageData,int w,int h);

    public static native boolean getFaceDetection(int[] imageData,int w,int h,String xmlStr);
}
