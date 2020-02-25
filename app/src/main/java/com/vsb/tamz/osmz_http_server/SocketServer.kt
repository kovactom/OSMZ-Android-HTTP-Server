package com.vsb.tamz.osmz_http_server

import android.os.Handler
import android.os.Message
import android.util.Log
import com.vsb.tamz.osmz_http_server.resolver.HttpRequestResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket

class SocketServer(private val port: Int, private val handler: Handler) : Runnable {

    @Volatile
    private var running = true;

    override fun run() {
        Log.d("SERVER", "Creating socket");
        try {
            while (running) {
                ServerSocket(port).use {
                    Log.d("SERVER", "Socket Waiting for connection");

                    val socket: Socket = it.accept();
                    GlobalScope.launch {
                        Log.d("SERVER", "Socket Accepted");

                        withContext(Dispatchers.IO) {
                            val outputStream = socket.getOutputStream();
                            val input = BufferedReader(InputStreamReader(socket.getInputStream()));

                            val response = input.readLine();
                            Log.d("SERVER","Accepted message: $response");

                            if (response != null) {
                                val requestResult = HttpRequestResolver.resolve(response);
                                val message = Message();
                                message.obj = RequestMetric(requestResult.uri ?: "", requestResult.contentLength);
                                handler.sendMessage(message)
                                requestResult.writeTo(outputStream);
                            }

                            socket.close();
                        }
                        Log.d("SERVER", "Socket closed");
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace();
            Log.d("SERVER", "Error occurred in communication!", e);
        }
    }

    fun stop() {
        this.running = false;
    }
}