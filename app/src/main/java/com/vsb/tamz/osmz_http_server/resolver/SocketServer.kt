package com.vsb.tamz.osmz_http_server.resolver

import android.os.Handler
import android.os.Message
import android.util.Log
import com.vsb.tamz.osmz_http_server.resolver.model.ContentType
import com.vsb.tamz.osmz_http_server.resolver.model.HttpResponse
import com.vsb.tamz.osmz_http_server.resolver.model.HttpResponseCode
import com.vsb.tamz.osmz_http_server.resolver.model.RequestMetric
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicBoolean

class SocketServer(private val port: Int, private var maxThreads: Int) : Runnable {

    @Volatile
    private var running: AtomicBoolean = AtomicBoolean(true);

    @Volatile
    private var semaphore = Semaphore(maxThreads);

    private var handler: Handler? = null;

    private var socketServer: ServerSocket? = null;

    override fun run() {
        Log.d("SERVER", "Creating socket");
        while (running.get()) {
            try {
                socketServer = ServerSocket(port);
                socketServer?.use {
                    Log.d("SERVER", "Socket Waiting for connection");

                    val socket: Socket = it.accept();
                    if (!semaphore.tryAcquire()) {
                        val message = "Server too busy";
                        val requestResult =
                            HttpResponse(
                                HttpResponseCode.OK,
                                ContentType.TEXT_HTML,
                                message.toByteArray().size.toLong(),
                                message
                            );
                        requestResult.writeTo(socket, running);
                        return@use;
                    }

                    GlobalScope.launch {
                        Log.d("SERVER", "Socket Accepted");

                        withContext(Dispatchers.IO) {
                            val input =
                                BufferedReader(InputStreamReader(socket.getInputStream()));
                            val response = input.readLine();
                            Log.d("SERVER", "Accepted message: $response");

                            if (response != null) {
                                val requestResult = HttpRequestResolver.resolve(response);
                                if (requestResult is HttpResponse) {
                                    val message = Message();
                                    message.obj =
                                        RequestMetric(
                                            requestResult.uri ?: "",
                                            requestResult.contentLength
                                        );
                                    handler?.sendMessage(message)
                                }
                                requestResult.writeTo(socket, running);
                            } else {
                                socket.close();
                                Log.d("SERVER", "Socket closed");
                            }
                            semaphore.release();
                        }
                    }
                }
            } catch (e: SocketException) {
                if (running.get()) {
                    Log.e("SERVER", "Error occurred in creating or accessing socket!", e);
                }
                semaphore.release();
            } catch (e: IOException) {
                Log.e("SERVER", "Error occurred in communication!", e);
                semaphore.release();
            }
        }
    }

    fun stop() {
        this.running.set(false);
        this.socketServer?.close();
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