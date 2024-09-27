package com.example.clarivate_employee_privilege.websocket;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class EventBus {
    private static EventBus instance;
    private final MutableLiveData<Boolean> adminStatusLiveData = new MutableLiveData<>();

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
}