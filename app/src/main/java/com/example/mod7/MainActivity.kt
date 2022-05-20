package com.example.mod7

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.View.OnTouchListener
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mod7.databinding.ActivityMainBinding


const val variableNameRegex = "[a-zA-Z]+(\\d|[a-zA-Z])*"
var selectedBlock = -1

class MainActivity : AppCompatActivity() {
    private lateinit var  binding: ActivityMainBinding
    private val program = Program()
    var count = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.rootLayout)

        binding.plusButton.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.block_type_selection_window, null)
            val window = PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true)
            view.findViewById<View>(R.id.crossButton).setOnTouchListener(OnTouchListener { v, event ->
                window.dismiss()
                true
            })
            window.showAtLocation(binding.scrollView2, Gravity.CENTER, 0, 0);
            view.findViewById<View>(R.id.createVariable).setOnClickListener {
                program.blockViewManager.addBlock(this, layoutInflater, binding, -1, ++count, "CREATE_VARIABLE")
            }
            view.findViewById<View>(R.id.assignVariable).setOnClickListener {
                program.blockViewManager.addBlock(this, layoutInflater, binding, -1, ++count, "CREATE_VARIABLE")
            }
            view.findViewById<View>(R.id.printVariable).setOnClickListener {
                program.blockViewManager.addBlock(this, layoutInflater, binding, -1, ++count, "CREATE_VARIABLE")
            }
//            val block = BlockCustomView(this)
//            binding.rootLayout.addView(block)
        }
        binding.upArrowButton.setOnClickListener{
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
        Toast.makeText(this, "$selectedBlock", Toast.LENGTH_SHORT).show()
    }
}
