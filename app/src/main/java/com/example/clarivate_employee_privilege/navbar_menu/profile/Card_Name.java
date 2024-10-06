
package com.example.clarivate_employee_privilege.navbar_menu.profile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.utils.Permission_Utils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

public class Card_Name extends AppCompatActivity {

    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_name);

        // Hide the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Register the permissions callback using PermissionUtils
        requestPermissionLauncher = Permission_Utils.registerForCameraPermission(
                this,
                () -> startActivity(new Intent(this, Scan_Card_Activity.class)),
                () -> Permission_Utils.handlePermissionDenied(this)
        );

        SharedPreferences sharedpreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String username = sharedpreferences.getString("username", "Not found");
        sharedpreferences = getSharedPreferences("name_card " + username, Context.MODE_PRIVATE);
        String cardId = sharedpreferences.getString("card_id", "Not found");
        Log.d("Inside CardName", "Card ID: " + cardId);

        ImageView cardname = findViewById(R.id.cardname_img);

        Log.d("Inside CardName", "Loading image from URL: " + cardname);

        // Force Picasso to skip caching and load the image fresh
        Picasso.get()
                .load(cardId)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .into(cardname);

        findViewById(R.id.cardname_back).setOnClickListener(v -> finish());
        findViewById(R.id.cardname_startcamera).setOnClickListener(v -> {
            requestPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
            finish();
        });
    }
}