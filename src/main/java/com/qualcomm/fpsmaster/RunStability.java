package com.qualcomm.fpsmaster;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class RunStability extends AppCompatActivity {
    private Display.Mode[] mDisplayModes;
    private int fToSet = -1;
    private float nowFPS = 0;
    private int no_of_modes = 3;
    private int iter = 0;
    private int i;
    private int total_iterations = 10;
    private int mCurrentModeIndex;
    private float defaultRefreshRate;
    SharedPreferences sharedPreferences;
    TextView textShow;
    TextView resShow;
    private volatile boolean runnableStop = false;
    private volatile boolean resFail = false;
    Handler handler = new Handler();
    @RequiresApi(api = Build.VERSION_CODES.M)

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_stability);
        setTitle("FPS Stability Test");
        textShow = (TextView) findViewById(R.id.textView5);
        resShow = (TextView) findViewById(R.id.textView6);
        sharedPreferences = getSharedPreferences("sharedPreferences",
                Context.MODE_PRIVATE);
        if (sharedPreferences.contains("no_of_modes")) {
            no_of_modes = sharedPreferences.getInt("no_of_modes", 0);
        }

        //GET DEFAULT FPS AND THE MODES
        this.mDisplayModes = getWindowManager().getDefaultDisplay().getSupportedModes();
        defaultRefreshRate = getWindowManager().getDefaultDisplay().getRefreshRate();
        Display.Mode mode = getWindowManager().getDefaultDisplay().getMode();
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        i = this.mCurrentModeIndex + 1;
        Display.Mode[] modeArr = this.mDisplayModes;
        int length = i % modeArr.length;
        attributes.preferredDisplayModeId = modeArr[length].getModeId();
        System.out.println("Default refresh rate: "+defaultRefreshRate);
        window.setAttributes(attributes);
        this.mCurrentModeIndex = length;
        final float[] refresh_rates = new float[modeArr.length];
        for(i=0;i<modeArr.length;i++){
            refresh_rates[i]=modeArr[i].getRefreshRate();
        }
        System.out.println("Refresh rates: "+refresh_rates[0]+" "+refresh_rates[1]+" "+refresh_rates[2]);

        //CREATE RUNNABLE TO COMPARE CONSEQUENT FPS
        final Runnable r = new Runnable() {
            public void run() {
                //IF STABILITY TEST COMPLETED
                if(runnableStop == true){
                    if(resFail == true){
                        resShow.setText(("Stability Test Failed"));
                    }
                    else{
                        String strr = "Completed "+total_iterations+" iterations";
                        textShow.setText(strr);
                        resShow.setText(("Stability Test Passed"));
                    }
                }
                //IF STABILITY TEST IN PROGRESS
                if (runnableStop == false) {
                    Window window = getWindow();
                    //CHANGE FPS
                    fToSet = (fToSet+1)%no_of_modes;
                    mDisplayModes = getWindowManager().getDefaultDisplay().getSupportedModes();
                    Display.Mode[] modeArr = mDisplayModes;
                    WindowManager.LayoutParams attributes = window.getAttributes();
                    attributes.preferredDisplayModeId = modeArr[fToSet].getModeId();
                    window.setAttributes(attributes);

                    //VERIFY FPS SWITCH
                    Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                    float refreshRating = display.getRefreshRate();
                    int toiter = (int)iter/no_of_modes;
                    String strr = ""+"iteration: "+toiter+"/"+total_iterations+" ; Setting to "+(int)refreshRating+"FPS";
                    if(toiter>0) {
                        textShow.setText(strr);
                        //if (refreshRating != nowFPS) {
//                        System.out.println("refreshrating: " + refreshRating + " refresh_rates[ftoset]: " + refresh_rates[(fToSet+2)%no_of_modes]);
                        if (refreshRating == refresh_rates[(fToSet+2)%no_of_modes]){
                            //System.out.println("FPS switched");
                            strr = "Successfully set FPS to " + (int)refreshRating + " Passed";
                            resShow.setText(strr);
                        } else {
                            //System.out.println("FPS failed to switch");
                            strr = "Failed to set FPS to " + (int)refreshRating + " Failed";
                            resShow.setText(strr);
                            resFail = true;
                        }
                    }
                    nowFPS = refreshRating;
                    iter+=1;
                    //STOP AFTER REQUIRED ITERATIONS
                    if(iter>=total_iterations*no_of_modes){
                        runnableStop = true;
                    }
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.postDelayed(r, 1000);

    }
    public void onDestroy() {
        super.onDestroy();
        runnableStop = true;
    }


}