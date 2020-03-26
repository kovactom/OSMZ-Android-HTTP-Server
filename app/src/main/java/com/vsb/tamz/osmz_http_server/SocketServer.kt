package com.vsb.tamz.osmz_http_server

import android.os.Handler
import android.os.Message
import android.util.Log
import com.vsb.tamz.osmz_http_server.resolver.ContentType
import com.vsb.tamz.osmz_http_server.resolver.HttpRequestResolver
import com.vsb.tamz.osmz_http_server.resolver.HttpResponse
import com.vsb.tamz.osmz_http_server.resolver.HttpResponseCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Semaphore

class SocketServer(private val port: Int, private var maxThreads: Int) : Runnable {

    @Volatile
    private var running = true;

    @Volatile
    private var semaphore = Semaphore(maxThreads);

    private var handler: Handler? = null;

    override fun run() {
        Log.d("SERVER", "Creating socket");
        while (running) {
            try {
                ServerSocket(port).use {
                    Log.d("SERVER", "Socket Waiting for connection");

                    val socket: Socket = it.accept();
                    if(!semaphore.tryAcquire()) {
                        val message = "Server too busy";
                        val requestResult = HttpResponse(
                            HttpResponseCode.OK,
                            ContentType.TEXT_HTML,
                            message.toByteArray().size.toLong(),
                            message
                        );
                        requestResult.writeTo(socket);
                        return@use;
                    }

                    GlobalScope.launch {
                        Log.d("SERVER", "Socket Accepted");

                        withContext(Dispatchers.IO) {
                            val input = BufferedReader(InputStreamReader(socket.getInputStream()));
                            val response = input.readLine();
                            Log.d("SERVER","Accepted message: $response");

                            if (response != null) {
                                val requestResult = HttpRequestResolver.resolve(response);
                                if (requestResult is HttpResponse) {
                                    val message = Message();
                                    message.obj = RequestMetric(requestResult.uri ?: "", requestResult.contentLength);
                                    handler?.sendMessage(message)
                                }
                                requestResult.writeTo(socket);
                            } else {
                                socket.close();
                                Log.d("SERVER", "Socket closed");
                            }
                            semaphore.release();
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace();
                Log.d("SERVER", "Error occurred in communication!", e);
            }
        }
    }

    fun stop() {
        this.running = false;
    }

    fun setMetricsHandler(handler: Handler) {
        this.handler = handler;
    }

    fun setMaxThreads(maxThreads: Int): Int {
        if (maxThreads > this.maxThreads) {
            semaphore.release(maxThreads - this.maxThreads);
        } else if (!semaphore.tryAcquire(this.maxThreads - maxThreads)) {
            return this.maxThreads;
        }
        this.maxThreads = maxThreads;
        return this.maxThreads;
    }
}