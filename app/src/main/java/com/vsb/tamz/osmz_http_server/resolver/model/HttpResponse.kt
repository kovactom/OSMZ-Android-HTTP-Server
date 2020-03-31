package com.vsb.tamz.osmz_http_server.resolver.model

import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

data class HttpResponse(
    val status: HttpResponseCode,
    val contentType: ContentType,
    val contentLength: Long,
    val stringContent: String? = null,
    val binaryContent: ByteArray? = null,
    val uri: String? = null
): GenericResponse {

    override fun writeTo(socket: Socket, running: AtomicBoolean) {
        val outputStream = socket.getOutputStream();
        val response = StringBuilder();
        response.appendln("HTTP/1.1 ${status.code} ${status.text}");
        response.appendln("Content-Type: ${contentType.textValue}");
        response.appendln("Content-Length: $contentLength");
        response.appendln();

        outputStream.write(response.toString().toByteArray());
        if (stringContent != null) {
            outputStream.write(stringContent.toByteArray());
        } else if (binaryContent != null) {
            outputStream.write(binaryContent);
        }
        outputStream.flush();
        socket.close();
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpResponse

        if (status != other.status) return false
        if (contentType != other.contentType) return false
        if (contentLength != other.contentLength) return false
        if (stringContent != other.stringContent) return false
        if (binaryContent != null) {
            if (other.binaryContent == null) return false
            if (!binaryContent.contentEquals(other.binaryContent)) return false
        } else if (other.binaryContent != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + contentLength.hashCode()
        result = 31 * result + (stringContent?.hashCode() ?: 0)
        result = 31 * result + (binaryContent?.contentHashCode() ?: 0)
        return result
    }
}