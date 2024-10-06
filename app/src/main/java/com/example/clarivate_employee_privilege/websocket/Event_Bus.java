// EventBus.java
package com.example.clarivate_employee_privilege.websocket;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Event_Bus {
    private static Event_Bus instance;
    private final MutableLiveData<Boolean> isadminLiveData = new MutableLiveData<>();
    private final MutableLiveData<JsonArray> categoriesLiveData = new MutableLiveData<>();
    private final MutableLiveData<JsonArray> merchantsLiveData = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> merchantByIdLiveData = new MutableLiveData<>();

    private Event_Bus() {}

    public static synchronized Event_Bus getInstance() {
        if (instance == null) {
            instance = new Event_Bus();
        }
        return instance;
    }

    public LiveData<Boolean> getIsadminLiveData() {
        return isadminLiveData;
    }

    public void postAdminStatusUpdate(boolean isAdmin) {
        isadminLiveData.postValue(isAdmin);
    }

    public LiveData<JsonArray> getCategoriesLiveData() {
        return categoriesLiveData;
    }

    public void postCategoriesUpdate(JsonArray categories) {
        categoriesLiveData.postValue(categories);
    }

    public void appendCategoriesUpdate(JsonObject category) {
        Log.d("EventBus", "Appending category: " + category);
        JsonArray currentCategories = categoriesLiveData.getValue();
        if (currentCategories == null) {
            currentCategories = new JsonArray();
        }
        currentCategories.add(category);
        categoriesLiveData.postValue(currentCategories);
    }

    public void removeCategoriesUpdate(int categoryId) {
        Log.d("EventBus", "Removing category with ID: " + categoryId);
        JsonArray currentCategories = categoriesLiveData.getValue();
        if (currentCategories != null) {
            JsonArray updatedCategories = new JsonArray();
            for (int i = 0; i < currentCategories.size(); i++) {
                JsonObject category = currentCategories.get(i).getAsJsonObject();
                if (category.get("ID").getAsInt() != categoryId) {
                    updatedCategories.add(category);
                }
            }
            categoriesLiveData.postValue(updatedCategories);
        }
    }

    public LiveData<JsonArray> getMerchantsLiveData() {
        return merchantsLiveData;
    }

    public void postMerchantsUpdate(JsonArray merchants) {
        merchantsLiveData.postValue(merchants);
    }

    public void appendMerchantsUpdate(JsonObject merchant) {
        Log.d("EventBus", "Appending merchant: " + merchant);
        JsonArray currentMerchants = merchantsLiveData.getValue();
        if (currentMerchants == null) {
            currentMerchants = new JsonArray();
        }
        currentMerchants.add(merchant);
        merchantsLiveData.postValue(currentMerchants);
    }

    public void removeMerchantsUpdate(int merchantId) {
        Log.d("EventBus", "Removing merchant with ID: " + merchantId);
        JsonArray currentMerchants = merchantsLiveData.getValue();
        if (currentMerchants != null) {
            JsonArray updatedMerchants = new JsonArray();
            for (int i = 0; i < currentMerchants.size(); i++) {
                JsonObject merchant = currentMerchants.get(i).getAsJsonObject();
                if (merchant.get("ID").getAsInt() != merchantId) {
                    updatedMerchants.add(merchant);
                }
            }
            merchantsLiveData.postValue(updatedMerchants);
        }
    }

    public void editMerchantsUpdate(JsonObject merchant) {
        Log.d("EventBus", "Updating merchant: " + merchant);
        JsonArray currentMerchants = merchantsLiveData.getValue();
        if (currentMerchants != null) {
            for (int i = 0; i < currentMerchants.size(); i++) {
                JsonObject currentMerchant = currentMerchants.get(i).getAsJsonObject();
                if (currentMerchant.get("ID").getAsInt() == merchant.get("ID").getAsInt()) {
                    currentMerchants.set(i, merchant); // Update the existing merchant
                    break;
                }
            }
            merchantsLiveData.postValue(currentMerchants);
        }
    }

    // New method to post merchant by ID update
    public LiveData<JsonObject> getMerchantByIdLiveData() {
        return merchantByIdLiveData;
    }

    public void postMerchantByIdUpdate(JsonObject merchant) {
        Log.d("EventBus", "Posting merchant by ID update: " + merchant);
        merchantByIdLiveData.postValue(merchant);
    }
}