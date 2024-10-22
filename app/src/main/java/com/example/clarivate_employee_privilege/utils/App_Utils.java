package com.example.clarivate_employee_privilege.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.clarivate_employee_privilege.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class App_Utils {
    /**
     * Truncates the text to a specified maximum length.
     * @param text The text to truncate.
     * @param maxLength The maximum length of the text.
     * @return The truncated text.
     */
    public static String truncateText(String text, int maxLength) {
        if (text.length() > maxLength) {
            return text.substring(0, maxLength - 3) + "...";
        } else {
            return text;
        }
    }
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

    public static void disableButton(final View button) {
        if (button instanceof Button || button instanceof ImageButton) {
            button.setEnabled(false);
        }
    }

    public static void disableTitleBar(Activity activity) {
        activity.findViewById(R.id.toolbar_title).setVisibility(View.GONE);
    }

    public static void enableTitleBar(Activity activity) {
        activity.findViewById(R.id.toolbar_title).setVisibility(View.VISIBLE);
    }

    public static void enableProfile(Activity activity) {
        activity.findViewById(R.id.toolbar_profile_layout).setVisibility(View.VISIBLE);
    }

    public static void disableProfile(Activity activity) {
        activity.findViewById(R.id.toolbar_profile_layout).setVisibility(View.GONE);
    }

    public static void setProfile(Activity activity, Boolean isAdmin) {
        enableProfile(activity);
        disableTitleBar(activity);

        SharedPreferences sharedPreferences = activity.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String image = sharedPreferences.getString("profile_image", "Not found");
        String username = sharedPreferences.getString("username", "Not found");

        TextView userLabel = activity.findViewById(R.id.toolbar_profile_label);
        userLabel.setText(isAdmin ? "Admin" : "Employee");

        TextView profileName = activity.findViewById(R.id.toolbar_profile_name);
        profileName.setText(username);

        ImageView profilePic = activity.findViewById(R.id.toolbar_profile_pic);
        if ("Not found".equals(image)) {
            profilePic.setImageResource(R.drawable.round_account_circle_24);
        } else {
            Picasso.get().load(image).into(profilePic);
        }
    }
}