package com.vsb.tamz.osmz_http_server.resolver.model

data class HttpRequest(
    val path: String,
    val method: HttpMethod,
    val protocol: String,
    val body: String? = ""
) {
}