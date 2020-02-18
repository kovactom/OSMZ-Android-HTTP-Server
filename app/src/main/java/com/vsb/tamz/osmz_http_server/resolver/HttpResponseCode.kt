package com.vsb.tamz.osmz_http_server.resolver

enum class HttpResponseCode(val code: Int, val text: String) {
    BAD_REQUEST(400, "Bad Request"), OK(200, "OK"), INTERNAL_SERVER_ERROR(500, "Internal Server Error"), NOT_FOUND(404, "Not Found");

}