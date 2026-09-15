package org.autojs.autojs.mcp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class McpService extends Service {
    private static final String TAG = "McpService";
    private McpServer server;
    
    @Override
    public void onCreate() {
        super.onCreate();
        startServer();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (server == null || !server.isAlive()) {
            startServer();
        }
        return START_STICKY;
    }
    
    private void startServer() {
        try {
            server = new McpServer(9317);
            server.start();
            Log.i(TAG, "MCP Server started on port 9317");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start MCP Server", e);
        }
    }
    
    @Override
    public void onDestroy() {
        if (server != null) {
            server.stop();
            Log.i(TAG, "MCP Server stopped");
        }
        super.onDestroy();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
