package com.example.drishtiaiapplication

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.drishtiaiapplication.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.sliderFont.addOnChangeListener { slider, value, fromUser ->

            // Reset all to gray
            binding.tvSmall.setTextColor(Color.parseColor("#777777"))
            binding.tvMedium.setTextColor(Color.parseColor("#777777"))
            binding.tvLarge.setTextColor(Color.parseColor("#777777"))

            binding.tvSmall.setTypeface(null, Typeface.NORMAL)
            binding.tvMedium.setTypeface(null, Typeface.NORMAL)
            binding.tvLarge.setTypeface(null, Typeface.NORMAL)

            when (value.toInt()) {
                0 -> {
                    binding.tvSmall.setTextColor(Color.parseColor("#C6FF00"))
                    binding.tvSmall.setTypeface(null, Typeface.BOLD)
                }
                1 -> {
                    binding.tvMedium.setTextColor(Color.parseColor("#C6FF00"))
                    binding.tvMedium.setTypeface(null, Typeface.BOLD)
                }
                2 -> {
                    binding.tvLarge.setTextColor(Color.parseColor("#C6FF00"))
                    binding.tvLarge.setTypeface(null, Typeface.BOLD)
                }
            }
        }
        binding.topToolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
        }
    }
}