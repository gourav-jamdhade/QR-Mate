package com.example.qrmate

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.example.qrmate.databinding.FragmentQrBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream


class QrFragment : Fragment() {

    private lateinit var binding: FragmentQrBinding

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private val REQUEST_CODE_QR_SCAN = 101
    private val REQUEST_CODE_MAP = 102
    private val REQUEST_CODE_CONTACT_PICK = 103
    private val REQUEST_CODE_IMAGE_PICK = 104
    private val PICK_PDF_FILE = 1
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentQrBinding.inflate(inflater, container, false)

        //QR Scanner
        binding.ivScanner.setOnClickListener {
            val intent = Intent(activity, QrScannerActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_QR_SCAN)
        }

        val pickPdfLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    // If a file is selected, upload the PDF
                    val pdfTitle = getFileNameFromUri(it)
                    val progressDialog = showProgressDialog(requireContext())
                    uploadPDFToFirebase(it,pdfTitle,progressDialog)
                }
            }

        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

                if (result.resultCode == Activity.RESULT_OK) {
                    Log.d("DATA", result.data.toString())
                    val data = result.data
                    val imageUri = data?.data
                    if (imageUri != null) {
                        processImageUri(imageUri)
                    }
                }
            }
        //plain text
        binding.btnClipboard.setOnClickListener {
            showPlainTextInputDialog()
        }


        //Location btn
        binding.btnLocation.setOnClickListener {
            openMapsForLocation()
        }


        //Website btn
        binding.btnWebsite.setOnClickListener {
            showWebsiteTextInputDialog()
        }

        //Contact btn
        binding.btnContact.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(Manifest.permission.READ_CONTACTS), 1
                )
            } else {
                pickContact()
            }
        }

        //Youtube btn
        binding.btnYoutube.setOnClickListener {
            showYoutubeTextInputDialog()
        }

        //Image btn
        binding.btnImage.setOnClickListener {
            pickImage()
        }

        //pdf btn
        binding.btnPDF.setOnClickListener {

            pickPdfLauncher.launch("application/pdf")
        }

        return binding.root

    }

    private fun showProgressDialog(requireContext: Context): ProgressDialog {

        val progressDialog = ProgressDialog(requireContext)
        progressDialog.setMessage("Uploading PDF...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }


    @SuppressLint("Range")
    private fun getFileNameFromUri(uri: Uri): String {
        var fileName = ""
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            if (cursor.moveToFirst()) {
                // Get the display name from the file metadata
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
        return fileName
    }

    private fun uploadPDFToFirebase(pdfUri: Uri, pdfTitle: Any, progressDialog: ProgressDialog) {

        val storageRef = Firebase.storage.reference
        val pdfRef = storageRef.child("pdfs/${System.currentTimeMillis()}.pdf")

        val uploadTask = pdfRef.putFile(pdfUri)

        uploadTask.addOnSuccessListener {
            pdfRef.downloadUrl.addOnSuccessListener { uri ->
                val pdfUrl = uri.toString()

                val bitmap = generateQR(pdfUrl)

                progressDialog.dismiss()
                if(bitmap!=null){
                    val byteArray = bitmapToByteArray(bitmap)
                    QrDisplayActivity.start(requireContext(), "PDF QR: $pdfTitle", byteArray)
                }
            }.addOnFailureListener { exception ->
                progressDialog.dismiss()

                Toast.makeText(activity, "Failed to Download PDF", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{ exception ->
            progressDialog.dismiss()

            Toast.makeText(activity, "Failed to Upload PDF", Toast.LENGTH_SHORT).show()
        }
    }


  

    private fun processImageUri(imageUri: Uri) {

        try {
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            val imageBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (imageBitmap != null) {
                val compressedBitmap = resizeBitmap(imageBitmap, 64, 64)
                val qrBitmap = generateQRFromImage(compressedBitmap)
                if (qrBitmap != null) {
                    val byteArray = bitmapToByteArray(qrBitmap)
                    QrDisplayActivity.start(requireContext(), "Image QR: $imageUri", byteArray)
                } else {
                    Log.e("QR Generation", "QR Bitmap is null")
                }
            } else {
                Log.e("Image Processing", "Bitmap is null")
            }
        } catch (e: Exception) {
            Log.e("Image Processing", "Error processing image URI", e)
        }

    }

    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }


    private fun pickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)

    }

    private fun showYoutubeTextInputDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Enter YouTube Video address")

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

    private fun pickContact() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_CONTACT_PICK)
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
        } else if (requestCode == REQUEST_CODE_CONTACT_PICK && resultCode == Activity.RESULT_OK) {
            data?.data?.let { contactUri ->
                val contactDetails = getContactDetails(contactUri)
                if (contactDetails != null) {
                    val qrBitmap = generateQR(contactDetails)
                    if (qrBitmap != null) {
                        val byteArray = bitmapToByteArray(qrBitmap)
                        QrDisplayActivity.start(requireContext(), contactDetails, byteArray)
                    } else {
                        Toast.makeText(activity, "Failed to generate QR Code", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(
                        activity, "Failed to retrieve contact details", Toast.LENGTH_SHORT
                    ).show()
                }

            }
        } else if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                val imageBitmap =
                    MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
                val qrBitmap = generateQRFromImage(imageBitmap)
                if (qrBitmap != null) {
                    val byteArray = bitmapToByteArray(qrBitmap)
                    Log.d("IMAGE PICK", "Image QR generated")
                    QrDisplayActivity.start(requireContext(), "Image QR", byteArray)
                }
            }

        } else {
            Log.d("RESULT", "NOT CALLED")
        }

    }

    private fun generateQRFromImage(imageBitmap: Bitmap?): Bitmap? {
        val writer = QRCodeWriter()

        return try {
            val baos = ByteArrayOutputStream()
            imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 25, baos)
            val imageBytes = baos.toByteArray()
            val imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT)
            Log.d("Image String", imageString) // Debuggin

            val bitMatrix = writer.encode(imageString, BarcodeFormat.QR_CODE, 512, 512)
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

    private fun getContactDetails(contactUri: Uri): String? {
        val cursor = requireContext().contentResolver.query(contactUri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val name =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                val hasPhoneNumber =
                    it.getInt(it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                var phoneNumber: String? = null
                if (hasPhoneNumber > 0) {
                    val phoneCursor = requireContext().contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )

                    phoneCursor?.use { phoneCursor ->
                        if (phoneCursor.moveToFirst()) {
                            phoneNumber = phoneCursor.getString(
                                phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            )
                        }

                    }
                }

                return if (phoneNumber != null) {
                    "$name: $phoneNumber"
                } else {
                    name
                }
            }
        }

        return null

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
        val dialog =
            AlertDialog.Builder(requireContext()).setTitle("QR Code Content").setMessage(contents)
                .setPositiveButton("OK", null).setNegativeButton("Copy") { _, _ ->
                    copyToClipboard(contents)

                }.create()
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickContact()
        } else if (requestCode == REQUEST_CODE_IMAGE_PICK && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImage()
        } else {
            Toast.makeText(activity, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

}