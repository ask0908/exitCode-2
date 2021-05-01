package com.psj.welfare;

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;

public class ScreenSize {
    //디바이스 스크린 화면 크기 구하기
    public Point getScreenSize(Activity activity){
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        return size;
    }
}
