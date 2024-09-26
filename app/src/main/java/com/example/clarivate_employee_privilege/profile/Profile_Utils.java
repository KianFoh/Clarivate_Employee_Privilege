package com.example.clarivate_employee_privilege.profile;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.api.SocketService;
import com.example.clarivate_employee_privilege.authentication.SignInActivity;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

public class Profile_Utils {
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    // SignOut
    public static void signOut(Activity activity, GoogleSignInClient googleSignInClient, SocketService socketService) {
        // Disconnect the socket
        if (socketService != null) {
            socketService.disconnectSocket();
        }

        googleSignInClient.signOut()
                .addOnCompleteListener(activity, task -> {
                    // Redirect user to signIn activity
                    Intent i = new Intent(activity, SignInActivity.class);
                    activity.startActivity(i);
                    activity.finish();
                });
    }

    // Show Add Admin Dialog
    public static void showAddAdminDialog(Context context, Profile_API profileAPI) {
        // Inflate the add_admin_form.xml layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.admin_manage, null);

        // Create the AlertDialog with custom style
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialog);
        builder.setView(dialogView);

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Set up the close button
        ImageButton close = dialogView.findViewById(R.id.toast_close);
        close.setOnClickListener(v -> dialog.dismiss());

        // Set up the add admin button
        Button submit = dialogView.findViewById(R.id.add_admin);
        submit.setOnClickListener(v -> {
            String email = ((TextInputLayout) dialogView.findViewById(R.id.admin_email)).getEditText().getText().toString();
            Log.d("EMAIL", email);
            profileAPI.add_admin(email, dialog);
        });

        // Set up the remove admin button
        Button remove = dialogView.findViewById(R.id.remove_admin);
        remove.setOnClickListener(v -> {
            String email = ((TextInputLayout) dialogView.findViewById(R.id.admin_email)).getEditText().getText().toString();
            Log.d("EMAIL", email);
            profileAPI.remove_admin(email, dialog);
        });

        // Set the dialog window size to custom width and height
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    // Scan Card
    public static void scanCard(Activity activity) {
        // Check if camera permission is granted
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Request camera permission if not granted
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
        else {
            // Redirect user to ScanCard activity if permission is already granted
            Intent i = new Intent(activity, ScanCardActivity.class);
            activity.startActivity(i);
        }
    }

    // Start ScanCard activity
    public static void startScanCardActivity(Activity activity) {
        Intent i = new Intent(activity, ScanCardActivity.class);
        activity.startActivity(i);
    }

    // Handle permission denied
    public static void handlePermissionDenied(Activity activity) {
        Toast.makeText(activity, "Camera permission denied. Please enable it in settings.", Toast.LENGTH_LONG).show();
    }

    public static void loadProfileImage(String imageUrl, ImageView imageView) {
        if (imageUrl.equals("Not found")) {
            imageView.setImageResource(R.drawable.round_account_circle_24);
        }
        else {
            Picasso.get().load(imageUrl).into(imageView);
        }
    }

    public static void loadCardImage(String cardId, ImageView cardImageView, ActivityResultLauncher<String[]> requestPermissionLauncher, Activity activity) {
        Picasso.get()
                .load(cardId)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .into(cardImageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        cardImageView.setOnClickListener(v -> {
                            Intent intent = new Intent(activity, CardName.class);
                            activity.startActivity(intent);
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        cardImageView.setImageResource(R.drawable.card_name);
                        cardImageView.setOnClickListener(v -> requestPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA}));
                    }
                });
    }
}