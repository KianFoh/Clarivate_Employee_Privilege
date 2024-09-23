package com.example.clarivate_employee_privilege.api;

public class SocketEventCallback {
    public interface EventCallback {
        void onAdminStatusUpdated(boolean isAdmin);
    }
}
