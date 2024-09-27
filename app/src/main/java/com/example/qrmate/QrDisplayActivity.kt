package com.example.qrmate

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.qrmate.DAO.QRCodeDao
import com.example.qrmate.DAO.QRCodeEntity
import com.example.qrmate.DAO.QRDatabase
import com.example.qrmate.databinding.ActivityQrDisplayBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
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
        val currentTime = System.currentTimeMillis()
        binding.tvTitle.text = "Your QR Code"
        binding.tvType.text = "$text"
        binding.ivQrCode.setImageBitmap(qrBitmap)

        var isSaved = false
        binding.btnSave.setOnClickListener {

            if(isSaved){
                // Ignore the click if a save operation is already in progress
                return@setOnClickListener
            }
            isSaved = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                }
            } else {
                // For Android 10 and below, request WRITE_EXTERNAL_STORAGE
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                }
            }

            saveToRoomDB(this, qrBitmap, text, qrDatabase, userId = FirebaseAuth.getInstance().currentUser?.email?.substringBefore("@") ?: "")

            if(isInternetAvailable(this)){
                saveToFirebase(this, qrBitmap!!, text!!, currentTime)

            }else{
                Toast.makeText(this, "No internet connection. Please check your connection and try again.", Toast.LENGTH_SHORT).show()
            }


        }

        binding.btnShare.setOnClickListener {
            shareQRCode(this, qrBitmap)
        }

    }

    private fun shareQRCode(context: Context, qrBitmap: Bitmap?) {
        try {
            // Save the bitmap to a file in external storage
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "shared_qr_code.png")
            val fileOutputStream = FileOutputStream(file)
            qrBitmap?.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            // Get the URI for the file using FileProvider
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider", // Use your app's package name here
                file
            )

            // Create the share intent
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "image/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant permission to read the file

            // Start the activity with a chooser
            context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun saveToFirebase(context: Context, qrBitmap: Bitmap, text: String, currentTime: Long) {


        // Get the current user from Firebase Auth
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        if (currentUser == null) {
            Toast.makeText(context, "User is not logged in.", Toast.LENGTH_SHORT).show()
            Log.e("Firebase_Save", "User is not logged in.")
            return
        }

        runOnUiThread{
            binding.progressBar.visibility = View.VISIBLE
            binding.tvSave.visibility = View.VISIBLE
        }


        // Get user ID from the current logged-in user (Google Sign-In ID)
        val userId = currentUser.uid

        // Reference to Firebase Storage
        val storageReference: StorageReference = FirebaseStorage.getInstance().reference
        val imageRef = storageReference.child("qrcodes/$userId/$text.png")

        // Reference to Firebase Realtime Database
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users").child(userId).child("qr_codes")

        // Convert Bitmap to byte array to upload to Firebase Storage
        val baos = ByteArrayOutputStream()
        qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageData = baos.toByteArray()

        // Upload the image to Firebase Storage
        val uploadTask = imageRef.putBytes(imageData)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            // Get the download URL of the uploaded image
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()

                // Save the QR code details and image URL to Firebase Realtime Database
                val database = FirebaseDatabase.getInstance()
                val userRef = database.getReference("users").child(userId).child("qrcodes")

                // Prepare the data to be saved in Realtime Database
                val qrCodeData = mapOf(
                    "name" to text,
                    "imageUrl" to imageUrl,
                    "timestamp" to currentTime
                )

                // Push the data to the user's specific node in Realtime Database
                userRef.push().setValue(qrCodeData).addOnCompleteListener { task ->
                    runOnUiThread{
                        binding.progressBar.visibility = View.GONE
                        binding.tvSave.visibility = View.GONE
                    }
                    if (task.isSuccessful) {

                        Toast.makeText(context, "QR Code saved to Firebase successfully.", Toast.LENGTH_SHORT).show()
                        Log.d("Firebase_Save", "QR Code saved successfully under user ID: $userId")
                    } else {
                        Toast.makeText(context, "Failed to save QR Code to Firebase.", Toast.LENGTH_SHORT).show()
                        Log.e("Firebase_Save", "Error saving QR Code: ${task.exception?.message}")
                    }


                }
            }.addOnFailureListener { e ->
                // Handle failure in getting the download URL
                Toast.makeText(context, "Failed to get download URL: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Firebase_Save", "Error getting download URL: ${e.message}")
            }
        }.addOnFailureListener { e ->
            // Handle failure in uploading the image
            Toast.makeText(context, "Failed to upload QR Code image: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("Firebase_Save", "Error uploading image: ${e.message}")
        }



    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }

    private fun saveToRoomDB(
        context: Context,
        qrBitmap: Bitmap?,
        qrCodeName: String?,
        qrDatabase: QRDatabase,
        userId: String
    ) {

        // Get the current user from Firebase Auth
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        if (currentUser == null) {
            Toast.makeText(context, "User is not logged in.", Toast.LENGTH_SHORT).show()
            Log.e("Room_Save", "User is not logged in.")
            return
        }

        // Get user ID from the current logged-in user (Google Sign-In ID)
        val userEmail = currentUser.email
        val userParts = userEmail?.substringBefore("@")?:"unknown_user"
        // Get the current time
        val currentTime = System.currentTimeMillis()

        // Save the QR code image to the "QR Mate" folder
        val qrMateFolder = File(Environment.getExternalStorageDirectory(), "QR Mate/$userParts")
        if (!qrMateFolder.exists()) {
            if (!qrMateFolder.mkdirs()) {
                Log.e("Error creating directory", "Failed to create QR Mate directory")
                // Display an error message to the user or try a different directory
            }
        }

// Request permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }


        // Save the bitmap as a PNG file
        val qrFile = File(qrMateFolder, "$qrCodeName.png")
        qrBitmap?.let { bitmap ->
            try {
                FileOutputStream(qrFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
                Toast.makeText(context, "QR Code saved successfully", Toast.LENGTH_SHORT).show()
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
                qrCodeName = qrCodeName!!,
                userId = userId
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