package com.example.clarivate_employee_privilege.navbar_menu.add_merchant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.example.clarivate_employee_privilege.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddMerchantFragment extends Fragment {

    private LinearLayout imageUrlLayout, adressLayout;
    private ImageView add_imageURL, add_address;
    private TextInputLayout imageurl, name, type, address, discount, terms;

    public AddMerchantFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_merchant, container, false);

        imageurl = view.findViewById(R.id.addmerchant_imageurl_field);
        name = view.findViewById(R.id.addmerchant_name_field);
        type = view.findViewById(R.id.addmerchant_type_field);
        address = view.findViewById(R.id.addmerchant_address_field);
        discount = view.findViewById(R.id.addmerchant_discount_field);
        terms = view.findViewById(R.id.addmerchant_terms_field);

        imageUrlLayout = view.findViewById(R.id.addmerchant_imageurl_layout);
        adressLayout = view.findViewById(R.id.addmerchant_address_layout);

        add_imageURL = view.findViewById(R.id.addmerchant_addimageurl_button);
        add_address = view.findViewById(R.id.addmerchant_addaddress_button);

        add_imageURL.setOnClickListener(v -> addNewFields(imageUrlLayout, "Paste Image URL"));
        add_address.setOnClickListener(v -> addNewFields(adressLayout, "Enter merchant address"));
        view.findViewById(R.id.addmerchant_submit).setOnClickListener(v -> {
            // Clear all errors
            for (int i = 0; i < imageUrlLayout.getChildCount(); i++) {
                TextInputLayout textInputLayout = (TextInputLayout) imageUrlLayout.getChildAt(i);
                textInputLayout.setError(null);
            }
            for (int i = 0; i < adressLayout.getChildCount(); i++) {
                TextInputLayout textInputLayout = (TextInputLayout) adressLayout.getChildAt(i);
                textInputLayout.setError(null);
            }


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

    }


    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}