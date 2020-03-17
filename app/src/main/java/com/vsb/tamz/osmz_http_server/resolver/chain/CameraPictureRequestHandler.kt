package com.vsb.tamz.osmz_http_server.resolver.chain

import com.vsb.tamz.osmz_http_server.CameraActivity
import com.vsb.tamz.osmz_http_server.resolver.ContentType
import com.vsb.tamz.osmz_http_server.resolver.HttpRequest
import com.vsb.tamz.osmz_http_server.resolver.HttpResponse
import com.vsb.tamz.osmz_http_server.resolver.HttpResponseCode

class CameraPictureRequestHandler(
    private val nextHandler: RequestHandler
): RequestHandler(nextHandler) {

    override fun handleRequest(request: HttpRequest): HttpResponse {
        if (request.path != "/camera/snapshot") {
            return super.handleRequest(request)
        }

        val picture = CameraActivity.lastPictureData;
        return HttpResponse(HttpResponseCode.OK, ContentType.IMAGE_JPEG, picture?.size?.toLong() ?: 0, binaryContent = picture);
    }
}