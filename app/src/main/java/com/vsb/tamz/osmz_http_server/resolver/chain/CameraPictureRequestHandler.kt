package com.vsb.tamz.osmz_http_server.resolver.chain

import android.hardware.Camera
import com.vsb.tamz.osmz_http_server.CameraActivity
import com.vsb.tamz.osmz_http_server.resolver.ContentType
import com.vsb.tamz.osmz_http_server.resolver.HttpRequest
import com.vsb.tamz.osmz_http_server.resolver.HttpResponse
import com.vsb.tamz.osmz_http_server.resolver.HttpResponseCode
import java.util.function.Consumer

class CameraPictureRequestHandler(
    private val nextHandler: RequestHandler
): RequestHandler(nextHandler) {

    override fun handleRequest(request: HttpRequest, callback: Consumer<HttpResponse>) {
        if (request.path != "/camera/snapshot") {
            return super.handleRequest(request, callback)
        }

        val camera = CameraActivity.getCameraInstance();
        val cameraCallback = Camera.PictureCallback { data, _ ->
            val httpResponse = HttpResponse(
                HttpResponseCode.OK,
                ContentType.IMAGE_JPEG,
                data.size.toLong(),
                binaryContent = data
            )
            camera?.startPreview();
            callback.accept(httpResponse);
        }
        camera?.takePicture(null, null, cameraCallback);
    }
}