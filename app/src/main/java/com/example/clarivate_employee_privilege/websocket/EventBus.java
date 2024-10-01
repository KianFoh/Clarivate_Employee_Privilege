// EventBus.java
package com.example.clarivate_employee_privilege.websocket;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class EventBus {
    private static EventBus instance;
    private final MutableLiveData<Boolean> adminStatusLiveData = new MutableLiveData<>();
    private final MutableLiveData<JsonArray> categoriesLiveData = new MutableLiveData<>();
    private final MutableLiveData<JsonArray> merchantsLiveData = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> merchantByIdLiveData = new MutableLiveData<>();

    private EventBus() {}

    public static synchronized EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    public LiveData<Boolean> getAdminStatusLiveData() {
        return adminStatusLiveData;
    }

    public void postAdminStatusUpdate(boolean isAdmin) {
        adminStatusLiveData.postValue(isAdmin);
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

    // New method to post merchant by ID update
    public LiveData<JsonObject> getMerchantByIdLiveData() {
        return merchantByIdLiveData;
    }

    public void postMerchantByIdUpdate(JsonObject merchant) {
        Log.d("EventBus", "Posting merchant by ID update: " + merchant);
        merchantByIdLiveData.postValue(merchant);
    }
}