package com.example.clarivate_employee_privilege.api;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.clarivate_employee_privilege.authentication.Auth_Utils;

import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

// CustomCallback use as standard callback for refresh token and retry api call
public abstract class Custom_Callback implements Callback {

    private final Context context;
    private final Request originalRequest;
    private final Runnable enableButtonRunnable;
    private static final int MAX_RETRIES = 3;
    private int retryCount = 0;

    public Custom_Callback(Context context, Request originalRequest) {
        this(context, originalRequest, null);
    }

    public Custom_Callback(Context context, Request originalRequest, Runnable enableButtonRunnable) {
        this.context = context;
        this.originalRequest = originalRequest;
        this.enableButtonRunnable = enableButtonRunnable;
    }

    // API call failed
    @Override
    public void onFailure(Call call, IOException e) {
        if (e instanceof SocketTimeoutException && retryCount < MAX_RETRIES) {
            retryCount++;
            Log.d("API_CALL", "Timeout occurred, retrying... (" + retryCount + "/" + MAX_RETRIES + ")");
            retryApiCall(null);
        } else {
            Log.e("API_CALL", "API call failed: " + e.getMessage());
            if (enableButtonRunnable != null) {
                ((Activity) context).runOnUiThread(enableButtonRunnable);
            }
        }
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
                    Auth_Utils.refreshToken(context, new Auth_Utils.TokenRefreshCallback() {
                        @Override
                        public void onTokenRefreshed(String newToken) {
                            retryApiCall(newToken);
                        }
                    });
                } else {
                    if ("Invalid token".equals(error)) {
                        Log.e("API_CALL", "Invalid token");
                    }
                    // Handle other errors
                    else {
                        Log.d("API_CALL", "API call failed: " + error);
                        handleFailResponse(response, responseBody); // Override to handle error response
                    }
                    if (enableButtonRunnable != null) {
                        ((Activity) context).runOnUiThread(enableButtonRunnable);
                    }
                }
            } catch (Exception e) {
                Log.e("API_CALL", "Failed to parse error message", e);
                if (enableButtonRunnable != null) {
                    ((Activity) context).runOnUiThread(enableButtonRunnable);
                }
            }
        } else {
            handleSuccessResponse(response); // Override to handle successful response
            if (enableButtonRunnable != null) {
                ((Activity) context).runOnUiThread(enableButtonRunnable);
            }
        }
    }

    // Retry the API call with the new token
    private void retryApiCall(String newToken) {
        Request.Builder requestBuilder = originalRequest.newBuilder();
        if (newToken != null) {
            requestBuilder.header("Authorization", "Bearer " + newToken);
        }
        Request newRequest = requestBuilder.build();
        Call_API.getClient().newCall(newRequest).enqueue(this);
    }

    public abstract void handleSuccessResponse(Response response) throws IOException;
    public abstract void handleFailResponse(Response response, String responseBody);
}