package com.example.qrmate

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.qrmate.databinding.FragmentQrBinding
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult


class QrFragment : Fragment() {

    private lateinit var binding: FragmentQrBinding

    private val REQUEST_CODE_QR_SCAN = 101
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentQrBinding.inflate(inflater, container, false)

        binding.openScanner.setOnClickListener {
            val intent = Intent(activity, QrScannerActivity::class.java)
            startActivityForResult(intent,REQUEST_CODE_QR_SCAN)
        }
        return binding.root

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {


        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_CODE_QR_SCAN && resultCode == Activity.RESULT_OK){
            val result = data?.getStringExtra("SCAN_RESULT")
            if(result != null){
                processQRCode(result)

            }else{
                Toast.makeText(activity, "Scan Cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun processQRCode(contents: String) {
        when {
            contents.startsWith("http://") || contents.startsWith("https://") -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(contents))
                startActivity(intent)
            }
            contents.startsWith("upi://") -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(contents))
                startActivity(intent)
            }
            contents.matches(Regex("^tel:\\+?[0-9]+$")) -> {
                // Handle phone numbers
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse(contents))
                startActivity(intent)
            }
            contents.matches(Regex("^[0-9]{10}$")) -> {
                // Handle phone numbers without "tel:" prefix
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$contents"))
                startActivity(intent)
            }
            contents.startsWith("geo:") -> {
                // Handle geo URIs
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(contents))
                intent.setPackage("com.google.android.apps.maps")
                startActivity(intent)
            }
            contents.startsWith("smsto:") -> {
                // Handle WhatsApp QR codes (if formatted as "smsto:")
                val whatsappIntent = Intent(Intent.ACTION_SENDTO, Uri.parse(contents))
                whatsappIntent.setPackage("com.whatsapp")
                if (whatsappIntent.resolveActivity(requireActivity().packageManager) != null) {
                    startActivity(whatsappIntent)
                } else {
                    Toast.makeText(activity, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                // Handle other plain text
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("QR Code Content")
                    .setMessage(contents)
                    .setPositiveButton("OK", null)
                    .create()
                dialog.show()
            }
        }

    }

}