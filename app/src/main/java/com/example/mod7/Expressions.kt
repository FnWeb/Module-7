package com.example.mod7

import java.util.Queue
import java.util.Stack
import java.util.LinkedList


interface Expr{
    fun run(): Variable{
        return when(this){
            is Variable -> this
            is Command -> this.execute()
            else -> throw Exception("Undefined run behavior for $this")
        }
    }
}

interface Command : Expr{
    open fun execute(): Variable
}

interface Variable : Expr{
    //    open fun retrieve(): Any
    open fun set(to: Any)
    open operator fun invoke(): Any //Retrieve atomic value
}

class IntegerType(init : Any) : Variable{
    private var value: Int = 0;
    init{
        set(init)
    }

//    override fun retrieve(): IntegerType{
//        return this
//    }

    override fun set(to: Any) {
        value = when(to){
            is Int -> to
            is String -> TODO("Requires string parser")
            is Double -> kotlin.math.round(to).toInt()
            is IntegerType -> to.value
            else -> throw Exception("Variable type mismatch: $to can not be converted to Int")
        }
    }

    operator fun plus(operand: IntegerType): IntegerType{
        return IntegerType(this.value + operand.value)
    }
    operator fun minus(operand: IntegerType): IntegerType{
        return IntegerType(this.value - operand.value)
    }
    operator fun div(operand: IntegerType): IntegerType{
        return IntegerType(this.value / operand.value)
    }
    operator fun rem(operand: IntegerType): IntegerType{
        return IntegerType(this.value % operand.value)
    }
    override operator fun invoke(): Int{
        return value
    }
}

class DoubleType(init : Any) : Variable{
    private var value: Double = 0.0;
    init{
        set(init)
    }

//    override fun retrieve(): DoubleType{
//        return this
//    }

    override fun set(to: Any) {
        value = when(to){
            is Int -> to.toDouble()
            is String -> TODO("Requires string parser")
            is Double -> to
            is IntegerType -> to().toDouble()
            else -> throw Exception("Variable type mismatch: $to can not be converted to Int")
        }
    }

    operator fun plus(operand: DoubleType): DoubleType{
        return DoubleType(this.value + operand.value)
    }
    operator fun minus(operand: DoubleType): DoubleType{
        return DoubleType(this.value - operand.value)
    }
    operator fun div(operand: DoubleType): DoubleType{
        return DoubleType(this.value / operand.value)
    }
    operator fun rem(operand: DoubleType): DoubleType{
        return DoubleType(this.value % operand.value)
    }
    override operator fun invoke(): Double{
        return value
    }
}

class BoolType(init : Any) : Variable{
    private var value: Boolean = false;
    init{
        set(init)
    }

//    override fun retrieve(): BoolType{
//        return this
//    }

    override fun set(to: Any) {
        value = when(to){
            is Int -> to>0
            is String -> to!=""
            is Double -> to>0
            else -> throw Exception("Variable type mismatch: $to can not be used to initialize a Boolean")
        }
    }

    operator fun plus(operand: BoolType): BoolType{
        return BoolType(this.value || operand.value)
    }
    operator fun rem(operand: BoolType): BoolType{
        return BoolType(this.value && !operand.value)
    }
    override operator fun invoke(): Boolean{
        return value
    }
}

fun castType(from: Expr, to: Expr): Variable{
    if(from is Command || to is Command)
        throw Exception("Unable to typecast expressions")
    return when(to){
        is IntegerType -> when(from) {
            is IntegerType -> from
            is BoolType -> IntegerType(from())
            is DoubleType -> IntegerType(from().toInt())
            else -> throw IllegalArgumentException("Unable to typecast $from to $to")
        }
        is DoubleType -> when(from) {
            is IntegerType -> DoubleType(from())
            is BoolType -> DoubleType(from())
            is DoubleType -> from
            else -> throw IllegalArgumentException("Unable to typecast $from to $to")
        }
        is BoolType -> when (from){
            is IntegerType -> BoolType(from()!=0)
            is BoolType -> from
            is DoubleType -> BoolType(from()!=0.0)
            else -> throw IllegalArgumentException("Unable to typecast $from to $to")
        }
        else -> throw Exception("Unable to typecast $from to $to")
    }
}

class Add(left: Expr, right: Expr): Command{
    private var result: Variable? = null

    var left: Expr = left
    var right: Expr = right

    override fun execute(): Variable {
        var operandL: Variable = left.run()
        var operandR: Variable = right.run()
        operandL = if(operandL is BoolType && operandR is BoolType)
            IntegerType(operandR()) else castType(operandL, operandR)

        operandR = castType(operandR, operandL)

        if(operandL is IntegerType &&  operandR is IntegerType)
            result = operandL + operandR
        if(operandL is DoubleType &&  operandR is DoubleType)
            result = operandL + operandR

        if(result == null)
            throw Exception("Addition error: $operandL + $operandR is null")
        return result!!
        TODO("Type cast and type-dependent operators")
        /*
            Command < Integer < Double < String < asdlkfj
         */
    }
}

fun precedence(operator: Char): Int{
    return when(operator){
        '*'-> 0
        '/'-> 0
        '%'-> 0
        '+'-> 1
        '-'-> 1
        else -> 100
    }
}

fun parseToRPN(input: String): String{
    var index = 0
    val numberRegex = "-?[0-9]+(\\\\.[0-9]+)?".toRegex()
    val operatorRegex = "[%/*+-]".toRegex()
    var token: String
    var output: Queue<String> = LinkedList<String>()// Kotlin moment
    var operators: Stack<Char> = Stack()
    while(index<input.length){// Цикл невозможно реализовать через for a in b, так как a неизменяемое и в таком случае потребуется куда больше заморочек со считыванием числа через continue
        // Но штраф за неиспользование for in нам все равно впаяют))0)
        token = ""+input[index]
        if(token.matches(numberRegex)){
            while(index+1 < input.length && (token + input[index+1]).matches(numberRegex)){
                token+=input[++index]
            }
            output.add(token)
        }
        else if(false)//todo TOKEN IS A FUNCTION
        {
//                operators.push(token[0])
        }
        else if(token.matches(operatorRegex)) {
            while(operators.isNotEmpty() && precedence(operators.peek()) <= precedence(token[0]) /*||token is left-associative && equal precedence*/){
                output.add(operators.pop().toString())
            }
            operators.push(token[0])
        }
        else if(token == "("){
            operators.push(token[0])
        }
        else if(token == ")"){
            while(operators.isNotEmpty() && operators.peek()!='('){
                output.add(operators.pop().toString())
            }
            if(operators.isEmpty()){
                throw Exception("RPN: Parenthesis Mismatch ${output.toString()} ( expected for $input")
            }
            operators.pop()
            if(false){//todo operators.peek() is a function
                output.add(operators.pop().toString())
            }
        }
        index++
    }

    while(operators.isNotEmpty()){
        if(operators.peek() == '(')
            throw Exception("RPN: Parenthesis Mismatch ${output.toString()} unresolved ( for $input")
        output.add(operators.pop().toString())
    }

    return output.joinToString(separator = " ")//todo Возможно можно будет парсить уже и output, не переводя в строку, но пока так
}
