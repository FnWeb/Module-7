package com.example.mod7

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inspector.PropertyReader
import android.widget.Button
import androidx.core.view.allViews
import java.lang.Exception
import com.example.mod7.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var  binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.rootLayout)

        binding.button8.setOnClickListener {
            val bullshit = BlockCustomView(this)
            binding.rootLayout.addView(bullshit)
        }
    }
}
