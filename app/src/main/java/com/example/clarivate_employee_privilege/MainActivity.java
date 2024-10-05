package com.example.clarivate_employee_privilege;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.clarivate_employee_privilege.authentication.SignInActivity;
import com.example.clarivate_employee_privilege.navbar_menu.HomeFragment;
import com.example.clarivate_employee_privilege.navbar_menu.add_merchant.AddMerchantFragment;
import com.example.clarivate_employee_privilege.navbar_menu.merchants.MerchantsFragment;
import com.example.clarivate_employee_privilege.navbar_menu.profile.ProfileFragment;
import com.example.clarivate_employee_privilege.navbar_menu.request_merchant.RequestMerchantFragment;
import com.example.clarivate_employee_privilege.utils.AppUtils;
import com.example.clarivate_employee_privilege.websocket.EventBus;
import com.example.clarivate_employee_privilege.websocket.SocketServiceManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instance;
    private SocketServiceManager socketServiceManager;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        AppUtils.showLoading(true, findViewById(R.id.main_progressbar));

        // Redirect to sign in page if user is not signed in
        user_Authentication();

        // Get user email
        SharedPreferences sharedpreferences = this.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String email = sharedpreferences.getString("email", "Not found");
        String token = sharedpreferences.getString("google_idToken", "Not found");

        // Initialize SocketServiceManager
        socketServiceManager = new SocketServiceManager(this, email, token);
        observeEventBus();
        setupToolbarBackButton();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the SocketService
        Log.d("MainActivity", "onStart");
        socketServiceManager.bindService();
    }

    // Hide the keyboard when the user taps outside of an input field
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            View currentFocus = getCurrentFocus();
            currentFocus.clearFocus();

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
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
    }

    // Check user is admin or not display navigation menu accordingly
    private void navbar(boolean isAdmin) {
        // Get the current fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment);

        // Set the appropriate menu based on user role
        BottomNavigationView bottomNavigationView = findViewById(R.id.main_bottomnavigation);

        // Get the selected item ID
        int selectedItemId = bottomNavigationView.getSelectedItemId();

        // Clear the current menu
        bottomNavigationView.getMenu().clear();

        if (isAdmin) {
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_admin);
            Log.d("MainActivity", "User is admin");
        } else {
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_employee);
            Log.d("MainActivity", "User is not admin");
        }

        // Check if the current fragment is accessible by the user role
        if (isAdmin && currentFragment instanceof RequestMerchantFragment) {
            loadFragment(new AddMerchantFragment());
            selectedItemId = R.id.nav_add_merchant;
        } else if (!isAdmin && currentFragment instanceof AddMerchantFragment) {
            loadFragment(new RequestMerchantFragment());
            selectedItemId = R.id.nav_Request;
        } else {
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

        // Clear the back stack
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_fragment, fragment);
        fragmentTransaction.commit();
    }

    public SocketServiceManager getSocketServiceManager() {
        return socketServiceManager;
    }

    public void observeEventBus() {
        // Observe the admin status updates
        EventBus.getInstance().getIsadminLiveData().observe(this, isAdmin -> {
            // Handle the admin status update here
            MainActivity.this.runOnUiThread(() -> navbar(isAdmin));
            Log.d("MainActivity", "Admin Status UI updated");
        });
    }

    private void setupToolbarBackButton() {
        ImageButton backButton = findViewById(R.id.toolbar_back_button);
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Check the back stack count and set the visibility of the back button
        if (fragmentManager.getBackStackEntryCount() > 0) {
            backButton.setVisibility(View.VISIBLE);
        } else {
            backButton.setVisibility(View.GONE);
        }

        // Set the click listener for the back button
        backButton.setOnClickListener(v -> {
            if (fragmentManager.getBackStackEntryCount() > 0) {
                Log.d("MainActivity", "Popping back stack");
                fragmentManager.popBackStack();
            }
        });

        // Add a back stack change listener to update the button visibility
        fragmentManager.addOnBackStackChangedListener(() -> {
            if (fragmentManager.getBackStackEntryCount() > 0) {
                backButton.setVisibility(View.VISIBLE);
            } else {
                backButton.setVisibility(View.GONE);
            }
        });
    }

    public static MainActivity getInstance() {
        return instance;
    }

    public static Context getContext() {
        return instance;
    }

}