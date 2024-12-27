package com.example.clarivate_employee_privilege.navbar_menu.profile;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.utils.Toast_Utils;
import com.google.common.util.concurrent.ListenableFuture;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class Scan_Card_Activity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private ImageCapture imageCapture;
    private File photoFile;
    private boolean isPhotoTaken = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_card);

        // Hide the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Check if camera permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Request camera permission if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
        else {
            // Start the camera if permission is already granted
            startCamera();
        }

        // Set up the capture button to take a photo when clicked
        ImageView captureButton = findViewById(R.id.scancard_capture);
        captureButton.setOnClickListener(v -> takePhoto());

        // Set up the keep and discard buttons
        ImageView keepButton = findViewById(R.id.scancard_save);
        ImageView discardButton = findViewById(R.id.scancard_back);

        keepButton.setOnClickListener(v -> {

            String username = getSharedPreferences("user_info", MODE_PRIVATE).getString("username", "User");

            if (photoFile != null) {
                // Create the new file for card_id.jpg
                File newFile = new File(photoFile.getParent(), username + "card_id.jpg");

                // Check if the target file already exists and delete it
                if (newFile.exists()) {
                    newFile.delete();  // Deletes the existing card_id.jpg file
                }

                // Rename the photoFile to username + "card_id.jpg"
                if (photoFile.renameTo(newFile)) {
                    Toast_Utils.showToast(this, "Photo saved", true);
                }
                else {
                    Toast_Utils.showToast(this, "Failed to save photo", false);
                }

                Uri imageUri = Uri.fromFile(newFile);
                SharedPreferences sharedPreferences = getSharedPreferences("name_card " + username, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("card_id", imageUri.toString());
                editor.apply();

                finish();
            }
        });

        // Set up the back button
        ImageView backButton = findViewById(R.id.scancard_back);
        backButton.setOnClickListener(v -> {
            if (isPhotoTaken) {
                // Discard the temp image and reset UI
                if (photoFile != null && photoFile.delete()) {
                    Log.d("CameraXApp", "Temp image deleted");
                }
                resetUI();
            }
            else {
                // Navigate back to the ProfileFragment
                finish();
            }
        });
    }

    private void startCamera() {
        // Get an instance of the camera provider
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                // Bind the camera provider to the lifecycle
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            }
            catch (ExecutionException | InterruptedException e) {
                Log.d("CameraXApp", "Error starting camera: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        // Set up the preview use case
        Preview preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Set up the image capture use case and assign it to the instance variable
        imageCapture = new ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build();

        // Bind the preview to the PreviewView
        PreviewView previewView = findViewById(R.id.scan_camera_camerapreview);
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Bind the camera to the lifecycle with the preview and image capture use cases
        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }

    private void takePhoto() {
        if (imageCapture == null) {
            return;
        }

        // Create a file to save the photo
        photoFile = new File(getExternalFilesDir(null), "card_id_temp.jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // Take a picture and save it to the file
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                String msg = "Photo capture succeeded: " + outputFileResults.getSavedUri();
                Log.d("CameraXApp", msg);
                isPhotoTaken = true;
                showCapturedImage();
                showButtons();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                String msg = "Photo capture failed: " + exception.getMessage();
                Toast_Utils.showToast(getBaseContext(), msg, false);
                Log.e("CameraXApp", msg);
            }
        });
    }

    private void showButtons() {
        findViewById(R.id.scancard_save).setVisibility(View.VISIBLE);
        findViewById(R.id.scancard_capture).setVisibility(View.GONE);
        Log.d("CameraXApp", "Buttons displayed");
    }

    private void showCapturedImage() {
        PreviewView previewView = findViewById(R.id.scan_camera_camerapreview);
        ImageView capturedImageView = findViewById(R.id.scancard_capturedimg);

        previewView.setVisibility(View.GONE);
        capturedImageView.setVisibility(View.VISIBLE);

        Uri imageUri = Uri.fromFile(photoFile);

        // Force Picasso to skip caching and load the image fresh
        Picasso.get()
                .load(imageUri)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .into(capturedImageView);

        Log.d("CameraXApp", "Captured image displayed");
    }

    private void resetUI() {
        findViewById(R.id.scancard_save).setVisibility(View.GONE);
        findViewById(R.id.scancard_capture).setVisibility(View.VISIBLE);
        findViewById(R.id.scan_camera_camerapreview).setVisibility(View.VISIBLE);
        findViewById(R.id.scancard_capturedimg).setVisibility(View.GONE);
        isPhotoTaken = false;
        photoFile = null;
        Log.d("CameraXApp", "UI reset");
    }
}