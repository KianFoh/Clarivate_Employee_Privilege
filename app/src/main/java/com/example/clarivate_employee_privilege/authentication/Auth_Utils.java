package com.example.clarivate_employee_privilege.authentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.clarivate_employee_privilege.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class Auth_Utils {

    public interface TokenRefreshCallback {
        void onTokenRefreshed(String newToken);
    }

    // Overloaded method without callback
    public static void refreshToken(Context context) {
        refreshToken(context, null);
    }

    public static void refreshToken(Context context, TokenRefreshCallback callback) {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(context.getString(R.string.server_client_id))
                .build());

        googleSignInClient.silentSignIn().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                GoogleSignInAccount account = task.getResult();
                String imgUrl = (account.getPhotoUrl() != null) ? account.getPhotoUrl().toString() : null;
                String idToken = account.getIdToken();

                // Store new ID token and profile image URL in SharedPreferences
                SharedPreferences sharedPreferences = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("profile_image", imgUrl);
                editor.putString("google_idToken", idToken);
                editor.apply();

                Log.d("Google_Idtoken", idToken);

                Log.d("TOKEN_STATUS", "Token refreshed successfully");
                if (callback != null) {
                    callback.onTokenRefreshed(idToken);
                }
            } else {
                Log.e("TOKEN_STATUS", "Silent sign-in failed");
                Context appContext = context.getApplicationContext();
                Intent signInIntent = new Intent(appContext, Sign_In_Activity.class);
                signInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                appContext.startActivity(signInIntent);
            }
        });
    }
}