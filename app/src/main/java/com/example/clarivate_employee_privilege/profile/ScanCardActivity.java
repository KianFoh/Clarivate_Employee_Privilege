package com.example.clarivate_employee_privilege.profile;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.common.util.concurrent.ListenableFuture;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class ScanCardActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private ImageCapture imageCapture;
    private File photoFile;
    private boolean isPhotoTaken = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_card);

        // Check if camera permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Request camera permission if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            // Start the camera if permission is already granted
            startCamera();
        }

        // Set up the capture button to take a photo when clicked
        ImageView captureButton = findViewById(R.id.button_capture);
        captureButton.setOnClickListener(v -> takePhoto());

        // Set up the keep and discard buttons
        ImageView keepButton = findViewById(R.id.save);
        ImageView discardButton = findViewById(R.id.back);

        keepButton.setOnClickListener(v -> {
            if (photoFile != null) {
                File newFile = new File(photoFile.getParent(), "card_id.jpg");
                if (photoFile.renameTo(newFile)) {
                    Toast.makeText(this, "Photo saved as card_id.jpg", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show();
                }
                resetUI();
            }
        });

        discardButton.setOnClickListener(v -> {
            if (photoFile != null && photoFile.delete()) {
                Toast.makeText(this, "Photo discarded", Toast.LENGTH_SHORT).show();
                resetUI();
            }
        });

        // Set up the back button
        ImageView backButton = findViewById(R.id.back);
        backButton.setOnClickListener(v -> {
            if (isPhotoTaken) {
                // Discard the temp image and reset UI
                if (photoFile != null && photoFile.delete()) {
                    Toast.makeText(this, "Photo discarded", Toast.LENGTH_SHORT).show();
                }
                resetUI();
            } else {
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
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        // Set up the preview use case
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Set up the image capture use case
        imageCapture = new ImageCapture.Builder().build();

        // Bind the preview to the PreviewView
        PreviewView previewView = findViewById(R.id.camera_preview);
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
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                Log.d("CameraXApp", msg);
                isPhotoTaken = true;
                showCapturedImage();
                showButtons();

            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                String msg = "Photo capture failed: " + exception.getMessage();
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                Log.e("CameraXApp", msg);
            }
        });
    }

    private void showButtons() {
        findViewById(R.id.save).setVisibility(View.VISIBLE);
        findViewById(R.id.button_capture).setVisibility(View.GONE);
        Log.d("CameraXApp", "Buttons displayed");
    }

    private void showCapturedImage() {
        PreviewView previewView = findViewById(R.id.camera_preview);
        ImageView capturedImageView = findViewById(R.id.captured_image);

        previewView.setVisibility(View.GONE);
        capturedImageView.setVisibility(View.VISIBLE);

        Uri imageUri = Uri.fromFile(photoFile);
        Picasso.get().load(imageUri).into(capturedImageView);
        Log.d("CameraXApp", "Captured image displayed");
    }

    private void resetUI() {
        findViewById(R.id.save).setVisibility(View.GONE);
        findViewById(R.id.button_capture).setVisibility(View.VISIBLE);
        findViewById(R.id.camera_preview).setVisibility(View.VISIBLE);
        findViewById(R.id.captured_image).setVisibility(View.GONE);
        isPhotoTaken = false;
        photoFile = null;
        Log.d("CameraXApp", "UI reset");
    }
}