package com.example.clarivate_employee_privilege.navbar_menu.add_merchant;

import static com.example.clarivate_employee_privilege.utils.APIUtils.loadCategories;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.api.CallAPI;
import com.example.clarivate_employee_privilege.api.CustomCallback;
import com.example.clarivate_employee_privilege.utils.ToastUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddMerchantFragment extends Fragment {

    private LinearLayout imageUrlLayout, adressLayout;
    private ImageView add_imageURL, add_address;
    private TextInputLayout imageurl, name, type, address, discount, info, terms;
    private JsonArray categories_json;
    private AutoCompleteTextView typeAutoComplete;
    private MutableLiveData<JsonArray> categoriesLiveData = new MutableLiveData<>();

    public AddMerchantFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_merchant, container, false);

        typeAutoComplete = view.findViewById(R.id.addmerchant_type);
        // Dynamically adjust dropdown height based on the number of suggestions

        // Observe the categoriesLiveData
        categoriesLiveData.observe(getViewLifecycleOwner(), new Observer<JsonArray>() {
            @Override
            public void onChanged(JsonArray categories_json) {
                if (categories_json != null) {
                    categories_json = categories_json; // Update the private variable
                    List<String> categoryNames = new ArrayList<>();
                    for (int i = 0; i < categories_json.size(); i++) {
                        JsonObject category = categories_json.get(i).getAsJsonObject();
                        String categoryName = category.get("Name").getAsString();
                        categoryNames.add(categoryName);
                    }

                    // Set up the AutoCompleteTextView
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            categoryNames
                    );
                    typeAutoComplete.setAdapter(adapter);
                    typeAutoComplete.setThreshold(1);

                    ScrollView scroll = view.findViewById(R.id.addmerchant_scroll);

                    // Dynamically adjust dropdown height based on the number of suggestions
                    typeAutoComplete.setOnFocusChangeListener((v, hasFocus) -> {
                        if (hasFocus) {
                            scroll.smoothScrollTo(0, discount.getTop());
                        }
                    });
                }
            }
        });


        // Load categories
        loadCategories(requireContext(), categoriesLiveData);


        imageurl = view.findViewById(R.id.addmerchant_imageurl_field);
        name = view.findViewById(R.id.addmerchant_name_field);
        type = view.findViewById(R.id.addmerchant_type_field);
        address = view.findViewById(R.id.addmerchant_address_field);
        discount = view.findViewById(R.id.addmerchant_discount_field);
        info = view.findViewById(R.id.addmerchant_info_field);
        terms = view.findViewById(R.id.addmerchant_terms_field);

        imageUrlLayout = view.findViewById(R.id.addmerchant_imageurl_layout);
        adressLayout = view.findViewById(R.id.addmerchant_address_layout);

        add_imageURL = view.findViewById(R.id.addmerchant_addimageurl_button);
        add_address = view.findViewById(R.id.addmerchant_addaddress_button);

        add_imageURL.setOnClickListener(v -> addNewFields(imageUrlLayout, "Paste Image URL"));
        add_address.setOnClickListener(v -> addNewFields(adressLayout, "Enter merchant address"));
        view.findViewById(R.id.addmerchant_submit).setOnClickListener(v -> {
            // Clear all errors
            clearErrors();
            addmerchant(requireContext());
        });

        return view;
    }

    private void addNewFields(LinearLayout layout, String hint) {
        // Create a new TextInputLayout
        TextInputLayout newTextInputLayout = new TextInputLayout(layout.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dpToPx(5));
        newTextInputLayout.setLayoutParams(params);
        newTextInputLayout.setHintEnabled(false);

        // Create a new TextInputEditText
        TextInputEditText newTextInputEditText = new TextInputEditText(newTextInputLayout.getContext());
        newTextInputEditText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        newTextInputEditText.setHint(hint);

        // Add the TextInputEditText to the TextInputLayout
        newTextInputLayout.addView(newTextInputEditText);
        newTextInputEditText.setMaxLines(1);
        newTextInputEditText.setSingleLine(true);
        newTextInputEditText.setHorizontallyScrolling(true);

        // Add the TextInputLayout to the specified LinearLayout
        layout.addView(newTextInputLayout);
    }

    private void clearErrors() {
        name.setError(null);
        type.setError(null);
        address.setError(null);
        discount.setError(null);
        terms.setError(null);
    }


    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void addmerchant(Context context) {
        // Collect all the values into a List
        List<String> imageURLList = new ArrayList<>();
        List<String> addressList = new ArrayList<>();

        // Get the text from dynamic fields
        getTextInputValues(imageURLList, imageUrlLayout);
        getTextInputValues(addressList, adressLayout);

        // Get the text from the other fields
        String merchantName = name.getEditText().getText().toString();
        String merchantType = type.getEditText().getText().toString();
        String merchantDiscount = discount.getEditText().getText().toString();
        String merchantInfo = info.getEditText().getText().toString();
        String merchantTerms = terms.getEditText().getText().toString();

        // Convert lists to JsonArray
        JsonArray imageURLArray = new JsonArray();
        for (String url : imageURLList) {
            imageURLArray.add(url);
        }

        JsonArray addressArray = new JsonArray();
        for (String addr : addressList) {
            addressArray.add(addr);
        }

        // Create the JSON object
        JsonObject add_merchant = new JsonObject();
        add_merchant.add("imageURL", imageURLArray);
        add_merchant.addProperty("name", merchantName);
        add_merchant.addProperty("type", merchantType);
        add_merchant.add("address", addressArray);
        add_merchant.addProperty("discount", merchantDiscount);
        add_merchant.addProperty("info", merchantInfo);
        add_merchant.addProperty("terms", merchantTerms);

        String body = add_merchant.toString();

        Headers headers = new Headers.Builder()
                .add("Authorization", "Bearer " + context
                        .getSharedPreferences("user_info", Context.MODE_PRIVATE)
                        .getString("google_idToken", ""))
                .build();

        Request request = new Request.Builder()
                .url(context.getString(R.string.api_url) + "/add_merchant")
                .post(RequestBody.create(body, MediaType.get("application/json; charset=utf-8")))
                .headers(headers)
                .build();

        CallAPI.getClient().newCall(request).enqueue(new CustomCallback(context, request) {

            @Override
            public void onFailure(Call call, IOException e) {
                ((Activity) context).runOnUiThread(() -> {
                    // Show fail api call message
                    Log.d("API_CALL_ADD_MERCHANT", e.toString());
                    String message = "Failed to add merchant";
                    ToastUtils.showToast(context, message, false);
                });
            }

            @Override
            public void handleSuccessResponse(Response response) {
                Log.d("API_CALL_ADD_MERCHANT", "Merchant added successfully");
                // Show success message
                ((Activity) context).runOnUiThread(() -> {
                    String message = name.getEditText().getText().toString() + " added to merchant list";
                    ToastUtils.showToast(context, message, true);

                    // Clear all static fields
                    imageurl.getEditText().setText("");
                    name.getEditText().setText("");
                    type.getEditText().setText("");
                    address.getEditText().setText("");
                    discount.getEditText().setText("");
                    info.getEditText().setText("");
                    terms.getEditText().setText("");

                    // Remove all dynamically added fields except the first one
                    removeAllViewsExceptFirst(imageUrlLayout);
                    removeAllViewsExceptFirst(adressLayout);
                });
            }

            @Override
            public void handleFailResponse(Response response, String responseBody) {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                String inputfield = jsonObject.get("input_field").getAsString();
                String error = jsonObject.get("error").getAsString();
                Log.d("API_CALL_ADD_MERCHANT", "Add merchant fail: " + error);

                ((Activity) context).runOnUiThread(() -> {
                    switch (inputfield) {
                        case "Name":
                            name.setError(("** " + error));
                            break;

                        case "Type":
                            type.setError(("** " + error));
                            break;

                        case "Address":
                            address.setError(("** " + error));
                            break;

                        case "Discount":
                            discount.setError(("** " + error));
                            break;

                        case "Terms":
                            terms.setError(("** " + error));
                            break;

                        default:
                            ToastUtils.showToast(context, error, false);
                            break;
                    }
                });
            }
        });
    }

    private void removeAllViewsExceptFirst(LinearLayout layout) {
        for (int i = layout.getChildCount() - 1; i > 0; i--) {
            layout.removeViewAt(i);
        }
    }

    private List<String> getTextInputValues(List<String> list,LinearLayout layout) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof TextInputLayout) {
                TextInputEditText editText = (TextInputEditText) ((TextInputLayout) child).getEditText();
                if (editText != null) {
                    list.add(editText.getText().toString());
                }
            }
        }
        return list;
    }
}