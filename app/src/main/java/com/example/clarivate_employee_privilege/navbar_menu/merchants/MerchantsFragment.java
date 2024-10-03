package com.example.clarivate_employee_privilege.navbar_menu.merchants;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.utils.AppUtils;
import com.example.clarivate_employee_privilege.websocket.EventBus;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public class MerchantsFragment extends Fragment {

    private JsonArray categories_json;
    private FilterButton_Adapter buttonAdapter;
    private Merchants_Adapter merchantsAdapter;
    private MerchantsSearchAdapter searchAdapter;
    private AutoCompleteTextView searchAutocomplete;
    private boolean isFragmentVisible = false;

    public MerchantsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLifecycle().addObserver(new MerchantsFragmentLifecycleObserver());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_merchants, container, false);

        // Get categories JSON
        categories_json = EventBus.getInstance().getCategoriesLiveData().getValue();

        // Initialize category names
        List<String> categoryNames = MerchantsUtils.initializeCategoryNames(categories_json);
        Log.d("MerchantsFragment", "Category names: " + categoryNames);

        // Set up the RecyclerView for categories
        setupCategoryRecyclerView(view, categoryNames);

        // Set up the RecyclerView for merchants
        setupMerchantsRecyclerView(view);

        // Set up the AutoCompleteTextView for search
        setupSearchAutocomplete(view);

        // Observe data changes
        observeMerchants();
        observeCategories();

        AppUtils.setToolbarTitle(requireActivity(), "Merchants");

        return view;
    }

    /**
     * Sets up the RecyclerView for displaying categories.
     * @param view The root view of the fragment.
     * @param categoryNames The list of category names.
     */
    private void setupCategoryRecyclerView(View view, List<String> categoryNames) {
        RecyclerView categoryRecyclerView = view.findViewById(R.id.merchants_filterRecycler);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        buttonAdapter = new FilterButton_Adapter(categoryNames, this::filterMerchants);
        categoryRecyclerView.setAdapter(buttonAdapter);

        // Set the "All" button to be toggled initially
        buttonAdapter.updateCategories(categoryNames);
        buttonAdapter.toggleButton("All");
    }

    /**
     * Sets up the RecyclerView for displaying merchants.
     * @param view The root view of the fragment.
     */
    private void setupMerchantsRecyclerView(View view) {
        RecyclerView merchantsRecyclerView = view.findViewById(R.id.merchants_merchantList_Recycler);
        merchantsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns
        merchantsAdapter = new Merchants_Adapter(getContext(), new JsonArray());
        merchantsRecyclerView.setAdapter(merchantsAdapter);
    }

    /**
     * Updates the search adapter with the current list of merchant names.
     */
    private void updateSearchAdapter() {
        List<String> merchantNames = getMerchantNamesBySelectedCategories();
        searchAdapter = new MerchantsSearchAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, merchantNames);
        searchAutocomplete.setAdapter(searchAdapter);
    }

    /**
     * Gets the list of merchant names based on the selected categories.
     * @return A list of merchant names.
     */
    private List<String> getMerchantNamesBySelectedCategories() {
        JsonArray allMerchants = EventBus.getInstance().getMerchantsLiveData().getValue();
        return MerchantsUtils.getMerchantNamesBySelectedCategories(allMerchants, buttonAdapter.getSelectedCategories());
    }

    /**
     * Sets up the AutoCompleteTextView for searching merchants.
     * @param view The root view of the fragment.
     */
    private void setupSearchAutocomplete(View view) {
        searchAutocomplete = view.findViewById(R.id.merchants_searchAutocomplete);
        updateSearchAdapter();

        searchAutocomplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed before text changes
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMerchantsByName(s.toString());
                updateSearchAdapter(); // Update the dropdown suggestions based on the current input
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed after text changes
            }
        });
    }

    /**
     * Filters the merchants based on the selected categories.
     * @param selectedCategories The list of selected categories.
     */
    private void filterMerchants(List<String> selectedCategories) {
        if (merchantsAdapter == null) {
            Log.e("MerchantsFragment", "merchantsAdapter is null");
            return;
        }

        JsonArray allMerchants = EventBus.getInstance().getMerchantsLiveData().getValue();
        JsonArray filteredMerchants = MerchantsUtils.filterMerchantsByCategories(allMerchants, selectedCategories);
        merchantsAdapter.updateData(filteredMerchants);
    }

    /**
     * Filters the merchants based on the search input name.
     * @param name The name to filter by.
     */
    private void filterMerchantsByName(String name) {
        if (merchantsAdapter == null) {
            Log.e("MerchantsFragment", "merchantsAdapter is null");
            return;
        }

        JsonArray allMerchants = EventBus.getInstance().getMerchantsLiveData().getValue();
        JsonArray filteredMerchants = MerchantsUtils.filterMerchantsByName(allMerchants, name, buttonAdapter.getSelectedCategories());
        merchantsAdapter.updateData(filteredMerchants);
    }

    /**
     * Observes changes in the merchants data and updates the UI accordingly.
     */
    private void observeMerchants() {
        EventBus.getInstance().getMerchantsLiveData().observe(getViewLifecycleOwner(), merchants -> {
            if (isFragmentVisible) {
                List<JsonObject> merchantList = MerchantsUtils.convertJsonArrayToList(merchants);
                Log.d("MerchantsFragment", "Merchants updated: " + merchantList);
                merchantsAdapter.updateData(merchants);
                updateSearchAdapter(); // Update the search adapter when merchants data changes
            } else {
                Log.d("MerchantsFragment", "Fragment not visible, skipping UI update.");
            }
        });
    }

    /**
     * Observes changes in the categories data and updates the UI accordingly.
     */
    private void observeCategories() {
        EventBus.getInstance().getCategoriesLiveData().observe(requireActivity(), categories -> {
            if (isFragmentVisible) {
                List<String> categoryNames = MerchantsUtils.initializeCategoryNames(categories);
                buttonAdapter.updateCategories(categoryNames);
                updateSearchAdapter(); // Update the search adapter when categories change
            } else {
                Log.d("MerchantsFragment", "Fragment not visible, skipping UI update.");
            }
        });
    }

    /**
     * Lifecycle observer to track the fragment's visibility state.
     */
    class MerchantsFragmentLifecycleObserver implements DefaultLifecycleObserver {

        @Override
        public void onStart(LifecycleOwner owner) {
            isFragmentVisible = true;
        }

        @Override
        public void onStop(LifecycleOwner owner) {
            isFragmentVisible = false;
        }
    }
}