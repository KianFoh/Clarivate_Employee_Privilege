package com.example.clarivate_employee_privilege;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.clarivate_employee_privilege.api.CallAPI;
import com.example.clarivate_employee_privilege.api.CustomCallback;
import com.example.clarivate_employee_privilege.api.SocketEventCallback.EventCallback;
import com.example.clarivate_employee_privilege.api.SocketServiceManager;
import com.example.clarivate_employee_privilege.authentication.AuthUtils;
import com.example.clarivate_employee_privilege.authentication.SignInActivity;
import com.example.clarivate_employee_privilege.navbar_menu.AddMerchantFragment;
import com.example.clarivate_employee_privilege.navbar_menu.HomeFragment;
import com.example.clarivate_employee_privilege.navbar_menu.MerchantsFragment;
import com.example.clarivate_employee_privilege.navbar_menu.ProfileFragment;
import com.example.clarivate_employee_privilege.navbar_menu.RequestMerchantFragment;
import com.example.clarivate_employee_privilege.utils.ToastUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements EventCallback {

    private SocketServiceManager socketServiceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Redirect to sign in page if user is not signed in or load latest user information
        user_Authentication();

        // Get user email
        SharedPreferences sharedpreferences = this.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String email = sharedpreferences.getString("email", "Not found");
        String token = sharedpreferences.getString("google_idToken", "Not found");

        // Initialize SocketServiceManager
        socketServiceManager = new SocketServiceManager(this, email, token, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the SocketService
        Log.d("MainActivity", "onStart");
        socketServiceManager.bindService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the SocketService
        socketServiceManager.unbindService();
    }

    // Check user sign in status if not sign in redirect to sign in page
    private void user_Authentication() {
        GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(this);
        if (acc == null) {
            Intent i = new Intent(this, SignInActivity.class);
            startActivity(i);
            finish();
        }
        else {
            loadUserInfo();
        }
    }

    private void loadUserInfo() {
        // Update the user's ID token and profile image URL
        AuthUtils.refreshToken(this);

        // Get old user information for API call
        SharedPreferences sharedpreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String token = sharedpreferences.getString("google_idToken", "Not found");
        String email = sharedpreferences.getString("email", "Not found");

        // Build the API request
        Headers headers = new Headers.Builder()
                .add("Authorization", "Bearer " + token)
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.api_url) + "/user_info?email=" + email)
                .get()
                .headers(headers)
                .build();

        // Make the API call
        CallAPI.getClient().newCall(request).enqueue(new CustomCallback(MainActivity.this, request) {

            @Override
            public void onFailure(Call call, IOException e) {
                MainActivity.this.runOnUiThread(() -> {
                    // Show fail api call message
                    Log.d("ERROR Get User Info:", e.toString());
                    Context context = MainActivity.this;
                    String message = "Failed to load latest user information";
                    ToastUtils.showToast(context, message, false);
                    MainActivity.this.runOnUiThread(() -> navbar());
                });
            }

            @Override
            public void handleSuccessResponse(Response response) throws IOException {

                // Extract user information from the API response
                String responseData = response.body().string();
                JsonObject jsonObject = JsonParser.parseString(responseData).getAsJsonObject();
                JsonObject userObject = jsonObject.getAsJsonObject("user");

                // Store the latest user information in SharedPreferences
                String username = userObject.get("username").getAsString();
                String email = userObject.get("email").getAsString();
                boolean isAdmin = userObject.get("admin").getAsBoolean();

                SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", username);
                editor.putString("email", email);
                editor.putBoolean("isAdmin", isAdmin);
                editor.apply();
                Log.d("API_CALL_GET_USER_INFO", "Loaded Latest User information");

                MainActivity.this.runOnUiThread(() -> navbar());
            }
            @Override
            public void handleFailResponse(Response response, String responseBody) {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                String error = jsonObject.get("error").getAsString();
                Log.e("API_CALL_GET_USER_INFO", "API call failed: " + error);
                MainActivity.this.runOnUiThread(() -> {
                    Context context = MainActivity.this;
                    String message = "Failed to load latest user information: " + error;
                    ToastUtils.showToast(context, message, false);
                    navbar();
                });
            }
        });
    }

    // Check user is admin or not display navigation menu accordingly
    private void navbar() {
        // Retrieve isAdmin status from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

        // Get the current fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);

        // Set the appropriate menu based on user role
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Get the selected item ID
        int selectedItemId = bottomNavigationView.getSelectedItemId();

        // Clear the current menu
        bottomNavigationView.getMenu().clear();

        if (isAdmin) {
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_admin);
            Log.d("MainActivity", "User is admin");
        }
        else {
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_employee);
            Log.d("MainActivity", "User is not admin");
        }

        // Check if the current fragment is accessible by the user role
        if (isAdmin && currentFragment instanceof RequestMerchantFragment) {
            loadFragment(new AddMerchantFragment());
            selectedItemId = R.id.nav_add_merchant;
        }
        else if (!isAdmin && currentFragment instanceof AddMerchantFragment) {
            loadFragment(new RequestMerchantFragment());
            selectedItemId = R.id.nav_Request;
        }
        else {
            // Set the default fragment if the current fragment is null
            if (currentFragment == null) {
                loadFragment(new HomeFragment());
                selectedItemId = R.id.nav_home;
            }
        }

        // Restore the selected item
        bottomNavigationView.setSelectedItemId(selectedItemId);

        // Set up the listener for navigation item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.nav_home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.nav_merchants:
                    selectedFragment = new MerchantsFragment();
                    break;
                case R.id.nav_Request:
                    selectedFragment = new RequestMerchantFragment();
                    break;
                case R.id.nav_profile:
                    selectedFragment = new ProfileFragment();
                    break;
                case R.id.nav_add_merchant:
                    selectedFragment = new AddMerchantFragment();
                    break;
            }
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
    }

    // Method to load the selected fragment
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment);
        fragmentTransaction.commit();
    }

    public SocketServiceManager getSocketServiceManager() {
        return socketServiceManager;
    }

    @Override
    public void onAdminStatusUpdated(boolean isAdmin) {
        // Update admin status in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isAdmin", isAdmin);
        editor.apply();

        // Handle the admin status update here
        MainActivity.this.runOnUiThread(this::navbar);
        Log.d("MainActivity", "Admin Status UI updated");
    }
}