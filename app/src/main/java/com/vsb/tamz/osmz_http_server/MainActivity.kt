package com.vsb.tamz.osmz_http_server

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : Activity() {

    private var logScrollView: ScrollView? = null;
    private var logTextView: TextView? = null;
    private var totalSendTextView: TextView? = null;
    private var maxThreadCountText: TextView? = null;

    private var socketServer: SocketServer? = null;
    private var socketServerThread: Thread? = null;
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

    private val READ_EXTERNAL_STORAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_http_server)

        val serverStartBtn = findViewById<Button>(R.id.startServerBtn);
        val serverStopBtn = findViewById<Button>(R.id.stopServerBtn);
        val maxtThreadsCountApplyBtn = findViewById<Button>(R.id.maxThreadsCountApplyButton);

        this.logTextView = findViewById(R.id.metricsLogOutputText);
        this.totalSendTextView = findViewById(R.id.sendBytesText);
        this.maxThreadCountText = findViewById(R.id.maxThreadCountText);
        this.logScrollView = findViewById(R.id.logScrollView);

        serverStartBtn.setOnClickListener(this::onServerStart);
        serverStopBtn.setOnClickListener(this::onServerStop);
        maxtThreadsCountApplyBtn.setOnClickListener(this::onSetMaxThreadCount);
//        maxThreadCountText?.addTextChangedListener(afterTextChanged = this::onMaxThreadCountChange);
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>?,
        grantResults: IntArray
    ) {
        when (requestCode) {
            READ_EXTERNAL_STORAGE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (socketServer == null) {
                    socketServer = SocketServer(12345, handler, 2);
                }
                if (socketServerThread == null) {
                    socketServerThread = Thread(socketServer);
                }
                socketServerThread?.start();
            }
            else -> {
            }
        }
    }


    fun onServerStart(view: View) {
        if (socketServer == null) {
            socketServer = SocketServer(12345, handler, 2);
        }
        if (socketServerThread == null) {
            socketServerThread = Thread(socketServer);
        }
        val permissionCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE
            )
        } else if (socketServerThread?.isAlive == false) {
            socketServerThread?.start();
        }
    }

    fun onServerStop(view: View) {
        socketServer?.stop();
    }

    fun onSetMaxThreadCount(view: View) {
        val maxThreads: Int = maxThreadCountText?.text.toString().toInt();
        val newMaxThreads = socketServer?.setMaxThreads(maxThreads)
        maxThreadCountText?.text = newMaxThreads.toString();
    }

//    fun onMaxThreadCountChange(editable: Editable?) {
//
//    }
}
