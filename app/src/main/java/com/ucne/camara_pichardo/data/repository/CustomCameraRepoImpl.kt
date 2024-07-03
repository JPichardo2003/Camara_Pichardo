package com.ucne.camara_pichardo.data.repository

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.ucne.camara_pichardo.domain.repository.CustomCameraRepo
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class CustomCameraRepoImpl @Inject constructor(
    private val cameraProvider: ProcessCameraProvider,
    private val preview: Preview,
    private val imageAnalysis: ImageAnalysis,
    private val imageCapture: ImageCapture,
    private val videoCapture: VideoCapture<Recorder>
) : CustomCameraRepo {

    private var isRecording = false
    private var currentRecording: Recording? = null

    override suspend fun captureAndSaveImage(context: Context) {
        val name = SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-SSS",
            Locale.ENGLISH
        ).format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > 28) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/My-Camera-App-Images")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Toast.makeText(
                        context,
                        "Saved image ${outputFileResults.savedUri!!}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        context,
                        "Some error occurred ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    override suspend fun showCameraPreview(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        isFrontCamera: Boolean
    ) {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(if (isFrontCamera) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(previewView.surfaceProvider)

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis,
                imageCapture,
                videoCapture
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun startOrStopRecording(context: Context) {
        if (isRecording) {
            currentRecording?.stop()
            isRecording = false
            Toast.makeText(context, "Recording stopped", Toast.LENGTH_LONG).show()
        } else {
            val name = SimpleDateFormat(
                "yyyy-MM-dd-HH-mm-ss-SSS",
                Locale.ENGLISH
            ).format(System.currentTimeMillis())

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT > 28) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/My-Camera-App-Videos")
                }
            }

            val mediaStoreOutputOptions = MediaStoreOutputOptions
                .Builder(
                    context.contentResolver,
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                )
                .setContentValues(contentValues)
                .build()

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            currentRecording = videoCapture.output
                .prepareRecording(context, mediaStoreOutputOptions)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                    when (recordEvent) {
                        is VideoRecordEvent.Start -> {
                            isRecording = true
                            Toast.makeText(context, "Recording started", Toast.LENGTH_LONG).show()
                        }
                        is VideoRecordEvent.Finalize -> {
                            if (!recordEvent.hasError()) {
                                Toast.makeText(context, "Video saved: ${recordEvent.outputResults.outputUri}", Toast.LENGTH_LONG).show()
                            } else {
                                recordEvent.error?.let {
                                    Toast.makeText(context, "Video capture failed: ", Toast.LENGTH_LONG).show()
                                }
                            }
                            isRecording = false
                        }
                    }
                }
        }
    }
}