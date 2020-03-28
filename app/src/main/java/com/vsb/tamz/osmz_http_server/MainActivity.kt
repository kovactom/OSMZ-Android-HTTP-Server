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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : Activity() {

    private val READ_EXTERNAL_STORAGE = 1
    private val HTTP_SERVER_NOTIFICATION_ID = 1;
    private val HTTP_SERVER_CHANNEL = "HTTP_SERVER_CHANNEL";

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
            totalSendTextView?.text = "$totalSendSize B";

            logScrollView?.post {
                logScrollView?.fullScroll(View.FOCUS_DOWN);
            }
        }
    }

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
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
        // Bind to LocalService
        serverServiceIntent = Intent(this, HttpServerService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
        mBound = false
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>?,
        grantResults: IntArray
    ) {
        when (requestCode) {
            READ_EXTERNAL_STORAGE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // start service
                startServerService();
            }
        }
    }

    fun startServerService() {
        // Bind to LocalService
        serverServiceIntent = Intent(this, HttpServerService::class.java).also { intent ->
            startService(intent);
            if (!mBound) {
                bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    fun stopServerService() {
        mServerService.stopSelf();
        if (mBound) {
            unbindService(connection)
            mBound = false;
        };
        mServerService.stopForeground(true);
    }

    fun onServerStart(view: View) {
        val permissionCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE
            )
        } else {
            // start service
            startServerService();
        }
    }

    fun onServerStop(view: View) {
        // stop service
        stopServerService();
    }

    fun onSetMaxThreadCount(view: View) {
        val maxThreads: Int = maxThreadCountText?.text.toString().toInt();
        val newMaxThreads = mServerService.setMaxThreadCount(maxThreads)
        maxThreadCountText?.text = newMaxThreads.toString();
    }

    fun onOpenCamera(view: View) {
        val intent = Intent(this, CameraActivity::class.java);
        startActivity(intent);
    }
}
