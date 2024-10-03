package com.example.clarivate_employee_privilege.navbar_menu.profile;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.api.CallAPI;
import com.example.clarivate_employee_privilege.api.CustomCallback;
import com.example.clarivate_employee_privilege.utils.ToastUtils;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

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
                .url(context.getString(R.string.api_url) + "/admin")
                .post(RequestBody.create(body, MediaType.get("application/json; charset=utf-8")))
                .headers(headers)
                .build();

        CallAPI.getClient().newCall(request).enqueue(new CustomCallback(context, request) {

            @Override
            public void onFailure(Call call, IOException e) {
                ((Activity) context).runOnUiThread(() -> {
                    // Show fail api call message
                    Log.d("API_CALL_ADD_ADMIN_ERROR", e.toString());
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
                    TextInputLayout email_v = dialog.findViewById(R.id.adminmanage_adminemail);
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
        String url = context.getString(R.string.api_url) + "/admin?email=" + email;

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
                    Log.d("API_CALL_REMOVE_ADMIN_ERROR", e.toString());
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
                    TextInputLayout email_v = dialog.findViewById(R.id.adminmanage_adminemail);
                    email_v.setError("**"+error);
                });
            }
        });
    }

    public static void downloadRequests(Context context) {

        Headers headers = new Headers.Builder()
                .add("Authorization", "Bearer " + context
                        .getSharedPreferences("user_info", Context.MODE_PRIVATE)
                        .getString("google_idToken", ""))
                .build();

        String url = context.getString(R.string.api_url) + "/export_request_merchants";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .headers(headers)
                .build();

        CallAPI.getClient().newCall(request).enqueue(new CustomCallback(context, request) {

            @Override
            public void onFailure(Call call, IOException e) {
                ((Activity) context).runOnUiThread(() -> {
                    Log.d("API_CALL_DOWNLOAD_REQUESTS_ERROR", e.toString());
                    String message = "Failed to download requests";
                    ToastUtils.showToast(context, message, false);
                });
            }

            @Override
            public void handleSuccessResponse(Response response) {

                ResponseBody responseBody = response.body();

                // Define the file path in the Downloads directory
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(downloadsDir, "request_merchants.xlsx");

                try (InputStream inputStream = responseBody.byteStream();
                     FileOutputStream fileOutputStream = new FileOutputStream(file)) {

                    // Write the response body to the file
                    byte[] buffer = new byte[2048];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }

                    ((Activity) context).runOnUiThread(() -> {
                        ToastUtils.showToast(context, "Requests downloaded successfully", true);
                    });

                }
                catch (IOException e) {
                    ((Activity) context).runOnUiThread(() -> {
                        ToastUtils.showToast(context, "Failed to save the file", false);
                    });
                }
            }

            @Override
            public void handleFailResponse(Response response, String responseBody) {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                String error = jsonObject.get("error").getAsString();
                Log.e("API_CALL_DOWNLOAD_REQUESTS", "API call failed: " + error);
                ((Activity) context).runOnUiThread(() -> {
                    ToastUtils.showToast(context, "Failed to download requests: " + error, false);
                });
            }
        });
    }
}