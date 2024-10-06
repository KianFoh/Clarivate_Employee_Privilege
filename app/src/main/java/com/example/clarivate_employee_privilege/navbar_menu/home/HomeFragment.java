// HomeFragment.java
package com.example.clarivate_employee_privilege.navbar_menu.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.navbar_menu.merchantdetail.MerchantDetailFragment;
import com.example.clarivate_employee_privilege.navbar_menu.merchants.MerchantsFragment;
import com.example.clarivate_employee_privilege.navbar_menu.merchants.MerchantsUtils;
import com.example.clarivate_employee_privilege.navbar_menu.merchants.Merchants_Adapter;
import com.example.clarivate_employee_privilege.utils.AppUtils;
import com.example.clarivate_employee_privilege.websocket.EventBus;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    private RecyclerView categoryRecyclerView;
    private CategoryAdapter categoryAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView home_seeAll_button = view.findViewById(R.id.home_seeAll_button);
        home_seeAll_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the bottom navigation bar
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.main_bottomnavigation);
                bottomNavigationView.setSelectedItemId(R.id.nav_merchants);
            }
        });

        // Set up Category RecyclerView
        categoryRecyclerView = view.findViewById(R.id.home_category);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(new ArrayList<>(), this);
        categoryRecyclerView.setAdapter(categoryAdapter);

        // Observe categories data
        observeCategories();

        // Observe merchants data
        observeMerchants();

        AppUtils.setToolbarTitle(getActivity(), "Home");
        return view;
    }

    private void observeCategories() {
        EventBus.getInstance().getCategoriesLiveData().observe(getViewLifecycleOwner(), new Observer<JsonArray>() {
            @Override
            public void onChanged(JsonArray categories) {
                List<String> categoryNames = MerchantsUtils.initializeCategoryNames(categories);
                categoryAdapter.updateCategories(categoryNames);
            }
        });
    }

    private void observeMerchants() {
        EventBus.getInstance().getMerchantsLiveData().observe(getViewLifecycleOwner(), new Observer<JsonArray>() {
            @Override
            public void onChanged(JsonArray merchants) {
                if (merchants != null && merchants.size() > 0) {
                    // Get the last JsonObject in the list
                    JsonObject newestMerchant = merchants.get(merchants.size() - 1).getAsJsonObject();

                    // Check if the image URL exists
                    String imageUrl = newestMerchant.get("Image").getAsString();
                    Log.d("HomeFragment", "Newest merchant image URL: " + newestMerchant);
                    // Load the image using Picasso
                    ImageView homeNewMerchantImageView = getView().findViewById(R.id.home_newmerchant);
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Picasso.get()
                                .load(imageUrl)
                                .placeholder(R.drawable.merchant_image_placeholder) // Placeholder image
                                .fit()
                                .centerCrop()
                                .into(homeNewMerchantImageView, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        // Image loaded successfully
                                        Log.d("HomeFragment", "Image loaded successfully");
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        // Error loading image, set placeholder directly
                                        homeNewMerchantImageView.setImageResource(R.drawable.merchant_image_placeholder);
                                        Log.d("HomeFragment", "Error loading image: " + e.getMessage());
                                    }
                                });
                    } else {
                        Log.d("HomeFragment", "Image URL is null or empty");
                        homeNewMerchantImageView.setImageResource(R.drawable.merchant_image_placeholder);
                    }

                    homeNewMerchantImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Replace the fragment and pass the merchant ID
                            FragmentActivity activity = getActivity();
                            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                            Fragment merchantFragment = new MerchantDetailFragment();

                            Bundle bundle = new Bundle();
                            bundle.putString("merchantId", newestMerchant.get("ID").getAsString());
                            merchantFragment.setArguments(bundle);

                            transaction.replace(R.id.main_fragment, merchantFragment);
                            transaction.addToBackStack(null);
                            transaction.commit();

                            // Update the bottom navigation bar
                            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.main_bottomnavigation);
                            bottomNavigationView.getMenu().findItem(R.id.nav_merchants).setChecked(true);
                        }
                    });


                    // Exclude the last merchant and get a list of remaining merchants
                    List<JsonObject> remainingMerchants = new ArrayList<>();
                    for (int i = 0; i < merchants.size() - 1; i++) {
                        remainingMerchants.add(merchants.get(i).getAsJsonObject());
                    }

                    // Shuffle the list and select four random merchants
                    Collections.shuffle(remainingMerchants);
                    List<JsonObject> randomFourMerchants = remainingMerchants.subList(0, Math.min(4, remainingMerchants.size()));

                    // Convert the list to JsonArray
                    JsonArray randomFourMerchantsJsonArray = new JsonArray();
                    for (JsonObject merchant : randomFourMerchants) {
                        randomFourMerchantsJsonArray.add(merchant);
                    }

                    // Update the RecyclerView with the selected merchants
                    RecyclerView homemerchantsRecycler = getView().findViewById(R.id.home_random_merchants);
                    homemerchantsRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns
                    Merchants_Adapter merchantsAdapter = new Merchants_Adapter(getContext(), randomFourMerchantsJsonArray);
                    homemerchantsRecycler.setAdapter(merchantsAdapter);

                }
            }
        });
    }

    @Override
    public void onCategoryClick(String category) {
        // Replace the fragment and pass the selected category
        MerchantsFragment merchantsFragment = MerchantsFragment.newInstance(category);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment, merchantsFragment)
                .commit();

        // Update the bottom navigation bar
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.main_bottomnavigation);
        bottomNavigationView.getMenu().findItem(R.id.nav_merchants).setChecked(true);
    }
}