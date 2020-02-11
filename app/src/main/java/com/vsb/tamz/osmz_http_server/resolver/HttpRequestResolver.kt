package com.vsb.tamz.osmz_http_server.resolver

import android.util.Log
import com.vsb.tamz.osmz_http_server.resolver.HttpMethod.*
import com.vsb.tamz.osmz_http_server.resolver.chain.GetRequestHandler
import java.util.regex.Pattern

object HttpRequestResolver {

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
            return HttpResponse(code = HttpResponseCode.BAD_REQUEST);
        }

        val request = HttpRequest(path, valueOf(method), protocol);
        Log.d("RESOLVER", request.toString());

        return requestHandlerChain.handleRequest(request);
    }
}