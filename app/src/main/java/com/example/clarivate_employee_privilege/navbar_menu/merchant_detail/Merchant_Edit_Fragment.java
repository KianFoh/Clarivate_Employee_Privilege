package com.example.clarivate_employee_privilege.navbar_menu.merchant_detail;

import static com.example.clarivate_employee_privilege.utils.API_Utils.loadMerchantById;

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
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.utils.App_Utils;
import com.example.clarivate_employee_privilege.utils.Merchant_Utils;
import com.example.clarivate_employee_privilege.websocket.Event_Bus;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class Merchant_Edit_Fragment extends Fragment {

    private JsonObject merchantData;
    private TextInputLayout name, type, discount, moreInfoTextView, terms;
    private LinearLayout imageUrlLayout, addressLayout;
    private ImageView addImageUrlButton, addAddressButton;
    private Button saveButton;
    private AutoCompleteTextView typeAutoComplete;
    private ArrayAdapter<String> categories_adapter;
    private JsonObject currentMerchantData;

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
            imageUrlLayout = view.findViewById(R.id.merchantForm_imageurl_layout);
            addressLayout = view.findViewById(R.id.merchantForm_address_layout);
            addImageUrlButton = view.findViewById(R.id.merchantForm_addimageurl_button);
            addAddressButton = view.findViewById(R.id.merchantForm_addaddress_button);
            Button cancelButton = view.findViewById(R.id.editmerchant_cancel);
            saveButton = view.findViewById(R.id.editmerchant_save);
            typeAutoComplete = view.findViewById(R.id.merchantForm_type);

            // Listeners
            addImageUrlButton.setOnClickListener(v -> Merchant_Utils.addNewFields(imageUrlLayout, "Paste Image URL", getContext()));
            addAddressButton.setOnClickListener(v -> Merchant_Utils.addNewFields(addressLayout, "Enter merchant address", getContext()));
            cancelButton.setOnClickListener(v -> {
                getActivity().getSupportFragmentManager().popBackStack();
            });
            saveButton.setOnClickListener(v -> {
                App_Utils.disableButton(v);
                clearErrors();
                saveEditedMerchant(requireContext(), () -> v.setEnabled(true));
            });

            // Update fields with merchant data
            updateMerchantFields(merchantData);
            observeCategories();
            observeMerchant();
        }
        return view;
    }

    public void saveEditedMerchant(Context context, Runnable enableButtonRunnable) {
        // Extract data from fields
        String name = this.name.getEditText().getText().toString();
        String type = this.type.getEditText().getText().toString();
        String discount = this.discount.getEditText().getText().toString();
        String moreInfo = moreInfoTextView.getEditText().getText().toString();
        String terms = this.terms.getEditText().getText().toString();

        // Get image URLs and addresses
        List<String> imageUrls = new ArrayList<>();
        List<String> addresses = new ArrayList<>();
        Merchant_Utils.getTextInputValues(addresses, addressLayout);
        Log.d("EditMerchant", "Addresses: " + addresses);

        // Validate image URLs
        CountDownLatch latch = new CountDownLatch(imageUrlLayout.getChildCount());
        AtomicBoolean hasError = new AtomicBoolean(false);
        Merchant_Utils.validateImageURLs(imageUrls, imageUrlLayout, latch, hasError);

        // Wait for validation to complete
        new Thread(() -> {
            try {
                latch.await();
                if (hasError.get()) {
                    ((Activity) context).runOnUiThread(enableButtonRunnable);
                    return;
                }

                // Convert lists to JsonArray
                JsonArray imageUrlsJsonArray = Merchant_Utils.convertListToJsonArray(imageUrls);
                JsonArray addressesJsonArray = Merchant_Utils.convertListToJsonArray(addresses);

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
                Merchant_Detail_API.edit_merchant(requireContext(), merchantData, Merchant_Edit_Fragment.this, enableButtonRunnable);
            } catch (InterruptedException e) {
                Log.d("EditMerchant", "Error waiting for image URL validation: " + e.getMessage());
                ((Activity) context).runOnUiThread(enableButtonRunnable);
            }
        }).start();
    }

    private void observeCategories() {
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

    private void observeMerchant() {
        Event_Bus.getInstance().getMerchantByIdLiveData().observe(getViewLifecycleOwner(), merchant -> {
            if (merchant != null && merchantData.get("ID").getAsString().equals(merchant.get("Merchant").getAsJsonObject().get("ID").getAsString())) {
                JsonObject newMerchantData = merchant.get("Merchant").getAsJsonObject();
                if (!newMerchantData.equals(currentMerchantData)) {
                    currentMerchantData = newMerchantData;
                    updateMerchantFields(newMerchantData);
                }
            }
        });

        Event_Bus.getInstance().getMerchantsLiveData().observe(getViewLifecycleOwner(), merchants -> {
            boolean merchantIdFound = false;
            for (int i = 0; i < merchants.size(); i++) {
                JsonObject merchant = merchants.get(i).getAsJsonObject();
                String id = merchant.get("ID").getAsString();
                if (merchantData.get("ID").getAsString().equals(id)) {
                    merchantIdFound = true;
                    loadMerchantById(requireContext(), id);
                    break;
                }
            }
            if (!merchantIdFound) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void updateMerchantFields(JsonObject merchantData) {
        // Assign values to views
        name.getEditText().setText(merchantData.get("Name").getAsString());
        type.getEditText().setText(merchantData.get("Category").getAsString());
        discount.getEditText().setText(merchantData.get("Discount").getAsString());
        moreInfoTextView.getEditText().setText(merchantData.get("More Info").getAsString());
        terms.getEditText().setText(merchantData.get("Terms").getAsString());

        // Generate fields for image URLs
        JsonArray imageUrls = merchantData.get("Images").getAsJsonArray();
        imageUrlLayout.removeAllViews();
        for (int i = 0; i < imageUrls.size(); i++) {
            String imageUrl = imageUrls.get(i).getAsString();
            Merchant_Utils.addNewFields(imageUrlLayout, "Paste Image URL", getContext());
            TextInputLayout textInputLayout = (TextInputLayout) imageUrlLayout.getChildAt(i);
            textInputLayout.getEditText().setText(imageUrl);
        }

        // Generate fields for addresses
        JsonArray addresses = merchantData.get("Addresses").getAsJsonArray();
        addressLayout.removeAllViews();
        for (int i = 0; i < addresses.size(); i++) {
            String address = addresses.get(i).getAsString();
            Merchant_Utils.addNewFields(addressLayout, "Enter merchant address", getContext());
            TextInputLayout textInputLayout = (TextInputLayout) addressLayout.getChildAt(i);
            textInputLayout.getEditText().setText(address);
        }
    }

    public void updateAdapter(List<String> newCategoryNames) {
        if (categories_adapter != null) {
            categories_adapter.clear();
            categories_adapter.addAll(newCategoryNames);
            categories_adapter.notifyDataSetChanged();
        } else {
            categories_adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    newCategoryNames
            );
            typeAutoComplete.setAdapter(categories_adapter);
            typeAutoComplete.setThreshold(1);
        }
    }

    public void handleError(String inputfield, String error) {
        Merchant_Utils.handleError(inputfield, error, name, type, addressLayout, imageUrlLayout, discount, terms, getContext());
    }
    private void clearErrors() {
        Merchant_Utils.clearErrors(
                new TextInputLayout[]{name, type, discount, moreInfoTextView, terms},
                imageUrlLayout, addressLayout
        );
    }
}