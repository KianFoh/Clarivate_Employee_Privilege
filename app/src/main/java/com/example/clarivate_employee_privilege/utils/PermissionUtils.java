// app/src/main/java/com/example/clarivate_employee_privilege/utils/PermissionUtils.java
package com.example.clarivate_employee_privilege.utils;

import android.Manifest;
import android.content.Context;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class PermissionUtils {

    public static ActivityResultLauncher<String[]> registerForCameraPermission(Fragment fragment, Runnable onGranted, Runnable onDenied) {
        return fragment.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean cameraGranted = result.getOrDefault(Manifest.permission.CAMERA, false);
                    if (cameraGranted != null && cameraGranted) {
                        onGranted.run();
                    } else {
                        onDenied.run();
                    }
                }
        );
    }

    public static ActivityResultLauncher<String[]> registerForCameraPermission(FragmentActivity activity, Runnable onGranted, Runnable onDenied) {
        return activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean cameraGranted = result.getOrDefault(Manifest.permission.CAMERA, false);
                    if (cameraGranted != null && cameraGranted) {
                        onGranted.run();
                    } else {
                        onDenied.run();
                    }
                }
        );
    }

    public static void handlePermissionDenied(Context context) {
        Toast.makeText(context, "Camera permission denied. Please enable it in settings.", Toast.LENGTH_LONG).show();
    }
}