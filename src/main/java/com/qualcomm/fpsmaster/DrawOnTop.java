package com.qualcomm.fpsmaster;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import androidx.annotation.RequiresApi;

public class DrawOnTop extends Service  {
    private int no_of_modes;
    private int screenWidth;
    private int nowRR = 0;
    private int screenHeight;
    SharedPreferences sharedPreferences;
    private int mpreferredDisplayModeId;
    private WindowManager mWindowManager;
    private View mFloatingView;
    public DrawOnTop() {
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();

        //GET REQUIRED DATA FROM SHARED PREFERENCES
        sharedPreferences = getSharedPreferences("sharedPreferences",
                Context.MODE_PRIVATE);
        if (sharedPreferences.contains("screenHeight")) {
            screenHeight = sharedPreferences.getInt("screenHeight", 0);
        }
        if (sharedPreferences.contains("no_of_modes")) {
            no_of_modes = sharedPreferences.getInt("no_of_modes", 0);
        }
        if (sharedPreferences.contains("screenWidth")) {
            screenWidth = sharedPreferences.getInt("screenHeight", 0);
        }
        if (sharedPreferences.contains("current_mode")) {
            mpreferredDisplayModeId = sharedPreferences.getInt("current_mode", 0);
        }
        if (sharedPreferences.contains("mpreferredDisplayModeId")) {
            mpreferredDisplayModeId = sharedPreferences.getInt("mpreferredDisplayModeId", 0);
            mpreferredDisplayModeId-=1;
        }
        System.out.println("SharedPreferences: "+sharedPreferences.getAll());
        System.out.println("mpreferredDisplayModeId:"+mpreferredDisplayModeId);

        String stringg = "refresh_rates["+mpreferredDisplayModeId+"]";
        if (sharedPreferences.contains(stringg)) {
            nowRR = sharedPreferences.getInt(stringg, 0);
        }
        if(mpreferredDisplayModeId!=no_of_modes-1) {
            mpreferredDisplayModeId = mpreferredDisplayModeId + 1;
        }
        else if(mpreferredDisplayModeId==no_of_modes-1) {
            mpreferredDisplayModeId = 0;
        }

        //CREATE LAYOUT OF THE DRAWONTOP SERVICE
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.activity_draw_on_top, null);

        //SET THE PROPERTIES OF THE LAYOUT
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        //ADD FLOATING VIEW
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);

        //SET LOCATION OF SPRITE FOR DRAWONTOP
        params.x = (int) ((int) screenHeight*0.9);
        params.y = (int) ((int) screenWidth*0.9);
        params.preferredDisplayModeId=mpreferredDisplayModeId;
        System.out.println("mpreferredDisplayModeId:"+mpreferredDisplayModeId);
        mWindowManager.updateViewLayout(mFloatingView, params);
        String textt = "Refresh rate set to "+nowRR+"FPS";
        System.out.println(textt);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
    }
}