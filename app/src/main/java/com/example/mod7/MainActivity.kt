package com.example.mod7

import android.app.Activity
import android.graphics.Color
import android.hardware.input.InputManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inspector.PropertyReader
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.allViews
import java.lang.Exception
import com.example.mod7.databinding.ActivityMainBinding

const val BLOCK_TYPE_VAR = "CREATE_VARIABLE"
const val BLOCK_TYPE_ASSIGN = "ASSIGN_VARIABLE"
const val BLOCK_TYPE_PRINT = "PRINT_VARIABLE"
const val variableNameRegex = "[a-zA-Z]+(\\d|[a-zA-Z])*"
var selectedBlock = -1

class MainActivity : AppCompatActivity() {
    private lateinit var  binding: ActivityMainBinding
    private lateinit var program: Program
    var count = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        program = Program(binding)
        setContentView(binding.rootLayout)
        binding.plusButton.setOnClickListener {
//            program.blockViewManager.addBlock(this, layoutInflater, -1, ++count, "CREATE_VARIABLE")
            program.appendInstruction(this, layoutInflater, -1, BLOCK_TYPE_VAR)
//            val block = BlockCustomView(this)
//            binding.rootLayout.addView(block)
        }
        binding.upArrowButton.setOnClickListener{
            program.moveInstructionUp()
        }

        binding.downArrowButton.setOnClickListener{
            program.moveInstructionDown()
        }

        binding.runCodeButton.setOnClickListener {
            program.startExecution()
        }

    }

    fun onVariableNameTextViewClick(view:View){
        view as TextView
        var name: String = view.text.toString()
        name.replace(Regex("\\s*,\\s*"), "")
        if(!name.matches("^${variableNameRegex}(,$variableNameRegex)*$".toRegex())) {
            Toast.makeText(this, "INVALID VARIABEL", Toast.LENGTH_SHORT).show()
            view.text=""
        }
        (view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view.windowToken, 0)//Единственный способ скрыть клавиатуру.......................
        view.clearFocus()
    }
    fun onBlockClicked(view: View){
        val id = view.id
        if(selectedBlock>=0){
            program.blockViewManager.blocks[selectedBlock].first.findViewById<TextView>(R.id.blockTitle).setTextColor(ContextCompat.getColor(this, R.color.orange))
        }
        val selectionColor = ContextCompat.getColor(this, R.color.white)
        program.blockViewManager.blocks.apply {
            val index = indexOf(find { it.first.id == id })

            if(index>=0){
                selectedBlock=index
                this[index].first.findViewById<TextView>(R.id.blockTitle).setTextColor(selectionColor)
            } else{
                selectedBlock=-1
                program.exceptionHandler.throwCritical("Error retrieving selected block ID")
            }
        }
    }
}
