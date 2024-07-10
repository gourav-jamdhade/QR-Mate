package com.example.qrmate

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.marginLeft
import androidx.core.view.marginStart
import androidx.core.view.setPadding
import com.example.qrmate.databinding.FragmentQrBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream


class QrFragment : Fragment() {

    private lateinit var binding: FragmentQrBinding

    private val REQUEST_CODE_QR_SCAN = 101
    private val REQUEST_CODE_MAP = 102

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentQrBinding.inflate(inflater, container, false)


        //plain text
        binding.btnClipboard.setOnClickListener {
            showPlainTextInputDialog()
        }

        binding.btnLocation.setOnClickListener {
            openMapsForLocation()
        }

        binding.ivScanner.setOnClickListener {
            val intent = Intent(activity, QrScannerActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_QR_SCAN)
        }


        binding.btnWebsite.setOnClickListener {
            showWebsiteTextInputDialog()
        }
        return binding.root

    }

    private fun showWebsiteTextInputDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Enter web address")

        val input = EditText(requireContext())
        input.setPadding(25)
        input.hint = "Enter web address in https:// format"
        input.setBackgroundResource(android.R.color.white)
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            val text = input.text.toString()
            if (text.isNotEmpty()) {
                val qrBitmap = generateQR(text)
                if (qrBitmap != null) {
                    val byteArray = bitmapToByteArray(qrBitmap)
                    Log.d("QR Bitmap", qrBitmap.toString())
                    QrDisplayActivity.start(requireContext(), text, byteArray)
                } else {
                    Log.d("Qr Bitmap", "Failed")
                }
            }

            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun openMapsForLocation() {
        val locationIntent = Intent(activity, MapActivity::class.java)
        //locationIntent.setPackage("com.google.android.apps.maps")
        startActivityForResult(locationIntent, REQUEST_CODE_MAP)
    }

    private fun showPlainTextInputDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Enter Text")

        val input = EditText(requireContext())
        input.setBackgroundResource(android.R.color.white)
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            val text = input.text.toString()
            if (text.isNotEmpty()) {
                val qrBitmap = generateQR(text)
                if (qrBitmap != null) {
                    val byteArray = bitmapToByteArray(qrBitmap)
                    Log.d("QR Bitmap", qrBitmap.toString())
                    QrDisplayActivity.start(requireContext(), text, byteArray)
                } else {
                    Log.d("Qr Bitmap", "Failed")
                }
            }

            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()

    }

    private fun generateQR(text: String): Bitmap? {

        val writer = QRCodeWriter()

        return try {
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (e: WriterException) {
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {


        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_QR_SCAN && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringExtra("SCAN_RESULT")
            if (result != null) {
                Log.d("RESUlT", result.toString())
                processQRCode(result)

            } else {
                Toast.makeText(activity, "Scan Cancelled", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == REQUEST_CODE_MAP && resultCode == Activity.RESULT_OK) {
            val coordinates = data?.getStringExtra("coordinates")
            if (coordinates != null) {
                val qrBitmap = generateQR("geo:$coordinates")
                if (qrBitmap != null) {
                    val byteArray = bitmapToByteArray(qrBitmap)
                    QrDisplayActivity.start(requireContext(), "Location: $coordinates", byteArray)
                } else {
                    Toast.makeText(activity, "Failed to generate QR Code", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } else {
            Log.d("RESULT", "NOT CALLED")
        }

    }

    private fun processQRCode(contents: String) {
        Log.d("QRCode", "QR content received: $contents")
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
                // Handle SMS URIs
                showDialog(contents)
            }


        }


    }


    private fun showDialog(contents: String) {
        Log.d("QRCode", "Showing dialog for content: $contents")
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("QR Code Content")
            .setMessage(contents)
            .setPositiveButton("OK", null)
            .setNegativeButton("Copy") { _, _ ->
                copyToClipboard(contents)

            }
            .create()
        dialog.show()
    }

    private fun copyToClipboard(contents: String) {
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("QR Code Content", contents)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Copied to Clipboard", Toast.LENGTH_SHORT).show()

    }

    private fun extractPhoneNumber(vcard: String): String? {

        val lines = vcard.lines()

        for (line in lines) {
            if (line.startsWith("TEL:")) {
                return line.substringAfter("TEL:")
            }

            if (line.startsWith("TEL;")) {
                return line.substringAfter("TEL;")
            }


        }
        return null
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

}