#include <jni.h>
#include <string>
#include <android/log.h>
#include <android/bitmap.h>
#include <opencv2/opencv.hpp>

#define LOG_TAG "EdgeDetection"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

using namespace cv;

extern "C" {

// Process image with Canny edge detection
JNIEXPORT void JNICALL
Java_com_prerana_edgedetection_native_NativeProcessor_processFrame(
        JNIEnv *env,
jobject /* this */,
jobject bitmapIn,
        jobject bitmapOut) {

AndroidBitmapInfo infoIn;
void *pixelsIn;
AndroidBitmapInfo infoOut;
void *pixelsOut;

// Get bitmap info and pixels
AndroidBitmap_getInfo(env, bitmapIn, &infoIn);
AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn);
AndroidBitmap_getInfo(env, bitmapOut, &infoOut);
AndroidBitmap_lockPixels(env, bitmapOut, &pixelsOut);

// Create Mat from bitmap
Mat input(infoIn.height, infoIn.width, CV_8UC4, pixelsIn);
Mat output(infoOut.height, infoOut.width, CV_8UC4, pixelsOut);

// Convert to grayscale
Mat gray;
cvtColor(input, gray, COLOR_RGBA2GRAY);

// Apply Canny edge detection
Mat edges;
Canny(gray, edges, 50, 150);

// Convert back to RGBA
cvtColor(edges, output, COLOR_GRAY2RGBA);

// Unlock pixels
AndroidBitmap_unlockPixels(env, bitmapIn);
AndroidBitmap_unlockPixels(env, bitmapOut);

LOGD("Frame processed successfully");
}

// Simple grayscale conversion (alternative/toggle option)
JNIEXPORT void JNICALL
Java_com_prerana_edgedetection_native_NativeProcessor_convertToGrayscale(
        JNIEnv *env,
jobject /* this */,
jobject bitmapIn,
        jobject bitmapOut) {

AndroidBitmapInfo infoIn;
void *pixelsIn;
AndroidBitmapInfo infoOut;
void *pixelsOut;

AndroidBitmap_getInfo(env, bitmapIn, &infoIn);
AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn);
AndroidBitmap_getInfo(env, bitmapOut, &infoOut);
AndroidBitmap_lockPixels(env, bitmapOut, &pixelsOut);

Mat input(infoIn.height, infoIn.width, CV_8UC4, pixelsIn);
Mat output(infoOut.height, infoOut.width, CV_8UC4, pixelsOut);

Mat gray;
cvtColor(input, gray, COLOR_RGBA2GRAY);
cvtColor(gray, output, COLOR_GRAY2RGBA);

AndroidBitmap_unlockPixels(env, bitmapIn);
AndroidBitmap_unlockPixels(env, bitmapOut);
}

} // extern "C"