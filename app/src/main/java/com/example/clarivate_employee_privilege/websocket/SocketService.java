package com.example.clarivate_employee_privilege.websocket;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.clarivate_employee_privilege.R;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketService extends Service {

    private final IBinder binder = new LocalBinder();
    private Socket socket;
    private SocketEventListener socketEventListener;

    public class LocalBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void initializeSocket(String email, String token) {
        if (socket != null) {
            socket.disconnect();
        }

        try {
            IO.Options options = new IO.Options();
            options.query = "email=" + email + "&token=" + token;

            socket = IO.socket(getString(R.string.api_url), options);

            socketEventListener = new SocketEventListener(this, getApplicationContext());

            socket.on(Socket.EVENT_CONNECT, socketEventListener.onConnect);
            socket.on(Socket.EVENT_DISCONNECT, socketEventListener.onDisconnect);
            socket.on(Socket.EVENT_CONNECT_ERROR, socketEventListener.onConnectError);
            socket.on("admin_status_update", socketEventListener.onAdminStatusUpdate);
            socket.on("category_added", socketEventListener.onCategoriesAddedUpdate);
            socket.on("category_deleted", socketEventListener.onCategoriesDeletedUpdate);
            socket.on("merchant_added", socketEventListener.onMerchantAddedUpdate);
            socket.on("merchant_deleted", socketEventListener.onMerchantDeletedUpdate);
            socket.on("merchant_edited", socketEventListener.OnMerchantEditUpdate);
            socket.connect();
        }
        catch (URISyntaxException e) {
            Log.d("SocketService", "Error initializing socket: " + e.getMessage());
        }
    }

    public void reconnectSocket(String newToken) {
        SharedPreferences sharedpreferences = getApplicationContext().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String email = sharedpreferences.getString("email", "");
        initializeSocket(email, newToken);
    }

    public void disconnectSocket() {
        if (socket != null) {
            socket.disconnect();
        }
    }

    public SocketEventListener getSocketEventListener() {
        return socketEventListener;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectSocket();
        Log.d("SocketService", "Service destroyed");
    }
}