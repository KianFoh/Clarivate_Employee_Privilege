package com.example.clarivate_employee_privilege.navbar_menu.merchantdetail;

import static com.example.clarivate_employee_privilege.utils.APIUtils.loadMerchantById;

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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.navbar_menu.profile.Profile_API;
import com.example.clarivate_employee_privilege.navbar_menu.profile.Profile_Utils;
import com.example.clarivate_employee_privilege.utils.AppUtils;
import com.example.clarivate_employee_privilege.websocket.EventBus;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class MerchantDetailFragment extends Fragment {

    private JsonObject merchantData;
    private Addresses_Adapter addressesAdapter;
    private Image_Adapter imageAdapter;
    private List<String> addressList, imgList;
    private String merchantId;
    private ImageButton toolbar_more;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_merchantdetail, container, false);

        merchantId = getArguments().getString("merchantId");
        observeMerchantById(merchantId, view);
        loadMerchantById(requireContext(), merchantId);

        // Initialize RecyclerView and Adapter for addresses
        RecyclerView recyclerView = view.findViewById(R.id.merchantdetail_addresses_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        addressList = new ArrayList<>();
        addressesAdapter = new Addresses_Adapter(addressList);
        recyclerView.setAdapter(addressesAdapter);

        // Initialize ViewPager2 and Adapter for images
        ViewPager2 viewPager = view.findViewById(R.id.merchantdetail_imgs_viewpager);
        TabLayout tabLayout = view.findViewById(R.id.merchantdetail_tab_layout);
        imgList = new ArrayList<>();
        imageAdapter = new Image_Adapter(imgList);
        viewPager.setAdapter(imageAdapter);

        // Attach TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // This can be left empty
        }).attach();

        AppUtils.setToolbarTitle(requireActivity(), "Merchant Details");

        boolean isAdmin = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE)
                .getBoolean("isAdmin", false);
        int visibility = isAdmin ? View.VISIBLE : View.GONE;

        toolbar_more = requireActivity().findViewById(R.id.toolbar_more);
        toolbar_more.setVisibility(visibility);
        toolbar_more.setOnClickListener(v -> merchantDetail_Popup(v));


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (toolbar_more != null && toolbar_more.getVisibility() == View.VISIBLE) {
            toolbar_more.setVisibility(View.GONE);
        }
    }

    private void merchantDetail_Popup(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.merchantdetail_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.merchantdetail_edit:
                        // Edit merchant
                        return true;
                    case R.id.merchantdetail_delete:
                        Merchant_Utils.showDeleteMerchantDialog(requireContext(), new Merchant_API(), merchantId);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private void observeMerchantById(String merchantId, View view) {
        EventBus.getInstance().getMerchantByIdLiveData().observe(getViewLifecycleOwner(), new Observer<JsonObject>() {
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
                        imgList.add("android.resource://" + requireContext().getPackageName() + "/" + R.drawable.merchant_image_placeholder); // Add the resource URI of the placeholder image
                    } else {
                        Log.d("fragment_merchant", "Images found: " + imgList.size());
                    }
                    imageAdapter.notifyDataSetChanged();

                    // Update merchant details
                    String name  = merchantData.get("Name").getAsString();
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
                }
            }
        });
    }
}