// APIUtils.java
package com.example.clarivate_employee_privilege.utils;

import android.content.Context;
import android.util.Log;

import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.api.CallAPI;
import com.example.clarivate_employee_privilege.api.CustomCallback;
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
}