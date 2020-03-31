package com.vsb.tamz.osmz_http_server.resolver.model

import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

interface GenericResponse {
    fun writeTo(socket: Socket, running: AtomicBoolean);
}