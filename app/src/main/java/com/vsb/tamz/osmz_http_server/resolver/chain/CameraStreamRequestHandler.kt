package com.vsb.tamz.osmz_http_server.resolver.chain

import com.vsb.tamz.osmz_http_server.CameraActivity
import com.vsb.tamz.osmz_http_server.resolver.ContentType
import com.vsb.tamz.osmz_http_server.resolver.GenericResponse
import com.vsb.tamz.osmz_http_server.resolver.HttpRequest
import java.net.Socket

class CameraStreamRequestHandler(
    private val nextHandler: RequestHandler
): RequestHandler(nextHandler) {

    override fun handleRequest(request: HttpRequest): GenericResponse {
        if (request.path != "/camera/stream") {
            super.handleRequest(request)
        }

        return object : GenericResponse {
            override fun writeTo(socket: Socket) {
                val outputStream = socket.getOutputStream();
                val boundaryMark = "PictureBoundary";
                val headerResponse = StringBuilder();
                headerResponse.appendln("HTTP/1.1 200 OK");
                headerResponse.appendln("Content-Type: ${ContentType.MULTIPART_MIXED.textValue}; boundary=\"$boundaryMark\"");
                headerResponse.appendln();

                outputStream.write(headerResponse.toString().toByteArray());

                val response = StringBuilder();
                response.appendln("--$boundaryMark");
                response.appendln("Content-Type: ${ContentType.IMAGE_JPEG.textValue}");
                response.appendln();
                outputStream.write(response.toString().toByteArray())
                outputStream.write(CameraActivity.lastPictureData);
                outputStream.flush();
                socket.close();
            }
        }
    }
}