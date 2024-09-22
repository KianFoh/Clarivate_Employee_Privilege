package com.example.clarivate_employee_privilege;

import android.annotation.SuppressLint;
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

import com.example.clarivate_employee_privilege.api.SocketService;
import com.example.clarivate_employee_privilege.api.SocketServiceManager;
import com.example.clarivate_employee_privilege.authentication.SignInActivity;
import com.example.clarivate_employee_privilege.navbar_menu.AddMerchantFragment;
import com.example.clarivate_employee_privilege.navbar_menu.HomeFragment;
import com.example.clarivate_employee_privilege.navbar_menu.MerchantsFragment;
import com.example.clarivate_employee_privilege.navbar_menu.ProfileFragment;
import com.example.clarivate_employee_privilege.navbar_menu.RequestMerchantFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private GoogleSignInClient googleSignInClient;
    private SocketService socketService;
    private SocketServiceManager socketServiceManager;
    private boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        user_Authentication ();

        // Recreate GoogleSignInClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Get user email
        SharedPreferences sharedpreferences = this.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String email = sharedpreferences.getString("email", "Not found");
        String token = sharedpreferences.getString("google_idToken", "Not found");

        // Initialize SocketServiceManager
        socketServiceManager = new SocketServiceManager(this, email, token);

        navbar();
        Log.d("MainActivity", "onCreate");
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
    private void user_Authentication (){
        GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(this);
        if (acc == null){
            Intent i = new Intent(this, SignInActivity.class);
            startActivity(i);
            finish();
        }
        else {
            // Get latest user information

        }
    }

    // Check user is admin or not display navigation menu accordingly
    @SuppressLint("NonConstantResourceId")
    private void navbar(){
        // Retrieve isAdmin status from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

        // Set the appropriate menu based on user role
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (isAdmin) {
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_admin);
        }
        else {
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_employee);
        }
        // Set the default fragment
        loadFragment(new HomeFragment());

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
}




