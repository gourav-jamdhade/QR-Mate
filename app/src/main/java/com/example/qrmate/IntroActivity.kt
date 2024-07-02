package com.example.qrmate

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class IntroActivity : AppCompatActivity() {

    private lateinit var button: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro);

        button = findViewById(R.id.btnGetStarted)

        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        var hasEntered = prefs.getBoolean("hasEntered", false)

        if(hasEntered){
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
            return
        }

        button.setOnClickListener {

            val editor = prefs.edit()
            editor.putBoolean("hasEntered", true)
            editor.apply()

            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }
}