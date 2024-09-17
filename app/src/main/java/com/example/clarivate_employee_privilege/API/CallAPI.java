package com.example.clarivate_employee_privilege.API;

import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class CallAPI {

    private static final OkHttpClient client = new OkHttpClient();

    public static void post(String url, String json, Callback callback, Headers headers) {
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .headers(headers)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void put(String url, String json, Callback callback, Headers headers) {
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .headers(headers)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void get(String url, Callback callback, Headers headers) {
        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void delete(String url, Callback callback, Headers headers) {
        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .delete()
                .build();

        client.newCall(request).enqueue(callback);
    }
}
