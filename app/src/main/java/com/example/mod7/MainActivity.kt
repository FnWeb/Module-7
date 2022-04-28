package com.example.mod7

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}

interface Expr{
    fun run(): Any{
        return when(this){
            is Variable -> this.retrieve()
            is Command -> this.execute()
            else -> throw Exception("Undefined run behavior for $this")
        }
    }
}

interface Command : Expr{
    open fun execute(): Variable
}

interface Variable : Expr{
    open fun retrieve(): Any
    open fun set(to: Any)
}

class Integer(init : Any) : Variable{
    private var value: Int = 0;
    init{
        set(init)
    }

    override fun retrieve(): Integer{
        return this
    }

    override fun set(to: Any) {
        when(to){
            is Int -> value = to
            is String -> TODO("Requires string parser")
            is Double -> value = round(to).toInt()
            else -> throw Exception("Variable type mismatch: $to can not be converted to Int")
        }
    }

    operator fun plus(operand: Integer): Integer{
        return Integer(this.value + operand.value)
    }
    operator fun minus(operand: Integer): Integer{
        return Integer(this.value - operand.value)
    }
    operator fun div(operand: Integer): Integer{
        return Integer(this.value / operand.value)
    }
    operator fun rem(operand: Integer): Integer{
        return Integer(this.value % operand.value)
    }
    operator fun invoke(): Int{
        return value
    }
}

class Add(left: Expr, right: Expr): Command{
    private var result: Variable? = null

    var left: Expr = left
    var right: Expr = right

    init{
        TODO("ugh")
    }

    override fun execute(): Variable {
        val operandL = left.run()
        val operandR = right.run()
        if(operandL is Integer && operandR is Integer) {
            result = operandL+operandL
        } else throw Exception("IDK what is $operandL or $operandR")
        if(result == null)
            throw Exception("Addition error: $operandL + $operandR is null")
        return result!!
        TODO("Type cast and type-dependent operators")
        /*
            Command < Integer < Double < String < asdlkfj
         */
    }
}