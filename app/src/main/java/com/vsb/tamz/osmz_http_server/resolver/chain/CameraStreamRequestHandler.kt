package com.vsb.tamz.osmz_http_server.resolver.chain

import android.util.Log
import com.vsb.tamz.osmz_http_server.CameraActivity
import com.vsb.tamz.osmz_http_server.resolver.model.ContentType
import com.vsb.tamz.osmz_http_server.resolver.model.GenericResponse
import com.vsb.tamz.osmz_http_server.resolver.model.HttpRequest
import java.net.Socket
import java.net.SocketException

class CameraStreamRequestHandler(
    private val nextHandler: RequestHandler
): RequestHandler(nextHandler) {

    override fun handleRequest(request: HttpRequest): GenericResponse {
        if (request.path != "/camera/stream") {
            return super.handleRequest(request)
        }

        return object :
            GenericResponse {
            override fun writeTo(socket: Socket) {
                val outputStream = socket.getOutputStream();
                val boundaryMark = "PictureBoundary";
                val headerResponse = StringBuilder();
                headerResponse.appendln("HTTP/1.1 200 OK");
                headerResponse.appendln("Content-Type: ${ContentType.MULTIPART_MIXED.textValue}; boundary=\"$boundaryMark\"");
                headerResponse.appendln();

                outputStream.write(headerResponse.toString().toByteArray());

                try {
                    while (socket.isConnected) {
                        val response = StringBuilder();
                        response.appendln("--$boundaryMark");
                        response.appendln("Content-Type: ${ContentType.IMAGE_JPEG.textValue}");
                        response.appendln();
                        outputStream.write(response.toString().toByteArray())
                        CameraActivity.currentPictureData?.let { outputStream.write(it) };
                        outputStream.flush();
                        Log.d("STREAM", "MJPEG frame sent.")
                        Thread.sleep(40)
                    }
                } catch (e: SocketException) {
                    Log.d("STREAM", "Stream closed.");
                }
            }
        }
    }
}