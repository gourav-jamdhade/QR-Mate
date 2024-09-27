package com.example.qrmate

import android.graphics.Matrix
import android.graphics.PointF
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.example.qrmate.databinding.ActivityQrcodeBinding

class QRCodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrcodeBinding
    private lateinit var scaleGesture: ScaleGestureDetector
    private var scaleFactor = 1.0f
    private val currentScale: Float
        get() = scaleFactor
    private val matrix = Matrix()


    // Variables to handle panning
    private var last = PointF()
    private var start = PointF()
    private val MODE_NONE = 0
    private val MODE_DRAG = 1
    private val MODE_ZOOM = 2
    private var mode = MODE_NONE


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityQrcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val qrName = intent.getStringExtra("QR_NAME")
        val qrImageUrl = intent.getStringExtra("QR_IMAGE_URL")

        scaleGesture = ScaleGestureDetector(this, ScaleListener())

        binding.tvQrCodeName.text = qrName

        Glide.with(this).load(qrImageUrl).into(binding.ivQrCode)

    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = Math.max(0.1f, Math.min(currentScale, 5.0f))
            matrix.setScale(currentScale, currentScale, detector.focusX, detector.focusY)
            binding.ivQrCode.imageMatrix = matrix
            return true
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGesture.onTouchEvent(event)

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                last.set(event.x, event.y)
                start.set(last)
                mode = MODE_DRAG
            }

            MotionEvent.ACTION_MOVE ->{
                if (mode == MODE_DRAG) {
                    val deltaX = event.x - last.x
                    val deltaY = event.y - last.y
                    matrix.postTranslate(deltaX, deltaY)
                    last.set(event.x, event.y)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP ->{
                mode = MODE_NONE
            }
        }

        binding.ivQrCode.imageMatrix = matrix
        return true

    }
}