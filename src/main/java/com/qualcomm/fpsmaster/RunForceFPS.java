package com.qualcomm.fpsmaster;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.Display.Mode;
import android.widget.Button;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Timer;
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

public class RunForceFPS extends AppCompatActivity {
    Button button4;
    SharedPreferences sharedPreferences;
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;
    private int screenWidth;
    private int screenHeight;
    public int frameRate;
    private float alpha;
    private int i=0;
    ScrollView scrollView;
    LinearLayout linearLayout;
    TextView currentFPS;
    TextView defaultFPS;
    private int mCurrentModeIndex;
    private Mode[] mDisplayModes;
    private float defaultRefreshRate;
    @RequiresApi(api = Build.VERSION_CODES.M)

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_force_fps);
        setTitle("FPS Switch");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            askPermission();
        }
        scrollView = (ScrollView) findViewById(R.id.scrollView1);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout1);
        button4 = (Button) findViewById(R.id.button4);

        // GET THE SCREEN DIMENSIONS
        WindowManager wm = getWindowManager();
        Display disp = wm.getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        alpha=1;
        defaultFPS = (TextView) findViewById(R.id.textView3);
        currentFPS = (TextView) findViewById(R.id.textView4);

        //GET DEFAULT FPS AND THE MODES
        this.mDisplayModes = getWindowManager().getDefaultDisplay().getSupportedModes();
        defaultRefreshRate = getWindowManager().getDefaultDisplay().getRefreshRate();
        Mode mode = getWindowManager().getDefaultDisplay().getMode();
        Window window = getWindow();
        LayoutParams attributes = window.getAttributes();
        i = this.mCurrentModeIndex + 1;
        Mode[] modeArr = this.mDisplayModes;
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


        i=0;
        //FOR EVERY REFRESH RATE ADD A BUTTON TO THE LINEARLAYOUT SCROLLVIEW AND SET THE TEXT ON THE BUTTON
        for (float value : refresh_rates) {
            Button button = new Button(this);
            button.setText(""+(int)value+"FPS");
            button.setId(i);   // ID set as 0 corresponding with refresh-rates[0] and modeArr[0]
            button.setOnClickListener(btnclick);
            linearLayout.addView(button);
            i+=1;
        }
        defaultFPS.setText("" + (int)defaultRefreshRate);
        defaultFPS.setTextColor(0xFF000000);

        int i = 0;

        //DEFAULT MODE
        while (true) {
            int refresh_mode=this.mDisplayModes[i].getModeId();
            if (i >= this.mDisplayModes.length) {
                break;
            } else if (mode.getModeId() == this.mDisplayModes[i].getModeId()) {
                this.mCurrentModeIndex = i;
                break;
            } else {
                i++;
            }
        }

        // ONCLICK LISTENER FOR THE STOP RENDERING BUTTON
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFPS.setText("");
                stopService(new Intent(RunForceFPS.this, DrawOnTop.class));

            }
        });

        //CREATE A SHARED PREFERENCES INSTANCE
        sharedPreferences = getSharedPreferences("sharedPreferences",
                Context.MODE_PRIVATE);
    }
    // ONCLICK LISTENER FOR THE BUTTONS GENERATED BASED ON THE MODES
    View.OnClickListener btnclick = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onClick(View view) {
            //STOP SERVICE OF DRAWONTOP IF ANY IS RUNNING
            stopService(new Intent(RunForceFPS.this, DrawOnTop.class));
            //GET THE ID OF THE BUTTON PRESSED, AS DEFINED WHEN EXPANDING THE SCROLL VIEW
            int tempID=view.getId();
            //SET THE MODE TO THE CORRESPONDING REFRESH RATE
            mDisplayModes = getWindowManager().getDefaultDisplay().getSupportedModes();
            Mode[] modeArr = mDisplayModes;
            Window window = getWindow();
            LayoutParams attributes = window.getAttributes();
            attributes.preferredDisplayModeId = modeArr[tempID].getModeId();
            window.setAttributes(attributes);

            //SET THE TEXT ON THE CURRENT FPS TEXTBOX
            currentFPS.setText(""+(int)modeArr[tempID].getRefreshRate());
            currentFPS.setTextColor(0xFF000000);
            frameRate=(int) (1000/modeArr[tempID].getRefreshRate());
            //System.out.println("frameRate: "+frameRate);

            //STORE REQUIRED DATA IN SHARED PREFERENCES FOR DRAWONTOP TO USE
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("mpreferredDisplayModeId", attributes.preferredDisplayModeId);
            editor.remove("current_mode");
            editor.putInt("frameRate", frameRate);
            editor.putInt("screenHeight", screenHeight);
            editor.putInt("screenWidth", screenWidth);
            editor.apply();

            //START THE DRAWONTOP SERVICE CONSIDERING PERMISSION REQUIRED TO BE GRANTED FIRST
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                //startService(new Intent(MainActivity.this, DrawOnTop.class));
                //finish();
            } else if (Settings.canDrawOverlays(getApplicationContext())) {
                startService(new Intent(RunForceFPS.this, DrawOnTop.class));
                //finish();
            } else {
                askPermission();
                Toast.makeText(getApplicationContext(), "You need System Alert Window Permission to do this", Toast.LENGTH_SHORT).show();
            }


        }
    };

    private void askPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }

    public void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("preferredDisplayModeId");
        editor.apply();
        stopService(new Intent(RunForceFPS.this, DrawOnTop.class));
    }
}