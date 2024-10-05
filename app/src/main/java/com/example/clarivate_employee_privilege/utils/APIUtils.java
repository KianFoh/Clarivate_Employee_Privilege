// APIUtils.java
package com.example.clarivate_employee_privilege.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.api.CallAPI;
import com.example.clarivate_employee_privilege.api.CustomCallback;
import com.example.clarivate_employee_privilege.authentication.AuthUtils;
import com.example.clarivate_employee_privilege.websocket.EventBus;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;

public class APIUtils {

    public static void loadUserInfo(Context context) {
        // Update the user's ID token and profile image URL
        AuthUtils.refreshToken(context);

        // Get old user information for API call
        SharedPreferences sharedpreferences = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String token = sharedpreferences.getString("google_idToken", "Not found");
        String email = sharedpreferences.getString("email", "Not found");

        // Build the API request
        Headers headers = new Headers.Builder()
                .add("Authorization", "Bearer " + token)
                .build();

        Request request = new Request.Builder()
                .url(context.getString(R.string.api_url) + "/user?email=" + email)
                .get()
                .headers(headers)
                .build();

        // Make the API call
        CallAPI.getClient().newCall(request).enqueue(new CustomCallback((Activity) context, request) {

            @Override
            public void onFailure(Call call, IOException e) {
                ((Activity) context).runOnUiThread(() -> {
                    Log.d("ERROR_API_CALL_GET_USER_INFO", e.toString());
                    ToastUtils.showToast(context, "Failed to load latest user information", false);
                    AppUtils.showLoading(false, ((Activity) context).findViewById(R.id.main_progressbar));
                });
            }

            @Override
            public void handleSuccessResponse(Response response) throws IOException {
                // Extract user information from the API response
                String responseData = response.body().string();
                JsonObject jsonObject = JsonParser.parseString(responseData).getAsJsonObject();
                JsonObject userObject = jsonObject.getAsJsonObject("user");

                // Update the isAdmin LiveData in EventBus
                boolean isAdmin = userObject.get("admin").getAsBoolean();
                EventBus.getInstance().postAdminStatusUpdate(isAdmin);

                ((Activity) context).runOnUiThread(() -> {
                    AppUtils.showLoading(false, ((Activity) context).findViewById(R.id.main_progressbar));
                });
            }

            @Override
            public void handleFailResponse(Response response, String responseBody) {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                String error = jsonObject.get("error").getAsString();
                Log.e("API_CALL_GET_USER_INFO", "API call failed: " + error);
                ((Activity) context).runOnUiThread(() -> {
                    ToastUtils.showToast(context, "Failed to load latest user information", false);
                    AppUtils.showLoading(false, ((Activity) context).findViewById(R.id.main_progressbar));
                });
            }
        });
    }

    public static void loadCategories(Context context) {
        Headers headers = new Headers.Builder()
                .add("Authorization", "Bearer " + context
                        .getSharedPreferences("user_info", Context.MODE_PRIVATE)
                        .getString("google_idToken", ""))
                .build();

        Request request = new Request.Builder()
                .url(context.getString(R.string.api_url) + "/categories")
                .get()
                .headers(headers)
                .build();

        CallAPI.getClient().newCall(request).enqueue(new CustomCallback(context, request) {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("ERROR_API_CALL_GET_CATEGORIES", e.toString());
            }

            @Override
            public void handleSuccessResponse(Response response) throws IOException {
                String responseData = response.body().string();
                JsonObject responseObject = JsonParser.parseString(responseData).getAsJsonObject();
                JsonArray categoriesArray = responseObject.getAsJsonArray("Categories");
                EventBus.getInstance().postCategoriesUpdate(categoriesArray);
            }

            @Override
            public void handleFailResponse(Response response, String responseBody) {
                Log.e("API_CALL_GET_CATEGORIES", "API call failed: " + responseBody);
            }
        });
    }

    public static void loadMerchants(Context context) {
        Headers headers = new Headers.Builder()
                .add("Authorization", "Bearer " + context
                        .getSharedPreferences("user_info", Context.MODE_PRIVATE)
                        .getString("google_idToken", ""))
                .build();

        Request request = new Request.Builder()
                .url(context.getString(R.string.api_url) + "/merchants")
                .get()
                .headers(headers)
                .build();

        CallAPI.getClient().newCall(request).enqueue(new CustomCallback(context, request) {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("API_CALL_GET_MERCHANTS_ERROR", e.toString());
            }

            @Override
            public void handleSuccessResponse(Response response) throws IOException {
                String responseData = response.body().string();
                JsonObject responseObject = JsonParser.parseString(responseData).getAsJsonObject();
                JsonArray merchantsArray = responseObject.getAsJsonArray("Merchants");
                EventBus.getInstance().postMerchantsUpdate(merchantsArray);
                Log.d("API_CALL_GET_MERCHANTS", "Merchants loaded: " + merchantsArray);
            }

            @Override
            public void handleFailResponse(Response response, String responseBody) {
                Log.d("API_CALL_GET_MERCHANTS", "API call failed: " + responseBody);
            }
        });
    }

    public static void loadMerchantById(Context context, String merchantId) {
        Headers headers = new Headers.Builder()
                .add("Authorization", "Bearer " + context
                        .getSharedPreferences("user_info", Context.MODE_PRIVATE)
                        .getString("google_idToken", ""))
                .build();

        Request request = new Request.Builder()
                .url(context.getString(R.string.api_url) + "/merchant?id=" + merchantId)
                .get()
                .headers(headers)
                .build();

        CallAPI.getClient().newCall(request).enqueue(new CustomCallback(context, request) {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("API_CALL_GET_MERCHANT_BY_ID_ERROR", e.toString());
            }

            @Override
            public void handleSuccessResponse(Response response) throws IOException {
                String responseData = response.body().string();
                JsonObject responseObject = JsonParser.parseString(responseData).getAsJsonObject();
                EventBus.getInstance().postMerchantByIdUpdate(responseObject);
                Log.d("API_CALL_GET_MERCHANT_BY_ID", "Merchant loaded: " + responseObject);
            }

            @Override
            public void handleFailResponse(Response response, String responseBody) {
                Log.d("API_CALL_GET_MERCHANT_BY_ID", "API call failed: " + responseBody);
            }
        });
    }
}