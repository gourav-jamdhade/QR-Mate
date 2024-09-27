package com.example.qrmate

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.qrmate.databinding.ActivitySmsBinding

class SmsActivity : AppCompatActivity() {
    private val SMS_PERMISSION_CODE = 101
    private lateinit var binding: ActivitySmsBinding
    private lateinit var smsAdapter: SMSAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, request it
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_SMS), SMS_PERMISSION_CODE)
        } else {
            // If permission is already granted, load SMS messages
            loadSmsMessages()
        }
    }

    private fun loadSmsMessages() {
        // Fetch and display the SMS messages
        val smsList = mutableListOf<String>()
        val cursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null)

        if (cursor != null) {
            val indexBody = cursor.getColumnIndex("body")
            if (indexBody >= 0) {
                while (cursor.moveToNext()) {
                    val smsBody = cursor.getString(indexBody)
                    smsList.add(smsBody)
                }
            }
            cursor.close()
        }

        smsAdapter = SMSAdapter(smsList, object : SMSAdapter.OnSmsClickListener {
            override fun onSmsClick(smsMessage: String) {
                // Return the selected SMS to the fragment
                val resultIntent = Intent().apply {
                    putExtra("selectedSms", smsMessage)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish() // Close the activity
            }
        })

        binding.rvSmsMessages.layoutManager = LinearLayoutManager(this)
        binding.rvSmsMessages.adapter = smsAdapter
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, load SMS messages
                loadSmsMessages()
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Permission denied to read SMS", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
