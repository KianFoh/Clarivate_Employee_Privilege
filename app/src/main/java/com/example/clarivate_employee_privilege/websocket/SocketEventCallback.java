package com.example.clarivate_employee_privilege.websocket;

public class SocketEventCallback {
    public interface EventCallback {
        void onAdminStatusUpdated(boolean isAdmin);
    }
}
