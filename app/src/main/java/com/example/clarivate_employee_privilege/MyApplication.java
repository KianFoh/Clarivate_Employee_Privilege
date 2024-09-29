package com.example.clarivate_employee_privilege;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.util.Log;

import com.example.clarivate_employee_privilege.websocket.SocketService;
import com.example.clarivate_employee_privilege.utils.AppUtils;


// Start the SocketService when the application starts
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (AppUtils.isAppInForeground(this)) {
            try {
                startService(new Intent(this, SocketService.class));
            }
            catch (Exception e) {
                Log.e("MyApplication", "Failed to start service", e);
            }
        }
        else {
            Log.w("MyApplication", "App is in background, not starting service");
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            // App is in the background, stop the SocketService
            Intent intent = new Intent(this, SocketService.class);
            stopService(intent);
            Log.d("MyApplication", "App is in the background, SocketService stopped");
        }
    }
}

