package com.example.wuyunpeng.facevertificationultimate;

/**
 * Created by 12282 on 2016/11/1.
 */

import java.io.UnsupportedEncodingException;

/**
 * 发送数据的类对象 包含用户的id号，用户姓名，特征向量
 */
public class FaceVertificationClient {
    private byte[] bufMat = null;//特征向量存放的字节流
    private String clientId = null;
    private String clientName = null;
    private byte[] buf = null;

    public FaceVertificationClient(){

    }

    public void setClientId(String id) {
        clientId = id;

    }

    public void setClientName(String name) {
        clientName = name;

    }

    public void setBuf(){
        int offset = (bufMat.length/1024+ 1)* 1024;
        buf = new byte[ offset+1024];
        try {
            System.arraycopy(bufMat, 0, buf, 0, bufMat.length);
            byte[] clientIdArray = clientId.getBytes();
            byte[] clientNameArray = clientName.getBytes("gbk");
            System.arraycopy(clientIdArray, 0, buf,offset, clientIdArray.length);
            System.arraycopy(clientNameArray, 0, buf, offset + 512, clientNameArray.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public byte[] getBuf(){
        return buf;
    }

    public void setBufMat(byte[] faceBuf){
        bufMat = faceBuf;
        setBuf();
    }


}

