package com.vsb.tamz.osmz_http_server.resolver

import java.net.Socket

interface GenericResponse {
    fun writeTo(socket: Socket);
}