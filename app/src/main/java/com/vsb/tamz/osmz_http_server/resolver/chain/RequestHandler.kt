package com.vsb.tamz.osmz_http_server.resolver.chain

import com.vsb.tamz.osmz_http_server.resolver.HttpRequest
import com.vsb.tamz.osmz_http_server.resolver.HttpResponse
import com.vsb.tamz.osmz_http_server.resolver.HttpResponseCode

abstract class RequestHandler(private val nextHandler: RequestHandler? = null) {
    open fun handleRequest(request: HttpRequest): HttpResponse {
        return nextHandler?.handleRequest(request) ?: HttpResponse(code = HttpResponseCode.INTERNAL_SERVER_ERROR);
    }
}