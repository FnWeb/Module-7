package com.example.mod7

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inspector.PropertyReader
import com.example.mod7.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var  binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val bcv = BlockCustomView(this)
        binding.rootLayout.addView(bcv)
        setContentView(R.layout.activity_main)
    }
}
