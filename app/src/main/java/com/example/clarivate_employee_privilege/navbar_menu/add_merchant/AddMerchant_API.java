package com.example.clarivate_employee_privilege.navbar_menu.add_merchant;

import android.app.Activity;
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
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddMerchant_API {

    public static void addMerchant(Context context, JsonObject add_merchant, AddMerchantFragment fragment) {
        String body = add_merchant.toString();

        Headers headers = new Headers.Builder()
                .add("Authorization", "Bearer " + context
                        .getSharedPreferences("user_info", Context.MODE_PRIVATE)
                        .getString("google_idToken", ""))
                .build();

        Request request = new Request.Builder()
                .url(context.getString(R.string.api_url) + "/merchant")
                .post(RequestBody.create(body, MediaType.get("application/json; charset=utf-8")))
                .headers(headers)
                .build();

        CallAPI.getClient().newCall(request).enqueue(new CustomCallback(context, request) {

            @Override
            public void onFailure(Call call, IOException e) {
                ((Activity) context).runOnUiThread(() -> {
                    Log.d("API_CALL_ADD_MERCHANT", e.toString());
                    String message = "Failed to add merchant";
                    ToastUtils.showToast(context, message, false);
                });
            }

            @Override
            public void handleSuccessResponse(Response response) {
                Log.d("API_CALL_ADD_MERCHANT", "Merchant added successfully");
                ((Activity) context).runOnUiThread(() -> {
                    String message = fragment.getName().getEditText().getText().toString() + " added to merchant list";
                    ToastUtils.showToast(context, message, true);
                    fragment.clearAllFields();
                });
            }

            @Override
            public void handleFailResponse(Response response, String responseBody) {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                String inputfield = jsonObject.get("input_field").getAsString();
                String error = jsonObject.get("error").getAsString();
                Log.d("API_CALL_ADD_MERCHANT", "Add merchant fail: " + error);

                ((Activity) context).runOnUiThread(() -> {
                    fragment.handleError(inputfield, error);
                });
            }
        });
    }
}