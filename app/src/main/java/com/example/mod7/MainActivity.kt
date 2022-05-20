package com.example.mod7

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inspector.PropertyReader
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.allViews
import java.lang.Exception
import com.example.mod7.databinding.ActivityMainBinding

const val variableNameRegex = "[a-zA-Z]+(\\d|[a-zA-Z])*"

class MainActivity : AppCompatActivity() {
    private lateinit var  binding: ActivityMainBinding
    private val program = Program()
    var count = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.rootLayout)

        binding.imageButton4.setOnClickListener {
            program.blockViewManager.addBlock(this, layoutInflater, binding, -1, ++count, "CREATE_VARIABLE")
//            val block = BlockCustomView(this)
//            binding.rootLayout.addView(block)
        }
        binding.imageButton8.setOnClickListener{
            program.exceptionHandler.throwRuntime("Ойшбибка (это ты) $count",this)
        }

    }

    fun onVariableNameTextViewClick(view:View){
        var name: String = (view as TextView).text.toString()
        if(!name.matches("^${variableNameRegex}(,$variableNameRegex)*$".toRegex())) {
            Toast.makeText(this, "INVALID VARIABEL", Toast.LENGTH_SHORT).show()
            (view as TextView).text=""
        }
    }
}
