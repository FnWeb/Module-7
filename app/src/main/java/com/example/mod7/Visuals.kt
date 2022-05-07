package com.example.mod7

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.mod7.databinding.BlockViewProfileBinding

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
    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(background, Paint(8290687))
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
        // TODO: Заливка
    }
}