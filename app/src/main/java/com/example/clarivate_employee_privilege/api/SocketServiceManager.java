package com.example.clarivate_employee_privilege.api;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class SocketServiceManager {
    private Context context;
    private SocketService socketService;
    private boolean isBound = false;
    private ServiceConnection connection;

    public SocketServiceManager(Context context, String email, String token) {
        this.context = context;

        // Define the service connection
        this.connection = new ServiceConnection() {

            // Callback when the service is connected
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                // Cast the IBinder to LocalBinder and get the service instance
                SocketService.LocalBinder binder = (SocketService.LocalBinder) service;

                // Get the service instance
                socketService = binder.getService();
                isBound = true;

                // Initialize the socket connection
                socketService.initializeSocket(email, token);
                Log.d("SocketServiceManager", "SocketService connected and socket initialized");
            }

            // Callback when the service is disconnected
            @Override
            public void onServiceDisconnected(ComponentName name) {
                isBound = false;
                Log.d("SocketServiceManager", "SocketService disconnected");
            }

            // Callback when the binding to the service is dead
            @Override
            public void onBindingDied(ComponentName name) {
                isBound = false;
                Log.d("SocketServiceManager", "SocketService binding died");
            }
        };
    }

    public void bindService() {
        // Specify the service to bind
        Intent intent = new Intent(context, SocketService.class);

        // Bind the service with the connection and create the service if it doesn't exist
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        Log.d("SocketServiceManager", "SocketService bound");
    }

    public void unbindService() {
        if (isBound) {
            context.unbindService(connection);
            isBound = false;
            Log.d("SocketServiceManager", "SocketService unbound");
        }
    }

    public SocketService getSocketService() {
        return socketService;
    }
}