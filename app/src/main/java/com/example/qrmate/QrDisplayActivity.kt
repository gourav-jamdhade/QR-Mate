package com.example.qrmate

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.qrmate.databinding.ActivityQrDisplayBinding

class QrDisplayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrDisplayBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val text = intent.getStringExtra(EXTRA_TEXT)
        val qrByteArray = intent.getByteArrayExtra(EXTRA_QR_BYTE_ARRAY)
        val qrBitmap = qrByteArray?.let { byteArrayToBitmap(it) }



        binding.tvTitle.text = "QR Code For Clipboard"
        binding.tvType.text = "Text: $text"
        binding.ivQrCode.setImageBitmap(qrBitmap)

    }
    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    companion object{
        private const val EXTRA_TEXT = "EXTRA_TEXT"
        private const val EXTRA_QR_BYTE_ARRAY = "EXTRA_QR_BYTE_ARRAY"


        fun start(context: Context, text: String, qrByteArray: ByteArray) {
            val intent = Intent(context, QrDisplayActivity::class.java).apply {
                putExtra(EXTRA_TEXT, text)
                putExtra(EXTRA_QR_BYTE_ARRAY, qrByteArray)
            }
            context.startActivity(intent)
        }
    }
}