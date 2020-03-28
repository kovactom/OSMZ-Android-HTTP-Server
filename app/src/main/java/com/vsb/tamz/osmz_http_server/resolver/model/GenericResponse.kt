package com.vsb.tamz.osmz_http_server.resolver.model

import java.net.Socket

interface GenericResponse {
    fun writeTo(socket: Socket);
}