package com.vsb.tamz.osmz_http_server.camera

import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import android.os.Environment
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CameraHolder {

    const val MEDIA_TYPE_IMAGE = 1

    @Volatile
    var lastPictureData: ByteArray? = null;

    @Volatile
    var currentPictureData: ByteArray? = null;

    private var mCamera: Camera? = null

    @Synchronized
    fun getCameraInstance(): Camera? {
        return try {
            if (mCamera == null) {
                mCamera = Camera.open()
            }
            mCamera;
        } catch (e: Exception) {
            null
        }
    }

    @Synchronized
    fun startStreaming() {
        val parameters = mCamera?.parameters
        val size = parameters?.previewSize
        val rectangle = Rect()
        rectangle.bottom = size?.height ?: 0
        rectangle.top = 0
        rectangle.left = 0
        rectangle.right = size?.width ?: 0

        mCamera?.setPreviewCallback { data, _ ->
            val image = YuvImage(
                data,
                ImageFormat.NV21,
                size?.width ?: 0,
                size?.height ?: 0,
                null
            )
            val imageBytes = ByteArrayOutputStream()
            image.compressToJpeg(rectangle, 100, imageBytes)
            currentPictureData = imageBytes.toByteArray();
        }
    }

    @Synchronized
    fun stopStreaming() {
        mCamera?.setPreviewCallback(null);
    }


    fun getOutputMediaFile(type: Int): File? {
        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "MyCameraApp"
        )
        mediaStorageDir.apply {
            if (!exists()) {
                if (!mkdirs()) {
                    Log.d("MyCameraApp", "failed to create directory")
                    return null
                }
            }
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return when (type) {
            MEDIA_TYPE_IMAGE -> {
                File("${mediaStorageDir.path}${File.separator}IMG_$timeStamp.jpg")
            }
            else -> null
        }
    }

    @Synchronized
    fun releaseCamera() {
        mCamera?.setPreviewCallback(null);
        mCamera?.release()
        mCamera = null
    }
}