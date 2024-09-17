package com.example.clarivate_employee_privilege.authentication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clarivate_employee_privilege.API.CallAPI;
import com.example.clarivate_employee_privilege.MainActivity;
import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Response;

public class SignInActivity extends AppCompatActivity {

    private static final int ONE_HOUR_IN_MILLIS = 3600 * 1000;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);

        configureGoogleSignIn();
        setupListener();
    }

    // Configure Google Sign-In
    private void configureGoogleSignIn() {
        // Configure Google Sign-In to request user email and ID token
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.server_client_id))
                .build();

        // Build a GoogleSignInClient with the options specified by gso
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Register activity result launcher for Google Sign-In
        googleSignInLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
            }
        });
    }

    private void setupListener() {
        findViewById(R.id.google_sign_in_button).setOnClickListener(v -> googleSignIn());
    }

    private void googleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {

            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String username = account.getDisplayName();
            String email = account.getEmail();
            String idToken = account.getIdToken();

            if (isEmailValid(email)) {
                storeAuthToken(idToken);

                // Define API request body
                User newUser = new User(username, email);
                String body = new Gson().toJson(newUser);

                // Define API request headers
                Headers headers = new Headers.Builder().add("Authorization", "Bearer " + idToken).build();

                makeApiCall(body, headers, account);
            }

            // Invalid email domain
            else {
                showToast("Sign-in is restricted to Clarivate Employee email only.");
                googleSignInClient.signOut();
            }
        }
        // Sign-in failed debug
        catch (ApiException e) {
            Log.d("ERROR", e.toString());
            showToast("Sign-in failed: " + e);
        }
    }

    // Verify the email Domain name
    private boolean isEmailValid(String email) {
        return email != null && email.endsWith("@" + getString(R.string.email_domain));
    }

    // Store the ID token and its expiration time in SharedPreferences
    private void storeAuthToken(String idToken) {
        SharedPreferences sharedPreferences = getSharedPreferences("auth", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("idToken", idToken);
        editor.putLong("tokenExpirationTime", System.currentTimeMillis() + ONE_HOUR_IN_MILLIS);
        editor.apply();
    }

    // Make an API call to check or create a new user
    private void makeApiCall(String body, Headers headers, GoogleSignInAccount account) {
        CallAPI.post(getString(R.string.api_url) + "/users", body, new Callback() {

            // Handle API call failure debug
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("ERROR", e.toString());
                runOnUiThread(() -> showToast("Sign-in failed: " + e.getMessage()));
            }

            // Handle API response
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    handleApiResponse(response, account);
                }

                // Handle API response error failure debug
                else {
                    Log.d("ERROR", response.toString());
                    runOnUiThread(() -> showToast("Sign-in failed: " + response.message()));
                }
            }
        }, headers);
    }

    private void handleApiResponse(Response response, GoogleSignInAccount account) throws IOException {
        // Extract user information from the API response
        String responseData = response.body().string();
        JsonObject jsonObject = JsonParser.parseString(responseData).getAsJsonObject();
        JsonObject userObject = jsonObject.getAsJsonObject("user");

        String username = userObject.get("username").getAsString();
        String email = userObject.get("email").getAsString();
        boolean isAdmin = userObject.get("admin").getAsBoolean();
        String profileImageUrl = (account.getPhotoUrl() != null) ? account.getPhotoUrl().toString() : null;

        storeUserInfo(username, email, isAdmin, profileImageUrl);
        redirectToMainActivity();
    }

    // Store user information in SharedPreferences
    private void storeUserInfo(String username, String email, boolean isAdmin, String profileImageUrl) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.putString("email", email);
        editor.putBoolean("isAdmin", isAdmin);
        editor.putString("profile_image", profileImageUrl);
        editor.apply();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void redirectToMainActivity() {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}