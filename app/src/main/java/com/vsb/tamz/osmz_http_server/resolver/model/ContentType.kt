package com.vsb.tamz.osmz_http_server.resolver.model

enum class ContentType(val textValue: String) {
    TEXT_HTML("text/html"),
    IMAGE_PNG("image/png"),
    IMAGE_JPEG("image/jpeg"),
    TEXT_PLAIN("text/plain"),
    MULTIPART_MIXED("multipart/x-mixed-replace");
}