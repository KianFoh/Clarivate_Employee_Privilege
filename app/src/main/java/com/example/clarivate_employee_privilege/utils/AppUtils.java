package com.example.clarivate_employee_privilege.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.example.clarivate_employee_privilege.R;

import java.util.List;

public class AppUtils {

    /**
     * Checks if the app is currently running in the foreground.
     *
     * @param context The context of the application or activity.
     * @return true if the app is in the foreground, false otherwise.
     */
    public static boolean isAppInForeground(Context context) {

        // Get the ActivityManager system service, which manages activities and app processes
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        // If ActivityManager is null, return false because we can't check foreground status
        if (activityManager == null) return false;

        // Get the list of all running app processes on the device
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();

        // If the appProcesses list is null, return false as we cannot get process info
        if (appProcesses == null) return false;

        // Get the package name of the current application
        final String packageName = context.getPackageName();

        // Loop through each running process to check if the app is in the foreground
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {

            // Check if the current process is running in the foreground
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    // Check if the current process belongs to the current app
                    && appProcess.processName.equals(packageName)) {
                // Return true if the app is in the foreground
                return true;
            }
        }

        // Return false if the app is not found to be in the foreground
        return false;
    }

    public static void showLoading(Boolean isLoading, View progressBar) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    // Method to set the toolbar title
    public static void setToolbarTitle(Activity activity, String title) {
        TextView toolbarTitle = activity.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(title);
    }
}