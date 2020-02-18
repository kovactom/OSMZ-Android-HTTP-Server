package com.vsb.tamz.osmz_http_server.resolver.chain

import com.vsb.tamz.osmz_http_server.resolver.ContentType
import com.vsb.tamz.osmz_http_server.resolver.HttpRequest
import com.vsb.tamz.osmz_http_server.resolver.HttpResponse
import com.vsb.tamz.osmz_http_server.resolver.HttpResponseCode

abstract class RequestHandler(private val nextHandler: RequestHandler? = null) {

    private val internalServerErrorResponse = """
        <html>
            <body>
                <h1>Internal server error!</h1>
            </body>
        </html>        
    """.trimIndent();

    open fun handleRequest(request: HttpRequest): HttpResponse {
        return nextHandler?.handleRequest(request)
            ?: HttpResponse(
                HttpResponseCode.INTERNAL_SERVER_ERROR,
                ContentType.TEXT_HTML,
                internalServerErrorResponse.toByteArray().size.toLong(),
                internalServerErrorResponse
            );
    }
}