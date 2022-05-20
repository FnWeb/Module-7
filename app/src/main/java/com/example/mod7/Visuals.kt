package com.example.mod7

import android.app.Application
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.withStyledAttributes
import androidx.core.view.updateLayoutParams
import com.example.mod7.databinding.ActivityMainBinding
import com.example.mod7.databinding.BlockViewProfileBinding

const val BLOCK_TYPE_VAR = "CREATE_VARIABLE"
const val BLOCK_TYPE_ASSIGN = "ASSIGN_VARIABLE"

class BlockCustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
): ConstraintLayout(context, attrs, defStyleAttr){
    var titleText = ""
    set(value){
        field = value
        binding.blockTitle.text = value
    }
    private val binding = BlockViewProfileBinding.inflate(LayoutInflater.from(context))
    private var background: RectF = RectF()
    var nestingLevel: Int = 0
    set(value){
        field = value
        /// TODO: Масштабирование блоков с изменением урованя вложенности
    }
    override fun onDraw(canvas: Canvas?) {
        throw Exception("WHERE DRAW")
        canvas?.drawRect(background, binding.blockTitle.paint)
        super.onDraw(canvas)
    }

    init{
        background.set(0F,0F,100F,100F,)
        binding.blockTitle.setBackgroundColor(Color.parseColor("#00FFFFFF"))
        // TODO: Заливка
    }

}
class BlockLineCustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
): ConstraintLayout(context, attrs, defStyleAttr){
    var titleText = ""
        set(value){
            field = value
            binding.blockTitle.text = value
        }
    private val binding = BlockViewProfileBinding.inflate(LayoutInflater.from(context))
    override fun onDraw(canvas: Canvas?) {
        canvas?.drawRect(background, Paint(8290687))
        throw Exception("WHERE DRAW")
        super.onDraw(canvas)
    }
    var nestingLevel: Int = 0
        set(value){
            field = value
            /// TODO: Масштабирование блоков с изменением урованя вложенности
        }
    private var background: RectF = RectF()

    init{
        background.set(0F,0F,100F,100F,)
        binding.blockTitle.setBackgroundColor(Color.parseColor("#fdfdfd"))
        // TODO: Заливка
    }

}

class BlockViewManager{
    var blocks = mutableListOf<Pair<View, View>>()
    var totalySafeIdCount = 123456
    fun addBlock(context: Context, layoutInflater: LayoutInflater, binding: ActivityMainBinding, parentBlock: Int, line: Int, type: String){//
//        val layoutInflater = LayoutInflater.from(context)
//        val binding = ActivityMainBinding.inflate(layoutInflater)
        val blockLayout =  when(type){
            BLOCK_TYPE_ASSIGN -> throw Exception("ASSIGN")
            BLOCK_TYPE_VAR ->  {
                R.layout.body_create_1_profile
            }
            else -> throw Exception("UNKNOWN VISUAL BLOCK TYPE")
        }
        val block = layoutInflater.inflate(blockLayout, null)
        val blockLine = layoutInflater.inflate(R.layout.block_line_view_profile, null)
        binding.blockConstraintLayout.apply {
            addView(blockLine)
            addView(block)
        }
        blockLine.findViewById<TextView>(R.id.lineNumberTextView).text = line.toString()
        block.findViewById<TextView>(R.id.blockTitle).text = type.toString()
        block.id = totalySafeIdCount++
        blockLine.updateLayoutParams<ConstraintLayout.LayoutParams> {
            bottomToBottom = block.id
            endToStart = binding.blockGuideline.id
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            topToTop = block.id
            width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            height = block.height
        }

        block.updateLayoutParams<ConstraintLayout.LayoutParams> {
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            horizontalBias = 1.0f
            startToStart = binding.blockGuideline.id
            if(blocks.size<=0) {
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            }
            else topToBottom = if (parentBlock < 0) blocks.last().first.id else blocks[parentBlock].first.id
            width = 0
            height = ConstraintLayout.LayoutParams.WRAP_CONTENT
        }
        block.setBackgroundColor(Color.TRANSPARENT)
        blockLine.setBackgroundColor(Color.TRANSPARENT)

        blocks.add(Pair<View, View>(block, blockLine))
//        block.visibility = View.INVISIBLE
    }

}