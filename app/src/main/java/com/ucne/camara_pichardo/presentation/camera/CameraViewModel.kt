package com.ucne.camara_pichardo.presentation.camera

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ucne.camara_pichardo.domain.repository.CustomCameraRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val repo: CustomCameraRepo
) : ViewModel() {
    fun showCameraPreview(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        isFrontCamera: Boolean
    ) {
        viewModelScope.launch {
            repo.showCameraPreview(previewView, lifecycleOwner, isFrontCamera)
        }
    }

    fun captureAndSave(context: Context) {
        viewModelScope.launch {
            repo.captureAndSaveImage(context)
        }
    }

    fun startOrStopRecording(context: Context) {
        viewModelScope.launch {
            repo.startOrStopRecording(context)
        }
    }
}