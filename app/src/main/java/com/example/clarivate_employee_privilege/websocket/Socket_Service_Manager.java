package com.example.clarivate_employee_privilege.websocket;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class Socket_Service_Manager {
    private Context context;
    private Socket_Service socketService;
    private boolean isBound = false;
    private ServiceConnection connection;

    public Socket_Service_Manager(Context context, String email, String token) {
        this.context = context;

        // Define the service connection
        this.connection = new ServiceConnection() {

            // Callback when the service is connected
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                // Cast the IBinder to LocalBinder and get the service instance
                Socket_Service.LocalBinder binder = (Socket_Service.LocalBinder) service;

                // Get the service instance
                socketService = binder.getService();
                isBound = true;

                // Initialize the socket connection
                socketService.startSocketConnection(email);
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
        Intent intent = new Intent(context, Socket_Service.class);

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

    public Socket_Service getSocketService() {
        return socketService;
    }
}