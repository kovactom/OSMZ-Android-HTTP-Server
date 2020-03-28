package com.vsb.tamz.osmz_http_server

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vsb.tamz.osmz_http_server.camera.CameraPreview
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


class CameraActivity : Activity() {

    private val REQUEST_WRITE_STORAGE_REQUEST_CODE: Int = 10;
    private val MEDIA_TYPE_IMAGE = 1
    private val MEDIA_TYPE_VIDEO = 2

    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null

    private var scheduledExecutorService: ScheduledExecutorService? = null;
    private var scheduleCaptureTask: ScheduledFuture<*>? = null;

    companion object {
        @Volatile
        var lastPictureData: ByteArray? = null;

        @Volatile
        var currentPictureData: ByteArray? = null;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        mCamera = getCameraInstance()
        mPreview = mCamera?.let {
            CameraPreview(this, it)
        }

        mPreview?.also {
            val preview: FrameLayout = findViewById(R.id.camera_preview)
            preview.addView(it)
        }

        val captureButton: Button = findViewById(R.id.captureButton)
        val startStreamButton: Button = findViewById(R.id.startStreamButton);
        val stopStreamButton: Button = findViewById(R.id.stopStreamButton);
        val startCaptureButton: Button = findViewById(R.id.startCaptureButton);
        val stopCaptureButton: Button = findViewById(R.id.stopCaptureButton);

        captureButton.setOnClickListener(this::onPictureTake);
        startStreamButton.setOnClickListener(this::startStreaming);
        stopStreamButton.setOnClickListener(this::stopStreaming);
        startCaptureButton.setOnClickListener(this::startCapturing)
        stopCaptureButton.setOnClickListener(this::stopCapturing);

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        requestAppPermissions();
    }

    private fun onPictureTake(view: View) {
        Log.d("CAMERA", "taking picture...");
        mCamera?.takePicture(null, null, mPicture)
    }

    private fun startCapturing(view: View) {
        scheduleCaptureTask = scheduledExecutorService?.scheduleAtFixedRate({
            mCamera?.takePicture(null, null, {
                    data, _ -> lastPictureData = data;
                    mCamera?.startPreview();
            })
        }, 0, 5, TimeUnit.SECONDS);
    }

    private fun stopCapturing(view: View) {
        scheduleCaptureTask?.cancel(false);
    }

    private fun startStreaming(view: View) {
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

    private fun stopStreaming(view: View) {
        mCamera?.setPreviewCallback(null);
    }

    private val mPicture = Camera.PictureCallback { data, _ ->
        val pictureFile: File = getOutputMediaFile(MEDIA_TYPE_IMAGE) ?: run {
            Log.d(TAG, ("Error creating media file, check storage permissions"))
            return@PictureCallback
        }

        Log.d("CAMERA", "saving picture...");
        try {
            val fos = FileOutputStream(pictureFile)
            fos.write(data)
            fos.close()
            mCamera?.startPreview();
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "File not found: ${e.message}")
        } catch (e: IOException) {
            Log.d(TAG, "Error accessing file: ${e.message}")
        }
    }

    private fun getCameraInstance(): Camera? {
        return try {
            if (mCamera == null) {
                mCamera = Camera.open()
            }
            mCamera;
        } catch (e: Exception) {
            null // returns null if camera is unavailable )
        }
    }

    /** Create a File for saving an image or video */
    private fun getOutputMediaFile(type: Int): File? {
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

    override fun onPause() {
        super.onPause()
        stopTasks();
        releaseCamera() // release the camera immediately on pause event
    }

    private fun stopTasks() {
        scheduledExecutorService?.shutdownNow();
    }

    private fun releaseCamera() {
        mCamera?.setPreviewCallback(null);
        mCamera?.release() // release the camera for other applications
        mCamera = null
    }

    private fun requestAppPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return
        }
        if (hasReadPermissions() && hasWritePermissions()) {
            return
        }
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),  REQUEST_WRITE_STORAGE_REQUEST_CODE
        )
    }

    private fun hasReadPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            baseContext,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasWritePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            baseContext,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
}
