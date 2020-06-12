package com.qualcomm.fpsmaster;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

public class AutoFPS extends AppCompatActivity {

    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;
    SharedPreferences sharedPreferences;
    private int hasAccPerm;
    Button reset;
    private int defaultMode;
    private int i = 0;
    ScrollView scrollView;
    LinearLayout linearLayout;
    private int mCurrentModeIndex;
    private Display.Mode[] mDisplayModes;
    private float defaultRefreshRate;
    String installedApps[][] = new String[1000][2];


    //CHECK IF APP HAS ACCESSIBILITY PERMISSIONS AND ASK IF IT DOES NOT HAVE
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AccessibilityManager am = (AccessibilityManager) this .getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> runningServices = am.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
        hasAccPerm = 0;
        for (AccessibilityServiceInfo service : runningServices) {
            if ("com.qualcomm.fpsmaster/.TheService".equals(service.getId())) {
                hasAccPerm = 1;
            }
        }
        if(hasAccPerm == 0){
            Intent intent1 = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent1);
            Toast.makeText(getApplicationContext(), "FPS Master needs Accessibility " +
                    "permission to work properly.\n" +
                    "Please give permission to FPS Master", Toast.LENGTH_LONG).show();
        }
        sharedPreferences = getSharedPreferences("sharedPreferences",
                Context.MODE_PRIVATE);

        //CREATE THE PAGE AND LAYOUT
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_fps);
        setTitle("Auo FPS");
        scrollView = (ScrollView) findViewById(R.id.scrollView1);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout1);
        reset = (Button) findViewById(R.id.reset);


        // GET AVAILABLE REFRESH RATES
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
        final float[] resolutionsx = new float[modeArr.length];
        final float[] resolutionsy = new float[modeArr.length];
        for(i=0;i<modeArr.length;i++){
            refresh_rates[i]=modeArr[i].getRefreshRate();
            resolutionsx[i] = modeArr[i].getPhysicalWidth();
            resolutionsy[i] = modeArr[i].getPhysicalHeight();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String stringg = "refresh_rates["+i+"]";
            editor.putInt(stringg,(int)refresh_rates[i]);
            editor.apply();

            if(defaultRefreshRate == refresh_rates[i]){
                defaultMode = i;
                editor.putInt("default_mode", defaultMode);
                System.out.println("Default Mode: "+defaultMode);
                editor.putInt("no_of_modes", modeArr.length);
                editor.apply();

            }
        }

        //PERMISSION TO DRAW OVER OTHER APPS
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //startService(new Intent(MainActivity.this, DrawOnTop.class));
            //finish();
        } else if (Settings.canDrawOverlays(getApplicationContext())) {
            startService(new Intent(AutoFPS.this, com.qualcomm.fpsmaster.DrawOnTop.class));
            //finish();
        } else {
            askPermission();
            Toast.makeText(getApplicationContext(), "You need System Alert Window Permission to do this", Toast.LENGTH_SHORT).show();
        }



        //GET LIST OF NON-SYSTEM INSTALLED APPS
        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        i = 0;
        for (ApplicationInfo packageInfo : packages) {
            if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                //Log.d(TAG, "Installed package :" + packageInfo.packageName);
                //Log.d(TAG, "App Name:" + packageInfo.loadLabel(getPackageManager()).toString());
                installedApps[i][0] = packageInfo.packageName;
                installedApps[i][1] = packageInfo.loadLabel(getPackageManager()).toString();
                i += 1;
            }
            else if(packageInfo.packageName.equals("org.codeaurora.gallery")){
                //System.out.println("Found codeaurora gallery");
                //Log.d(TAG, "Installed package :" + packageInfo.packageName);
                //Log.d(TAG, "App Name:" + packageInfo.loadLabel(getPackageManager()).toString());
                installedApps[i][0] = packageInfo.packageName;
                installedApps[i][1] = packageInfo.loadLabel(getPackageManager()).toString();
                i += 1;

            }
            else if(packageInfo.packageName.contains("com.android.launcher")){
                //System.out.println("Found codeaurora gallery");
                //Log.d(TAG, "Installed package :" + packageInfo.packageName);
                //Log.d(TAG, "App Name:" + packageInfo.loadLabel(getPackageManager()).toString());
                installedApps[i][0] = packageInfo.packageName;
                installedApps[i][1] = "Home Screen/ App Launcher";
                i += 1;

            }
        }

        //CREATE LAYOUT WITH INSTALLED APPS NAMES AND AVAILABLE MODES
        int count = i;
        for (int i = 0; i < count+1; i++) {
            TextView button = new TextView(this);
            button.setText("" + installedApps[i][1]);
            button.setTextSize(15);
            button.setAllCaps(Boolean.TRUE);
            button.setTextColor(-16700000);
            linearLayout.addView(button);

            // ADD RADIO BUTTONS FOR APP
            final RadioButton[] rb = new RadioButton[6];
            RadioGroup rg = new RadioGroup(this); //create the RadioGroup
            rg.setOrientation(RadioGroup.VERTICAL);//or RadioGroup.VERTICAL
            for(int j=0; j<modeArr.length; j++){
                rb[j]  = new RadioButton(this);
                rb[j].setOnClickListener(btnclick);
                rb[j].setText((int)refresh_rates[j]+"FPS ; "+(int)resolutionsx[j]+"x"+(int)resolutionsy[j]);
                rb[j].setScaleX((float) 0.9);
                rb[j].setScaleY((float) 0.9);
                rb[j].setTextSize(10);
                rb[j].setId((10*i)+j);
                if (sharedPreferences.contains(installedApps[i][0])) {
                    if(sharedPreferences.getInt(installedApps[i][0], 0)==j){
                        rb[j].setChecked(true);
                    }
                }
                else{
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(installedApps[i][0], defaultMode);

                }
                rg.addView(rb[j]);
            }
            linearLayout.addView(rg);
        }

        //START ACCESSIBILITY SERVICE TO DETECT EVENTS
        startService(new Intent(AutoFPS.this, com.qualcomm.fpsmaster.TheService.class));

        //RESET BUTTON TO REMOVE ALL AUTO FPS PREFERENCES
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Resetting all choices");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                finish();
                startActivity(getIntent());
                System.out.println("SharedPreferences: "+sharedPreferences.getAll());
            }

        });
    }

    private void askPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }

    //WHEN ANY RADIO BUTTON IS PRESSED
    View.OnClickListener btnclick = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onClick(View view) {
            //GET THE ID OF THE BUTTON PRESSED, AS DEFINED WHEN EXPANDING THE SCROLL VIEW
            int tempID=view.getId();
            //SET THE MODE TO THE CORRESPONDING REFRESH RATE
            int floor = (int) tempID/10;
            int mode = tempID%10;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(installedApps[floor][0], mode);
            editor.remove("mpreferredDisplayModeId");
            editor.apply();
            System.out.println("SharedPreferences: "+sharedPreferences.getAll());
        }
    };
}
