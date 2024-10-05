package com.example.clarivate_employee_privilege.navbar_menu.merchantdetail;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.api.CallAPI;
import com.example.clarivate_employee_privilege.api.CustomCallback;
import com.example.clarivate_employee_privilege.utils.ToastUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
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

                    // Go back to the previous fragment in the stack
                    ((FragmentActivity) context).getSupportFragmentManager().popBackStack();
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
    public static void edit_merchant(Context context, JsonObject merchantData, Merchant_Edit fragment) {
        Headers headers = new Headers.Builder()
                .add("Authorization", "Bearer " + context
                        .getSharedPreferences("user_info", Context.MODE_PRIVATE)
                        .getString("google_idToken", ""))
                .add("Content-Type", "application/json")
                .build();

        // Build the URL with the email as a query parameter
        String url = context.getString(R.string.api_url) + "/merchant";

        Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create(merchantData.toString(), MediaType.get("application/json; charset=utf-8")))
                .headers(headers)
                .build();

        CallAPI.getClient().newCall(request).enqueue(new CustomCallback(context, request) {

            @Override
            public void onFailure(Call call, IOException e) {
                ((Activity) context).runOnUiThread(() -> {
                    // Show fail API call message
                    Log.d("API_CALL_EDIT_MERCHANT_ERROR", e.toString());
                    String message = "Failed to edit merchant";
                    ToastUtils.showToast(context, message, false);
                });
            }

            @Override
            public void handleSuccessResponse(Response response) {
                Log.d("API_CALL_EDIT_MERCHANT", "Merchant edited successfully");
                // Show success message
                ((Activity) context).runOnUiThread(() -> {
                    String message = "Merchant edited successfully";
                    ToastUtils.showToast(context, message, true);

                    // Go back to the previous fragment in the stack
                    ((FragmentActivity) context).getSupportFragmentManager().popBackStack();
                });
            }

            @Override
            public void handleFailResponse(Response response, String responseBody) {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                String inputfield = jsonObject.get("input_field").getAsString();
                String error = jsonObject.get("error").getAsString();
                Log.e("API_CALL_EDIT_MERCHANT", "API call failed: " + error);

                ((Activity) context).runOnUiThread(() -> {
                    fragment.handleError(inputfield, error);
                });
            }
        });
    }
}