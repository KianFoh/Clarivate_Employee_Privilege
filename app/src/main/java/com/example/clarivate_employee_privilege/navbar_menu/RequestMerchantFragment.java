package com.example.clarivate_employee_privilege.navbar_menu;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.fragment.app.Fragment;

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

public class RequestMerchantFragment extends Fragment {

    private TextInputLayout name_field;
    private TextInputLayout type_field;
    private TextInputLayout contact_field;

    public RequestMerchantFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request_merchant, container, false);

        // Type of business
        String[] typeOfBusiness = new String[] {"F&B", "LifeStyle"};

        AutoCompleteTextView type_dropdown = view.findViewById(R.id.type_dropdown);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, typeOfBusiness);

        // Set the adapter to the AutoCompleteTextView
        type_dropdown.setAdapter(adapter);


        name_field = view.findViewById(R.id.name);
        type_field = view.findViewById(R.id.type);
        contact_field = view.findViewById(R.id.contact);
        Button submit = view.findViewById(R.id.submit);

        submit.setOnClickListener(v -> {
            clearErrors();
            String name = name_field.getEditText().getText().toString();
            String type = type_field.getEditText().getText().toString();
            String contact = contact_field.getEditText().getText().toString();
            add_request_merchant(requireActivity(), name, type, contact);
        });


        return view;
    }

    private void add_request_merchant(Context context, String name, String type, String contact) {

        JsonObject request_merchant = new JsonObject();
        request_merchant.addProperty("name", name);
        request_merchant.addProperty("type", type);
        request_merchant.addProperty("contact", contact);

        String body = request_merchant.toString();

        Headers headers = new Headers.Builder()
                .add("Authorization", "Bearer " + context
                        .getSharedPreferences("user_info", Context.MODE_PRIVATE)
                        .getString("google_idToken", ""))
                .build();

        Request request = new Request.Builder()
                .url(context.getString(R.string.api_url) + "/add_request_merchant")
                .post(RequestBody.create(body, MediaType.get("application/json; charset=utf-8")))
                .headers(headers)
                .build();

        CallAPI.getClient().newCall(request).enqueue(new CustomCallback(context, request) {

            @Override
            public void onFailure(Call call, IOException e) {
                ((Activity) context).runOnUiThread(() -> {
                    // Show fail api call message
                    Log.d("API_CALL_ADD_REQUEST_MERCHANT", e.toString());
                    String message = "Failed to add request merchant";
                    ToastUtils.showToast(context, message, false);
                });
            }

            @Override
            public void handleSuccessResponse(Response response) {
                Log.d("API_CALL_ADD_REQUEST_MERCHANT", "Requested merchant added successfully");
                // Show success message
                ((Activity) context).runOnUiThread(() -> {
                    String message = body + " added as Admin";
                    ToastUtils.showToast(context, message, true);
                    name_field.getEditText().setText("");
                    type_field.getEditText().setText("");
                    contact_field.getEditText().setText("");
                });
            }

            @Override
            public void handleFailResponse(Response response, String responseBody) {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                String inputfield = jsonObject.get("input_field").getAsString();
                String error = jsonObject.get("error").getAsString();
                ((Activity) context).runOnUiThread(() -> {
                    switch (inputfield) {
                        case "Name":
                            name_field.setError(("** "+error));
                            break;
                        case "Type of business":
                            type_field.setError(("** "+error));
                            break;
                        case "Contact":
                            contact_field.setError(("** "+error));
                            break;
                    }
                });
            }
        });
    }
    private void clearErrors() {
        name_field.setError(null);
        type_field.setError(null);
        contact_field.setError(null);
    }
}