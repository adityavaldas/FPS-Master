package com.qualcomm.fpsmaster;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class TheService extends AccessibilityService {
    SharedPreferences sharedPreferences;
    private static final String TAG = "TAGS";
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        if (Build.VERSION.SDK_INT >= 16)
            config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        setServiceInfo(config);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null && event.getClassName() != null) {
                ComponentName componentName = new ComponentName(
                        event.getPackageName().toString(),
                        event.getClassName().toString()
                );

                ActivityInfo activityInfo = tryGetActivity(componentName);
                boolean isActivity = activityInfo != null;
                if (isActivity) {
                    String currentActivity = componentName.flattenToShortString().split("/")[0];
                    sharedPreferences = getSharedPreferences("sharedPreferences",
                            Context.MODE_PRIVATE);
                    if (sharedPreferences.contains(currentActivity)) {
                        stopService(new Intent(TheService.this, com.qualcomm.fpsmaster.DrawOnTop.class));
                        System.out.println("Found " + currentActivity + " set to "+sharedPreferences.getInt(currentActivity, 0));
                        int currentMode = sharedPreferences.getInt(currentActivity, 0);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("current_mode", currentMode);
                        System.out.println("Current Mode: "+currentMode);
                        editor.apply();
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        }
                        else if (Settings.canDrawOverlays(getApplicationContext())) {
                            startService(new Intent(TheService.this, com.qualcomm.fpsmaster.DrawOnTop.class));
                        } else {
                            Toast.makeText(getApplicationContext(), "You need System Alert Window Permission to do this", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        stopService(new Intent(TheService.this, com.qualcomm.fpsmaster.DrawOnTop.class));
                    }
                }
            }
        }
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public void onInterrupt() {}
}