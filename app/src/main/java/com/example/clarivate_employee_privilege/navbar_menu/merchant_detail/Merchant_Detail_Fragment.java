package com.example.clarivate_employee_privilege.navbar_menu.merchant_detail;

import static com.example.clarivate_employee_privilege.utils.API_Utils.loadMerchantById;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.utils.App_Utils;
import com.example.clarivate_employee_privilege.websocket.Event_Bus;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Merchant_Detail_Fragment extends Fragment {

    private JsonObject merchantData;
    private Merchant_Detail_Addresses_Adapter addressesAdapter;
    private Merchant_Detail_Image_Adapter imageAdapter;
    private List<String> addressList, imgList;
    private String merchantId;
    private ImageButton toolbar_more;
    private AlertDialog dialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        App_Utils.showLoading(true, requireActivity().findViewById(R.id.main_progressbar));
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_merchantdetail, container, false);
        merchantId = getArguments().getString("merchantId");
        observeMerchant(merchantId, view);

        // Initialize RecyclerView and Adapter for addresses
        RecyclerView recyclerView = view.findViewById(R.id.merchantdetail_addresses_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        addressList = new ArrayList<>();
        addressesAdapter = new Merchant_Detail_Addresses_Adapter(addressList);
        recyclerView.setAdapter(addressesAdapter);

        // Initialize ViewPager2 and Adapter for images
        ViewPager2 viewPager = view.findViewById(R.id.merchantdetail_imgs_viewpager);
        TabLayout tabLayout = view.findViewById(R.id.merchantdetail_tab_layout);
        imgList = new ArrayList<>();
        imageAdapter = new Merchant_Detail_Image_Adapter(imgList);
        viewPager.setAdapter(imageAdapter);

        // Attach TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // This can be left empty
        }).attach();

        // Set item click listener
        imageAdapter.setOnItemClickListener(imageUrl -> {
            int position = imgList.indexOf(imageUrl);
            FullscreenImageFragment fullscreenImageFragment = FullscreenImageFragment.newInstance(imgList, position);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, fullscreenImageFragment)
                    .addToBackStack(null)
                    .commit();
        });

        App_Utils.setToolbarTitle(requireActivity(), "Merchant Details");

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (toolbar_more != null && toolbar_more.getVisibility() == View.VISIBLE) {
            toolbar_more.setVisibility(View.GONE);
        }
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        App_Utils.showLoading(false, requireActivity().findViewById(R.id.main_progressbar));
    }

    private void merchantDetail_Popup(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.merchantdetail_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.merchantdetail_edit:
                        // Start the Merchant_Edit fragment
                        Merchant_Edit_Fragment merchantEditFragment = new Merchant_Edit_Fragment();
                        Bundle args = new Bundle();
                        args.putString("merchantData", merchantData.toString());
                        merchantEditFragment.setArguments(args);
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_fragment, merchantEditFragment)
                                .addToBackStack("merchant_edit")
                                .commit();
                        return true;
                    case R.id.merchantdetail_delete:
                        dialog = Merchant_Detail_Utils.showDeleteMerchantDialog(requireContext(), new Merchant_Detail_API(), merchantId);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private void observeMerchant(String merchantId, View view) {
        // Observe admin status
        Event_Bus.getInstance().getIsadminLiveData().observe(getViewLifecycleOwner(), this::updateToolbarVisibility);

        // load merchant by ID
        Event_Bus.getInstance().getMerchantByIdLiveData().observe(getViewLifecycleOwner(), new Observer<JsonObject>() {

           // API call to get merchant by ID
            @Override
            public void onChanged(JsonObject merchant) {
                if (merchant != null && merchantId.equals(merchant.get("Merchant").getAsJsonObject().get("ID").getAsString())) {
                    Log.d("fragment_merchant", "Merchant data: " + merchant);
                    merchantData = merchant; // Store the merchant data in the field

                    JsonObject merchantData = merchant.get("Merchant").getAsJsonObject();

                    // Update image list and notify adapter
                    JsonArray images = merchantData.get("Images").getAsJsonArray();
                    imgList.clear();
                    for (int i = 0; i < images.size(); i++) {
                        if (!images.get(i).isJsonNull()) {
                            String image = images.get(i).getAsString();
                            if (image != null && !image.isEmpty()) {
                                imgList.add(image);
                            }
                        }
                    }
                    if (imgList.isEmpty()) {
                        Log.d("fragment_merchant", "No valid images found, adding placeholder image");
                        imgList.add("android.resource://" + requireContext().getPackageName() + "/" + R.drawable.clarivate_logo_black); // Add the resource URI of the placeholder image
                    } else {
                        Log.d("fragment_merchant", "Images found: " + imgList.size());
                    }
                    imageAdapter.notifyDataSetChanged();

                    // Update merchant details
                    String name = merchantData.get("Name").getAsString();
                    ((TextView)view.findViewById(R.id.merchantdetail_name)).setText(name);

                    String category = merchantData.get("Category").getAsString();
                    ((TextView)view.findViewById(R.id.merchantdetail_category)).setText(category);

                    // Update address list and notify adapter
                    JsonArray addresses = merchantData.get("Addresses").getAsJsonArray();
                    addressList.clear();
                    for (int i = 0; i < addresses.size(); i++) {
                        addressList.add(addresses.get(i).getAsString());
                    }
                    addressesAdapter.notifyDataSetChanged();

                    String discount = merchantData.get("Discount").getAsString();
                    ((TextView)view.findViewById(R.id.merchantdetail_discount)).setText(discount);

                    // Update more info section
                    if (merchantData.has("More Info") && !merchantData.get("More Info").getAsString().isEmpty()) {
                        String moreInfo = merchantData.get("More Info").getAsString();
                        ((TextView)view.findViewById(R.id.merchantdetail_info)).setText(moreInfo);
                        view.findViewById(R.id.merchantdetail_info_tag).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.merchantdetail_info_container).setVisibility(View.VISIBLE);
                    } else {
                        view.findViewById(R.id.merchantdetail_info_tag).setVisibility(View.GONE);
                        view.findViewById(R.id.merchantdetail_info_container).setVisibility(View.GONE);
                    }

                    String terms = merchantData.get("Terms").getAsString();
                    ((TextView)view.findViewById(R.id.merchantdetail_terms)).setText(terms);

                    boolean isAdmin = requireActivity().getSharedPreferences(   "user_info", Context.MODE_PRIVATE)
                            .getBoolean("isAdmin", false);
                    int visibility = isAdmin ? View.VISIBLE : View.GONE;

                    toolbar_more = requireActivity().findViewById(R.id.toolbar_more);
                    toolbar_more.setVisibility(visibility);
                    toolbar_more.setOnClickListener(v -> merchantDetail_Popup(v));
                    App_Utils.showLoading(false, requireActivity().findViewById(R.id.main_progressbar));
                    updateToolbarVisibility(Event_Bus.getInstance().getIsadminLiveData().getValue());
                }
            }
        });
        Event_Bus.getInstance().getMerchantsLiveData().observe(getViewLifecycleOwner(), new Observer<JsonArray>() {
            @Override
            public void onChanged(JsonArray merchants) {
                boolean merchantIdFound = false;
                for (int i = 0; i < merchants.size(); i++) {
                    JsonObject merchant = merchants.get(i).getAsJsonObject();
                    String id = merchant.get("ID").getAsString();
                    if (merchantId.equals(id)) {
                        Log.d("fragment_merchant", "Current merchant ID found in the updated merchants list");
                        merchantIdFound = true;
                        refreshMerchantDetails(merchantId);
                        break;
                    }
                }
                if (!merchantIdFound) {
                    Log.d("fragment_merchant", "Current merchant ID not found in the updated merchants list");
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
    }
    private void updateToolbarVisibility(Boolean isAdmin) {
        if (isAdmin != null && merchantData != null) {
            int visibility = isAdmin ? View.VISIBLE : View.GONE;
            toolbar_more = requireActivity().findViewById(R.id.toolbar_more);
            toolbar_more.setVisibility(visibility);
            toolbar_more.setOnClickListener(v -> merchantDetail_Popup(v));
        }
    }
    private void refreshMerchantDetails(String merchantId) {
        loadMerchantById(requireContext(), merchantId);
    }
}