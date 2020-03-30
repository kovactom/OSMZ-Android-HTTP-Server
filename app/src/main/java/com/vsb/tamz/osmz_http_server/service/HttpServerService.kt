package com.vsb.tamz.osmz_http_server.service

import android.app.Service
import android.content.Intent
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Binder
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.util.Log
import com.vsb.tamz.osmz_http_server.camera.CameraHolder
import com.vsb.tamz.osmz_http_server.camera.CameraPreview
import com.vsb.tamz.osmz_http_server.resolver.SocketServer

class HttpServerService: Service() {

    private val binder = LocalBinder();

    private var socketServer: SocketServer? = null;
    private var socketServerThread: Thread? = null;

    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private var mSurfaceTexture: SurfaceTexture? = null;

    override fun onCreate() {
        HandlerThread("ServiceStartArguments", THREAD_PRIORITY_BACKGROUND).apply {
            start()

            mCamera = CameraHolder.getCameraInstance()
            mPreview = mCamera?.let {
                CameraPreview(this@HttpServerService, it)
            }
            mSurfaceTexture = SurfaceTexture(0);
            mCamera?.setPreviewTexture(mSurfaceTexture);
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("SRV", "Starting service");

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
            mCamera?.startPreview();
            CameraHolder.startStreaming();
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder;
    }

    override fun onDestroy() {
        Log.d("SRV", "Stopping service");
        socketServer?.stop();
        CameraHolder.releaseCamera();
    }

    fun setMetricsHandler(handler: Handler) {
        socketServer?.setMetricsHandler(handler);
    }

    fun setMaxThreadCount(count: Int): Int {
        return socketServer?.setMaxThreads(count)?: 0;
    }

    inner class LocalBinder : Binder() {
        fun getService(): HttpServerService = this@HttpServerService
    }
}