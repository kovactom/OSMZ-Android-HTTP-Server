package com.vsb.tamz.osmz_http_server.resolver.chain

import com.vsb.tamz.osmz_http_server.camera.CameraHolder
import com.vsb.tamz.osmz_http_server.resolver.model.*

class CameraPictureRequestHandler(
    private val nextHandler: RequestHandler
): RequestHandler(nextHandler) {

    override fun handleRequest(request: HttpRequest): GenericResponse {
        if (request.path != "/camera/snapshot") {
            return super.handleRequest(request)
        }

        val picture = CameraHolder.lastPictureData;
        return HttpResponse(
            HttpResponseCode.OK,
            ContentType.IMAGE_JPEG,
            picture?.size?.toLong() ?: 0,
            binaryContent = picture,
            uri = request.path
        );
    }
}