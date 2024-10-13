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
import com.example.clarivate_employee_privilege.navbar_menu.merchant_detail.Merchant_Detail_Fragment;
import com.example.clarivate_employee_privilege.navbar_menu.merchants.Merchants_Fragment;
import com.example.clarivate_employee_privilege.navbar_menu.merchants.Merchants_Utils;
import com.example.clarivate_employee_privilege.navbar_menu.merchants.Merchants_Adapter;
import com.example.clarivate_employee_privilege.utils.App_Utils;
import com.example.clarivate_employee_privilege.websocket.Event_Bus;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Home_Fragment extends Fragment implements Category_Adapter.OnCategoryClickListener {

    private RecyclerView categoryRecyclerView;
    private Category_Adapter categoryAdapter;
    private RecyclerView homemerchantsRecycler;
    private ImageView noMerchantsImage, homeNewMerchantImageView;
    private Boolean previousIsAdmin = null;

    public Home_Fragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        homemerchantsRecycler = view.findViewById(R.id.home_random_merchants);
        noMerchantsImage = view.findViewById(R.id.home_no_merchants_image);
        homeNewMerchantImageView = view.findViewById(R.id.home_newmerchant);

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
        categoryAdapter = new Category_Adapter(new ArrayList<>(), this);
        categoryRecyclerView.setAdapter(categoryAdapter);

        // Observe categories data
        observeEventBus();

        // Observe merchants data
        observeMerchants();

        App_Utils.setToolbarTitle(getActivity(), "Home");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        App_Utils.setToolbarTitle(getActivity(), "Home");
    }

    @Override
    public void onStop() {
        super.onStop();
        App_Utils.enableTitleBar(getActivity());
        App_Utils.disableProfile(getActivity());
    }

    private void observeEventBus() {
        Event_Bus.getInstance().getCategoriesLiveData().observe(getViewLifecycleOwner(), new Observer<JsonArray>() {
            @Override
            public void onChanged(JsonArray categories) {
                List<String> categoryNames = Merchants_Utils.initializeCategoryNames(categories);
                categoryAdapter.updateCategories(categoryNames);
            }
        });

        Event_Bus.getInstance().getIsadminLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isAdmin){
                if (previousIsAdmin == null || !previousIsAdmin.equals(isAdmin)) {
                    previousIsAdmin = isAdmin; // Update the previous isAdmin status
                    getActivity().runOnUiThread(() -> {
                        App_Utils.setProfile(getActivity(), isAdmin);
                    });
                }
            }
        });
    }

    private void observeMerchants() {
        Event_Bus.getInstance().getMerchantsLiveData().observe(getViewLifecycleOwner(), new Observer<JsonArray>() {
            @Override
            public void onChanged(JsonArray merchants) {
                if (merchants != null && merchants.size() > 0) {
                    if (merchants.size() == 1) {
                        noMerchantsImage.setVisibility(View.VISIBLE);
                        homemerchantsRecycler.setVisibility(View.GONE);
                    }
                    else {
                        noMerchantsImage.setVisibility(View.GONE);
                        homemerchantsRecycler.setVisibility(View.VISIBLE);
                    }

                    // Get the last JsonObject in the list
                    JsonObject newestMerchant = merchants.get(merchants.size() - 1).getAsJsonObject();

                    // Check if the image URL exists
                    String imageUrl = newestMerchant.get("Image").getAsString();
                    Log.d("HomeFragment", "Newest merchant image URL: " + newestMerchant);
                    // Load the image using Picasso
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
                            Fragment merchantFragment = new Merchant_Detail_Fragment();

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

                    if (merchants.size() >1) {
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
                else{
                    noMerchantsImage.setVisibility(View.VISIBLE);
                    homemerchantsRecycler.setVisibility(View.GONE);
                    homeNewMerchantImageView.setImageDrawable(getResources().getDrawable(R.drawable.spongebob_rainbow));
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.main_bottomnavigation);
        bottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
    }

    @Override
    public void onCategoryClick(String category) {
        // Create an array with the selected category
        String[] selectedCategories = new String[]{category};

        // Replace the fragment and pass the selected category
        Merchants_Fragment merchantsFragment = Merchants_Fragment.newInstance(selectedCategories);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment, merchantsFragment)
                .commit();

        // Update the bottom navigation bar
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.main_bottomnavigation);
        bottomNavigationView.getMenu().findItem(R.id.nav_merchants).setChecked(true);
    }
}