package com.example.clarivate_employee_privilege.navbar_menu.add_merchant;

import android.content.Context;
import android.os.Bundle;
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
import com.example.clarivate_employee_privilege.utils.ToastUtils;
import com.example.clarivate_employee_privilege.websocket.EventBus;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class AddMerchantFragment extends Fragment {

    private LinearLayout imageUrlLayout, adressLayout;
    private ImageView add_imageURL, add_address;
    private TextInputLayout imageurl, name, type, address, discount, info, terms;
    private JsonArray categories_json;
    private AutoCompleteTextView typeAutoComplete;
    private ArrayAdapter<String> adapter;

    public AddMerchantFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_merchant, container, false);

        typeAutoComplete = view.findViewById(R.id.addmerchant_type);
        categories_json = EventBus.getInstance().getCategoriesLiveData().getValue();
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
                    scroll.smoothScrollTo(0, discount.getTop());
                }
            });
        }
        observeCategories();

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

        add_imageURL.setOnClickListener(v -> AddMerchantUtils.addNewFields(imageUrlLayout, "Paste Image URL", getContext()));
        add_address.setOnClickListener(v -> AddMerchantUtils.addNewFields(adressLayout, "Enter merchant address", getContext()));
        view.findViewById(R.id.addmerchant_submit).setOnClickListener(v -> {
            clearErrors();
            addMerchant(requireContext());
        });

        return view;
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
        }
    }

    private void clearErrors() {
        name.setError(null);
        type.setError(null);
        address.setError(null);
        discount.setError(null);
        terms.setError(null);
    }

    private void addMerchant(Context context) {
        List<String> imageURLList = new ArrayList<>();
        List<String> addressList = new ArrayList<>();
        AtomicBoolean hasError = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(imageUrlLayout.getChildCount());

        AddMerchantUtils.validateImageURLs(imageURLList, imageUrlLayout, latch, hasError);
        AddMerchantUtils.getTextInputValues(addressList, adressLayout);

        new Thread(() -> {
            try {
                latch.await(); // Wait for all image URL validations to complete
                if (hasError.get()) {
                    // If there was an error, do not proceed with the API call
                    return;
                }

                String merchantName = name.getEditText().getText().toString();
                String merchantType = type.getEditText().getText().toString();
                String merchantDiscount = discount.getEditText().getText().toString();
                String merchantInfo = info.getEditText().getText().toString();
                String merchantTerms = terms.getEditText().getText().toString();

                JsonObject add_merchant = new JsonObject();
                add_merchant.add("imageURL", AddMerchantUtils.convertListToJsonArray(imageURLList));
                add_merchant.addProperty("name", merchantName);
                add_merchant.addProperty("type", merchantType);
                add_merchant.add("address", AddMerchantUtils.convertListToJsonArray(addressList));
                add_merchant.addProperty("discount", merchantDiscount);
                add_merchant.addProperty("info", merchantInfo);
                add_merchant.addProperty("terms", merchantTerms);

                AddMerchant_API.addMerchant(context, add_merchant, AddMerchantFragment.this);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
        AddMerchantUtils.removeAllViewsExceptFirst(imageUrlLayout);
        AddMerchantUtils.removeAllViewsExceptFirst(adressLayout);
    }

    public void handleError(String inputfield, String error) {
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
                ToastUtils.showToast(getContext(), error, false);
                break;
        }
    }

    public TextInputLayout getName() {
        return name;
    }
}