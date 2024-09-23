package com.example.clarivate_employee_privilege.api;

import android.content.Context;
import android.util.Log;

import com.example.clarivate_employee_privilege.authentication.AuthUtils;

import org.json.JSONObject;

import io.socket.emitter.Emitter;

public class SocketEventListener {

    private final SocketService socketService;
    private SocketEventCallback.EventCallback eventCallback;
    private Context context;

    public SocketEventListener(SocketService socketService, Context context) {
        this.socketService = socketService;
        this.context = context;
    }

    public Emitter.Listener onConnect = args -> Log.d("SocketEventListener", "Connected to the server");

    public Emitter.Listener onDisconnect = args -> Log.d("SocketEventListener", "Disconnected from the server");

    public Emitter.Listener onConnectError = args -> {
        Log.d("SocketEventListener", "Error connecting to the server: " + args[0]);

        try {
            Object errorObject = args[0];
            String errorMessage = null;

            // Check if the error message is a string or JSON object
            if (errorObject instanceof String) {
                errorMessage = (String) errorObject;
            }
            // Extract error message from JSON object
            else if (errorObject instanceof JSONObject) {
                JSONObject jsonError = (JSONObject) errorObject;
                errorMessage = jsonError.optString("message");
            }

            // Check if the error message matches
            if ("Connection rejected by server".equals(errorMessage)) {
                // Refresh the token
                AuthUtils.refreshToken(context, new AuthUtils.TokenRefreshCallback() {
                    // Reconnect the socket with the new token
                    @Override
                    public void onTokenRefreshed(String newToken) {
                        socketService.reconnectSocket(newToken);
                    }
                    @Override
                    public void onTokenRefreshFailed() {
                        Log.d("SocketEventListener", "Failed to refresh token");
                    }
                });
            }
        } catch (Exception e) {
            Log.e("SocketEventListener", "Failed to parse error message", e);
        }
    };

    public Emitter.Listener onAdminStatusUpdate = args -> {
        JSONObject data = (JSONObject) args[0];
        boolean isAdmin = data.optBoolean("isadmin");
        Log.d("SocketEventListener", "User update received: isAdmin = " + isAdmin);

        // Refresh UI based on admin status
        // Notify the callback
        if (eventCallback != null) {
            eventCallback.onAdminStatusUpdated(isAdmin);
        }
    };

    public void setEventCallback(SocketEventCallback.EventCallback callback) {
        if (callback == null) {
            Log.d("SocketEventListener", "Event callback is missing");
        }
        this.eventCallback = callback;
    }
}