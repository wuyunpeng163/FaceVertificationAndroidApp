//
// Created by 12282 on 2016/11/1.
//
#include "com_example_wuyunpeng_facevertificationultimate_AlogrithmFromCpp.h"
#include <string>
#include <vector>
#include <opencv2/opencv.hpp>
JNIEXPORT jbyteArray JNICALL Java_com_example_wuyunpeng_facevertificationultimate_AlogrithmFromCpp_getResizeImage
  (JNIEnv * env, jclass obj, jintArray imageData, jint w, jint h)
  {
    //接收java平台的原始图片数据，以及图片的宽和高
    jint * buf = env->GetIntArrayElements(imageData,JNI_FALSE);
    if(NULL == buf){
      return 0;
    }
    cv::Mat colorImage(h,w,CV_8UC4,(unsigned char*) buf);
    cv::Mat originalMat(h,w,CV_8UC3,cv::Scalar(0));
    cv::cvtColor(colorImage,originalMat,CV_RGBA2RGB);//必须转换为RGB而不能是BGR，才不会产生色偏
    cv::resize(originalMat,originalMat,cv::Size(160,300));
    int size = originalMat.cols * originalMat.rows * 3;
      unsigned char cbuf[size];
      int index = 0;
      for (int i = 0; i < originalMat.rows; ++i) {
          cv::Vec3b * pt = originalMat.ptr<cv::Vec3b>(i);
          for (int j = 0; j < originalMat.cols; ++j,index+=3) {
              cbuf[index] = pt[j][0];
              cbuf[index+1] = pt[j][1];
              cbuf[index+2] = pt[j][2];
          }
      }
    jbyteArray array = env->NewByteArray(size);
      env->SetByteArrayRegion(array,0,size,(jbyte *)cbuf);
      env->ReleaseIntArrayElements(imageData,buf,JNI_ABORT);
    return array;
  }

JNIEXPORT jboolean JNICALL Java_com_example_wuyunpeng_facevertificationultimate_AlogrithmFromCpp_getFaceDetection
        (JNIEnv * env, jclass obj, jintArray imageData, jint w, jint h,jstring xmlStr)
{
    //接收java平台的原始图片数据，以及图片的宽和高
    jint * buf = env->GetIntArrayElements(imageData,JNI_FALSE);
    if(NULL == buf){
        return 0;
    }
    cv::Mat colorImage(h,w,CV_8UC4,(unsigned char*) buf);
    cv::Mat grayMat(h,w,CV_8UC1,cv::Scalar(0));
    cv::cvtColor(colorImage,grayMat,CV_RGBA2GRAY);//必须转换为RGB而不能是BGR，才不会产生色偏
    cv::equalizeHist(grayMat,grayMat);//直方图均衡化以提高检测的成功率
    const char* jnamestr = env->GetStringUTFChars(xmlStr, NULL);
    std::string xmlFileName(jnamestr);
    cv::CascadeClassifier faceCascade;
    faceCascade.load(xmlFileName);
    if(faceCascade.empty())
        return 0;
    std::vector<cv::Rect> faces;
    faceCascade.detectMultiScale(grayMat,faces,1.1,2,0|CV_HAAR_SCALE_IMAGE,cv::Size(20,20),cv::Size(300,300));
    unsigned char IsfoundFace = 0;
    if(faces.size())
        IsfoundFace = 1;
    env->ReleaseIntArrayElements(imageData,buf,JNI_ABORT);
    return jboolean(IsfoundFace);
}