package com.vsb.tamz.osmz_http_server

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button

class MainActivity : Activity() {

    private var socketServer: SocketServer? = null;
    private var socketServerThread: Thread? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_http_server)

        val serverStartBtn = findViewById<Button>(R.id.button1);
        val serverStopBtn = findViewById<Button>(R.id.button2);

        serverStartBtn.setOnClickListener(this::onServerStart);
        serverStopBtn.setOnClickListener(this::onServerStop);
    }

    fun onServerStart(view: View) {
        if (socketServer == null) {
            socketServer = SocketServer(12345);
        }
        if (socketServerThread == null) {
            socketServerThread = Thread(socketServer);
        }
        socketServerThread?.start();
    }

    fun onServerStop(view: View) {
        socketServer?.stop();
    }
}
