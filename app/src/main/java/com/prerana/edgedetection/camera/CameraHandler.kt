package com.yourname.edgedetection.camera

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.yourname.edgedetection.native.NativeProcessor
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraHandler(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
    private val onFrameProcessed: (Bitmap) -> Unit
) {

    private val nativeProcessor = NativeProcessor()
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var imageAnalyzer: ImageAnalysis? = null

    var isProcessingEnabled = true
    var useEdgeDetection = true // Toggle between edge detection and grayscale

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // Image analyzer
            imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, ImageAnalyzer())
                }

            // Select back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e(TAG, "Camera binding failed", e)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    fun shutdown() {
        cameraExecutor.shutdown()
    }

    private inner class ImageAnalyzer : ImageAnalysis.Analyzer {

        private var frameCount = 0
        private var lastFpsTime = System.currentTimeMillis()

        override fun analyze(image: ImageProxy) {
            if (!isProcessingEnabled) {
                image.close()
                return
            }

            try {
                // Convert ImageProxy to Bitmap
                val bitmap = image.toBitmap()

                // Create output bitmap
                val outputBitmap = Bitmap.createBitmap(
                    bitmap.width,
                    bitmap.height,
                    Bitmap.Config.ARGB_8888
                )

                // Process with native code
                if (useEdgeDetection) {
                    nativeProcessor.processFrame(bitmap, outputBitmap)
                } else {
                    nativeProcessor.convertToGrayscale(bitmap, outputBitmap)
                }

                // Calculate FPS
                frameCount++
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastFpsTime >= 1000) {
                    Log.d(TAG, "FPS: $frameCount")
                    frameCount = 0
                    lastFpsTime = currentTime
                }

                // Send to renderer
                onFrameProcessed(outputBitmap)

            } catch (e: Exception) {
                Log.e(TAG, "Frame processing error", e)
            } finally {
                image.close()
            }
        }

        private fun ImageProxy.toBitmap(): Bitmap {
            val yBuffer = planes[0].buffer
            val uBuffer = planes[1].buffer
            val vBuffer = planes[2].buffer

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)

            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)

            val yuvImage = android.graphics.YuvImage(
                nv21,
                android.graphics.ImageFormat.NV21,
                width,
                height,
                null
            )

            val out = java.io.ByteArrayOutputStream()
            yuvImage.compressToJpeg(
                android.graphics.Rect(0, 0, width, height),
                100,
                out
            )

            val imageBytes = out.toByteArray()
            return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }
    }

    companion object {
        private const val TAG = "CameraHandler"
    }
}