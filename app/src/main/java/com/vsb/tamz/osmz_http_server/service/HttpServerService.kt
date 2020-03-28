package com.vsb.tamz.osmz_http_server.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.util.Log
import com.vsb.tamz.osmz_http_server.resolver.SocketServer

class HttpServerService: Service() {

    private val binder = LocalBinder();

    private var socketServer: SocketServer? = null;
    private var socketServerThread: Thread? = null;

    override fun onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread("ServiceStartArguments", THREAD_PRIORITY_BACKGROUND).apply {
            start()
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("SRV", "Starting service");

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        if (socketServer == null) {
            socketServer =
                SocketServer(12345, 2);
        }
        if (socketServerThread == null) {
            socketServerThread = Thread(socketServer);
        }
        if (socketServerThread?.state == Thread.State.NEW || socketServerThread?.state == Thread.State.TERMINATED) {
            Log.d("SRV", "Starting thread");
            socketServerThread?.start();
        }

        // If we get killed, after returning from here, restart
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder;
    }

    override fun onDestroy() {
        Log.d("SRV", "Stopping service");
        socketServer?.stop();
    }

    fun setMetricsHandler(handler: Handler) {
        socketServer?.setMetricsHandler(handler);
    }

    fun setMaxThreadCount(count: Int): Int {
        return socketServer?.setMaxThreads(count)?: 0;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): HttpServerService = this@HttpServerService
    }
}