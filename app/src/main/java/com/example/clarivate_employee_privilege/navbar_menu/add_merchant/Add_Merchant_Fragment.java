package com.example.clarivate_employee_privilege.navbar_menu.add_merchant;

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

import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.utils.App_Utils;
import com.example.clarivate_employee_privilege.utils.Merchant_Utils;
import com.example.clarivate_employee_privilege.websocket.Event_Bus;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class Add_Merchant_Fragment extends Fragment {

    private LinearLayout imageUrlLayout, addressLayout;
    private ImageView add_imageURL, add_address;
    private TextInputLayout imageurl, name, type, address, discount, info, terms;
    private JsonArray categories_json;
    private AutoCompleteTextView typeAutoComplete;
    private ArrayAdapter<String> adapter;

    public Add_Merchant_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_merchant, container, false);

        typeAutoComplete = view.findViewById(R.id.merchantForm_type);
        categories_json = Event_Bus.getInstance().getCategoriesLiveData().getValue();
        if (categories_json != null) {
            List<String> categoryNames = new ArrayList<>();
            for (int i = 0; i < categories_json.size(); i++) {
                JsonObject category = categories_json.get(i).getAsJsonObject();
                String categoryName = category.get("Name").getAsString();
                categoryNames.add(categoryName);
            }

            adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    categoryNames
            );
            typeAutoComplete.setAdapter(adapter);
            typeAutoComplete.setThreshold(1);

            ScrollView scroll = view.findViewById(R.id.addmerchant_scroll);
            typeAutoComplete.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    int offset = 400;
                    scroll.smoothScrollTo(type.getLeft(), ((int)type.getY()-offset));
                }
            });
        }
        observeCategories();

        imageurl = view.findViewById(R.id.merchantForm_imageurl_field);
        name = view.findViewById(R.id.merchantForm_name_field);
        type = view.findViewById(R.id.merchantForm_type_field);
        address = view.findViewById(R.id.merchantForm_address_field);
        discount = view.findViewById(R.id.merchantForm_discount_field);
        info = view.findViewById(R.id.merchantForm_info_field);
        terms = view.findViewById(R.id.merchantForm_terms_field);

        imageUrlLayout = view.findViewById(R.id.merchantForm_imageurl_layout);
        addressLayout = view.findViewById(R.id.merchantForm_address_layout);

        add_imageURL = view.findViewById(R.id.merchantForm_addimageurl_button);
        add_address = view.findViewById(R.id.merchantForm_addaddress_button);

        add_imageURL.setOnClickListener(v -> Merchant_Utils.addNewFields(imageUrlLayout, "Paste Image URL", getContext()));
        add_address.setOnClickListener(v -> Merchant_Utils.addNewFields(addressLayout, "Enter merchant address", getContext()));
        view.findViewById(R.id.addmerchant_submit).setOnClickListener(v -> {
            App_Utils.disableButton(v);
            clearErrors();
            addMerchant(requireContext(), () -> v.setEnabled(true));
        });

        App_Utils.setToolbarTitle(requireActivity(), "Add Merchant");

        return view;
    }

    public void observeCategories() {
        Event_Bus.getInstance().getCategoriesLiveData().observe(requireActivity(), categories -> {
            List<String> categoryNames = new ArrayList<>();
            for (int i = 0; i < categories.size(); i++) {
                JsonObject category = categories.get(i).getAsJsonObject();
                String categoryName = category.get("Name").getAsString();
                categoryNames.add(categoryName);
            }
            updateAdapter(categoryNames);
        });
    }

    public void updateAdapter(List<String> newCategoryNames) {
        if (adapter != null) {
            adapter.clear();
            adapter.addAll(newCategoryNames);
            adapter.notifyDataSetChanged();
        }
    }

    private void addMerchant(Context context, Runnable enableButtonRunnable) {
        // Initialize lists to store image URLs and addresses
        List<String> imageURLList = new ArrayList<>();
        List<String> addressList = new ArrayList<>();

        // AtomicBoolean to track if there are any errors during URL validation
        AtomicBoolean hasError = new AtomicBoolean(false);

        // CountDownLatch to wait for all image URL validations to complete
        CountDownLatch latch = new CountDownLatch(imageUrlLayout.getChildCount());

        // Validate image URLs and populate imageURLList
        Merchant_Utils.validateImageURLs(imageURLList, imageUrlLayout, latch, hasError);

        // Get text input values for addresses and populate addressList
        Merchant_Utils.getTextInputValues(addressList, addressLayout);

        // Start a new thread to wait for URL validations to complete
        new Thread(() -> {
            try {
                // Wait for all image URL validations to complete
                latch.await();

                // If there was an error, do not proceed with the API call
                if (hasError.get()) {
                    ((Activity) context).runOnUiThread(enableButtonRunnable);
                    return;
                }

                // Retrieve merchant details from input fields
                String merchantName = name.getEditText().getText().toString();
                String merchantType = type.getEditText().getText().toString();
                String merchantDiscount = discount.getEditText().getText().toString();
                String merchantInfo = info.getEditText().getText().toString();
                String merchantTerms = terms.getEditText().getText().toString();

                // Create a JSON object to hold the merchant details
                JsonObject add_merchant = new JsonObject();
                add_merchant.add("imageURL", Merchant_Utils.convertListToJsonArray(imageURLList));
                add_merchant.addProperty("name", merchantName);
                add_merchant.addProperty("type", merchantType);
                add_merchant.add("address", Merchant_Utils.convertListToJsonArray(addressList));
                add_merchant.addProperty("discount", merchantDiscount);
                add_merchant.addProperty("info", merchantInfo);
                add_merchant.addProperty("terms", merchantTerms);

                // Call the API to add the merchant
                Add_Merchant_API.addMerchant(context, add_merchant, Add_Merchant_Fragment.this, enableButtonRunnable);
            }
            catch (InterruptedException e) {
                Log.e("AddMerchantFragment", "Error waiting for image URL validation", e);
                ((Activity) context).runOnUiThread(enableButtonRunnable);
            }
        }).start();
    }

    public void clearAllFields() {
        imageurl.getEditText().setText("");
        name.getEditText().setText("");
        type.getEditText().setText("");
        address.getEditText().setText("");
        discount.getEditText().setText("");
        info.getEditText().setText("");
        terms.getEditText().setText("");
        Merchant_Utils.removeAllViewsExceptFirst(imageUrlLayout);
        Merchant_Utils.removeAllViewsExceptFirst(addressLayout);
    }
    public void handleError(String inputfield, String error) {
        Merchant_Utils.handleError(inputfield, error, name, type, addressLayout, discount, terms, getContext());
    }
    private void clearErrors() {
            Merchant_Utils.clearErrors(
                new TextInputLayout[]{name, type, address, discount, terms},
                imageUrlLayout, addressLayout
        );
    }
    public TextInputLayout getName() {
        return name;
    }
}