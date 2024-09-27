package com.example.qrmate

import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.qrmate.databinding.FragmentAboutBinding


class AboutFragment : Fragment() {


    private lateinit var binding: FragmentAboutBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = FragmentAboutBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        val htmlContent = """
            <h2><b>About QR Mate</b></h2>
            <p><i>QR Mate</i> is an innovative application designed to enhance your experience with QR codes. With QR Mate, you can effortlessly <b>generate</b>, <b>scan</b>, and <b>manage</b> QR codes right from your mobile device.</p>
            <h3><b>Features:</b></h3>
            <ul>
                <li><b>Generate QR Codes:</b> Create QR codes for any text or URL with just a few taps.</li>
                <li><b>Scan QR Codes:</b> Use the built-in scanner to quickly read and access information encoded in QR codes.</li>
                <li><b>Manage Your Codes:</b> Save, share, and organize your QR codes for easy access later.</li>
                <li><b>Wi-Fi QR Generation:</b> Easily generate QR codes for Wi-Fi networks, including options for password protection.</li>
                <li><b>Share Your Codes:</b> Share QR codes effortlessly with friends and family through various sharing options.</li>
            </ul>
            <h3><b>Why Choose QR Mate?</b></h3>
            <p>With a user-friendly interface and robust features, QR Mate is the ultimate tool for anyone who uses QR codes. Whether you’re a business owner looking to share information with customers or just someone who loves tech, QR Mate has everything you need!</p>
            <p>Thank you for choosing <i>QR Mate</i>. We hope you enjoy using our app!</p>
        """.trimIndent()

        binding.tvAbout.text = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY)
        binding.tvAbout.movementMethod = ScrollingMovementMethod()

        return binding.root


    }


}