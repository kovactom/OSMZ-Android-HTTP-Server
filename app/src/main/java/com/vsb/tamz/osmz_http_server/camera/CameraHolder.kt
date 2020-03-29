package com.vsb.tamz.osmz_http_server.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CameraHolder {

    val MEDIA_TYPE_IMAGE = 1
    val MEDIA_TYPE_VIDEO = 2

    @Volatile
    var lastPictureData: ByteArray? = null;

    @Volatile
    var currentPictureData: ByteArray? = null;

    private val REQUEST_WRITE_STORAGE_REQUEST_CODE: Int = 10;
    private var mCamera: Camera? = null

    fun getCameraInstance(): Camera? {
        return try {
            if (mCamera == null) {
                mCamera = Camera.open()
            }
            mCamera;
        } catch (e: Exception) {
            null // returns null if camera is unavailable )
        }
    }

    fun startStreaming() {
        val parameters = mCamera?.parameters
        val size = parameters?.previewSize
        val rectangle = Rect()
        rectangle.bottom = size?.height ?: 0
        rectangle.top = 0
        rectangle.left = 0
        rectangle.right = size?.width ?: 0

//        mCamera?.startPreview();
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

    fun stopStreaming() {
        mCamera?.setPreviewCallback(null);
    }


    /** Create a File for saving an image or video */
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
            MEDIA_TYPE_VIDEO -> {
                File("${mediaStorageDir.path}${File.separator}VID_$timeStamp.mp4")
            }
            else -> null
        }
    }

    fun releaseCamera() {
        mCamera?.setPreviewCallback(null);
        mCamera?.release() // release the camera for other applications
        mCamera = null
    }

    fun requestAppPermissions(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return
        }
        if (hasReadPermissions(activity.baseContext) && hasWritePermissions(activity.baseContext)) {
            return
        }
        ActivityCompat.requestPermissions(
            activity, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),  REQUEST_WRITE_STORAGE_REQUEST_CODE
        )
    }

    fun hasReadPermissions(baseContext: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            baseContext,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasWritePermissions(baseContext: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            baseContext,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
}