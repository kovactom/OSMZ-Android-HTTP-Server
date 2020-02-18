package com.vsb.tamz.osmz_http_server.resolver

import android.util.Log
import com.vsb.tamz.osmz_http_server.resolver.HttpMethod.*
import com.vsb.tamz.osmz_http_server.resolver.chain.GetRequestHandler
import java.util.regex.Pattern

object HttpRequestResolver {

    private val BAD_REQUEST_MESSAGE = """
        <html>
            <body>
                <h1>Bad request!</h1>
            </body>
        </html>        
    """.trimIndent();

    private val pattern = Pattern.compile("(${GET}|${POST}|${PUT}|${DELETE}|${HEAD}|${OPTIONS}|${PATCH}) (/.*) (HTTP/.*)");
    private val requestHandlerChain = GetRequestHandler();

    fun resolve(responseBody: String): HttpResponse {
        val matcher = pattern.matcher(responseBody);

        var method: String? = null;
        var path: String? = null;
        var protocol: String? = null;

        while (matcher.find()) {
            method = matcher.group(1);
            path = matcher.group(2);
            protocol = matcher.group(3);
        }

        if (method == null || path == null || protocol == null) {
            return HttpResponse(
                HttpResponseCode.BAD_REQUEST,
                ContentType.TEXT_HTML,
                BAD_REQUEST_MESSAGE.toByteArray().size.toLong(),
                BAD_REQUEST_MESSAGE
            );
        }

        val request = HttpRequest(path, valueOf(method), protocol);
        Log.d("RESOLVER", request.toString());

        return requestHandlerChain.handleRequest(request);
    }
}