package com.example.clarivate_employee_privilege.navbar_menu.merchants;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.navbar_menu.merchantdetail.MerchantDetailFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class Merchants_Adapter extends RecyclerView.Adapter<Merchants_Adapter.MerchantViewHolder> {

    private final Context context;
    private JsonArray merchants;

    public Merchants_Adapter(Context context, JsonArray merchants) {
        this.context = context;
        this.merchants = merchants;
    }

    /**
     * Updates the data in the adapter and notifies the changes.
     * @param newMerchants The new list of merchants.
     */
    public void updateData(JsonArray newMerchants) {
        // Store previous states in a map
        Map<String, JsonObject> previousStates = new HashMap<>();
        for (int i = 0; i < merchants.size(); i++) {
            JsonObject merchant = merchants.get(i).getAsJsonObject();
            previousStates.put(merchant.get("Name").getAsString(), merchant);
        }

        // Create a new JsonArray to hold the updated merchants
        JsonArray updatedMerchants = new JsonArray();

        // Add new merchants and update existing ones
        for (int i = 0; i < newMerchants.size(); i++) {
            JsonObject newMerchant = newMerchants.get(i).getAsJsonObject();
            String merchantName = newMerchant.get("Name").getAsString();
            if (!containsMerchant(merchants, merchantName)) {
                updatedMerchants.add(newMerchant);
                notifyItemInserted(i);
            } else {
                int index = getMerchantIndex(merchants, merchantName);
                updatedMerchants.add(newMerchant);
                notifyItemChanged(index);
            }
        }

        // Remove merchants that are no longer present
        for (int i = merchants.size() - 1; i >= 0; i--) {
            JsonObject merchant = merchants.get(i).getAsJsonObject();
            if (!containsMerchant(newMerchants, merchant.get("Name").getAsString())) {
                notifyItemRemoved(i);
            }
        }

        // Update the merchants array
        merchants = updatedMerchants;
    }

    /**
     * Checks if a merchant with the given name exists in the JsonArray.
     * @param merchants The JsonArray of merchants.
     * @param name The name of the merchant to check.
     * @return True if the merchant exists, false otherwise.
     */
    private boolean containsMerchant(JsonArray merchants, String name) {
        for (int i = 0; i < merchants.size(); i++) {
            if (merchants.get(i).getAsJsonObject().get("Name").getAsString().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the index of a merchant with the given name in the JsonArray.
     * @param merchants The JsonArray of merchants.
     * @param name The name of the merchant to find.
     * @return The index of the merchant, or -1 if not found.
     */
    private int getMerchantIndex(JsonArray merchants, String name) {
        for (int i = 0; i < merchants.size(); i++) {
            if (merchants.get(i).getAsJsonObject().get("Name").getAsString().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public MerchantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.merchant, parent, false);
        return new MerchantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MerchantViewHolder holder, int position) {
        JsonObject merchant = merchants.get(position).getAsJsonObject();
        holder.bind(merchant);
    }

    @Override
    public int getItemCount() {
        return merchants.size();
    }

    class MerchantViewHolder extends RecyclerView.ViewHolder {

        private final ImageView merchantImage;
        private final TextView merchantName;
        private final TextView merchantCategory;

        public MerchantViewHolder(@NonNull View itemView) {
            super(itemView);
            merchantImage = itemView.findViewById(R.id.merchant_image);
            merchantName = itemView.findViewById(R.id.merchant_name);
            merchantCategory = itemView.findViewById(R.id.merchant_category);
        }

        /**
         * Binds the merchant data to the view holder.
         * @param merchant The merchant data.
         */
        public void bind(JsonObject merchant) {
            String name = merchant.has("Name") && !merchant.get("Name").isJsonNull() ? merchant.get("Name").getAsString() : "Unknown";
            String category = merchant.has("Category") && !merchant.get("Category").isJsonNull() ? merchant.get("Category").getAsString() : "Unknown";
            String merchantId = merchant.has("ID") && !merchant.get("ID").isJsonNull() ? merchant.get("ID").getAsString() : "";

            merchantName.setText(truncateText(name, 20)); // Adjust the maxLength as needed
            merchantCategory.setText(truncateText(category, 20)); // Adjust the maxLength as needed

            String imageUrl = merchant.has("Image") && !merchant.get("Image").isJsonNull() ? merchant.get("Image").getAsString() : null;
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.merchant_image_placeholder) // Placeholder image
                        .fit()
                        .centerCrop()
                        .into(merchantImage, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                // Image loaded successfully
                            }

                            @Override
                            public void onError(Exception e) {
                                // Error loading image, load placeholder
                                Picasso.get()
                                        .load(R.drawable.merchant_image_placeholder) // Default image
                                        .placeholder(R.drawable.merchant_image_placeholder) // Placeholder image
                                        .fit()
                                        .centerCrop()
                                        .into(merchantImage);
                            }
                        });
            } else {
                Picasso.get()
                        .load(R.drawable.merchant_image_placeholder) // Default image
                        .placeholder(R.drawable.merchant_image_placeholder) // Placeholder image
                        .fit()
                        .centerCrop()
                        .into(merchantImage);
            }
            merchantImage.setOnClickListener(v -> startMerchantFragment(merchantId));
        }
    }

    /**
     * Truncates the text to a specified maximum length.
     * @param text The text to truncate.
     * @param maxLength The maximum length of the text.
     * @return The truncated text.
     */
    private static String truncateText(String text, int maxLength) {
        if (text.length() > maxLength) {
            return text.substring(0, maxLength - 3) + "...";
        } else {
            return text;
        }
    }

    private void startMerchantFragment(String merchantId) {
        FragmentActivity activity = (FragmentActivity) context;
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        Fragment merchantFragment = new MerchantDetailFragment();

        Bundle bundle = new Bundle();
        bundle.putString("merchantId", merchantId);
        merchantFragment.setArguments(bundle);

        transaction.replace(R.id.main_fragment, merchantFragment);
        transaction.addToBackStack(null);
        transaction.commit();

        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.main_bottomnavigation);
        bottomNavigationView.getMenu().findItem(R.id.nav_merchants).setChecked(true);
    }

}