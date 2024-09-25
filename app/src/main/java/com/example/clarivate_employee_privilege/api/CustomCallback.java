package com.example.clarivate_employee_privilege.api;

import android.content.Context;
import android.util.Log;

import com.example.clarivate_employee_privilege.authentication.AuthUtils;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

// CustomCallback use as standard callback for refresh token and retry api call
public abstract class CustomCallback implements Callback {

    private final Context context;
    private final Request originalRequest;

    public CustomCallback(Context context, Request originalRequest) {
        this.context = context;
        this.originalRequest = originalRequest;
    }

    // API call failed
    @Override
    public void onFailure(Call call, IOException e) {
        Log.e("API_CALL", "API call failed: " + e.getMessage());
    }

    // API call returned a response
    @Override
    public void onResponse(Call call, Response response) throws IOException {

        // Check if the response failed
        if (!response.isSuccessful()) {

            String responseBody = response.body().string();

            try {
                // Parse the response body
                JSONObject jsonObject = new JSONObject(responseBody);
                String error = jsonObject.optString("error");

                if ("Token is expired".equals(error)) {
                    AuthUtils.refreshToken(context, new AuthUtils.TokenRefreshCallback() {
                        @Override
                        public void onTokenRefreshed(String newToken) {
                            retryApiCall(newToken);
                        }

                        @Override
                        public void onTokenRefreshFailed() {
                            Log.e("API_CALL", "Token refresh failed");
                        }
                    });
                }
                else {
                    if ("Invalid token".equals(error)) {
                        Log.e("API_CALL", "Invalid token");
                    }
                    // Handle other errors
                    else {
                        Log.d("API_CALL", "API call failed: " + error);
                        handleFailResponse(response, responseBody); //Override to handle error response
                    }
                }
            }
            catch (Exception e) {
                Log.e("API_CALL", "Failed to parse error message", e);
            }
        }
        else {
            handleSuccessResponse(response); // Override to handle successful response
        }
    }

    // Retry the API call with the new token
    private void retryApiCall(String newToken) {
        Request newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + newToken)
                .build();

        CallAPI.getClient().newCall(newRequest).enqueue(this);
    }

    public abstract void handleSuccessResponse(Response response) throws IOException;
    public abstract void handleFailResponse(Response response, String responseBody);
}