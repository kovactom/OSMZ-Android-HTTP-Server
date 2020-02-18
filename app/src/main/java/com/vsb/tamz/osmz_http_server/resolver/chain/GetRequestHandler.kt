package com.vsb.tamz.osmz_http_server.resolver.chain

import android.os.Environment
import android.util.Log
import com.vsb.tamz.osmz_http_server.resolver.*
import java.nio.file.Files
import java.nio.file.Paths

class GetRequestHandler(private val nextHandler: RequestHandler? = null) :
    RequestHandler(nextHandler) {

    private val fileNotFoundResponse = """
        <html>
            <body>
                <h1>File not found!</h1>
            </body>
        </html>
    """.trimIndent();

    override fun handleRequest(request: HttpRequest): HttpResponse {
        if (request.method !== HttpMethod.GET) {
            super.handleRequest(request)
        }
        Log.d("HANDLER", "GET");

        val externalStorage = Environment.getExternalStorageDirectory();
        val filePath = Paths.get(externalStorage.absolutePath + "/Download${request.path}");
        Log.d("SD: ", filePath.toString());

        if (!Files.exists(filePath)) {
            return HttpResponse(
                HttpResponseCode.NOT_FOUND,
                ContentType.TEXT_HTML,
                fileNotFoundResponse.toByteArray().size.toLong(),
                fileNotFoundResponse
            );
        } else {
            val contentBytes = Files.readAllBytes(filePath);
            val contentType = when(request.path.substring(request.path.lastIndexOf("."))) {
                ".html" -> ContentType.TEXT_HTML
                ".png" -> ContentType.IMAGE_PNG
                else -> ContentType.TEXT_PLAIN
            }
            val contentLength = filePath.toFile().length();

            return HttpResponse(HttpResponseCode.OK, contentType, contentLength, binaryContent = contentBytes);
        }
    }
}