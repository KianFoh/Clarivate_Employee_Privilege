package com.example.clarivate_employee_privilege.navbar_menu.merchants;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

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

import java.util.ArrayList;
import java.util.List;

public class MerchantsFragment extends Fragment {

    private JsonArray categories_json;
    private FilterButton_Adapter buttonAdapter;
    private Merchants_Adapter merchantsAdapter;
    private MerchantsSearchAdapter searchAdapter;
    private AutoCompleteTextView searchAutocomplete;
    private boolean isFragmentVisible = false;
    private RecyclerView merchantsRecyclerView;
    private ImageView noMerchantsImage;

    public MerchantsFragment() {
        // Required empty public constructor
    }

    public static MerchantsFragment newInstance(String category) {
        MerchantsFragment fragment = new MerchantsFragment();
        Bundle args = new Bundle();
        args.putString("selectedCategory", category);
        fragment.setArguments(args);
        return fragment;
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

        // Get the selected category from the arguments if any
        Bundle args = getArguments();
        if (args != null) {
            String selectedCategory = args.getString("selectedCategory");
            buttonAdapter.toggleButton(selectedCategory);
            Log.d("MerchantsFragment", "Selected category: " + selectedCategory);
        }
        else{
            Log.d("MerchantsFragment", "No selected category");
        }
        buttonAdapter.updateCategories(categoryNames);
    }

    /**
     * Sets up the RecyclerView for displaying merchants.
     * @param view The root view of the fragment.
     */
    private void setupMerchantsRecyclerView(View view) {
        merchantsRecyclerView = view.findViewById(R.id.merchants_merchantList_Recycler);
        merchantsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns
        merchantsAdapter = new Merchants_Adapter(getContext(), new JsonArray());
        merchantsRecyclerView.setAdapter(merchantsAdapter);

        noMerchantsImage = view.findViewById(R.id.merchants_nodata_image);
    }

    /**
     * Updates the search adapter with the current list of merchant names.
     */
    private void updateSearchAdapter() {
        String currentText = searchAutocomplete.getText().toString().toLowerCase();
        List<String> allMerchantNames = getMerchantNamesBySelectedCategories();
        List<String> filteredMerchantNames = new ArrayList<>();

        for (String name : allMerchantNames) {
            if (name.toLowerCase().contains(currentText)) {
                filteredMerchantNames.add(name);
            }
        }

        searchAdapter = new MerchantsSearchAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, filteredMerchantNames);
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
        String currentSearchText = searchAutocomplete.getText().toString();
        JsonArray filteredMerchants = MerchantsUtils.filterMerchantsByName(allMerchants, currentSearchText, selectedCategories);
        merchantsAdapter.updateData(filteredMerchants);

        // Update the search adapter with the filtered merchant names
        updateSearchAdapter();
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

        // Update the search adapter with the filtered merchant names
        updateSearchAdapter();
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

                if (merchants.size() == 0) {
                    noMerchantsImage.setVisibility(View.VISIBLE);
                    merchantsRecyclerView.setVisibility(View.GONE);
                } else {
                    noMerchantsImage.setVisibility(View.GONE);
                    merchantsRecyclerView.setVisibility(View.VISIBLE);
                }

                // Reapply the current filters
                filterMerchants(buttonAdapter.getSelectedCategories());

                // Reapply the search filter
                String currentSearchText = searchAutocomplete.getText().toString();
                filterMerchantsByName(currentSearchText);

                // Update the search adapter with the filtered merchant names
                updateSearchAdapter();
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