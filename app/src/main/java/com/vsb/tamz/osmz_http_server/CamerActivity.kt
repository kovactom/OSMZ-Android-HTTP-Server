package com.vsb.tamz.osmz_http_server

import android.app.Activity
import android.content.ContentValues.TAG
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import com.vsb.tamz.osmz_http_server.camera.CameraHolder
import com.vsb.tamz.osmz_http_server.camera.CameraPreview
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


class CameraActivity : Activity() {

    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null

    private var scheduledExecutorService: ScheduledExecutorService? = null;
    private var scheduleCaptureTask: ScheduledFuture<*>? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        mCamera = CameraHolder.getCameraInstance()
        mPreview = mCamera?.let {
            CameraPreview(this, it)
        }

        mPreview?.also {
            val preview: FrameLayout = findViewById(R.id.camera_preview)
            preview.addView(it)
        }

        val captureButton: Button = findViewById(R.id.captureButton)
        val startCaptureButton: Button = findViewById(R.id.startCaptureButton);
        val stopCaptureButton: Button = findViewById(R.id.stopCaptureButton);

        captureButton.setOnClickListener(this::onPictureTake);
        startCaptureButton.setOnClickListener(this::startCapturing)
        stopCaptureButton.setOnClickListener(this::stopCapturing);

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        CameraHolder.requestAppPermissions(this);
    }

    private fun onPictureTake(view: View) {
        Log.d("CAMERA", "taking picture...");
        mCamera?.takePicture(null, null, mPicture)
    }

    private fun startCapturing(view: View) {
        scheduleCaptureTask = scheduledExecutorService?.scheduleAtFixedRate({
            mCamera?.takePicture(null, null, {
                    data, _ -> CameraHolder.lastPictureData = data;
                    mCamera?.startPreview();
            })
        }, 0, 5, TimeUnit.SECONDS);
    }

    private fun stopCapturing(view: View) {
        scheduleCaptureTask?.cancel(false);
    }

    private val mPicture = Camera.PictureCallback { data, _ ->
        val pictureFile: File = CameraHolder.getOutputMediaFile(CameraHolder.MEDIA_TYPE_IMAGE) ?: run {
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

    override fun onPause() {
        super.onPause()
        stopTasks();
        CameraHolder.releaseCamera() // release the camera immediately on pause event
        Toast
            .makeText(this, "Camera streaming was suspended. For resume, restart server service.", Toast.LENGTH_LONG)
            .show();
    }

    private fun stopTasks() {
        scheduledExecutorService?.shutdownNow();
    }
}
