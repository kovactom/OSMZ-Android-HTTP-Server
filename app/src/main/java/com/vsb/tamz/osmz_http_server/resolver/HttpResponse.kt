package com.vsb.tamz.osmz_http_server.resolver

data class HttpResponse(
    val body: String = "",
    val code: HttpResponseCode
) {

}