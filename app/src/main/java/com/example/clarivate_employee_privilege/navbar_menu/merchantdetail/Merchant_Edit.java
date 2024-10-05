package com.example.clarivate_employee_privilege.navbar_menu.merchantdetail;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.utils.MerchantUtils;
import com.example.clarivate_employee_privilege.utils.ToastUtils;
import com.example.clarivate_employee_privilege.websocket.EventBus;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class Merchant_Edit extends Fragment {

    private JsonObject merchantData;
    private TextInputLayout name, type, discount, moreInfoTextView, terms, address;
    private LinearLayout imageUrlLayout, addressLayout;
    private ImageView addImageUrlButton, addAddressButton;
    private Button saveButton;
    private AutoCompleteTextView typeAutoComplete;
    private ArrayAdapter<String> adapter;
    private JsonArray categories_json;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_merchant_edit, container, false);

        if (getArguments() != null) {
            merchantData = JsonParser.parseString(getArguments().getString("merchantData"))
                    .getAsJsonObject()
                    .get("Merchant")
                    .getAsJsonObject();

            // Retrieve views from the fragment
            name = view.findViewById(R.id.merchantForm_name_field);
            type = view.findViewById(R.id.merchantForm_type_field);
            discount = view.findViewById(R.id.merchantForm_discount_field);
            moreInfoTextView = view.findViewById(R.id.merchantForm_info_field);
            terms = view.findViewById(R.id.merchantForm_terms_field);
            address = view.findViewById(R.id.merchantForm_address_field);
            imageUrlLayout = view.findViewById(R.id.merchantForm_imageurl_layout);
            addressLayout = view.findViewById(R.id.merchantForm_address_layout);
            addImageUrlButton = view.findViewById(R.id.merchantForm_addimageurl_button);
            addAddressButton = view.findViewById(R.id.merchantForm_addaddress_button);
            Button cancelButton = view.findViewById(R.id.editmerchant_cancel);
            saveButton = view.findViewById(R.id.editmerchant_save);
            typeAutoComplete = view.findViewById(R.id.merchantForm_type);

            // Assign values to views
            name.getEditText().setText(merchantData.get("Name").getAsString());
            type.getEditText().setText(merchantData.get("Category").getAsString());
            discount.getEditText().setText(merchantData.get("Discount").getAsString());
            moreInfoTextView.getEditText().setText(merchantData.get("More Info").getAsString());
            terms.getEditText().setText(merchantData.get("Terms").getAsString());

            // Listeners
            addImageUrlButton.setOnClickListener(v -> MerchantUtils.addNewFields(imageUrlLayout, "Paste Image URL", getContext()));
            addAddressButton.setOnClickListener(v -> MerchantUtils.addNewFields(addressLayout, "Enter merchant address", getContext()));
            cancelButton.setOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());
            saveButton.setOnClickListener(v -> saveEditedMerchant());

            // Generate fields for image URLs
            JsonArray imageUrls = merchantData.get("Images").getAsJsonArray();
            if (imageUrls.size() > 0) {
                TextInputLayout defaultImageUrlField = (TextInputLayout) imageUrlLayout.getChildAt(0);
                defaultImageUrlField.getEditText().setText(imageUrls.get(0).getAsString());
                for (int i = 1; i < imageUrls.size(); i++) {
                    String imageUrl = imageUrls.get(i).getAsString();
                    MerchantUtils.addNewFields(imageUrlLayout, "Paste Image URL", getContext());
                    TextInputLayout textInputLayout = (TextInputLayout) imageUrlLayout.getChildAt(i);
                    textInputLayout.getEditText().setText(imageUrl);
                }
            }

            // Generate fields for addresses
            JsonArray addresses = merchantData.get("Addresses").getAsJsonArray();
            if (addresses.size() > 0) {
                TextInputLayout defaultAddressField = (TextInputLayout) addressLayout.getChildAt(0);
                defaultAddressField.getEditText().setText(addresses.get(0).getAsString());
                for (int i = 1; i < addresses.size(); i++) {
                    String address = addresses.get(i).getAsString();
                    MerchantUtils.addNewFields(addressLayout, "Enter merchant address", getContext());
                    TextInputLayout textInputLayout = (TextInputLayout) addressLayout.getChildAt(i);
                    textInputLayout.getEditText().setText(address);
                }
            }
        }

        observeCategories();
        return view;
    }

    public void saveEditedMerchant() {
        // Extract data from fields
        String name = this.name.getEditText().getText().toString();
        String type = this.type.getEditText().getText().toString();
        String discount = this.discount.getEditText().getText().toString();
        String moreInfo = moreInfoTextView.getEditText().getText().toString();
        String terms = this.terms.getEditText().getText().toString();

        // Get image URLs and addresses
        List<String> imageUrls = new ArrayList<>();
        List<String> addresses = new ArrayList<>();
        MerchantUtils.getTextInputValues(addresses, addressLayout);
        Log.d("EditMerchant", "Addresses: " + addresses);

        // Validate image URLs
        CountDownLatch latch = new CountDownLatch(imageUrlLayout.getChildCount());
        AtomicBoolean hasError = new AtomicBoolean(false);
        MerchantUtils.validateImageURLs(imageUrls, imageUrlLayout, latch, hasError);

        // Wait for validation to complete
        new Thread(() -> {
            try {
                latch.await();
                if (hasError.get()) {
                    return;
                }

                // Convert lists to JsonArray
                JsonArray imageUrlsJsonArray = MerchantUtils.convertListToJsonArray(imageUrls);
                JsonArray addressesJsonArray = MerchantUtils.convertListToJsonArray(addresses);

                // Update merchantData
                merchantData.addProperty("Name", name);
                merchantData.addProperty("Category", type);
                merchantData.addProperty("Discount", discount);
                merchantData.addProperty("More Info", moreInfo);
                merchantData.addProperty("Terms", terms);
                merchantData.add("Images", imageUrlsJsonArray);
                merchantData.add("Addresses", addressesJsonArray);

                Log.d("EditMerchant", "Updated merchant data: " + merchantData);

                // Call the API to update the merchant
                Merchant_API.edit_merchant(requireContext(), merchantData, Merchant_Edit.this);
            } catch (InterruptedException e) {
                Log.d("EditMerchant", "Error waiting for image URL validation: " + e.getMessage());
            }
        }).start();
    }

    public void observeCategories() {
        EventBus.getInstance().getCategoriesLiveData().observe(requireActivity(), categories -> {
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
        } else {
            adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    newCategoryNames
            );
            typeAutoComplete.setAdapter(adapter);
            typeAutoComplete.setThreshold(1);
        }
    }

    public void handleError(String inputfield, String error) {
        switch (inputfield) {
            case "Name":
                name.setError(("** " + error));
                break;
            case "Category":
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
                ToastUtils.showToast(getContext(), error, false);
                break;
        }
    }
}