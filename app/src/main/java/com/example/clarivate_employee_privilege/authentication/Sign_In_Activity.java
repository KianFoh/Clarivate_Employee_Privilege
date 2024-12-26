package com.example.clarivate_employee_privilege.authentication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clarivate_employee_privilege.Main_Activity;
import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.api.Call_API;
import com.example.clarivate_employee_privilege.api.Custom_Callback;
import com.example.clarivate_employee_privilege.model.User;
import com.example.clarivate_employee_privilege.utils.Toast_Utils;
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
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Sign_In_Activity extends AppCompatActivity {

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
        findViewById(R.id.signin_googlesignin_button).setOnClickListener(v -> googleSignIn());
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
            Log.d("Google_Idtoken", idToken);

            if (isEmailValid(email)) {

                // Define API request body
                User newUser = new User(username, email);
                String body = new Gson().toJson(newUser);

                // Define API request headers
                Headers headers = new Headers.Builder().add("Authorization", "Bearer " + idToken).build();

                createUser(body, headers, account);
            }

            // Invalid email domain
            else {
                String domainEmail = getString(R.string.email_domain);
                Toast_Utils.showToast(this, "Sign-in is restricted to " + domainEmail + " domain only.", false);
                googleSignInClient.signOut();
            }
        }
        // Sign-in failed debug
        catch (ApiException e) {
            Log.d("ERROR SIGNIN", e.toString());
            Toast_Utils.showToast(this,"Sign-in failed: " + e, false);
        }
    }

    // Verify the email Domain name
    private boolean isEmailValid(String email) {
        return email != null && email.endsWith("@" + getString(R.string.email_domain));
    }

    // Make an API call to check or create a new user
    private void createUser(String body, Headers headers, GoogleSignInAccount account) {

        Request request = new Request.Builder()
                .url(getString(R.string.api_url) + "/user")
                .post(RequestBody.create(body, MediaType.get("application/json; charset=utf-8")))
                .headers(headers)
                .build();

        Call_API.getClient().newCall(request).enqueue(new Custom_Callback(this, request) {
            @Override
            public void handleSuccessResponse(Response response) throws IOException {
                handleCreateUser(response, account);
            }
            @Override
            public void handleFailResponse(Response response, String responseBody) {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                String error = jsonObject.get("error").getAsString();
                Log.e("API_SIGNIN", "API call failed: " + error);
                runOnUiThread(() -> Toast_Utils.showToast(Sign_In_Activity.this,"Sign-in failed: " + responseBody, false));
                googleSignInClient.signOut();
            }
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast_Utils.showToast(Sign_In_Activity.this,"Sign-in failed: " + e.getMessage(), false));
                googleSignInClient.signOut();
            }
        });
    }

    private void handleCreateUser(Response response, GoogleSignInAccount account) throws IOException {
        // Extract user information from the API response
        String responseData = response.body().string();
        JsonObject jsonObject = JsonParser.parseString(responseData).getAsJsonObject();
        JsonObject userObject = jsonObject.getAsJsonObject("user");

        String username = userObject.get("username").getAsString();
        String email = userObject.get("email").getAsString();
        boolean isAdmin = userObject.get("admin").getAsBoolean();
        String profileImageUrl = (account.getPhotoUrl() != null) ? account.getPhotoUrl().toString() : null;

        storeUserInfo(username, email, isAdmin, profileImageUrl, account.getIdToken());
        redirectToMainActivity();
    }

    // Store user information in SharedPreferences
    private void storeUserInfo(String username, String email, boolean isAdmin, String profileImageUrl, String idToken) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.putString("email", email);
        editor.putBoolean("isAdmin", isAdmin);
        editor.putString("profile_image", profileImageUrl);
        editor.putString("google_idToken", idToken);
        editor.apply();
    }

    private void redirectToMainActivity() {
        Intent intent = new Intent(Sign_In_Activity.this, Main_Activity.class);
        startActivity(intent);
        finish();
    }
}