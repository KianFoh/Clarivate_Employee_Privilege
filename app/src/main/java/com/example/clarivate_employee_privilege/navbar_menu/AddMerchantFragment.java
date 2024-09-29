package com.example.clarivate_employee_privilege.navbar_menu;

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

    private LinearLayout imageUrlLayout;
    private LinearLayout adressLayout;
    private ImageView add_imageURL;
    private ImageView add_address;

    public AddMerchantFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_merchant, container, false);

        imageUrlLayout = view.findViewById(R.id.image_url_layout);
        adressLayout = view.findViewById(R.id.address_layout);

        add_imageURL = view.findViewById(R.id.add_imageurl);
        add_address = view.findViewById(R.id.add_address);

        add_imageURL.setOnClickListener(v -> addNewFields(imageUrlLayout, "Paste Image URL"));
        add_address.setOnClickListener(v -> addNewFields(adressLayout, "Enter merchant address"));


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

        // Add the TextInputLayout to the specified LinearLayout
        layout.addView(newTextInputLayout);
    }



    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}