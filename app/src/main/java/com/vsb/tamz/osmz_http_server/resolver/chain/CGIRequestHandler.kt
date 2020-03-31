package com.vsb.tamz.osmz_http_server.resolver.chain

import android.util.Log
import com.vsb.tamz.osmz_http_server.resolver.model.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URLDecoder
import java.util.concurrent.TimeUnit

class CGIRequestHandler(private val nextHandler: RequestHandler? = null):
    RequestHandler(nextHandler) {

    companion object {
        const val pathPrefix = "/cgi-bin/";
    }

    override fun handleRequest(request: HttpRequest): GenericResponse {
        if (!request.path.startsWith(pathPrefix)) {
            return super.handleRequest(request)
        }

        val rawCommand = request.path.replaceFirst(pathPrefix, "");
        val command = URLDecoder.decode(rawCommand, "UTF-8");
        val program = command.substringBefore(' ')
        val parameters = command.substringAfter(' ', "")

        Log.d("CMD", "$program $parameters");

        val processBuilder =
            if (parameters.isNotEmpty()) ProcessBuilder(program, *parameters.split(" ").toTypedArray())
            else ProcessBuilder(program);
        val cmdProcess = processBuilder.start();
        var processOutput: String? = null;

        cmdProcess.waitFor(10, TimeUnit.SECONDS);
        BufferedReader(InputStreamReader(cmdProcess.inputStream)).use {
            processOutput = it.readText();
        }

        val pageContent = """
            <html>
                <body>
                    <h1>$command</h1>
                    <pre>$processOutput</pre>
                </body>
            </html>
        """.trimIndent()

        return HttpResponse(
            HttpResponseCode.OK,
            ContentType.TEXT_HTML,
            pageContent.toByteArray().size.toLong(),
            pageContent,
            uri = request.path
        )
    }
}