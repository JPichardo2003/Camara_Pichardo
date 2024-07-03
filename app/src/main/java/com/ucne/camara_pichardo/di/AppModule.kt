package com.ucne.camara_pichardo.di

import android.app.Application
import androidx.camera.core.*
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.ucne.camara_pichardo.data.repository.CustomCameraRepoImpl
import com.ucne.camara_pichardo.domain.repository.CustomCameraRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    @Singleton
    fun providesMoshi(): Moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    @Provides
    @Singleton
    fun provideCameraProvider(application: Application): ProcessCameraProvider {
        return ProcessCameraProvider.getInstance(application).get()
    }

    @Provides
    @Singleton
    fun providePreview(): Preview {
        return Preview.Builder().build()
    }

    @Provides
    @Singleton
    fun provideImageCapture(): ImageCapture {
        return ImageCapture.Builder().build()
    }

    @Provides
    @Singleton
    fun provideImageAnalysis(): ImageAnalysis {
        return ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }

    @Provides
    @Singleton
    fun provideVideoCapture(): VideoCapture<Recorder> {
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
            .build()
        return VideoCapture.withOutput(recorder)
    }

    @Provides
    @Singleton
    fun provideCustomCameraRepo(
        cameraProvider: ProcessCameraProvider,
        preview: Preview,
        imageAnalysis: ImageAnalysis,
        imageCapture: ImageCapture,
        videoCapture: VideoCapture<Recorder>
    ): CustomCameraRepo {
        return CustomCameraRepoImpl(
            cameraProvider,
            preview,
            imageAnalysis,
            imageCapture,
            videoCapture
        )
    }
}