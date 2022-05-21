package com.example.mod7

import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.mod7.databinding.ActivityMainBinding

const val BLOCK_TYPE_VAR = "CREATE_VARIABLE"
const val BLOCK_TYPE_ASSIGN = "ASSIGN_VARIABLE"
const val BLOCK_TYPE_PRINT = "PRINT_VARIABLE"
const val programSpeed: Int = 5
const val variableNameRegex = "[a-zA-Z]+(\\d|[a-zA-Z])*"

//const val expressionRegex = "(\\d+|$variableNameRegex)(\\s*[+-/*%]\\s*(\\d+|$variableNameRegex))*"
var selectedBlock = -1

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var program: Program
    var count = -1

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        program = Program(binding)
        setContentView(binding.rootLayout)
    }

    override fun onStart() {
        binding.plusButton.setOnClickListener {
//            program.blockViewManager.addBlock(this, layoutInflater, -1, ++count, "CREATE_VARIABLE")
//            program.appendInstruction(this, layoutInflater, -1, BLOCK_TYPE_VAR)
//            val block = BlockCustomView(this)
//            binding.rootLayout.addView(block)
            val view = layoutInflater.inflate(R.layout.block_type_selection_window, null)
            val window = PopupWindow(
                view,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                true
            )
            view.findViewById<View>(R.id.crossButton)
                .setOnTouchListener(View.OnTouchListener { v, event ->
                    window.dismiss()
                    true
                })
            window.showAtLocation(binding.scrollView2, Gravity.CENTER, 0, 0);
            view.findViewById<View>(R.id.createVariable).setOnClickListener {
                program.appendInstruction(this, layoutInflater, -1, BLOCK_TYPE_VAR)
            }
            view.findViewById<View>(R.id.assignVariable).setOnClickListener {
                program.appendInstruction(this, layoutInflater, -1, BLOCK_TYPE_ASSIGN)
            }
            view.findViewById<View>(R.id.printVariable).setOnClickListener {
                program.appendInstruction(this, layoutInflater, -1, BLOCK_TYPE_PRINT)
            }
        }
        binding.plusButton.setOnLongClickListener {
            program.clearInstructions()
            selectedBlock = -1
            return@setOnLongClickListener true
        }
        binding.upArrowButton.setOnClickListener {
            program.moveInstructionUp()
        }

        binding.downArrowButton.setOnClickListener {
            program.moveInstructionDown()
        }

        binding.runCodeButton.setOnClickListener {
            program.startExecution()
        }

        binding.runCodeButton.setOnLongClickListener {
            program.clearConsoleOutput()
            return@setOnLongClickListener true
        }
        super.onStart()
    }

    fun onVariableTypeClick(view: Spinner) {
        val variableTypeSpinner: Spinner = view
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.variableTypes,
            R.layout.type_selection_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            variableTypeSpinner.adapter = adapter
        }

    }

    fun onVariableNameTextViewClick(view: View) {
        view as TextView
        var name: String = view.text.toString()
        name = name.replace(Regex("\\s*,\\s*"), ",")
        if (!name.matches("^${variableNameRegex}(,$variableNameRegex)*$".toRegex())) {
            Toast.makeText(this, "INVALID VARIABEL $name", Toast.LENGTH_SHORT).show()
            view.text = ""
        }
        (view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            view.windowToken,
            0
        )//Единственный способ скрыть клавиатуру.......................
        view.clearFocus()
        view.text = name
    }

    fun onExpressionFieldClick(view: View) {
//        val expression = view.findViewById<EditText>(R.id.value).text.toString()
//        if(!expression.matches(expressionRegex.toRegex())){
//            Toast.makeText(this, "INVALID EXPRESSION", Toast.LENGTH_SHORT).show()
//            view.findViewById<EditText>(R.id.value).text.clear()
//        }
        (view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            view.windowToken,
            0
        )
        view.clearFocus()
    }

    fun onBlockClicked(view: View) {
        if (program.isProgramRunning()) {
            return
        }
        val id = view.id
        if (selectedBlock >= 0) {
            program.blockViewManager.selectBlock(selectedBlock)
        }
        program.blockViewManager.apply {
            val index = blocks.indexOf(blocks.find { it.first.id == id })
            if(index>=0){
                selectBlock(index)
                selectedBlock = index
            } else {
                selectedBlock = -1
                program.exceptionHandler.throwCritical("Error retrieving selected block ID")
            }
        }
    }
}
