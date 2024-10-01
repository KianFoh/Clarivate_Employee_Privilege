// MerchantsFragment.java
package com.example.clarivate_employee_privilege.navbar_menu.merchants;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.utils.APIUtils;
import com.example.clarivate_employee_privilege.websocket.EventBus;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class MerchantsFragment extends Fragment {

    private JsonArray categories_json;
    private FilterButton_Adapter buttonAdapter;
    private Merchants_Adapter merchantsAdapter;

    public MerchantsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_merchants, container, false);
        categories_json = EventBus.getInstance().getCategoriesLiveData().getValue();

        // Initialize the categoryNames list and add "All" to the beginning
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("All");

        // Extract category names from the JSON array
        if (categories_json != null) {
            for (int i = 0; i < categories_json.size(); i++) {
                JsonObject category = categories_json.get(i).getAsJsonObject();
                String categoryName = category.get("Name").getAsString();
                categoryNames.add(categoryName);
            }
        }
        Log.d("MerchantsFragment", "Category names: " + categoryNames);

        // Set up the RecyclerView for categories
        RecyclerView categoryRecyclerView = view.findViewById(R.id.merchants_filterRecycler);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        buttonAdapter = new FilterButton_Adapter(categoryNames, this::filterMerchants);
        categoryRecyclerView.setAdapter(buttonAdapter);

        // Set the "All" button to be toggled initially
        buttonAdapter.updateCategories(categoryNames);
        buttonAdapter.toggleButton("All");

        // Set up the RecyclerView for merchants with GridLayoutManager
        RecyclerView merchantsRecyclerView = view.findViewById(R.id.merchants_merchantList_Recycler);
        merchantsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns

        // Initialize the adapter
        merchantsAdapter = new Merchants_Adapter(getContext(), new JsonArray());
        merchantsRecyclerView.setAdapter(merchantsAdapter);

        // Load initial data
        loadMerchantsData();

        observeMerchants();
        observeCategories();

        return view;
    }

    private void filterMerchants(String category) {
        // Implement filtering logic here
    }

    private void loadMerchantsData() {
        APIUtils.loadMerchants(getContext());
    }

    private void observeMerchants() {
        EventBus.getInstance().getMerchantsLiveData().observe(getViewLifecycleOwner(), merchants -> {
            List<JsonObject> merchantList = new ArrayList<>();
            for (int i = 0; i < merchants.size(); i++) {
                JsonObject merchant = merchants.get(i).getAsJsonObject();
                merchantList.add(merchant);
            }
            Log.d("MerchantsFragment", "Merchants updated: " + merchantList);
            merchantsAdapter.updateData(merchants);
        });
    }

    private void observeCategories() {
        EventBus.getInstance().getCategoriesLiveData().observe(requireActivity(), categories -> {
            List<String> categoryNames = new ArrayList<>();
            categoryNames.add("All");
            for (int i = 0; i < categories.size(); i++) {
                JsonObject category = categories.get(i).getAsJsonObject();
                String categoryName = category.get("Name").getAsString();
                categoryNames.add(categoryName);
            }
            buttonAdapter.updateCategories(categoryNames);
        });
    }
}