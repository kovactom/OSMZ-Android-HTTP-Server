package com.vsb.tamz.osmz_http_server.resolver.chain

import com.vsb.tamz.osmz_http_server.resolver.HttpRequest
import com.vsb.tamz.osmz_http_server.resolver.HttpResponse
import java.util.function.Consumer

class CameraStreamRequestHandler(
    private val nextHandler: RequestHandler
): RequestHandler(nextHandler) {

    override fun handleRequest(request: HttpRequest, callback: Consumer<HttpResponse>) {
        if (request.path != "/camera/stream") {
            super.handleRequest(request, callback)
        }


    }
}