package com.vsb.tamz.osmz_http_server

import android.util.Log
import com.vsb.tamz.osmz_http_server.resolver.HttpRequestResolver
import java.io.*
import java.net.ServerSocket
import java.net.Socket

class SocketServer(private val port: Int) : Runnable {

    @Volatile
    private var running = true;

    override fun run() {
        Log.d("SERVER", "Creating socket");
        try {
            while (running) {
                ServerSocket(port).use {
                    Log.d("SERVER", "Socket Waiting for connection");

                    val socket: Socket = it.accept();
                    Log.d("SERVER", "Socket Accepted");

                    val output = BufferedWriter(OutputStreamWriter(socket.getOutputStream()));
                    val input = BufferedReader(InputStreamReader(socket.getInputStream()));

                    val response = input.readLine();
                    Log.d("SERVER","Accepted message: $response");

                    val requestResult = HttpRequestResolver.resolve(response);
                    output.write(requestResult.body);

                    output.flush();
                    socket.close();
                    Log.d("SERVER", "Socket closed");
                }
            }
        } catch (e: IOException) {
            e.printStackTrace();
            Log.d("SERVER", "Error occurred in communication!", e);
        }
    }

    public fun stop() {
        this.running = false;
    }
}