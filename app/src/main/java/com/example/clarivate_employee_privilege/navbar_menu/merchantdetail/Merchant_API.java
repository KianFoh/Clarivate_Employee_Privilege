package com.example.clarivate_employee_privilege.navbar_menu.merchantdetail;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.api.CallAPI;
import com.example.clarivate_employee_privilege.api.CustomCallback;
import com.example.clarivate_employee_privilege.utils.ToastUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;

public class Merchant_API {

    public static void deleteMerchant(Context context, AlertDialog dialog, String merchantId) {
        Headers headers = new Headers.Builder()
                .add("Authorization", "Bearer " + context
                        .getSharedPreferences("user_info", Context.MODE_PRIVATE)
                        .getString("google_idToken", ""))
                .build();

        // Build the URL with the email as a query parameter
        String url = context.getString(R.string.api_url) + "/merchant?id=" + merchantId;

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
                    Log.d("API_CALL_REMOVE_MERCHANT_ERROR", e.toString());
                    String message = "Failed to remove merchant";
                    ToastUtils.showToast(context, message, false);
                });
            }

            @Override
            public void handleSuccessResponse(Response response) {
                Log.d("API_CALL_REMOVE_MERCHANT", "Admin removed successfully");
                // Show success message
                ((Activity) context).runOnUiThread(() -> {
                    String message = "Merchant removed successfully";
                    ToastUtils.showToast(context, message, true);
                    dialog.dismiss();
                });
            }

            @Override
            public void handleFailResponse(Response response, String responseBody) {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                String error = jsonObject.get("error").getAsString();
                Log.e("API_CALL_REMOVE_MERCHANT", "API call failed: " + error);
                ((Activity) context).runOnUiThread(() -> {
                    ToastUtils.showToast(context, error, false);
                });
            }
        });
    }
}
