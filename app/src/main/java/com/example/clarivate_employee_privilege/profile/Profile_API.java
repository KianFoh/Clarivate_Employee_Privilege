package com.example.clarivate_employee_privilege.profile;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.api.CallAPI;
import com.example.clarivate_employee_privilege.api.CustomCallback;
import com.example.clarivate_employee_privilege.utils.ToastUtils;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Profile_API {
    private Context context;

    public Profile_API(Context context) {
        this.context = context;
    }

    // Add Admin
    public void add_admin(String body, AlertDialog dialog) {
        Headers headers = new Headers.Builder()
                .add("Authorization", "Bearer " + context
                        .getSharedPreferences("user_info", Context.MODE_PRIVATE)
                        .getString("google_idToken", ""))
                .build();

        Request request = new Request.Builder()
                .url(context.getString(R.string.api_url) + "/add_admin")
                .post(RequestBody.create(body, MediaType.get("application/json; charset=utf-8")))
                .headers(headers)
                .build();

        CallAPI.getClient().newCall(request).enqueue(new CustomCallback(context, request) {

            @Override
            public void onFailure(Call call, IOException e) {
                ((Activity) context).runOnUiThread(() -> {
                    // Show fail api call message
                    Log.d("API_CALL_ADD_ADMIN", e.toString());
                    String message = "Failed to add admin";
                    ToastUtils.showToast(context, message, false);
                });
            }

            @Override
            public void handleSuccessResponse(Response response) {
                Log.d("API_CALL_ADD_ADMIN", "Admin added successfully");
                // Show success message
                ((Activity) context).runOnUiThread(() -> {
                    String message = body + " added as Admin";
                    ToastUtils.showToast(context, message, true);
                    dialog.dismiss();
                });
            }

            @Override
            public void handleFailResponse(Response response, String responseBody) {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                String error = jsonObject.get("error").getAsString();
                Log.e("API_CALL_ADD_ADMIN", "API call failed: " + error);
                ((Activity) context).runOnUiThread(() -> {
                    TextInputLayout email_v = dialog.findViewById(R.id.admin_email);
                    email_v.setError((("**"+error)));
                });
            }
        });
    }

    // Remove Admin
    public void remove_admin(String email, AlertDialog dialog) {
        Headers headers = new Headers.Builder()
                .add("Authorization", "Bearer " + context
                        .getSharedPreferences("user_info", Context.MODE_PRIVATE)
                        .getString("google_idToken", ""))
                .build();

        // Build the URL with the email as a query parameter
        String url = context.getString(R.string.api_url) + "/remove_admin?email=" + email;

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .headers(headers)
                .build();

        CallAPI.getClient().newCall(request).enqueue(new CustomCallback(context, request) {

            @Override
            public void onFailure(Call call, IOException e) {
                ((Activity) context).runOnUiThread(() -> {
                    // Show fail API call message
                    Log.d("ERROR", e.toString());
                    String message = "Failed to remove admin";
                    ToastUtils.showToast(context, message, false);
                });
            }

            @Override
            public void handleSuccessResponse(Response response) {
                Log.d("API_CALL_REMOVE_ADMIN", "Admin removed successfully");
                // Show success message
                ((Activity) context).runOnUiThread(() -> {
                    String message = email + " removed from Admin";
                    ToastUtils.showToast(context, message, true);
                    dialog.dismiss();
                });
            }

            @Override
            public void handleFailResponse(Response response, String responseBody) {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                String error = jsonObject.get("error").getAsString();
                Log.e("API_CALL_REMOVE_ADMIN", "API call failed: " + error);
                ((Activity) context).runOnUiThread(() -> {
                    TextInputLayout email_v = dialog.findViewById(R.id.admin_email);
                    email_v.setError(error);
                });
            }
        });
    }
}