package com.ucne.camara_pichardo.domain.repository

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner

interface CustomCameraRepo {
    suspend fun captureAndSaveImage(context: Context)
    suspend fun showCameraPreview(previewView: PreviewView, lifecycleOwner: LifecycleOwner, isFrontCamera: Boolean)
    suspend fun startOrStopRecording(context: Context)
}