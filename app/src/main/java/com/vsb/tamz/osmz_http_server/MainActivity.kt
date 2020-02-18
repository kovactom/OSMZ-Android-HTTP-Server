package com.vsb.tamz.osmz_http_server

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : Activity() {

    private var socketServer: SocketServer? = null;
    private var socketServerThread: Thread? = null;

    private val READ_EXTERNAL_STORAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_http_server)

        val serverStartBtn = findViewById<Button>(R.id.button1);
        val serverStopBtn = findViewById<Button>(R.id.button2);

        serverStartBtn.setOnClickListener(this::onServerStart);
        serverStopBtn.setOnClickListener(this::onServerStop);
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>?,
        grantResults: IntArray
    ) {
        when (requestCode) {
            READ_EXTERNAL_STORAGE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (socketServer == null) {
                    socketServer = SocketServer(12345);
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
            socketServer = SocketServer(12345);
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
}
