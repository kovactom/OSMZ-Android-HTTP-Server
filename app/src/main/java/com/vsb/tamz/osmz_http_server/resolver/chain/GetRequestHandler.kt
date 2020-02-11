package com.vsb.tamz.osmz_http_server.resolver.chain

import android.util.Log
import com.vsb.tamz.osmz_http_server.resolver.HttpMethod
import com.vsb.tamz.osmz_http_server.resolver.HttpRequest
import com.vsb.tamz.osmz_http_server.resolver.HttpResponse
import com.vsb.tamz.osmz_http_server.resolver.HttpResponseCode

class GetRequestHandler(private val nextHandler: RequestHandler? = null) :
    RequestHandler(nextHandler) {

    private val staticResponse = """
        HTTP/1.1 200 OK
        Content-Type: text/html

        <html>
            <body>
                <h1>Response</h1>
            </body>
        </html>
    """.trimIndent();

    override fun handleRequest(request: HttpRequest): HttpResponse {
        if (request.method !== HttpMethod.GET) {
            super.handleRequest(request)
        }
        Log.d("HANDLER", "GET");
        return HttpResponse(staticResponse, HttpResponseCode.OK);
    }
}