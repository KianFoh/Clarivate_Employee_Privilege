package com.example.clarivate_employee_privilege.authentication;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.clarivate_employee_privilege.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class AuthUtils {
    public static void checkToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        String idToken = sharedPreferences.getString("idToken", null);
        long tokenExpirationTime = sharedPreferences.getLong("tokenExpirationTime", 0);

        // Check if the token is about to expire within 10 seconds
        if (idToken == null || System.currentTimeMillis() > tokenExpirationTime - 10000) {
            // Token is expire or about to expire, perform silent sign-in
            refreshToken(context);
        }
    }

    private static void refreshToken(Context context) {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(context.getString(R.string.server_client_id))
                .build());

        googleSignInClient.silentSignIn().addOnCompleteListener((Activity) context, task -> {
            // Verify the result of the silent sign-in
            if (task.isSuccessful()) {
                GoogleSignInAccount account = task.getResult();
                String idToken = account.getIdToken();

                // Store new ID token and expiration time
                SharedPreferences sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("idToken", idToken);
                editor.putLong("tokenExpirationTime", System.currentTimeMillis() + 3600 * 1000); // 1 hour validity
                editor.apply();

                Log.d("TOKEN_STATUS", "Token refreshed successfully");
            }
            else {
                // Silent sign-in failed
                Log.e("TOKEN_STATUS", "Silent sign-in failed");
            }
        });
    }
}
