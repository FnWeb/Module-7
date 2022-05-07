package com.example.mod7

import android.util.AttributeSet
import java.util.*
import kotlin.Exception

class Program{
    val uninitializedBool: BoolType? = null; val uninitializedInteger: IntegerType? = null; val uninitializedDouble: DoubleType? = null
    val typeNameBool = "Bool"; val typeNameInteger = "Int"; val typeNameDouble = "Double"
    private var variables: MutableMap<String, Variable?> = mutableMapOf()
    public fun getVariableMap(): Map<String, Variable?>{
        return variables.toMap() //readonly
    }
    public fun deleteVariable(variable: String){
        variables.remove(variable)
    }
    public fun get(variable: String): Variable {
        if(!variables.containsKey(variable)){
            throw Exception("Accessing unknown variable $variable")
        }
        return variables[variable]?:throw Exception("Accessing uninitialized variable $variable")
    }
    public fun createVariable(variable: String, type: String, init: Any): Variable{
        if(variables.containsKey(variable)){
            throw Exception("Attempting to create existing variable $variable")
        }
        variables[variable] = when(type){
            typeNameInteger -> IntegerType(init)
            typeNameBool -> BoolType(init)
            typeNameDouble -> DoubleType(init)
            else -> throw Exception("Unable to create variable: unknown type $type")
        }
        return variables[variable]?:throw Exception("Error creating variable $variable")
    }
    public fun createVariable(variable: String, type: String){
        if(variables.containsKey(variable)){
            throw Exception("Attempting to create existing variable $variable")
        }
        variables[variable] = when(type){
            typeNameInteger -> uninitializedInteger
            typeNameBool -> uninitializedBool
            typeNameDouble -> uninitializedDouble
            else -> throw Exception("Unable to create variable: unknown type $type")
        }
    }
    public fun setValue(variable: String, value: Any): Variable{
        if(variables.containsKey(variable)){
            throw Exception("Unable to set variable $variable - doesn't exist")
        }
        if(variables[variable] != null){
            variables[variable]?.set(value)
            return variables[variable]?:throw Exception("Error assigning $variable (is ${variables[variable]}) to $value")
        }

        variables[variable] = when(variables[variable]){
            is IntegerType -> IntegerType(value)
            is BoolType -> BoolType(value)
            is DoubleType -> DoubleType(value)
            else -> throw Exception("Unable to assign $variable - unknown type")
        }
        return variables[variable]?:throw Exception("Error assigning $variable (is ${variables[variable]}) to $value")
    }


    private var instructions: LinkedList<Expr> = LinkedList<Expr>()
    fun appendInstruction(type: String){
        when(type){
            "VarCreate"-> //
            "VarAssign"
        }
    }
    fun moveInstructionUp(index: Int){
        if(instructions.size <= 1 || index == 0){
            return
        }
    }
    fun moveInstructionDown(){

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
