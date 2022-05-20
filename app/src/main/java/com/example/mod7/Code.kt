package com.example.mod7

import android.content.Context
import android.app.Application
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import com.example.mod7.databinding.ActivityMainBinding
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.selects.select
import java.lang.NullPointerException
import java.util.*
import kotlin.Exception
import kotlin.coroutines.coroutineContext
import kotlin.concurrent.timerTask

const val typeNameBool = "Bool"; const val typeNameInteger = "Int"; const val typeNameDouble = "Double"

class Program (binding: ActivityMainBinding) {
    val uninitializedBool: BoolType? = null;
    val uninitializedInteger: IntegerType? = null;
    val uninitializedDouble: DoubleType? = null
    private var timer:Timer = Timer()
    val exceptionHandler = ExceptionHandler(this, binding)
    val blockViewManager = BlockViewManager(binding)
    private var isRunning = false
    private var variables: MutableMap<String, Variable?> = mutableMapOf()
    private var binding: ActivityMainBinding = binding
    private var iterationCounter = 0
    private var consoleOutput = ""

    fun getVariableMap(): Map<String, Variable?> {
        return variables.toMap() //readonly
    }

    fun deleteVariable(variable: String) {
        variables.remove(variable)
    }

    fun get(variable: String): Variable {
        if (!variables.containsKey(variable)) {
            throw Exception("Accessing unknown variable $variable")
        }
        return variables[variable] ?: throw Exception("Accessing uninitialized variable $variable")
    }

    fun createVariable(variable: String, type: String, init: Any): Variable {
        if (variables.containsKey(variable)) {
            exceptionHandler.throwRuntime("Attempting to create existing variable $variable")
        }
        if(listOf<String>(typeNameInteger, typeNameBool, typeNameDouble).indexOf(type)<0) {
            exceptionHandler.throwRuntime("Unable to create variable: unknown type $type")
        }
        variables[variable] = when (type) {
            typeNameInteger -> IntegerType(init)
            typeNameBool -> BoolType(init)
            typeNameDouble -> DoubleType(init)
            else -> IntegerType(0)
        }
        if(variables[variable] == null){
            exceptionHandler.throwCritical("Error creating variable $variable")
        }
        return variables[variable]!!
    }

    public fun createVariable(variable: String, type: String) {
        if (variables.containsKey(variable)) {
            throw Exception("Attempting to create existing variable $variable")
        }
        variables[variable] = when (type) {
            typeNameInteger -> uninitializedInteger
            typeNameBool -> uninitializedBool
            typeNameDouble -> uninitializedDouble
            else -> throw Exception("Unable to create variable: unknown type $type")
        }
    }

    public fun setValue(variable: String, value: Any): Variable {
        if (variables.containsKey(variable)) {
            throw Exception("Unable to set variable $variable - doesn't exist")
        }
        if (variables[variable] != null) {
            variables[variable]?.set(value)
            return variables[variable]
                ?: throw Exception("Error assigning $variable (is ${variables[variable]}) to $value")
        }

        variables[variable] = when (variables[variable]) {
            is IntegerType -> IntegerType(value)
            is BoolType -> BoolType(value)
            is DoubleType -> DoubleType(value)
            else -> throw Exception("Unable to assign $variable - unknown type")
        }
        return variables[variable]
            ?: throw Exception("Error assigning $variable (is ${variables[variable]}) to $value")
    }


    private var instructions: LinkedList<String> = LinkedList<String>()
    fun appendInstruction(
        context: Context,
        layoutInflater: LayoutInflater,
        parentBlock: Int,
        type: String
    ) {
        when (type) {
//            BLOCK_TYPE_VAR ->
//            BLOCK_TYPE_ASSIGN ->
//            BLOCK_TYPE_PRINT ->
//            else -> exceptionHandler.throwCritical("Unknown block type $type")
        }
        if (!(type in listOf(BLOCK_TYPE_ASSIGN, BLOCK_TYPE_VAR, BLOCK_TYPE_PRINT))) {
            exceptionHandler.throwCritical("Unknown block type $type")
        }
        instructions.push(type)
        blockViewManager.addBlock(context, layoutInflater, parentBlock, instructions.size, type)
    }

    fun moveInstructionUp() {
        if (instructions.size <= 1 || selectedBlock <= 0 || selectedBlock >= instructions.size) {
            exceptionHandler.throwRuntime(
                "Unable to move the block #$selectedBlock Up",
                binding.rootLayout.context
            )
            return
        }
        blockViewManager.swapBlocks(selectedBlock, selectedBlock - 1);
        Collections.swap(instructions, selectedBlock, --selectedBlock)
    }

    fun moveInstructionDown() {
        if (instructions.size <= 1 || selectedBlock < 0 || selectedBlock >= instructions.size - 1) {
            exceptionHandler.throwRuntime(
                "Unable to move the block #$selectedBlock Up",
                binding.rootLayout.context
            )
            return
        }
        blockViewManager.swapBlocks(selectedBlock, selectedBlock + 1);
        Collections.swap(instructions, selectedBlock, ++selectedBlock)
    }

    fun printToConsole(tag: String = "", msg: String = "\n") {
        Log.d("", "$tag$msg")
        consoleOutput += "$tag$msg\n"
    }

    fun stopExecution() {
        timer.cancel()
        timer.purge()
        isRunning = false
    }

    fun startExecution() {
        if (isRunning) {
            return
        }
        if(instructions.size<1){
            exceptionHandler.throwRuntime("Instruction pool empty or corrupted")
        }
        isRunning = true
        iterationCounter = 0
        variables.clear()
        timer = Timer()
        timer.schedule(
            timerTask {
                if (iterationCounter < instructions.size) {
                    runInstructionSet()
                } else {
                    stopExecution()
                    consoleOutput+="---------------------------------------\n"
                    exceptionHandler.throwRuntime(consoleOutput)
                }
                      },
            250,
            750
        )
    }

    private fun runInstructionSet() {
        when (instructions[iterationCounter]) {
            BLOCK_TYPE_VAR -> {
                val contents =
                    blockViewManager.blocks[iterationCounter].first.findViewById<EditText>(R.id.variableNameTextView).text.toString()
                        .split(
                            variableNameRegex, ","
                        ) as List<String>
                contents.forEach { createVariable(it, typeNameInteger); }
                printToConsole(
                    "Variable${if (contents.size > 1) "s" else ""} [${
                        contents.joinToString(
                            ", "
                        )
                    }] created"
                )
            }
        }
        iterationCounter++
    }


    fun precedence(operator: Char): Int {
        return when (operator) {
            '*' -> 0
            '/' -> 0
            '%' -> 0
            '+' -> 1
            '-' -> 1
            else -> 100
        }
    }

    fun parseToRPN(input: String): String {
        var index = 0
        val numberRegex = "-?[0-9]+(\\\\.[0-9]+)?".toRegex()
        val operatorRegex = "[%/*+-]".toRegex()
        var token: String
        var output: Queue<String> = LinkedList<String>()// Kotlin moment
        var operators: Stack<Char> = Stack()
        while (index < input.length) {// Цикл невозможно реализовать через for a in b, так как a неизменяемое и в таком случае потребуется куда больше заморочек со считыванием числа через continue
            // Но штраф за неиспользование for in нам все равно впаяют))0)
            token = "" + input[index]
            if (token.matches(numberRegex)) {
                while (index + 1 < input.length && (token + input[index + 1]).matches(numberRegex)) {
                    token += input[++index]
                }
                output.add(token)
            } else if (false)//todo TOKEN IS A FUNCTION
            {
//                operators.push(token[0])
            } else if (token.matches(operatorRegex)) {
                while (operators.isNotEmpty() && precedence(operators.peek()) <= precedence(token[0]) /*||token is left-associative && equal precedence*/) {
                    output.add(operators.pop().toString())
                }
                operators.push(token[0])
            } else if (token == "(") {
                operators.push(token[0])
            } else if (token == ")") {
                while (operators.isNotEmpty() && operators.peek() != '(') {
                    output.add(operators.pop().toString())
                }
                if (operators.isEmpty()) {
                    exceptionHandler.throwRuntime("RPN: Parenthesis Mismatch ${output.toString()} ( expected for $input")
                }
                operators.pop()
                if (false) {//todo operators.peek() is a function
                    output.add(operators.pop().toString())
                }
            }
            index++
        }

        while (operators.isNotEmpty()) {
            if (operators.peek() == '(')
                exceptionHandler.throwRuntime("RPN: Parenthesis Mismatch ${output.toString()} unresolved ( for $input")
            output.add(operators.pop().toString())
        }

        return output.joinToString(separator = " ")//todo Возможно можно будет парсить уже и output, не переводя в строку, но пока так
    }

}