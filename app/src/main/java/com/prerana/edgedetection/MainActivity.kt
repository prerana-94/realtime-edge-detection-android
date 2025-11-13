package com.prerana.edgedetection

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.prerana.edgedetection.camera.CameraHandler
import com.prerana.edgedetection.gl.GLRenderer

class MainActivity : AppCompatActivity() {

    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var previewView: PreviewView
    private lateinit var btnToggleMode: Button
    private lateinit var tvFps: TextView

    private lateinit var glRenderer: GLRenderer
    private lateinit var cameraHandler: CameraHandler

    private val mainHandler = Handler(Looper.getMainLooper())
    private var frameCount = 0
    private var lastFpsUpdate = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        glSurfaceView = findViewById(R.id.glSurfaceView)
        previewView = findViewById(R.id.previewView)
        btnToggleMode = findViewById(R.id.btnToggleMode)
        tvFps = findViewById(R.id.tvFps)

        // Setup OpenGL
        glSurfaceView.setEGLContextClientVersion(2)
        glRenderer = GLRenderer()
        glSurfaceView.setRenderer(glRenderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        // Setup camera handler
        cameraHandler = CameraHandler(
            context = this,
            lifecycleOwner = this,
            previewView = previewView,
            onFrameProcessed = ::onFrameProcessed
        )

        // Toggle button
        btnToggleMode.setOnClickListener {
            cameraHandler.useEdgeDetection = !cameraHandler.useEdgeDetection
            btnToggleMode.text = if (cameraHandler.useEdgeDetection) {
                "Edge Detection"
            } else {
                "Grayscale"
            }
        }

        // Check permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun startCamera() {
        cameraHandler.startCamera()
    }

    private fun onFrameProcessed(bitmap: Bitmap) {
        // Update OpenGL texture
        glRenderer.updateTexture(bitmap)
        glSurfaceView.requestRender()

        // Update FPS counter
        frameCount++
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastFpsUpdate >= 1000) {
            val fps = frameCount
            mainHandler.post {
                tvFps.text = "FPS: $fps"
            }
            frameCount = 0
            lastFpsUpdate = currentTime
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraHandler.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}