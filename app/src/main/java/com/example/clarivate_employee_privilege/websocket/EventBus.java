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
        Log.d("EventBus", "Appending category: " + category);
        currentCategories.add(category);
        categoriesLiveData.postValue(currentCategories);
    }
}