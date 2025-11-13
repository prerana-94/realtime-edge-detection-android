package com.yourname.edgedetection.native

import android.graphics.Bitmap

class NativeProcessor {

    companion object {
        init {
            System.loadLibrary("edgedetection")
        }
    }

    /**
     * Process frame with Canny edge detection
     * @param input Input bitmap
     * @param output Output bitmap (must be same size)
     */
    external fun processFrame(input: Bitmap, output: Bitmap)

    /**
     * Convert frame to grayscale
     */
    external fun convertToGrayscale(input: Bitmap, output: Bitmap)
}