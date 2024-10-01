package com.example.clarivate_employee_privilege.navbar_menu.merchants;

import static com.example.clarivate_employee_privilege.utils.APIUtils.loadMerchantById;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.websocket.EventBus;
import com.google.gson.JsonObject;

public class fragment_merchant extends Fragment {

    private JsonObject merchantData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_merchant, container, false);

        String merchantId = getArguments().getString("merchantId");
        observeMerchantById(merchantId, view);
        loadMerchantById(requireContext(), merchantId);

        return view;
    }

    private void observeMerchantById(String merchantId, View view) {
        EventBus.getInstance().getMerchantByIdLiveData().observe(getViewLifecycleOwner(), new Observer<JsonObject>() {
            @Override
            public void onChanged(JsonObject merchant) {
                if (merchant != null && merchantId.equals(merchant.get("Merchant").getAsJsonObject().get("ID").getAsString())) {
                    Log.d("fragment_merchant", "Merchant data: " + merchant);
                    merchantData = merchant; // Store the merchant data in the field


                    String name  = merchantData.get("Merchant").getAsJsonObject().get("Name").getAsString();
                    ((TextView)view.findViewById(R.id.merchantDetail_name)).setText(name);
                }
            }
        });
    }
}