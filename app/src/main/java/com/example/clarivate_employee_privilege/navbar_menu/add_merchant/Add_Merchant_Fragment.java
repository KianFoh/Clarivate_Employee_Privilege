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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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
    private ArrayAdapter<String> categories_adapter;

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

            categories_adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    categoryNames
            );
            typeAutoComplete.setAdapter(categories_adapter);
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

        Button cancelButton = view.findViewById(R.id.addmerchant_cancel);
        cancelButton.setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            AdminSecondaryConfirmationDialog dialog = new AdminSecondaryConfirmationDialog();
            dialog.setOnConfirmCallback(this::clearAllFields);
            dialog.show(fragmentManager, "AdminSecondaryConfirmationDialog");

        });

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
        if (categories_adapter != null) {
            categories_adapter.clear();
            categories_adapter.addAll(newCategoryNames);
            categories_adapter.notifyDataSetChanged();
        }
    }

    public void addMerchant(Context context, Runnable enableButtonRunnable) {
        // Extract data from fields
        String name = this.name.getEditText().getText().toString();
        String type = this.type.getEditText().getText().toString();
        String discount = this.discount.getEditText().getText().toString();
        String moreInfo = this.info.getEditText().getText().toString();
        String terms = this.terms.getEditText().getText().toString();

        // Get image URLs and addresses
        List<String> imageUrls = new ArrayList<>();
        List<String> addresses = new ArrayList<>();
        Merchant_Utils.getTextInputValues(addresses, addressLayout);
        Log.d("AddMerchant", "Addresses: " + addresses);

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

                // Create a new JsonObject for the merchant data
                JsonObject newMerchantData = new JsonObject();
                newMerchantData.addProperty("Name", name);
                newMerchantData.addProperty("Category", type);
                newMerchantData.addProperty("Discount", discount);
                newMerchantData.addProperty("More Info", moreInfo);
                newMerchantData.addProperty("Terms", terms);
                newMerchantData.add("Images", imageUrlsJsonArray);
                newMerchantData.add("Addresses", addressesJsonArray);

                Log.d("AddMerchant", "New merchant data: " + newMerchantData);

                // Call the API to add the merchant
                Add_Merchant_API.addMerchant(requireContext(), newMerchantData, Add_Merchant_Fragment.this, enableButtonRunnable);
            } catch (InterruptedException e) {
                Log.d("AddMerchant", "Error waiting for image URL validation: " + e.getMessage());
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
        Merchant_Utils.handleError(inputfield, error, name, type, addressLayout, imageUrlLayout, discount, terms, getContext());
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