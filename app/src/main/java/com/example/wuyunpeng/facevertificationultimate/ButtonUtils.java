package com.example.wuyunpeng.facevertificationultimate; /**
 * Created by wuyunpeng on 2016/7/29.
 */
/************************************************************************
 *                           按钮防止暴力点击类			                *
 ************************************************************************/
public class ButtonUtils {
    private static long lastClickTime;
    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if ( time - lastClickTime < 200) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

}
