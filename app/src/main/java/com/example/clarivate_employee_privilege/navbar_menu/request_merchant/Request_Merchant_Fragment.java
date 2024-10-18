package com.example.clarivate_employee_privilege.navbar_menu.request_merchant;

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
import com.example.clarivate_employee_privilege.api.Call_API;
import com.example.clarivate_employee_privilege.api.Custom_Callback;
import com.example.clarivate_employee_privilege.utils.App_Utils;
import com.example.clarivate_employee_privilege.utils.Toast_Utils;
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

public class Request_Merchant_Fragment extends Fragment {

    private TextInputLayout name_field;
    private TextInputLayout type_field;
    private TextInputLayout contact_field;

    public Request_Merchant_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request_merchant, container, false);

        // Type of business
        String[] typeOfBusiness = new String[] {"F&B", "LifeStyle"};

        AutoCompleteTextView type_dropdown = view.findViewById(R.id.requestmerchant_type_dropdown);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, typeOfBusiness);

        // Set the adapter to the AutoCompleteTextView
        type_dropdown.setAdapter(adapter);


        name_field = view.findViewById(R.id.requestmerchant_name_field);
        type_field = view.findViewById(R.id.requestmerchant_type_field);
        contact_field = view.findViewById(R.id.requestmerchant_contact_field);
        Button submit = view.findViewById(R.id.requestmerchant_submit);

        submit.setOnClickListener(v -> {
            App_Utils.disableButton(v);
            clearErrors();
            String name = name_field.getEditText().getText().toString();
            String type = type_field.getEditText().getText().toString();
            String contact = contact_field.getEditText().getText().toString();
            add_request_merchant(requireActivity(), name, type, contact, () -> v.setEnabled(true));
        });

        App_Utils.setToolbarTitle(requireActivity(), "Request Merchant");

        return view;
    }

    private void add_request_merchant(Context context, String name, String type, String contact, Runnable enableButton) {

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
                .url(context.getString(R.string.api_url) + "/request_merchant")
                .post(RequestBody.create(body, MediaType.get("application/json; charset=utf-8")))
                .headers(headers)
                .build();

        Call_API.getClient().newCall(request).enqueue(new Custom_Callback(context, request, enableButton) {

            @Override
            public void onFailure(Call call, IOException e) {
                ((Activity) context).runOnUiThread(() -> {
                    // Show fail api call message
                    Log.d("API_CALL_ADD_REQUEST_MERCHANT", e.toString());
                    String message = "Failed to add request merchant";
                    Toast_Utils.showToast(context, message, false);
                });
            }

            @Override
            public void handleSuccessResponse(Response response) {
                Log.d("API_CALL_ADD_REQUEST_MERCHANT", "Requested merchant added successfully");
                // Show success message
                ((Activity) context).runOnUiThread(() -> {
                    String message = name_field.getEditText().getText().toString() + " added to request merchant list";
                    Toast_Utils.showToast(context, message, true);
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