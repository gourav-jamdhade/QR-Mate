package com.example.qrmate

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.qrmate.DAO.QRCodeDao
import com.example.qrmate.DAO.QRCodeEntity
import com.example.qrmate.DAO.QRDatabase
import com.example.qrmate.databinding.ActivityQrDisplayBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class QrDisplayActivity : AppCompatActivity() {

    private lateinit var qrDatabase: QRDatabase
    private lateinit var qrCodeDao: QRCodeDao
    private lateinit var binding: ActivityQrDisplayBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val text = intent.getStringExtra(EXTRA_TEXT)
        val qrByteArray = intent.getByteArrayExtra(EXTRA_QR_BYTE_ARRAY)
        val qrBitmap = qrByteArray?.let { byteArrayToBitmap(it) }


        qrDatabase = QRDatabase.getDatabase(this)

        binding.tvTitle.text = "Your QR Code"
        binding.tvType.text = "$text"
        binding.ivQrCode.setImageBitmap(qrBitmap)


        binding.btnSave.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                }
            } else {
                // For Android 10 and below, request WRITE_EXTERNAL_STORAGE
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                }
            }

            saveToRoomDB(this, qrBitmap, text, qrDatabase)
            //saveToFirebase()
        }

    }

    private fun saveToRoomDB(
        context: Context,
        qrBitmap: Bitmap?,
        qrCodeName: String?,
        qrDatabase: QRDatabase
    ) {

        // Get the current time
        val currentTime = System.currentTimeMillis()

        // Save the QR code image to the "QR Mate" folder
        val qrMateFolder = File(Environment.getExternalStorageDirectory(), "QR Mate")
        if (!qrMateFolder.exists()) {
            qrMateFolder.mkdirs()
        }

        // Save the bitmap as a PNG file
        val qrFile = File(qrMateFolder, "$qrCodeName.png")
        qrBitmap?.let { bitmap ->
            try {
                FileOutputStream(qrFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
            } catch (e: IOException) {
                // Handle the exception
                Log.e("Error saving QR code", e.toString())
            }
        } ?: run {
            // Handle the case where qrBitmap is null
            Log.e("Error saving QR code", "qrBitmap is null")
        }


        Log.d("QR_Mate", "QR Code saved at: ${qrFile.absolutePath}")
        // Insert into Room DB (store the file path, name, and timestamp)
        CoroutineScope(Dispatchers.IO).launch {
            val qrEntity = QRCodeEntity(
                qrCodePath = qrFile.absolutePath,
                timestamp = currentTime,
                qrCodeName = qrCodeName!!
            )
            qrDatabase.qrCodeDao().insert(qrEntity)
        }

    }

    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    companion object {
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