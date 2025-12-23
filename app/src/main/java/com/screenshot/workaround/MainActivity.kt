package com.screenshot.workaround

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    private lateinit var projectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null
    private lateinit var imageReader: ImageReader
    private var virtualDisplay: VirtualDisplay? = null

    private val REQUEST_CODE = 100
    private var screenWidth = 0
    private var screenHeight = 0
    private var screenDensity = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        val captureButton = Button(this).apply {
            text = "Capture Screenshot"
            setOnClickListener { startScreenCapture() }
        }

        layout.addView(captureButton)
        setContentView(layout)

        projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        screenDensity = metrics.densityDpi
    }

    private fun startScreenCapture() {
        startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data)
            setupVirtualDisplay()

            Handler(Looper.getMainLooper()).postDelayed({
                captureScreen()
            }, 500)
        }
    }

    private fun setupVirtualDisplay() {
        imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2)

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            screenWidth,
            screenHeight,
            screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface,
            null,
            null
        )
    }

    private fun captureScreen() {
        val image: Image? = imageReader.acquireLatestImage()

        if (image != null) {
            val planes = image.planes
            val buffer: ByteBuffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * screenWidth

            val bitmap = Bitmap.createBitmap(
                screenWidth + rowPadding / pixelStride,
                screenHeight,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)
            image.close()

            saveBitmap(bitmap)
            stopProjection()
        }
    }

    private fun saveBitmap(bitmap: Bitmap) {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val screenshotDir = File(picturesDir, "Screenshots")
        screenshotDir.mkdirs()

        val filename = "screenshot_${System.currentTimeMillis()}.png"
        val file = File(screenshotDir, filename)

        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            runOnUiThread {
                Toast.makeText(this, "Screenshot saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Failed to save screenshot: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun stopProjection() {
        virtualDisplay?.release()
        mediaProjection?.stop()
        mediaProjection = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopProjection()
    }
}
