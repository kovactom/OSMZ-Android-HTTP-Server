package com.vsb.tamz.osmz_http_server

import android.Manifest
import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.*
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.vsb.tamz.osmz_http_server.resolver.model.RequestMetric
import com.vsb.tamz.osmz_http_server.service.HttpServerService


class MainActivity : Activity() {

    companion object {
        private const val PERMISSION_REQUEST_ID = 1;
        private const val HTTP_SERVER_NOTIFICATION_ID = 1;
        private const val HTTP_SERVER_CHANNEL = "HTTP_SERVER_CHANNEL";
    }

    private lateinit var mServerService: HttpServerService;
    private var serverServiceIntent: Intent? = null;
    private var mBound: Boolean = false;

    private var logScrollView: ScrollView? = null;
    private var logTextView: TextView? = null;
    private var totalSendTextView: TextView? = null;
    private var maxThreadCountText: TextView? = null;

    private var totalSendSize: Long = 0;

    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val metric: RequestMetric = msg.obj as RequestMetric;
            totalSendSize += metric.responseSize;
            logTextView?.append("URI: ${metric.uri} Size: ${metric.responseSize} B\n");
            totalSendTextView?.text = getString(R.string.sendBytesTemplate, totalSendSize);

            logScrollView?.post {
                logScrollView?.fullScroll(View.FOCUS_DOWN);
            }
        }
    }

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as HttpServerService.LocalBinder
            mServerService = binder.getService()
            mBound = true

            mServerService.setMetricsHandler(handler);

            val channel = NotificationChannel(HTTP_SERVER_CHANNEL, HTTP_SERVER_CHANNEL, NotificationManager.IMPORTANCE_NONE);
            val notificationService = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            notificationService.createNotificationChannel(channel);

            val pendingIntent: PendingIntent =
                Intent(this@MainActivity, MainActivity::class.java).let { notificationIntent ->
                    PendingIntent.getActivity(this@MainActivity, 0, notificationIntent, 0)
                }

            val notification: Notification = Notification.Builder(this@MainActivity, HTTP_SERVER_CHANNEL)
                .setContentTitle("HTTP Server")
                .setContentText("HTTP Server is still running.")
                .setContentIntent(pendingIntent)
                .build()

            mServerService.startForeground(HTTP_SERVER_NOTIFICATION_ID, notification)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_http_server)

        val serverStartBtn = findViewById<Button>(R.id.startServerBtn);
        val serverStopBtn = findViewById<Button>(R.id.stopServerBtn);
        val maxtThreadsCountApplyBtn = findViewById<Button>(R.id.maxThreadsCountApplyButton);
        val openCamerButton = findViewById<Button>(R.id.openCameraButton);

        this.logTextView = findViewById(R.id.metricsLogOutputText);
        this.totalSendTextView = findViewById(R.id.sendBytesText);
        this.maxThreadCountText = findViewById(R.id.maxThreadCountText);
        this.logScrollView = findViewById(R.id.logScrollView);

        serverStartBtn.setOnClickListener(this::onServerStart);
        serverStopBtn.setOnClickListener(this::onServerStop);
        maxtThreadsCountApplyBtn.setOnClickListener(this::onSetMaxThreadCount);
        openCamerButton.setOnClickListener(this::onOpenCamera);

        startServerService();
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mBound) {
            unbindService(connection)
            mBound = false
        }
    }

    private fun startServerService() {
        val hasReadExternalStoragePermission = hasReadExternalStoragePermission(this);
        val hasWriteExternalStoragePermission = hasWriteExternalStoragePermission(this);
        val hasCameraPermission = hasCameraPermission(this);

        if(hasReadExternalStoragePermission && hasWriteExternalStoragePermission && hasCameraPermission) {
            serverServiceIntent = Intent(this, HttpServerService::class.java).also { intent ->
                startService(intent);
                if (!mBound) {
                    bindService(intent, connection, Context.BIND_AUTO_CREATE)
                    Toast
                        .makeText(this@MainActivity, "HTTP Server started...", Toast.LENGTH_LONG)
                        .show();
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ),
                PERMISSION_REQUEST_ID
            )
        }
    }

    private fun stopServerService() {
        mServerService.stopSelf();
        if (mBound) {
            unbindService(connection)
            mBound = false;
        };
        mServerService.stopForeground(true);
        Toast
            .makeText(this@MainActivity, "HTTP Server stopped.", Toast.LENGTH_LONG)
            .show();
    }

    private fun onServerStart(view: View) {
        startServerService();
    }

    private fun onServerStop(view: View) {
        stopServerService();
    }

    private fun onSetMaxThreadCount(view: View) {
        val maxThreads: Int = maxThreadCountText?.text.toString().toInt();
        val newMaxThreads = mServerService.setMaxThreadCount(maxThreads)
        maxThreadCountText?.text = newMaxThreads.toString();
    }

    private fun onOpenCamera(view: View) {
        val intent = Intent(this, CameraActivity::class.java);
        startActivity(intent);
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>?,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_ID -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startServerService();
            }
        }
    }
}
