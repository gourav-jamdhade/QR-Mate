package com.example.qrmate

import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.Transliterator.Position
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentManager
import com.example.qrmate.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fragmentManager: FragmentManager
    private val CAMERA_PERMISSION_CODE = 100
    private var currentFragment = 1 // Base QR Fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fragmentManager = getSupportFragmentManager()

        binding.btnQr.setOnClickListener {
            if (currentFragment != 1) {
                selectFragment(1);
            }

        }



        binding.btnInfo.setOnClickListener {
            if (currentFragment != 0) {
                selectFragment(0);

            }
        }

        binding.btnProfile.setOnClickListener {
            if (currentFragment != 2) {
                selectFragment(2);
            }
        }

        selectFragment(1)

    }



    private fun selectFragment(position: Int) {

        val fragment = when (position) {

            1 -> QrFragment()
            0 -> ProfileFragment()
            2 -> AboutFragment()
            else -> null

        }

        if (fragment != null) {
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }



        updateButtonAppearance(position)

        currentFragment = position

    }

    private fun updateButtonAppearance(position: Int) {
        val buttons = listOf(binding.btnInfo, binding.btnQr, binding.btnProfile)
        val activatedDrawable = getDrawable(R.drawable.blue_bg)
        val deactivatedDrawable = getDrawable(R.drawable.rounded_bg)
        val activatedColor = getColorStateList(R.color.white)
        val deactivatedColor = getColorStateList(R.color.white)

        buttons.forEach { button ->
            button.background = deactivatedDrawable
            button.imageTintList = deactivatedColor
            button.scaleX = 0.75f
            button.scaleY = 0.75f
            button.alpha = 0.5f
        }

        when (position) {
            0 -> {
                buttons[0].background = activatedDrawable
                buttons[0].imageTintList = activatedColor
                buttons[0].scaleX = 1.25f
                buttons[0].scaleY = 1.25f
                buttons[0].alpha = 1.0f
            }

            1 -> {
                buttons[1].background = activatedDrawable
                buttons[1].imageTintList = activatedColor
                buttons[1].scaleX = 1.25f
                buttons[1].scaleY = 1.25f
                buttons[1].alpha = 1.0f
            }

            2 -> {
                buttons[2].background = activatedDrawable
                buttons[2].imageTintList = activatedColor
                buttons[2].scaleX = 1.25f
                buttons[2].scaleY = 1.25f
                buttons[2].alpha = 1.0f
            }
        }

    }

    override fun onStart() {
        super.onStart()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser == null){
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }


}
