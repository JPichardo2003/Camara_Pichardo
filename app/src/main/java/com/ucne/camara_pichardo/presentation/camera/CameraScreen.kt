package com.ucne.camara_pichardo.presentation.camera

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.ucne.camara_pichardo.R

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel()
) {

    val permissions = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        )
    } else listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    val permissionState = rememberMultiplePermissionsState(permissions = permissions)

    if (!permissionState.allPermissionsGranted) {
        SideEffect {
            permissionState.launchMultiplePermissionRequest()
        }
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    var previewView: PreviewView by remember { mutableStateOf(PreviewView(context)) }

    var isFrontCamera by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (permissionState.allPermissionsGranted) {
            Box(modifier = Modifier
                .height(screenHeight * 0.85f)
                .width(screenWidth)) {
                AndroidView(
                    factory = {
                        previewView = PreviewView(it)
                        viewModel.showCameraPreview(previewView, lifecycleOwner, isFrontCamera)
                        previewView
                    },
                    modifier = Modifier
                        .height(screenHeight * 0.85f)
                        .width(screenWidth)
                )
            }
        }

        Box(
            modifier = Modifier
                .height(screenHeight * 0.15f),
            contentAlignment = Alignment.Center
        ) {
            Row {
                IconButton(onClick = {
                    if (permissionState.allPermissionsGranted) {
                        viewModel.captureAndSave(context)
                    } else {
                        Toast.makeText(
                            context,
                            "Please accept permission in app settings",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }) {
                    Image(
                        painter = painterResource(id = R.drawable.icons8_camera_100),
                        contentDescription = "",
                        modifier = Modifier.size(45.dp)
                    )
                }

                IconButton(onClick = {
                    isFrontCamera = !isFrontCamera
                    viewModel.showCameraPreview(previewView, lifecycleOwner, isFrontCamera)
                }) {
                    Image(
                        painter = painterResource(id = R.drawable.flipcamera),
                        contentDescription = "",
                        modifier = Modifier.size(45.dp)
                    )
                }

                IconButton(onClick = {
                    if (permissionState.allPermissionsGranted) {
                        viewModel.startOrStopRecording(context)
                    } else {
                        Toast.makeText(
                            context,
                            "Please accept permission in app settings",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }) {
                    Image(
                        painter = painterResource(id = R.drawable.video_camera),
                        contentDescription = "",
                        modifier = Modifier.size(45.dp)
                    )
                }
            }
        }
    }
}
