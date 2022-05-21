package com.example.mod7

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mod7.databinding.ActivityMainBinding
import com.example.mod7.databinding.ActivityErrorBinding

class Error : AppCompatActivity() {
    private lateinit var binding: ActivityErrorBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)
        binding = ActivityErrorBinding.inflate(layoutInflater)
        setContentView(binding.rootLayout)

        val extras = intent.extras
        if (extras != null) {
            binding.msgTextView.text = extras.getString("EXCEPTION_MSG")
        }
        binding.errorBackButton.setOnClickListener {
            super.finish()
        }
    }
}